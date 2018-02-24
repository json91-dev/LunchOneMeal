package com.example.user.lunchonemeal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.max;
import static android.R.attr.negativeButtonText;
import static android.R.attr.order;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.user.lunchonemeal.R.id.greeting;
import static com.example.user.lunchonemeal.R.id.myimage;
import static com.example.user.lunchonemeal.R.id.nickname;
import static com.nhn.android.data.g.e;
import static com.nhn.android.data.g.h;
import static com.sun.mail.imap.protocol.INTERNALDATE.format;

/**
 * Created by user on 2017-05-29.
 */

public class chatActivity extends AppCompatActivity  implements View.OnClickListener{

    private String m_user_name="상대방";

    private SimpleDateFormat m_data_format=null;
    private SimpleDateFormat m_time_format=null;

    ListView chat_listview;
    chat_list_adapter adapter;


    Button transButton;
    Button pictureButton;

    EditText et;


    //채팅에 필요한 ip주소 및 포트 설정

    Handler msghandler;

    SocketClient client;
    ReceiveThread receive;
    Socket socket;

    SendThread send;

    PipedInputStream sendstream=null;
    PipedOutputStream receivestream=null;

    LinkedList <SocketClient> threadList;

    //소켓에 sendmessage로 보낼 friendid
    String friendid_;

    SharedPreferences shared;
    SQLiteDatabase sqlite;
    SharedPreferences shared2;



    //사진 전송을 위한 변수 설정 부분
    //이미지자원의 위치를 나타내는 URI
    private Uri mImageCaptureUri;


    //onActivityResult를 위한 request코드 상수 설정.
    private static final int PICK_FROM_CAMERA=0;
    private static final int PICK_FROM_ALBUM=1;
    private static final int CROP_FROM_CAMERA=2;

    Bitmap bm;

    @Override
    protected void onResume() {

        super.onResume();

        //서비스를 종료시킨후
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        getApplicationContext().stopService(intent);

        //내부소켓을 돌린다.
        client=new SocketClient(Global.SocketIp,"5001");
        threadList.add(client);
        client.start();







    }
    @Override
    protected void onPause()
    {

        //왜 onPause에서 실행시키는가 -> 강제종료했을때도 서비스가 계속 동작하도록 내부소켓을 종료시키고 외부소켓 실행

        //내부소켓을 종료시킨후 외부소켓을 실행시킨다.
        receive.setThreadflag(false);
        //onDestory가 호출되면 SocketService를 실행시킨다.
        //이때 SocketFlag값은 false
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        getApplicationContext().startService(intent);
        Log.e("채팅엑티비티","온퍼즈 호출");

        super.onPause();

    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatactivity);

        Log.e("채팅엑티비티","온크리트 호출");


        Global.ResumeFlag=1;

        chat_listview=(ListView) findViewById(R.id.chat_listview);
        et=(EditText)findViewById(R.id.editText1) ;

        transButton=(Button)findViewById(R.id.transButton);
        transButton.setOnClickListener(this);

        shared=getSharedPreferences("loginvalue",MODE_PRIVATE);


        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite= h.getReadableDb();

        shared2=getSharedPreferences("chatvalue",MODE_PRIVATE);
        String loginid=shared.getString("id","");
        String friendid=shared2.getString("friendid","");


        //onCreate될때 readcheck 1회 갱신
        sqlite.execSQL("update talk set readcheck=1 where loginid='"+loginid+"' and friendid='"+friendid+"'");


        Cursor c=sqlite.rawQuery("select * from talk where loginid='"+loginid+"' and friendid='"+friendid+"' order by time asc ; ",null);

        adapter=new chat_list_adapter(getApplicationContext(),c);
        chat_listview.setAdapter(adapter);
        chat_listview.setSelection(c.getCount()-1);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //멀티 채팅 시작

        //소켓 연결
        threadList=new LinkedList<SocketClient>();





        //핸들러 설정



