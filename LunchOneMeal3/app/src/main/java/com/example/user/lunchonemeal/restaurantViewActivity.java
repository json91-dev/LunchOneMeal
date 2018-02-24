package com.example.user.lunchonemeal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.kakaolink.v2.model.ContentObject;
import com.kakao.kakaolink.v2.model.LinkObject;
import com.kakao.kakaolink.v2.model.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.R.attr.description;
import static android.R.attr.dial;
import static android.R.attr.name;
import static android.R.attr.switchMinWidth;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.user.lunchonemeal.R.drawable.profile;
import static com.example.user.lunchonemeal.R.id.email;
import static com.example.user.lunchonemeal.R.id.pw;

/**
 * Created by user on 2017-05-03.
 */

public class restaurantViewActivity extends AppCompatActivity implements View.OnClickListener{

    String no=null;
    ImageView mainImage;
    TextView title,resname,prices,detail,telephone,links;
    MapFragment mapfragment;
    ImageView trans_image;
    ScrollView scrollview;
    Button frined_request_btn;

    TextView nickname,sex,greeting,age;
    de.hdodenhof.circleimageview.CircleImageView profileImage;

    String imagefilepath;


    String titleitem,
            titlelocations,
            name,
            price,
            description,
            urlimage,
            longitude,
            latitude,
            roadAddress,
            address,
            telnumber,
            link,
            id;

    String nickname_,
            sex_,
            greeting_,
            age_,
            profileurl_;

    SQLiteDatabase sqlite;

    String loginid_,friendid_;





