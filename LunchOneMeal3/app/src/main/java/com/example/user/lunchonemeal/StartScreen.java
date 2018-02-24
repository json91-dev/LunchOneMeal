package com.example.user.lunchonemeal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.R.attr.start;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.example.user.lunchonemeal.R.id.login;
import static com.nhn.android.data.g.i;

public class StartScreen extends AppCompatActivity implements Button.OnClickListener{

    //변수 선언
    DB_Manager db_manager;
    EditText ed_id,ed_pw;

    SharedPreferences mPref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        db_manager=new DB_Manager();
        ed_id=(EditText)findViewById(R.id.id);
        ed_pw=(EditText)findViewById(R.id.pw);

        Button login=(Button)findViewById(R.id.login);
        login.setOnClickListener(this);

        Button join=(Button)findViewById(R.id.join);
        join.setOnClickListener(this);




        //db설정
        mPref=getSharedPreferences("loginvalue",MODE_PRIVATE);
        editor=mPref.edit();

        if(mPref.getInt("loginvalue",0)==1) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }
        else{
            startActivity(new Intent(this,SplashActivity.class));//스플레시 이미지 인텐트
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.login :
                String id=ed_id.getText().toString();
                String pw=ed_pw.getText().toString();
                int result=db_manager.HttpLogin(id,pw);

                if(result==2)
                {

                    editor.putInt("loginvalue",1);//1일때 로그인
                    editor.putString("id",id);
                    editor.commit();

                    SharedPreferences shared2=getSharedPreferences("tokenstore",MODE_PRIVATE);
                    String token=shared2.getString("token","");

                    if(token.equals(""))//만약에 토큰이 DB에 저장되어 있지 않다면
                    {
                        //그냥 로그인은 한다.
                    }
                    else
                    {
                        //토큰이 DB에 저장되어 있다면?
                        new TokenUpdate(token).start();
                    }

                    Intent i=new Intent(this,MainScreenActivity.class);
                    startActivity(i);
                    finish();
                }else if(result==1)
                {
                    Toast.makeText(getApplicationContext(),"이메일 인증을 해주시기 바랍니다.",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"아이디 패스워드를 확인해주세요",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.join:
                startActivity(new Intent(this,MembershipActivity.class));
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


    public class TokenUpdate extends Thread
    {
        String token=null;

        public TokenUpdate(String token)
        {
            this.token=token;
        }

        @Override
        public void run() {

            sendRegistrationToServer(token);
            super.run();
        }

        public void sendRegistrationToServer(String token)
        {
            SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

            Log.e("전달전달", shared.getString("id",""));

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("Token",token)
                    .add("userid",shared.getString("id",""))
                    .build();

            Request request= new Request.Builder()
                    .url("http://jw910911.vps.phps.kr/fcm_register.php")
                    .post(body)
                    .build();

            try{

                client.newCall(request).execute();

            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
