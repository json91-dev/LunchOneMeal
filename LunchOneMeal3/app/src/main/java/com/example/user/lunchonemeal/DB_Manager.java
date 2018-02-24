package com.example.user.lunchonemeal;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.nhn.android.data.g.g;


/**
 * Created by user on 2017-04-12.
 */

public class DB_Manager {
    private String urlPath;
    //회원가입 php URL
    private final String info_urlPath="http://jw910911.vps.phps.kr/join.php";
    private final String http_login_urlPath="http://jw910911.vps.phps.kr/login.php";
    private final String HTTP_ID_CHECK_URLPATH="http://jw910911.vps.phps.kr/id_check.php";
    private final String Update_locations="http://jw910911.vps.phps.kr/location.php";

    private String id;
    private String pw;
    private String email;

    private String [] location;

    //위치정보 DB에 업데이트 하는 부분..

    public void locationUpdate(String [] location)
    {
        this.location=location;
        try {
            new locationset().execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return;
    }

    private class locationset extends AsyncTask<Void,Void,Void>
    {
        //no title description address roadaddress telnumber mapx mapy
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                URL url=new URL(Update_locations);
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");


                Log.d("location-Update","백그라운드 시작");

                String params="title="+location[0]+"&description="+location[1]+"&address="+location[2]+
                        "&roadaddress="+location[3]+"&telnumber="+location[4]+"&mapx="+location[5]+
                        "&mapy="+location[6]+"&link="+location[7];

                OutputStream outputStream=con.getOutputStream();
                outputStream.write(params.getBytes());
                outputStream.flush();
                outputStream.close();

                Log.d("location-Update","백그라운드 중앙");

                BufferedReader rd=null;
                rd=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                String line=null;

                while((line=rd.readLine())!=null){
                    Log.d("location-Update",line);
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Log.d("location 업데이트","성공");
        }
    }






    ArrayList<String> results;

    //유저에게 값 입력 받는 부분
    public ArrayList<String> setup_member_information(String id ,String pw,String email)
    {
        urlPath=info_urlPath;
        this.id=id;
        this.pw=pw;
        this.email=email;

        try{
            results=new PostUserInformation().execute().get();
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        return results;
    }




    //회원가입 http로 전송하는 부분
    public class PostUserInformation extends AsyncTask<Void,Void,ArrayList<String>>{
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try{
                URL url=new URL(urlPath);
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param="id="+id+"&pw="+pw+"&email="+email;

                OutputStream outputStream=con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd=null;
                rd=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                String line=null;
                while((line=rd.readLine())!=null){
                    Log.d("BufferedReader:",line);
                }

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
        }
    }



    //http 로그인 값 얻어오는 함수
    public int HttpLogin(String id,String pw)
    {

        int result=0;
        urlPath=http_login_urlPath;
        this.id=id;
        this.pw=pw;

        try{
            result=new PostHttpLoginInformation().execute().get();
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }catch (ExecutionException e)
        {
            e.printStackTrace();
        }



        return result;
    }

    class PostHttpLoginInformation extends AsyncTask<Void,Void,Integer> { //내부적으로 처리 => 로그인값 0,1,2 얻어오는 백그라운드 쓰레드 처리
        @Override
        protected Integer doInBackground(Void... voids) {

            int resultvalue=0;
            try {
                URL url = new URL(urlPath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param = "id=" + id + "&pw=" + pw;

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                String line = null;
                while ((line = rd.readLine()) != null) {
                    Log.d("BufferedReader:",line);
                    resultvalue=Integer.parseInt(line);

                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resultvalue;
        }

        @Override
        protected void onPostExecute(Integer resultvalue) {
            super.onPostExecute(resultvalue);
        }

    }

    //아이디 중복 확인 값 얻어오는 함수

    //http 로그인 값 얻어오는 함수
    public int HTTP_ID_CHECK(String id)
    {

        int result=0;
        urlPath=HTTP_ID_CHECK_URLPATH;
        this.id=id;


        try{
            result=new HTTP_ID_CHECK_THREAD().execute().get();
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    class HTTP_ID_CHECK_THREAD extends AsyncTask<Void,Void,Integer> { //내부적으로 처리 => 로그인값 0,1,2 얻어오는 백그라운드 쓰레드 처리
        @Override
        protected Integer doInBackground(Void... voids) {

            int resultvalue=0;
            try {
                URL url = new URL(urlPath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param = "id=" + id;

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                String line = null;
                while ((line = rd.readLine()) != null) {
                    Log.d("버퍼:",line);
                    resultvalue=Integer.parseInt(line);

                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resultvalue;
        }

        @Override
        protected void onPostExecute(Integer resultvalue) {
            super.onPostExecute(resultvalue);
        }

    }



}