    OnMapReadyCallback onmapreadycallback=new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));


            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latlng);
            markerOptions.title(name);
            markerOptions.snippet(telnumber);
            googleMap.addMarker(markerOptions);


            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,16));
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resautrantviewactivity);

        frined_request_btn=(Button)findViewById(R.id.friend_request_btn);
        frined_request_btn.setOnClickListener(this);




        Intent intent=getIntent();
        no=intent.getExtras().getString("no");

        nickname=(TextView)findViewById(R.id.nickname);
        sex=(TextView)findViewById(R.id.sex);
        greeting=(TextView)findViewById(R.id.greeting);
        age=(TextView)findViewById(R.id.age);
        profileImage=(de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.mImage);


        //지도
        mapfragment=(MapFragment)getFragmentManager().findFragmentById(R.id.v_mapview);
        scrollview=(ScrollView)findViewById(R.id.scrollview);
        trans_image=(ImageView)findViewById(R.id.trans_image);
        trans_image.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scrollview.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scrollview.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollview.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        Button kakaobutton=(Button)findViewById(R.id.kakao);
        kakaobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imagefilepath!=null)
                {
                    LocationTemplate params=LocationTemplate.newBuilder(roadAddress,
                            ContentObject.newBuilder(titlelocations,
                                    urlimage,
                                    LinkObject.newBuilder()
                                            .setWebUrl(link)
                                            .setMobileWebUrl(link)
                                            .build())
                                    .setDescrption(description)
                                    .build())
                            .setAddressTitle(titlelocations)
                            .build();

                    KakaoLinkService.getInstance().sendDefault(getApplicationContext(), params, new ResponseCallback<KakaoLinkResponse>() {
                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Logger.e(errorResult.toString());
                        }

                        @Override
                        public void onSuccess(KakaoLinkResponse result) {

                        }
                    });
                    /*
                    Intent intent=new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(imagefilepath));
                    intent.setPackage("com.kakao.talk");
                    startActivity(intent);
                    */
                }

            }
        });

        //데이터들
        mainImage=(ImageView)findViewById(R.id.v_mainimage);
        title=(TextView)findViewById(R.id.v_title);
        resname=(TextView)findViewById(R.id.v_resname);
        prices=(TextView)findViewById(R.id.v_price);
        detail=(TextView)findViewById(R.id.detail);
        telephone=(TextView)findViewById(R.id.telephone);
        links=(TextView)findViewById(R.id.link);
        links.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(links.getText().toString()));
                startActivity(intent);
            }
        });

        //JSON값 가져와서 View작업
        new httpGet().execute();


        //db helper 생성
        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite= h.getSQLiteDb();

        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid_+"' and friendid='"+friendid_+"' and statement=1",null);
        if(c.getCount()==1)
        {
            frined_request_btn.setBackgroundResource(R.drawable.button2);
            frined_request_btn.setText("친구");
        }




        //loginid 설정

        SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);
        loginid_=shared.getString("id","");
    }

    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.friend_request_btn)
        {
            SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);
            if(shared.getString("id","").equals(id))
            {
                Toast.makeText(getApplicationContext(),"본인이 등록한 게시물입니다.",Toast.LENGTH_LONG).show();
            }else
            {
                final EditText et=new EditText(restaurantViewActivity.this);
                AlertDialog.Builder ad=new AlertDialog.Builder(restaurantViewActivity.this);
                ad.setTitle("친구요청");
                ad.setMessage("친구요청시 보내실 메세지를 입력해주세요.^^");
                ad.setView(et);
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid_+"' and friendid='"+friendid_+"'",null);

                        //만약에 친구목록에 내 아이디와 상대방의 아이디가 저장된 컬럼이 있다면?

                        if(c.getCount()==1)
                        {
                            c=null;
                            c=sqlite.rawQuery("select * from friendlist where loginid='"+loginid_+"' and friendid='"+friendid_+"' and statement=2",null);

                            //저장된 컬럼이 있고 상태가 친구요청 대기중이라면
                            if(c.getCount()==1)
                            {
                                //친구요청 대기중이라는 메세지를 띄운다.
                                Toast.makeText(getApplicationContext(),"친구요청 대기중",Toast.LENGTH_LONG).show();

                                //fcm 요청을 전송하는 쓰레드를 실행한다.
                                new fcmrequest().execute(et.getText().toString());

                            }else
                            {
                                Toast.makeText(getApplicationContext(),"이미 친구입니다",Toast.LENGTH_LONG).show();
                            }

                        }
                        //친구목록에 없다면.?
                        else
                        {
                            //친구 요청 대기중이라는 뷰를 가진 아이템을 추가한다.
                            sqlite.execSQL("insert into friendlist (loginid,friendid,statement) values ('"+loginid_+"','"+friendid_+"',2)");
                            new fcmrequest().execute(et.getText().toString());
                        }

                    }
                });
                ad.show();

            }
        }
    }

    public class fcmrequest extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... s) {

            SharedPreferences sp=getSharedPreferences("loginvalue",MODE_PRIVATE);

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("message",s[0].toString())
                    .add("senderid",sp.getString("id",""))
                    .add("userid",id)
                    .build();

            Request request= new Request.Builder()
                    .url("http://jw910911.vps.phps.kr/push_notification.php")
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
            Toast.makeText(getApplicationContext(),"친구 요청 완료.~",Toast.LENGTH_LONG).show();
         }
    }

    public class httpGet extends AsyncTask<Void,Void,Void>
    {
        String json;//json 문자열

        @Override
        protected Void doInBackground(Void... voids) {

            BufferedReader bufferedReader = null;
            try {
                URL url = new URL("http://jw910911.vps.phps.kr/getitemdata.php");
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param="no="+no;

                OutputStream outputStream=con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                StringBuilder sb=new StringBuilder();
                bufferedReader=new BufferedReader(new InputStreamReader(con.getInputStream()));


                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n");
                }
                json=sb.toString().trim();

                JSONObject jsonobj=new JSONObject(json);
                JSONArray jsonarray=jsonobj.getJSONArray("result");

                jsonobj=jsonarray.getJSONObject(0);

                titleitem=jsonobj.getString("titleitem");
                titlelocations=jsonobj.getString("titlelocations");
                name=jsonobj.getString("name");
                price=jsonobj.getString("price");
                description=jsonobj.getString("description");
                urlimage=jsonobj.getString("url");
                longitude=jsonobj.getString("longitude");
                latitude=jsonobj.getString("latitude");
                roadAddress=jsonobj.getString("roadaddress");
                address=jsonobj.getString("address");
                telnumber=jsonobj.getString("telnumber");
                link=jsonobj.getString("link");
                id=jsonobj.getString("id");

                nickname_=jsonobj.getString("nickname");
                sex_=jsonobj.getString("sex");
                greeting_=jsonobj.getString("greeting");
                age_=jsonobj.getString("age");
                profileurl_=jsonobj.getString("profileurl");


                //url로 친구 id 설정
                friendid_=id;


            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(getApplicationContext(),json.toString().substring(0,30),Toast.LENGTH_LONG).show();

            Toast.makeText(getApplicationContext(),urlimage+"",Toast.LENGTH_LONG).show();


            title.setText(titleitem);
            detail.setText(getDetail());
            resname.setText(name);
            prices.setText(price+"원대");
            telephone.setText(telnumber);
            links.setText(link);

            nickname.setText(nickname_);
            sex.setText(sex_);
            greeting.setText(greeting_);
            age.setText(age_+"세");


            try {
                Glide.with(getApplicationContext()).load(urlimage).thumbnail(0.4f).into(mainImage);

                //Bitmap bit = new ImageDownloader(getApplicationContext(), mainImage, urlimage).execute().get();
                //mainImage.setImageBitmap(bit);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            try {

                Glide.with(getApplicationContext()).load(profileurl_).thumbnail(0.4f).into(profileImage);


                //Bitmap bit = new ImageDownloader(getApplicationContext(), profileImage, profileurl_).execute().get();
                //profileImage.setImageBitmap(bit);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            mapfragment.getMapAsync(onmapreadycallback);

            //캐시의 이미지 파일 경로 저장..
            imagefilepath=getApplicationContext().getCacheDir().getAbsolutePath()+urlimage.substring(urlimage.lastIndexOf("/"),urlimage.length());


        }



        public String getDetail() {
            return "업체명 : "+titlelocations+ "\n정보 : "+description+"\n구주소 : "+address+"\n도로명주소 : "+roadAddress;
        }
    }
}





/*
    public String toString() {
        return "업체명 : "+title+ "\n정보 : "+description+"\n구주소 : "+address+"\n도로명주소 : "+roadaddress+
                "\n전화번호 : "+telnumber;
    }
*/