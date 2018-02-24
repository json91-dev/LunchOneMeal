package com.example.user.lunchonemeal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.value;

import static com.example.user.lunchonemeal.R.id.join;
import static java.lang.Boolean.FALSE;

/**
 * Created by user on 2017-04-12.
 */

public class MembershipActivity extends Activity implements Button.OnClickListener{

    EditText et_id,et_pw,et_pw2,et_email;
    String id,pw,pw2,email;
    boolean id_ok=false;
    DB_Manager db_manager;
    Button mybutton,idcheckbutton;
    String join_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.membership);

        et_id=(EditText)findViewById(R.id.id);
        et_pw=(EditText)findViewById(R.id.pw);
        et_pw2=(EditText)findViewById(R.id.pw2);
        et_email=(EditText)findViewById(R.id.email);
        db_manager=new DB_Manager();

        join_value="BeforeAuth";

        //버튼에 리스너 설정
        mybutton=(Button)findViewById(R.id.mybutton);
        mybutton.setOnClickListener(this);
        idcheckbutton=(Button)findViewById(R.id.idcheckbutton);
        idcheckbutton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.idcheckbutton)
        {
            id=et_id.getText().toString();
            AlertDialog.Builder alert = new AlertDialog.Builder(MembershipActivity.this);
            if(db_manager.HTTP_ID_CHECK(id)==1)//1일때 id중복
            {
                join_value="BeforeAuth";

                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage("아이디가 중복되었습니다.");
                alert.show();

            }else
            {
                join_value="Authentication";

                alert = new AlertDialog.Builder(MembershipActivity.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage("사용 가능한 아이디입니다.");
                alert.show();

            }
        }



        if(view.getId()==R.id.mybutton)
        {
            id=et_id.getText().toString();
            pw=et_pw.getText().toString();
            pw2=et_pw2.getText().toString();
            email=et_email.getText().toString();

            //아이디 유효성 검사
            if(id.length()==0){//개수
                Toast.makeText(getApplicationContext(),"아이디를 입력해주세요",Toast.LENGTH_LONG).show();
                return;
            }else if(id.length()<8 || id.length()>15)
            {
                Toast.makeText(getApplicationContext(),"아이디 글자수 오류\n(8자 이상 15자 이하)",Toast.LENGTH_LONG).show();
                return;
            }else{}

            if(RegularExpressionCheck("id",id)==false)//형식
            {
                Toast.makeText(getApplicationContext(),"아이디 형식 오류\n(영문자+숫자)",Toast.LENGTH_LONG).show();
                return;
            }

            //패스워드 유효성 검사
            if(pw.length()==0){//개수
                Toast.makeText(getApplicationContext(),"패스워드를 입력해주세요",Toast.LENGTH_LONG).show();
                return;
            }else if(pw.length()<8 || pw.length()>15)
            {
                Toast.makeText(getApplicationContext(),"패스워드 글자수 오류\n(8자 이상 15자 이하)",Toast.LENGTH_LONG).show();
                return;
            }else{}

            if(RegularExpressionCheck("password",pw)==false)//형식
            {
                Toast.makeText(getApplicationContext(),"비밀번호 형식 오류",Toast.LENGTH_LONG).show();
                return;
            }

            if(!pw.equals(pw2)){//일치
                Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
                return;
            }

            //이메일 유효성 검사

            if(email.length()==0) {//개수
                Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_LONG).show();
                return;
            }

            if(RegularExpressionCheck("email",email)==false)//형식
            {
                Toast.makeText(getApplicationContext(),"이메일 형식 오류",Toast.LENGTH_LONG).show();
                return;
            }

            if(join_value=="BeforeAuth")
            {

                Toast.makeText(getApplicationContext(),"아이디 중복확인을 해주세요",Toast.LENGTH_LONG).show();
                return;
            }

            db_manager.setup_member_information(id,pw,email);
            //인증 메일 전송
            sendmail();
            Toast.makeText(getApplicationContext(),"정보 전송",Toast.LENGTH_LONG).show();

            AlertDialog.Builder alert = new AlertDialog.Builder(MembershipActivity.this);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Intent i=new Intent(getApplicationContext(),StartScreen.class); //닫기
                    //startActivity(i);
                    finish();
                }
            });
            alert.setMessage("본인인증 메일이 발송되었습니다.\n 확인 후 인증해주세요.");
            alert.show();



        }

        //google로 메일 보내는 함수

    }

    public void sendmail()
    {
        GMailSender sender = new GMailSender("jjjw910911", "cylpisabcvidzvpv"); // SUBSTITUTE

        try
        {
            Toast.makeText(getApplicationContext(),"메일 전송",Toast.LENGTH_LONG).show();
            sender.sendMail("회원가입 확인 인증 메일입니다.",
                    id,
                    "jjjw910911@gmail.com",
                    email // to.getText().toString()
            );

        } catch (Exception e)
        {
            Log.e("SendMail", e.getMessage(), e);
        }

    }




    //유효성 검사 (정규식)
    boolean RegularExpressionCheck(String option,String value)
    {
        if(option=="password") {
            Pattern p = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,15}$");
            Matcher m=p.matcher(value);
            return m.matches();//맞으면 true 틀리면 false
        }
        if(option=="id")
        {
            Pattern p=Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).{8,15}$");
            Matcher m=p.matcher(value);
            return m.matches();//맞으면 true 틀리면 false
        }
        if(option=="email")
        {
            Pattern p=Pattern.compile("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$");
            Matcher m=p.matcher(value);
            return m.matches();//맞으면 true 틀리면 false
        }
        return false;
    }

}
