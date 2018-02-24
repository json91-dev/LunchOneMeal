package com.example.user.lunchonemeal;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.example.user.lunchonemeal.Fragment_Sharing.messagehandler;
import static com.example.user.lunchonemeal.Global.isPopup;
import static com.example.user.lunchonemeal.R.id.chat_listview;

/**
 * Created by user on 2017-06-18.
 */

public class SocketService extends Service {

    SocketClient socketClient;
    ReceiveThread receive;
    String msg_;
    Handler h=new Handler();


    SharedPreferences shared;
    SQLiteDatabase sqlite;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        socketClient=new SocketClient(Global.SocketIp,"5001");
        shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

        DbOpenHelper h = new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite = h.getSQLiteDb();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        socketClient.start();

        //onStartService가 호출될떄 실행된다
        //포그라운드에서 동작하도록 실행시킨다.


        startForeground(1,new Notification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //쓰레드 종료
        socketClient.interrupt();
        super.onDestroy();
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

                            if(parse.length==2)
                            {


                                //msg형식 sj940531#안녕하세요 [senderid(friendid) / 메세지]
                                //이미지 일때 sj940531#http://jw910911.vps.phps.kr
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
                                                    toast.showToast(message,Toast.LENGTH_SHORT,friendid);

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

                                //스태틱 핸들러(fragment_sharing)에 메세지 전달.
                                if(Fragment_Sharing.messagehandler!=null)
                                {
                                    Message hdmsg = Fragment_Sharing.messagehandler.obtainMessage();
                                    hdmsg.what = 1111;
                                    hdmsg.obj = msg;
                                    Fragment_Sharing.messagehandler.sendMessage(hdmsg);
                                }

                            }else if(parse.length==3) {
                                if (isPopup) {


                                    Thread uiThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            h.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //Toast.makeText(getApplicationContext(),msg_,Toast.LENGTH_LONG).show();

                                                    CommonToast toast = new CommonToast(getApplicationContext());
                                                    toast.setGravity(Gravity.TOP, 0, 0);
                                                    toast.showToast("사진", Toast.LENGTH_SHORT, friendid);

                                                }
                                            });
                                        }
                                    });
                                    uiThread.start();
                                }

                                String loginid = shared.getString("id", "");
                                //친구한테 메세지가 온 것이라면
                                Cursor c = sqlite.rawQuery("select * from friendlist where loginid='" + loginid + "' and friendid='" + friendid + "' and statement=1", null);
                                if (c.getCount() == 1) {


                                    SimpleDateFormat simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String s=simple.format(new Date());


                                    Cursor cursor=sqlite.rawQuery("select * from talk where loginid='"+loginid+"' and friendid='"+friendid+"'",null);
                                    //이부분에서 날짜데이터가 없어서 오류남

                                    //이전 아이템의 nickname과 imageurl이다.
                                    cursor.moveToFirst();
                                    String nickname=cursor.getString(cursor.getColumnIndex("nickname"));
                                    String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));

                                    //2번째 파싱한값이 chatimageurl값임.
                                    String chatimageurl=message;

                                    sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype,message,time,chatimageurl) values " +
                                            "('"+loginid+"','"+friendid+"','"+nickname+"','"+imageurl+"',3,'사진','"+s+"','"+chatimageurl+"')");




                                }
                                //스태틱 핸들러(fragment_sharing)에 메세지 전달.
                                if(Fragment_Sharing.messagehandler!=null)
                                {
                                    Message hdmsg = Fragment_Sharing.messagehandler.obtainMessage();
                                    hdmsg.what = 1111;
                                    hdmsg.obj = msg;
                                    Fragment_Sharing.messagehandler.sendMessage(hdmsg);
                                }

                            }
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
                                        Toast.makeText(getApplicationContext(),"SocketService : "+msg_,Toast.LENGTH_LONG).show();

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