        msghandler=new Handler(){

            @Override
            public void handleMessage(Message hdmsg)
            {
                //1111 태그의 메세지가 온다면
                if(hdmsg.what==1111)
                {
                    String hdmessage=hdmsg.obj.toString();
                    String [] parse=hdmessage.split("#");
                    //#을 포함한 메세지가 없다면
                    if(hdmessage.indexOf("#")==-1)
                    {
                        Toast.makeText(getApplicationContext(),hdmessage,Toast.LENGTH_LONG).show();
                    }

                    //#을 포함하고 나눈 갯수가 2개이면
                    else if(parse.length==2)
                    {

                        String loginid=shared.getString("id","");
                        String friendid=parse[0];
                        String message=parse[1];

                        //친구한테 메세지가 온 것이라면
                        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid+"' and friendid='"+friendid+"' and statement=1",null);
                        if(c.getCount()==1)
                        {

                            SimpleDateFormat simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String s=simple.format(new Date());


                            //Intent intent=getIntent();
                            //String imageurl=intent.getExtras().getString("imageurl");
                            //String nickname=intent.getExtras().getString("nickname");

                            //String imageurl=shared2.getString("imageurl","");
                            //String nickname=shared2.getString("nickname","");

                            //Cursor cursor=sqlite.rawQuery("select * from talk where loginid='"+loginid+"' and friendid='"+friendid+"' and messagetype=1",null);
                            Cursor cursor=sqlite.rawQuery("select * from talk where loginid='"+loginid+"' and friendid='"+friendid+"'",null);
                            //이부분에서 날짜데이터가 없어서 오류남


                            cursor.moveToFirst();
                            String nickname=cursor.getString(cursor.getColumnIndex("nickname"));
                            String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));

                            sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype,message,time) values " +
                                    "('"+loginid+"','"+friendid+"','"+nickname+"','"+imageurl+"',0,'"+message+"','"+s+"')");



                            Log.e("핸들러에서 talk값 입력",": OK");


                            //어뎁터를 가져오는 과정에서는 SharedPreperence를 쓴다

                            String loginid_=shared.getString("id","");
                            String friendid_=shared2.getString("friendid","");

