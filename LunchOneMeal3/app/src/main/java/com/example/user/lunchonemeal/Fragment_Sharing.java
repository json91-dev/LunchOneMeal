package com.example.user.lunchonemeal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.interpolator.linear;
import static android.R.string.no;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.example.user.lunchonemeal.R.id.greeting;
import static com.example.user.lunchonemeal.R.id.login;
import static com.example.user.lunchonemeal.R.id.nickname;
import static com.example.user.lunchonemeal.R.id.sex;
import static com.nhn.android.data.g.h;
import static com.nhn.android.data.g.i;

/**
 * Created by user on 2017-04-18.
 */

public class Fragment_Sharing extends Fragment  implements TabHost.OnTabChangeListener{

    private ListView friends_list=null;
    friends_list_adapter f_adapter=null;

    private ListView dialog_list=null;
    dialog_list_adapter d_adapter=null;

    Cursor c;

    SQLiteDatabase sqlite;

    String friendid_,position_;

    SharedPreferences idshared;


    //바인드 서비스 변수.
    MessageBindService messagebindservice;
    ServiceConnection serviceconn;

    boolean servicebindflag=false;

    public static Handler messagehandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sharing, container, false);

        friendid_ = null;
        position_ = null;

        TabHost tabHost = (TabHost) rootView.findViewById(R.id.tabhost);
        tabHost.setup();


        TabHost.TabSpec spec = tabHost.newTabSpec("tag1");
        spec.setContent(R.id.tv1);
        spec.setIndicator("친구목록");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tv2);
        spec.setIndicator("대화방");
        tabHost.addTab(spec);

        tabHost.setOnTabChangedListener(this);
        ///▲위쪽은 탭호스트 설정하는부분


        //DB와 SharedPreperence 초기화 하는 부분
        DbOpenHelper h = new DbOpenHelper(getActivity());
        h.open();//SQLiteOpenHelper 구현
        sqlite = h.getSQLiteDb();

        idshared = getActivity().getSharedPreferences("loginvalue", getActivity().MODE_PRIVATE);

        //sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype, message,time) values ('jw910911','ssj940531','테스트','null',1,'"+"날짜데이터"+"','"+"2017-06-04 11:09:54"+"');");


        //친구 목록 리스트뷰
        friends_list = (ListView) rootView.findViewById(R.id.friendsList);
        f_adapter = new friends_list_adapter(getActivity());
        friendrefresh();


        SQLiteDatabase sqlite2 = h.getReadableDb();
        // 최신 시간의 데이터를 friendid 별로 가져와서 각각의 채팅방을 나누고 최신의 데이터를 보여준다.
        c = sqlite.rawQuery("select * from talk as A where time=(select max(time) from talk where friendid=A.friendid ) order by time desc", null);

        //select * from talk (

        dialog_list = (ListView) rootView.findViewById(R.id.dialogList);
        if (c.getCount() > 0) {
            d_adapter = new dialog_list_adapter(getActivity(), c);
            dialog_list.setAdapter(d_adapter);
        } else {
            //결과값 없음
        }


        friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                ListData item = (ListData) f_adapter.getItem(position);
                SharedPreferences shared = getActivity().getSharedPreferences("loginvalue", getActivity().MODE_PRIVATE);


                Cursor c = sqlite.rawQuery("select * from friendlist where loginid='" + shared.getString("id", "") + "' and friendid='" + item.friendid + "' and statement=1", null);

                if (c.getCount() == 1)//만약에 클릭된 아이템의 friendid값과 현재 로그인한 사용자의 값이 디비에 존재하고 친구상태(statement=1)이라면,
                {
                    Intent intent = new Intent(getActivity(), chatActivity.class); //대화방으로 넘어가라

                    //chatvalue의 DB로 값을 전달해준다.
                    //listview의 아이템이 클릭될때 chatvalue DB의 friendid,imagueurl nickname 값이 설정된다.
                    SharedPreferences shared2 = getActivity().getSharedPreferences("chatvalue", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared2.edit();
                    editor.putString("friendid", item.friendid);
                    editor.putString("imageurl", item.imageurl);
                    editor.putString("nickname", item.nickname);
                    editor.commit();


                    //intent.putExtra("friendid", item.friendid);
                    //intent.putExtra("imageurl", item.imageurl);
                    //intent.putExtra("nickname", item.nickname);

                    /*
                    //chatActivity로 넘어가기전 기존의 서비스를 종료시켜준다.
                    Intent socketintent = new Intent(getActivity(), SocketService.class);
                    getActivity().stopService(socketintent);
                    */


                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "아직 친구가 아닙니다...", Toast.LENGTH_LONG).show();
                }

            }
        });

        dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = sqlite.rawQuery("select * from talk as A where time=(select max(time) from talk where friendid=A.friendid) order by time desc", null);
                c.moveToPosition(position);

                Intent intent = new Intent(getActivity(), chatActivity.class);

                SharedPreferences shared2 = getActivity().getSharedPreferences("chatvalue", MODE_PRIVATE);
                SharedPreferences.Editor editor = shared2.edit();

                editor.putString("friendid", c.getString(c.getColumnIndex("friendid")));
                editor.putString("imageurl", c.getString(c.getColumnIndex("imageurl")));
                editor.putString("nickname", c.getString(c.getColumnIndex("nickname")));
                editor.commit();


                //intent.putExtra("friendid", c.getString(c.getColumnIndex("friendid")));
                //intent.putExtra("imageurl", c.getString(c.getColumnIndex("imageurl")));
                //intent.putExtra("nickname", c.getString(c.getColumnIndex("nickname")));

                /*
                //chatActivity로 넘어가기전 기존의 서비스를 종료시켜준다.

                Intent socketintent = new Intent(getActivity(), SocketService.class);
                getActivity().stopService(socketintent);
                */

                startActivity(intent);

            }
        });

        SharedPreferences shared = getActivity().getSharedPreferences("loginvalue", getActivity().MODE_PRIVATE);

        //친구 목록 보는 로그
        Cursor c = sqlite.rawQuery("select * from friendlist", null);
        c.moveToFirst();


        for (int i = 0; i < c.getCount(); i++) {

            Log.w(i + "번째", c.getString(1) + " " + c.getString(2) + " " + c.getString(3) + " ");
            c.moveToNext();
        }


        Toast.makeText(getActivity(), "로그인 아이디 :" + shared.getString("id", ""), Toast.LENGTH_LONG).show();


        //소켓 서비스 시작 ; 처음에 어플이 깔릴때 처음만 시작한다.




        /*
        Intent intent = new Intent(getActivity(), SocketService.class);
        getActivity().startService(intent);
          */

        //서비스 연결 객체 생성

        serviceconn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                //서비스와 연결되면 호출됨

                MessageBindService.MyBinder mb = (MessageBindService.MyBinder) iBinder;
                messagebindservice = mb.getService();
                servicebindflag = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                servicebindflag = false;
            }
        };

        messagehandler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                //메세지가 왔을때 핸들러를 통해서 dialog_adapter을 갱신하고 최신의 데이터를 보여준다.
                Cursor c=sqlite.rawQuery("select * from talk as A where time=(select max(time) from talk where friendid=A.friendid) order by time desc",null);
                d_adapter=new dialog_list_adapter(getActivity(),c);
                dialog_list.setAdapter(d_adapter);
                d_adapter.notifyDataSetChanged();
                
                return false;
            }
        });



        return rootView;

    }

    //프렌드리스트 어뎁터를 설정한다
    //Http요청을 해서 friendid를 보내서 그 정보를 PHP를 통해 서버의 DB에서 가져온다
    public void friendrefresh()
    {
        SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",getActivity().MODE_PRIVATE);
        Cursor cur=sqlite.rawQuery("select * from friendlist where loginid='"+shared.getString("id","")+"'",null);
        cur.moveToFirst();

        f_adapter.removeAllData();
        for(int i=0;i<cur.getCount();i++)
        {

            String friendid=cur.getString(cur.getColumnIndex("friendid"));
            int statement=cur.getInt(cur.getColumnIndex("statement"));

            new friendinfoGet().execute(friendid,statement+"");

            Log.w("친구목록",friendid);

            cur.moveToNext();
        }
        friends_list.setAdapter(f_adapter);
    }



    @Override
    public void onTabChanged(String s) {
        onResume();
    }


    //갱신되는 부분 !
    @Override
    public void onResume() {
        super.onResume();

        Toast.makeText(getActivity(),"onResume 호출",Toast.LENGTH_LONG ).show();

        //ResumeFlag 는 readcheck를 (읽지않은메세지 확인) update하기 위해 만들었음.
        if(Global.ResumeFlag==1)
        {
            SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",getActivity().MODE_PRIVATE);

            SharedPreferences chat_shared=getActivity().getSharedPreferences("chatvalue",getActivity().MODE_PRIVATE);
            String friendid=chat_shared.getString("friendid","");

            sqlite.execSQL("update talk set readcheck=1 where loginid='"+shared.getString("id","")+"' and friendid='"+friendid+"' ");
            //sqlite.execSQL("update talk set readcheck=1 where ");

            Log.e("리썸!! ",friendid);
            Global.ResumeFlag=0;
        }


        Log.d("프레그먼트 OnResume","확인");


        DbOpenHelper h=new DbOpenHelper(getActivity());
        h.open();
        SQLiteDatabase sqlite2=h.getReadableDb();
        friends_list.setAdapter(f_adapter);


        //c=sqlite.rawQuery("select * from talk where messagetype=0 or messagetype=2 order by time desc limit 0 , 1; ",null);
        c=sqlite.rawQuery("select * from talk as A where time=(select max(time) from talk where friendid=A.friendid) order by time desc",null);
        d_adapter=new dialog_list_adapter(getActivity(),c);
        dialog_list.setAdapter(d_adapter);
        d_adapter.notifyDataSetChanged();


        //소켓 서비스 시작하는 부분

        if(Global.SocketServiceFlag==true) {
            Intent intent = new Intent(getActivity(), SocketService.class);
            getActivity().startService(intent);
            Global.SocketServiceFlag=false;

        }


        //바인드 서비스 시작하는 부분 오류 .. 엑티비티에서 바인드

        //Intent intent=new Intent(getActivity(),MessageBindService.class);
        //getActivity().bindService(intent,serviceconn,getActivity().BIND_AUTO_CREATE);


        //messagebindservice.coneectionHandler();

    }

    public class ListData{
        public String imageurl;
        public String nickname;
        public String sex;
        public String message;
        public int statement;
        public String friendid;
    }

    public class ViewHolder
    {
        CircleImageView mImage;
        TextView nickname;
        TextView sex;
        TextView message;
        Button friendButton;

    }

    public class dialog_list_adapter extends CursorAdapter{

        public dialog_list_adapter(Context context,Cursor c)
        {
            super(context,c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final ImageView image=(ImageView)view.findViewById(R.id.mImage);
            final TextView nickname=(TextView)view.findViewById(R.id.nickname);
            final TextView message=(TextView)view.findViewById(R.id.message);
            final TextView date=(TextView)view.findViewById(R.id.date);
            final TextView newmessage=(TextView)view.findViewById(R.id.newmessage);

            String loginid=cursor.getString(cursor.getColumnIndex("loginid"));
            String friendid=cursor.getString(cursor.getColumnIndex("friendid"));

            Cursor c=sqlite.rawQuery("select * from talk where readcheck=0 and (messagetype=0 or messagetype=2 or messagetype=3) and friendid='"+friendid+"' and loginid='"+loginid+"'",null);
            //날짜 메세지를 제외한 메세지중에 읽지 않은 메세지가 있으면,
            if(c.getCount()>=1)
            {
                //갯수를 newmessage(TextView)에 표시한다.
                newmessage.setVisibility(View.VISIBLE);
                newmessage.setText(c.getCount()+"");
            }
            else
            {
                //없으면 newmessage(TextView)를 안보이게 한다.
                newmessage.setVisibility(View.INVISIBLE);
            }

            if(cursor.getString(cursor.getColumnIndex("imageurl")).equals("null"))
            {
                image.setImageDrawable(getResources().getDrawable(R.drawable.android));
            }
            else
            {
                //이미지 url값이 db에 존재하면 glide로 이미지를 가져온다.
                String imageurl=cursor.getString(cursor.getColumnIndex("imageurl"));
                Glide.with(getActivity()).load(imageurl).thumbnail(0.4f).into(image);
            }

            //DB에 설정된 nickname과 message와 time의 값을 설정한다.
            nickname.setText(cursor.getString(cursor.getColumnIndex("nickname")));
            message.setText(cursor.getString(cursor.getColumnIndex("message")));
            date.setText(cursor.getString(cursor.getColumnIndex("time"))+"");

            /*
            코드 작성하는 부분
             */
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            LayoutInflater inflater=LayoutInflater.from(context);
            View v=inflater.inflate(R.layout.listview_item3,parent,false);

            return v;
        }
    }


    private class friends_list_adapter extends BaseAdapter implements View.OnClickListener{

        private ArrayList <ListData> mListData=new ArrayList<ListData>();
        private Context mContext;

        public friends_list_adapter(Context mContext){
            super();
            this.mContext=mContext;
        }

        public void removeAllData()
        {
            mListData=null;
            mListData=new ArrayList<ListData>();
        }

        public void addItem()
        {
            ListData item=new ListData();
            item.nickname="정연이";
            item.sex="여자";
            item.message="같이 점심 한끼 먹어요. ^^";

            mListData.add(0,item);
        }

        public void addItem(String nickname,String sex, String message,String imageurl,int statement,String friendid)
        {
            ListData item=new ListData();
            item.nickname=nickname;
            item.imageurl=imageurl;
            item.message=message;
            item.statement=statement;

            if(sex.equals("man"))
            {
                item.sex="남성";
            }else
            {
                item.sex="여성";
            }
            item.friendid=friendid;

            mListData.add(0,item);


        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int i) {
            return mListData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            ViewHolder holder;
            if(convertView==null)
            {
                holder=new ViewHolder();
                LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView=inflater.inflate(R.layout.listview_item2,null);


                holder.mImage=(CircleImageView)convertView.findViewById(R.id.mImage);
                holder.nickname=(TextView)convertView.findViewById(R.id.nickname);
                holder.sex=(TextView)convertView.findViewById(R.id.sex);
                holder.message=(TextView)convertView.findViewById(R.id.message);
                holder.friendButton=(Button)convertView.findViewById(R.id.friendButton);


                convertView.setTag(holder);


            }
            else{
                holder=(ViewHolder)convertView.getTag();
            }



            ListData item=mListData.get(position);

            Glide.with(getActivity()).load(item.imageurl).thumbnail(0.4f).into(holder.mImage);


            holder.nickname.setText(item.nickname);
            holder.message.setText(item.message);
            holder.sex.setText(item.sex);



            holder.friendButton.setTag(item.friendid+"&"+item.imageurl+"&"+item.nickname+"&"+item.message+"&"+position+"");

            if(item.statement==1)
            {
                holder.friendButton.setText("친 구");
                holder.friendButton.setBackgroundResource(R.drawable.button2);
            }
            else if(item.statement==0)
            {
                holder.friendButton.setText("친구맺기");
                holder.friendButton.setBackgroundResource(R.drawable.button3);
            }
            else if(item.statement==2)
            {
                holder.friendButton.setText("요청중");
                holder.friendButton.setBackgroundResource(R.drawable.button5);

            }


            holder.friendButton.setOnClickListener(this);


            //holder.friendButton.setBackgroundResource(R.drawable.edittextheme);



            return convertView;
        }


        @Override
        public void onClick(View view) {

            SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",getActivity().MODE_PRIVATE);

            String loginid=shared.getString("id",null);

            String tag=view.getTag()+"";
            String [] parser =tag.split("&");


            String friendid=parser[0];
            String imageurl=parser[1];
            String nicknames=parser[2];
            String message=parser[3];
            String position=parser[4];



            friendid_=friendid;
            position_=position;







            Cursor cur=sqlite.rawQuery("select * from friendlist where loginid='"+loginid+"' and friendid='"+friendid+"' and statement=0",null);

            if(cur.getCount()==1)
            {

                //리니어 레이아웃
                LinearLayout linear=new LinearLayout(getActivity());
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                linear.setLayoutParams(llp);

                linear.setOrientation(LinearLayout.VERTICAL);
                linear.setGravity(Gravity.CENTER_HORIZONTAL);

                //이미지뷰

                ImageView circle=new ImageView(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                circle.setLayoutParams(lp);

                //텍스트뷰

                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lllp.topMargin=30;

                TextView nickname=new TextView(getActivity());
                nickname.setText("닉네임 : "+nicknames);
                nickname.setLayoutParams(lllp);
                TextView greeting=new TextView(getActivity());
                greeting.setText("인사말 : "+message);
                greeting.setLayoutParams(lllp);




                linear.addView(circle);
                linear.addView(nickname);
                linear.addView(greeting);
                //circle.setScaleType(ImageView.ScaleType.FIT_XY);

                AlertDialog.Builder ad=new AlertDialog.Builder(getActivity());
                ad.setTitle("친구맺기");
                ad.setView(linear);
                Glide.with(getActivity()).load(imageurl).override(250,250).transform(new CircleTransform(getActivity())).thumbnail(0.6f).into(circle);




                ad.setMessage("친구를 맺으시겠습니까 ?? :)");
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",getActivity().MODE_PRIVATE);
                        sqlite.execSQL("update friendlist set statement=1 where loginid='"+shared.getString("id","")+"' and friendid='"+friendid_+"'");
                        Log.i("친구 상태가 1로 바뀜 ",": Ok");

                        ListData item= (ListData)f_adapter.getItem(Integer.parseInt(position_));
                        item.statement=1;
                        f_adapter.notifyDataSetChanged();

                        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+shared.getString("id","")+"' and friendid='"+friendid_+"'",null);
                        c.moveToFirst();


                        new fcm_return().execute(friendid_);


                    }
                });
                ad.show();


            }
            else
            {
                Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid+"' and friendid='"+friendid+"' and statement=1",null);

                if(c.getCount()==1)
                {
                    Toast.makeText(getActivity(),"이미 친구 상태입니다",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getActivity(),"친구 요청 대기중입니다",Toast.LENGTH_LONG).show();
                }
            }
        }
    }



    public class fcm_return extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... s) {

            SharedPreferences sp=getActivity().getSharedPreferences("loginvalue",getActivity().MODE_PRIVATE);

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                     .add("senderid",sp.getString("id",""))
                    .add("userid",s[0].toString())// 받는사람 id
                    .build();

            Request request= new Request.Builder()
                    .url("http://jw910911.vps.phps.kr/push_notification_return.php")
                    .post(body)
                    .build();

            try{

                client.newCall(request).execute();

            }catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getActivity(),"친구 요청 확인 메세지 전송",Toast.LENGTH_LONG).show();
        }
    }


    // params[0]에 친구의 ID를 입력해서 그정보를 Json으로 받아와서 파싱한다.
    // 대화가 없을때 날짜 데이터 예외처리
    // nickname imageurl sex gretting을 받아와서 리스트뷰에 집어넣고 갱신.
    class friendinfoGet extends AsyncTask<String, Void, Void> {

        String json;
        int statement;
        String friendid;


        @Override
        protected Void doInBackground(String... params) {


            try{
                URL url = new URL("http://jw910911.vps.phps.kr/getfriendlist.php");
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param="id="+params[0];

                OutputStream outputStream=con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                StringBuilder sb=new StringBuilder();
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(con.getInputStream()));


                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n");
                }

                json=sb.toString().trim();
                statement=Integer.parseInt(params[1]);
                friendid=params[0];


            }catch(Exception e)
            {
                e.printStackTrace();
            }

            /*
            HttpUrl httpurl = new HttpUrl.Builder()
                    .scheme("http")
                    .host("http://jw910911.vps.phps.kr")
                    .addPathSegment("getfriendlist.php")
                    .build();
               */





            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid){

            //Toast.makeText(getActivity(),json+"",Toast.LENGTH_LONG).show();
            JSONObject jsonobj=null;
            JSONArray jarray=null;

            String nickname=null,sex=null,greeting=null,imageurl=null;

            try
            {

                Log.w("이부분 문제 생길듯 :","");
                jsonobj=new JSONObject(json);

                jarray=jsonobj.getJSONArray("result");
                jsonobj=jarray.getJSONObject(0);
                nickname=jsonobj.getString("nickname");
                sex=jsonobj.getString("sex");
                greeting=jsonobj.getString("greeting");
                imageurl=jsonobj.getString("imageurl");
            }catch(Exception e)
            {
                e.printStackTrace();
            }

            f_adapter.addItem(nickname,sex,greeting,imageurl,statement,friendid);
            f_adapter.notifyDataSetChanged();

            //이부분에 디비의 값에 nickname,imageurl업로드 시켜줘야함

            sqlite.execSQL("update talk set nickname='"+nickname+"', imageurl='"+imageurl+"' where loginid='"+idshared.getString("id","")+"' and friendid='"+friendid+"'");


            //처음 날짜 데이터 업로드
            //대화가 없을때 날짜 데이터 예외처리

            SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date=sd.format(new Date());

            Cursor c=sqlite.rawQuery("select * from talk where loginid='"+idshared.getString("id","")+"' and friendid='"+friendid+"'",null);
            if(c.getCount()==0)
            {
                //데이터를 받아올때 데이터가 없으면 날짜데이터를 집어넣는다.
                //오류 이유 이미 대화의 데이터가 있기 때문에 날짜 데이터가 들어가지 않음 .
                sqlite.execSQL("insert into talk (loginid,friendid,nickname,imageurl,messagetype, message,time) values ('"+idshared.getString("id","")+"','"+friendid+"','"+nickname+"','"+imageurl+"',1,'"+"날짜데이터"+"','"+date+"');");

            }

            //해쉬맵에 imageurl데이터 추가
            Global.urlHash.put(friendid,imageurl);
            Global.nicknameHash.put(friendid,nickname);

        }
    }


}


