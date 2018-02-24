package com.example.user.lunchonemeal;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.example.user.lunchonemeal.Global.isPopup;

/**
 * Created by user on 2017-06-18.
 */

public class MessageBindService extends Service {

    IBinder mbinder=new MyBinder();

    SharedPreferences shared;
    SQLiteDatabase sqlite;

    SocketClient socketClient;
    ReceiveThread receive;

    Handler h=new Handler();

    Handler messagehandler;

    String msg_;



    class MyBinder extends Binder{
        MessageBindService getService(){
            return MessageBindService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //액티비티에서 bindService()를 실행하면 호출된다.
        //리턴한 iBinder 객체는 서비스와 클라이언트 사이의 인터페이스 역활을 한다.



        return mbinder;//서비스 객체를 리턴한다.
    }

    int getRan()
    {
        return new Random().nextInt();
    }
    @Override
    public void onCreate(){
        super.onCreate();

        socketClient=new SocketClient(Global.SocketIp,"5001");
        shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

        DbOpenHelper h = new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite = h.getSQLiteDb();

    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        return super.onStartCommand(intent,flags,startId);

    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void coneectionHandler(Handler h)
    {
        messagehandler=h;
    }


    public class SocketClient extends Thread {
        boolean threadAlive;
        String ip;
        String port;
        String clientid;

        OutputStream outputStream=null;
        BufferedReader br=null;

        private DataOutputStream output=null;

        public SocketClient(String ip,String port)
        {
            threadAlive=true;
            this.ip=ip;
            this.port=port;

        }

        public void run(){
            try{

                Socket socket=new Socket(ip,Integer.parseInt(port));
                output=new DataOutputStream(socket.getOutputStream());
                receive=new ReceiveThread(socket);
                receive.start();

                //mac주소를 받아오기 위해 설정
                //WifiManager mng=(WifiManager)getSystemService(WIFI_SERVICE);
                //WifiInfo info=mng.getConnectionInfo();

                SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

                clientid=shared.getString("id","");


                //mac전송

                output.writeUTF(clientid);

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    class ReceiveThread extends Thread{
        private Socket socket=null;
        DataInputStream input;


        public ReceiveThread(Socket socket)
        {

            this.socket=socket;
            try{
                input=new DataInputStream(socket.getInputStream());
                Global.dis=input;
            }catch(Exception e)
            {

            }
        }


        public void run(){
            try{
                Global.setReceiveFlag(true);
                while (input != null) {
                    String msg = input.readUTF();
                    //메세지 형식 sj940531#안녕하세요

                    msg_=msg;

                    if (msg != null) {
                        /*
                        Log.d("메세지 전달받음 : ", msg);
                        Message hdmsg = msghandler.obtainMessage();
                        hdmsg.what = 1111;
                        hdmsg.obj = msg;
                        msghandler.sendMessage(hdmsg);
                        */

                        if(msg.indexOf("#")!=-1) {
                            //만약 # 문자를 들어온 메세지에서 찾았다면

                            Log.d("메세지 전달받음 : ", msg);
                            String [] parse=msg.split("#");
                            final String friendid=parse[0];
                            final String message=parse[1];

                            //msg형식 sj940531#안녕하세요 [senderid(friendid) / 메세지]

                            if (isPopup) {
                                // 팝업으로 사용할 액티비티를 호출할 인텐트를 작성한다.


                                Thread uiThread=new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Toast.makeText(getApplicationContext(),msg_,Toast.LENGTH_LONG).show();

                                                CommonToast toast = new CommonToast(getApplicationContext());
                                                toast.setGravity(Gravity.TOP,0,0);
                                                toast.showToast(message, Toast.LENGTH_SHORT,friendid);

                                            }
                                        });
                                    }
                                });
                                uiThread.start();


                                //nickname message duration friendname

                            }

                            // SQLite DB에 값 저장하는 부분.


                            //String [] parse=hdmessage.split("#");
                            //String friendid=parse[0];
                            //String message=parse[1];

                            String loginid=shared.getString("id","");


                            //친구한테 메세지가 온 것이라면
                            Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid+"' and friendid='"+friendid+"' and statement=1",null);
                            if(c.getCount()==1)
                            {

                                SimpleDateFormat simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String s=simple.format(new Date());



                                Cursor cursor=sqlite.rawQuery("select * from talk where loginid='"+loginid+"' and friendid='"+friendid+"'",null);
                                //이부분에서 날짜데이터가 없어서 오류남



                                cursor.moveToFirst();
                                String nickname=cursor.getString(cursor.getColumnIndex("nickname"));
                                String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));

                                sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype,message,time) values " +
                                        "('"+loginid+"','"+friendid+"','"+nickname+"','"+imageurl+"',0,'"+message+"','"+s+"')");

                            }

                            Message hdmsg = messagehandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            messagehandler.sendMessage(hdmsg);

                        }

                    }
                    else
                    {
                        Thread uiThread= new Thread(new Runnable() {
                            @Override
                            public void run() {
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"MessageBindService : "+msg_,Toast.LENGTH_LONG).show();

                                    }
                                });
                            }
                        });

                        uiThread.start();


                    }


                }

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }


}