                            c=sqlite.rawQuery("select * from talk where loginid='"+loginid_+"' and friendid='"+friendid_+"' order by time asc ; ",null);
                            adapter=new chat_list_adapter(getApplicationContext(),c);
                            chat_listview.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            chat_listview.setSelection(c.getCount()-1);

                        }

                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

                    }

                    //나눈 갯수가 3개이면면
                    else if(parse.length==3)
                    {
                        //소켓에서 받은 형식 => jw910911#http://jw910911.vps.phps.kr/ChattingImages/jw213123.png#공백

                        String loginid=shared.getString("id","");
                        String friendid=parse[0];
                        String chatimageurl=parse[1];

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
                            //상대방

                            sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype,time,chatimageurl,message) values " +
                                    "('"+loginid+"','"+friendid+"','"+nickname+"','"+imageurl+"',3,'"+s+"','"+chatimageurl+"','사진')");


                            Log.e("핸들러에서 talk값 입력",": OK");


                            //어뎁터를 가져오는 과정에서는 SharedPreperence를 쓴다

                            String loginid_=shared.getString("id","");
                            String friendid_=shared2.getString("friendid","");

                            c=sqlite.rawQuery("select * from talk where loginid='"+loginid_+"' and friendid='"+friendid_+"' order by time asc ; ",null);
                            adapter=new chat_list_adapter(getApplicationContext(),c);
                            chat_listview.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            chat_listview.setSelection(c.getCount()-1);

                        }
                    }


                }
            }
        };

        //사진 전송 버튼 초기화
        pictureButton=(Button)findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(this);

        bm=null;

        //외부소켓 종료 내부소켓 시작

        //teamnova 2G -> 192.168.1.129
        //KT Giga 2G -> 172.30.1.60
        //내 폰 -> 192.168.43.174
        //내 서버 -> 115.71.233.49
        //우리집 -> 192.168.0.6


    }


    @Override
    protected void onDestroy() {



        super.onDestroy();
    }

    @Override
    public void onClick(View view) {



        if(view.getId()==R.id.transButton)
        {
            DbOpenHelper h=new DbOpenHelper(getApplicationContext());
            h.open();//SQLiteOpenHelper 구현
            SQLiteDatabase sqlite= h.getSQLiteDb();


            Cursor c;
            // id값이 가장 큰 놈 한 컬럼만 가져와라

            c=sqlite.rawQuery("select * from talk where _id=(select max(_id) from talk);",null);
            c.moveToFirst();

            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Date current_date=null,db_date=null;

            try {
                String s=sdf.format(new Date());
                current_date= sdf.parse(s);

                if(c.getCount()!=0)
                    db_date=sdf.parse(c.getString(c.getColumnIndex("time")).substring(0,10));

            }catch (Exception e)
            {
                e.printStackTrace();
            }


            if(!et.getText().toString().equals(""))
            {

                //메세지가 들어가는 부분

                String nickname,imageurl,friendid;
                Intent intent=getIntent();

                //버튼이 공백이 아니라면 chatvalue DB에 저장되있는 값으로 imageurl을 저장한다.
                friendid=shared2.getString("friendid","");
                imageurl=shared2.getString("imageurl","");
                nickname=shared2.getString("nickname","");

                //friendid_=friendid;


                //friendid=intent.getExtras().getString("friendid");
                //imageurl=intent.getExtras().getString("imageurl");
                //nickname=intent.getExtras().getString("nickname");



                SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

                SimpleDateFormat simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=simple.format(new Date());


                //날짜 데이터가 없을때에는 이전엑티비티에서 가져온 friendid의 값과 nickname값과 imageurl값을 가져와서 날짜데이터를 추가한다.
                //-> 수정 chatvaluer값의 sharedPreperence를 사용한다.
                if(db_date==null)
                {
                    sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype, message,time) values ('"+shared.getString("id","")+"','"+friendid+"','"+nickname+"','"+imageurl+"',1,'"+"날짜데이터"+"','"+s+"');");
                }
                else if(current_date.compareTo(db_date)==1)
                {
                    sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype, message,time) values ('"+shared.getString("id","")+"','"+friendid+"','"+nickname+"','"+imageurl+"',1,'"+"날짜데이터"+"','"+s+"');");
                }

                sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype, message,time,readcheck) values ('" + shared.getString("id", "") + "','" + friendid + "','" + nickname + "','" + imageurl + "',2,'" + et.getText() + "','" + s + "',1);");


                c=null;
                //리스트뷰에 입력한 채팅 item을 추가하고 갱신한다.
                c=sqlite.rawQuery("select * from talk where loginid='"+shared.getString("id","")+"' and friendid='"+friendid+"' order by time asc ; ",null);
                adapter=new chat_list_adapter(getApplicationContext(),c);
                chat_listview.setAdapter(adapter);
                chat_listview.setSelection(c.getCount()-1);
                adapter.notifyDataSetChanged();


                //전송버튼 클릭 소켓
                //SendThread를 통해 메세지를 전송한다.

                send=new SendThread(socket,true,null);
                send.start();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"메세지를 입력해주세요",Toast.LENGTH_LONG).show();
            }

            et.setText("");

        }else if(view.getId()==R.id.pictureButton)
        {

            DialogInterface.OnClickListener cameraListener=new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doTakePhotoAction();
                }
            };
            DialogInterface.OnClickListener albumListener=new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doTakeAlbumAction();
                }
            };
            DialogInterface.OnClickListener cancelListener=new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };

            new AlertDialog.Builder(chatActivity.this)
                    .setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영",cameraListener)
                    .setNeutralButton("앨범선택",albumListener)
                    .setNegativeButton("취소",cancelListener)
                    .show();

        }




    }


    //사진선택 및 앨범 선택시 리스너 호출 후 수행하는 동작에 대한 함수

    private void doTakePhotoAction()
    {

        //이미지 캡처후 반환하는 인텐트를 생성한다.
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //이미지의 임시파일을 currenttimemillis함수를 통해 설정한다.
        //아직 이미지 데이터는 들어가지 않음
        String url="tmp_"+String.valueOf(System.currentTimeMillis());
        mImageCaptureUri= Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));

        //이미지 임시파일에 이미지 데이터를 삽입한다.
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);

        startActivityForResult(intent,PICK_FROM_CAMERA);
    }

    private void doTakeAlbumAction()
    {
        //아이템을 선택하고 선택한 아이템을 반환하는 Intent
        Intent intent=new Intent(Intent.ACTION_PICK);
        //선택할 데이터의 타입을 선택한다.
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        //반환할 데이터의 타입을 선택한다.

        //어플의 외부데이터의 사진의 URI를 반환한다.
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //intent에 값을 실어 보낸다.
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode!=RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case CROP_FROM_CAMERA:
            {
                /*
                final Bundle extras=data.getExtras();
                if(extras!=null)
                {
                    Bitmap photo=extras.getParcelable("data");
                    myimage.setImageBitmap(photo);
                }
                //임시파일 삭제
                File f=new File(mImageCaptureUri.getPath());
                if(f.exists()){f.delete();}
                break;
                */
            }
            case PICK_FROM_ALBUM:
            {
                Bitmap bit=null;
                try {

                    //안드로이드의 외부데이터경로에서 선택한 사진 데이터를 가져와서 bitmap 변수에 대입한다.
                    //URI에서 Bitmap값 추출
                    bit=MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),data.getData());

                }catch(Exception e)
                {
                    e.printStackTrace();

                }



                //이미지의 임시파일을 currenttimemillis함수를 통해 설정한다.
                String url="tmp_"+String.valueOf(System.currentTimeMillis());

                //아직 이미지 데이터는 들어가지 않음
                mImageCaptureUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));



                //이미지를 서버에 업로드 시키는 쓰레드를 실행시킨다.
                new PictureUploadAndShowDB().execute(bit);

                //이미지를 CROP 시키는 부분으로 넘어가는 소스
               /*
                Intent intent=new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri,"image/*");
                intent.putExtra("outputX",90);
                intent.putExtra("outputY",90);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);

                startActivityForResult(intent,CROP_FROM_CAMERA);
                */
                break;



            }

            case PICK_FROM_CAMERA:
            {
                /*

                bm=new BitmapUtil().loadBitmapRotated(mImageCaptureUri.getPath(),4);
                Toast.makeText(getActivity(),bm+"",Toast.LENGTH_LONG).show();
                try {
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG,80,stream);
                    byte[] bytearray=stream.toByteArray();

                    FileOutputStream fos=new FileOutputStream(mImageCaptureUri.getPath());
                    fos.write(bytearray);

                }catch(Exception e)
                {

                }

                Intent intent=new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri,"image/*");
                intent.putExtra("outputX",90);
                intent.putExtra("outputY",90);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);

                startActivityForResult(intent,CROP_FROM_CAMERA);

                break;
                */
            }

        }


    }

    public class PictureUploadAndShowDB extends AsyncTask<Bitmap,Void,Void>
    {

        byte[] bytearray=null;
        String imagename=null;
        String imageurl=null;



        @Override
        protected Void doInBackground(Bitmap... bm) {


            try {
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bm[0].compress(Bitmap.CompressFormat.PNG,80,stream);
                bytearray=stream.toByteArray();


            }catch(Exception e)
            {
                e.printStackTrace();

            }

            imagename=shared.getString("id","")+String.valueOf(System.currentTimeMillis());
            imageurl="http://jw910911.vps.phps.kr/ChattingImages/"+imagename+".png";








            StringRequest stringRequest=new StringRequest(Request.Method.POST,"http://jw910911.vps.phps.kr/chat_pictureupload.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                            onProgressUpdate();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    String encodedImage=null;
                    try {

                        encodedImage= Base64.encodeToString(bytearray,Base64.DEFAULT);

                    }
                    catch(Exception e){
                        e.printStackTrace();

                    }

                    Map<String, String> params = new Hashtable<String, String>();

                    params.put("Content-Type", "application/json; charset=utf-8");
                    params.put("image", encodedImage);
                    SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

                    params.put("imagename",imagename);

                    return params;
                }
            };

            RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);




            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            adapter.notifyDataSetChanged();

            //이미지가 서버에 업로드 되었을때 Socket으로 사진 메세지 전송
            send=new SendThread(socket,false,imageurl);
            send.start();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            //이부분부터 DB작업 시작.
            SimpleDateFormat simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String s=simple.format(new Date());

            String friendid,imageurl,nickname;

            friendid=shared2.getString("friendid","");
            imageurl=shared2.getString("imageurl","");
            nickname=shared2.getString("nickname","");

            //messagetype 0 -> 상대방이쓴 텍스트 1->날씨  2->내가 쓴 텍스트 3-> 상대방이 보낸 이미지 4-> 내가 보낸 이미지 보낸 이미지
            String chatimageurl="http://jw910911.vps.phps.kr/ChattingImages/"+imagename+".png";
                                                                                                                                            // 로그인 아이디                      친구아이디             닉네임               이미지url  메세지 타입  날짜
            sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype,time,chatimageurl,message) values ('"+shared.getString("id", "") +"','" + friendid + "','" + nickname + "','" + imageurl + "',4,'" + s + "','"+chatimageurl+"','사진');");




            Cursor c=sqlite.rawQuery("select * from talk where loginid='"+shared.getString("id","")+"' and friendid='"+friendid+"' order by time asc ; ",null);
            adapter=new chat_list_adapter(getApplicationContext(),c);
            chat_listview.setAdapter(adapter);
            chat_listview.setSelection(c.getCount()-1);
            adapter.notifyDataSetChanged();

        }
    }


    public  class ExamData
    {
        String date;
        String talk;
        int type;// 1. 상대방 2. 날짜 3. 나

    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    private class ExamAdapter extends BaseAdapter
    {
        private LayoutInflater m_inflater=null;

        private ArrayList<ExamData> m_data_list;

        public void addItem(String date,String talk,int option)
        {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemViewType(int position)
        {
            return m_data_list.get(position).type;
        }


        @Override
        public View getView(int position, View convertview, ViewGroup viewGroup) {

            View view=null;
            int type=getItemViewType(position);

            if(convertview==null)
            {
                switch(type){
                    case 0:
                        //view=m_inflater.inflate(1);
                        break;
                    case 1:
                        //view=m_inflater.inflate(2);
                        break;
                    case 2:
                        //view=m_inflater.inflate(3);
                        break;
                }
            }
            else{
                view=convertview;
            }


            return null;
        }
    }

    public class chat_list_adapter extends CursorAdapter {



        public chat_list_adapter(Context context, Cursor c)
        {
            super(context,c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            //chat1 -> 상대방에게 온 Text
            //chat2 -> 날짜데이터
            //chat3 -> 내가 보낸 Text
            //chat5 -> 상대방에게 온 이미지
            //chat6 -> 내가 보낸 이미지

            RelativeLayout chat1,chat3,chat5,chat6;
            LinearLayout chat2;

            chat1=(RelativeLayout)view.findViewById(R.id.chat1);
            chat2=(LinearLayout)view.findViewById(R.id.chat2);
            chat3=(RelativeLayout)view.findViewById(R.id.chat3);
            chat5=(RelativeLayout)view.findViewById(R.id.chat5);
            chat6=(RelativeLayout)view.findViewById(R.id.chat6);


            if(cursor.getInt(cursor.getColumnIndex("messagetype"))==0)
            {

                chat1.setVisibility(View.VISIBLE);
                chat2.setVisibility(View.GONE);
                chat3.setVisibility(View.GONE);
                chat5.setVisibility(view.GONE);
                chat6.setVisibility(view.GONE);





                SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=null;

                try{
                    Date d=sd.parse(cursor.getString(cursor.getColumnIndex("time")));
                    sd=null;
                    sd=new SimpleDateFormat("a hh:mm");
                    s=sd.format(d);
                    s.replace("AM","오전");
                    s.replace("PM","오후");

                }catch(ParseException e)
                {
                    s="error";
                    e.printStackTrace();
                }


                final TextView message=(TextView)view.findViewById(R.id.message);
                final TextView date=(TextView)view.findViewById(R.id.date);
                final TextView nickname=(TextView)view.findViewById(R.id.nickname);
                final CircleImageView mimage=(CircleImageView)view.findViewById(R.id.mimage);



                date.setText(s);
                message.setText(cursor.getString(cursor.getColumnIndex("message")));
                nickname.setText(cursor.getString(cursor.getColumnIndex("nickname")));
                String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));
                Glide.with(getApplicationContext()).load(imageurl).thumbnail(0.4f).into(mimage);

                //final ImageView image=(ImageView)findViewById(R.id.mimage);



            }
            else if(cursor.getInt(cursor.getColumnIndex("messagetype"))==1)
            {

                chat1.setVisibility(view.GONE);
                chat2.setVisibility(View.VISIBLE);
                chat3.setVisibility(View.GONE);
                chat5.setVisibility(view.GONE);
                chat6.setVisibility(view.GONE);


                final TextView date=(TextView)view.findViewById(R.id.date2);

                SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=null;
                try{
                    Date d=sd.parse(cursor.getString(cursor.getColumnIndex("time")));
                    sd=null;
                    sd=new SimpleDateFormat("yyyy년 MM월 dd일 (E)");
                    s=sd.format(d);

                }catch(ParseException e)
                {
                    s="error";
                    e.printStackTrace();
                }

                date.setText(s);

            }
            else if(cursor.getInt(cursor.getColumnIndex("messagetype"))==2)
            {
                chat1.setVisibility(view.GONE);
                chat2.setVisibility(View.GONE);
                chat3.setVisibility(View.VISIBLE);
                chat5.setVisibility(view.GONE);
                chat6.setVisibility(view.GONE);


                SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=null;

                try{
                    Date d=sd.parse(cursor.getString(cursor.getColumnIndex("time")));
                    sd=null;
                    sd=new SimpleDateFormat("a hh:mm");
                    s=sd.format(d);
                    s.replace("AM","오전");
                    s.replace("PM","오후");

                }catch(ParseException e)
                {
                    s="error";
                    e.printStackTrace();
                }


                final TextView message=(TextView)view.findViewById(R.id.message3);
                final TextView date=(TextView)view.findViewById(R.id.date3);

                date.setText(s);
                message.setText(cursor.getString(cursor.getColumnIndex("message")));

            }
            else if(cursor.getInt(cursor.getColumnIndex("messagetype"))==3)
            {
                SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=null;

                try{
                    Date d=sd.parse(cursor.getString(cursor.getColumnIndex("time")));
                    sd=null;
                    sd=new SimpleDateFormat("a hh:mm");
                    s=sd.format(d);
                    s.replace("AM","오전");
                    s.replace("PM","오후");

                }catch(ParseException e)
                {
                    s="error";
                    e.printStackTrace();
                }


                chat1.setVisibility(view.GONE);
                chat2.setVisibility(View.GONE);
                chat3.setVisibility(View.GONE);
                chat5.setVisibility(view.VISIBLE);
                chat6.setVisibility(view.GONE);


                final ImageView message=(ImageView)view.findViewById(R.id.message5);
                final TextView date=(TextView)view.findViewById(R.id.date5);
                final TextView nickname=(TextView)view.findViewById(R.id.nickname5);
                final CircleImageView mimage=(CircleImageView)view.findViewById(R.id.mimage5);







                //message.setText(cursor.getString(cursor.getColumnIndex("message")));
                //message -> Imageview이다
                String chatimageurl=cursor.getString(cursor.getColumnIndex("chatimageurl"));
                Glide.with(getApplicationContext()).load(chatimageurl).thumbnail(0.4f).into(message);

                nickname.setText(cursor.getString(cursor.getColumnIndex("nickname")));

                String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));
                Glide.with(getApplicationContext()).load(imageurl).thumbnail(0.4f).into(mimage);
                date.setText(s);


            }




            //내가 보낸 이미지 확인하는 부분
            else if(cursor.getInt(cursor.getColumnIndex("messagetype"))==4)
            {
                SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s=null;

                try{
                    Date d=sd.parse(cursor.getString(cursor.getColumnIndex("time")));
                    sd=null;
                    sd=new SimpleDateFormat("a hh:mm");
                    s=sd.format(d);
                    s.replace("AM","오전");
                    s.replace("PM","오후");

                }catch(ParseException e)
                {
                    s="error";
                    e.printStackTrace();
                }

                chat1.setVisibility(view.GONE);
                chat2.setVisibility(View.GONE);
                chat3.setVisibility(View.GONE);
                chat5.setVisibility(view.GONE);
                chat6.setVisibility(view.VISIBLE);




                final TextView date=(TextView)view.findViewById(R.id.date6);
                final ImageView message=(ImageView)view.findViewById(R.id.message6);

                String chatimageurl=cursor.getString(cursor.getColumnIndex("chatimageurl"));

                //Toast.makeText(getApplicationContext(),chatimageurl,Toast.LENGTH_LONG).show();
                Glide.with(getApplicationContext()).load(chatimageurl).thumbnail(0.4f).into(message);

                date.setText(s);


            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View v=null;
            /*
            if(cursor.getInt(cursor.getColumnIndex("messagetype"))==0)
            {
                LayoutInflater inflater=LayoutInflater.from(context);
                v=inflater.inflate(R.layout.chat_1,parent,false);
            }
            else if(cursor.getInt(cursor.getColumnIndex("messagetype"))==1)
            {
                LayoutInflater inflater=LayoutInflater.from(context);
                v=inflater.inflate(R.layout.chat_2,parent,false);
            }
            else
            {
                LayoutInflater inflater=LayoutInflater.from(context);
                v=inflater.inflate(R.layout.chat_3,parent,false);
            }
            */
            LayoutInflater inflater=LayoutInflater.from(context);

            v=inflater.inflate(R.layout.chat_4,parent,false);

            return v;

        }
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

                socket=new Socket(ip,Integer.parseInt(port));
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

        private  boolean threadflag=true;

        public void setThreadflag(boolean threadflag) {
            this.threadflag = threadflag;
        }

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
        //메세지 수신후 핸들러로 전달
        public void run(){
            try{
                Global.setReceiveFlag(true);

                    while (input != null) {

                        if(threadflag==false)
                            break;

                        String msg = input.readUTF();
                        if (msg != null) {

                            if (Global.getReceiveFlag() == false)
                                break;

                            Log.d("메세지 전달받음 : ", msg);
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);
                        }
                    }

                //Log.i("Receive 쓰레드 &어플 종료",":OK");
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    class SendThread extends Thread{
        private Socket socket;
        //sendmsg는 TextView의 메세지이다.
        String sendmsg=et.getText().toString();
        DataOutputStream output;
        String imageurl;

        //텍스트이미지인지 사진 이미지인지 확인하는 플래그
        boolean isText;

        public SendThread(Socket socket,Boolean isText,String imageurl)
        {
            this.isText=isText;
            this.socket=socket;
            this.imageurl=imageurl;
            try{
                output=new DataOutputStream(socket.getOutputStream());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void run()
        {
            try {
                Log.d(ACTIVITY_SERVICE, "111111");
                String mac = null;
                WifiManager mng = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo info = mng.getConnectionInfo();
                mac = info.getMacAddress();

                SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);
                String senderid=null,receiverid=null;
                senderid=shared.getString("id",null);
                //받는사람의 주소는 chatvalueDB의 friendid값이다.
                receiverid=shared2.getString("friendid","");


                if (output != null&&isText==true) {
                    if (sendmsg != null) {

                        output.writeUTF(senderid+"#"+receiverid+"#"+ sendmsg);

                    }
                }

                if(output!=null&isText==false)
                {
                    output.writeUTF(senderid+"#"+receiverid+"#"+"imageurl#"+imageurl);
                }

            }catch(IOException e)
            {
                e.printStackTrace();
            }catch(NullPointerException npe)
            {
                npe.printStackTrace();
            }
        }
    }

}

