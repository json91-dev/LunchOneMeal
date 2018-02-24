package com.example.user.lunchonemeal;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Map;

import static android.R.attr.bitmap;
import static android.R.string.no;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

import static com.example.user.lunchonemeal.R.id.myimage;
import static com.example.user.lunchonemeal.R.id.res_info;
import static com.example.user.lunchonemeal.R.id.res_price;
import static com.example.user.lunchonemeal.R.id.res_title;
import static com.example.user.lunchonemeal.R.id.sex;
import static com.example.user.lunchonemeal.R.id.woman;
import static com.nhn.android.data.g.a;
import static com.nhn.android.data.g.f;
import static java.lang.Integer.parseInt;

/**
 * Created by user on 2017-04-18.
 */

public class Fragment_Profile extends Fragment implements View.OnClickListener{

    //http://jw910911.vps.phps.kr/uploads/0.png

    ImageView myimage;
    TextView nickname,greeting;
    RadioGroup sexRadio;
    Spinner ageSpinner;

    Button modify;

    Bitmap bm;


    private static final int PICK_FROM_CAMERA=0;
    private static final int PICK_FROM_ALBUM=1;
    private static final int CROP_FROM_CAMERA=2;


    //URI : Uniform Resource Identifier

    private Uri mImageCaptureUri;

    String IMAGE_URL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_profile,container,false);
        myimage=(ImageView)rootView.findViewById(R.id.myimage);
        myimage.setOnClickListener(this);
        modify=(Button)rootView.findViewById(R.id.modify);
        modify.setOnClickListener(this);


        nickname=(TextView)rootView.findViewById(R.id.nickname);
        greeting=(TextView)rootView.findViewById(R.id.greeting);
        sexRadio=(RadioGroup)rootView.findViewById(sex);
        ageSpinner=(Spinner)rootView.findViewById(R.id.age);

        new getprofile().execute();





        return rootView;
    }

    private void doTakePhotoAction()
    {
        /*
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url="tmp_"+String.valueOf(System.currentTimeMillis());
        mImageCaptureUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);

        startActivityForResult(intent,PICK_FROM_CAMERA);
        */
        //opencv 테스트
        Intent i=new Intent(getActivity(),OpencvCamera.class);
        startActivity(i);
    }

    private void doTakeAlbumAction()
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent,PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode!=RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case CROP_FROM_CAMERA:
            {
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
            }
            case PICK_FROM_ALBUM:
            {

                Bitmap bit=null;
                try {
                    bit = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                }catch(Exception e)
                {
                    e.printStackTrace();

                }
                String url="tmp_"+String.valueOf(System.currentTimeMillis());
                mImageCaptureUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));


                try {
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.PNG,80,stream);
                    bm=bit;
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



            }

            case PICK_FROM_CAMERA:
            {


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
                    e.printStackTrace();
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
            }
        }
    }

    @Override
    public void onClick(View view) {

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
        if(view.getId()==myimage.getId())
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영",cameraListener)
                    .setNeutralButton("앨범선택",albumListener)
                    .setNegativeButton("취소",cancelListener)
                    .show();
        }else if(view.getId()==modify.getId())
        {
            uploadImage();

            //이미지 처리


            SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",MODE_PRIVATE);
            String id=shared.getString("id","");

            SharedPreferences shared2=getActivity().getSharedPreferences(id,MODE_PRIVATE);
            SharedPreferences.Editor myEdit=shared2.edit();
            myEdit.putString("nickname",nickname.getText().toString());
            myEdit.putString("greeting",greeting.getText().toString());


            //업로드 후에 cash에 로그인한아이디_profile 의 이름값으로 경로를 만든다
            String filePath=getActivity().getCacheDir().getAbsolutePath()+"/"+id+"_profile";
            myEdit.putString("filepath",filePath);

            myEdit.commit();

            try {

                //경로로 파일을 만들고 아웃풋 스트림을 만든다
                FileOutputStream fos = new FileOutputStream(new File(filePath));
                ByteArrayOutputStream stream=new ByteArrayOutputStream();

                //현재 bm의 값을 압축하여 파일에 저장한다.
                bm.compress(Bitmap.CompressFormat.PNG,90,stream);
                byte [] bytes=stream.toByteArray();
                fos.write(bytes);

            }catch (Exception e)
            {

            }

            onPause();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        //네비게이션뷰의 프로필 설정하는 부분

        SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",MODE_PRIVATE);
        String id=shared.getString("id","");

        SharedPreferences shared2=getActivity().getSharedPreferences(id,MODE_PRIVATE);
        Toast.makeText(getActivity(),"filePath : "+shared2.getString("filepath",null)+"",Toast.LENGTH_LONG).show();

        if(shared2.getString("filepath",null)!=null) {
            NavigationView navigationview = (NavigationView)getActivity().findViewById(R.id.nav_view);
            View nav_header_view = navigationview.getHeaderView(0);

            TextView nickname = (TextView) nav_header_view.findViewById(R.id.nickname);
            TextView greeting = (TextView) nav_header_view.findViewById(R.id.greeting);
            de.hdodenhof.circleimageview.CircleImageView profile_image =
                    (de.hdodenhof.circleimageview.CircleImageView) nav_header_view.findViewById(R.id.profile_image);

            nickname.setText(shared2.getString("nickname", ""));
            greeting.setText(shared2.getString("greeting", ""));

            String filePath = shared2.getString("filepath", "");
            Bitmap bm = BitmapFactory.decodeFile(filePath);

            //현재 이미지에 bm값 설정.
            profile_image.setImageBitmap(bm);
        }
    }

    public class getprofile extends AsyncTask<Void,Void,Void>
    {

        String nickname_,sex_,greeting_;
        int age_;
        String json;


        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("http://jw910911.vps.phps.kr/getprofiledata.php");
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",MODE_PRIVATE);
                String param="id="+shared.getString("id","");
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

                JSONObject jsonobj=new JSONObject(json);
                JSONArray jsonarray=jsonobj.getJSONArray("result");
                jsonobj=jsonarray.getJSONObject(0);

                nickname_=jsonobj.getString("nickname");
                sex_=jsonobj.getString("sex");
                age_=jsonobj.getInt("age");
                greeting_=jsonobj.getString("greeting");
                IMAGE_URL=jsonobj.getString("imageurl");

                //비트맵을 받아서 bm에 저장시킨다.
                //bm=Glide.with(getActivity()).load(IMAGE_URL).asBitmap().into(400,400).get();


                //url 받아오는 부분


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            Toast.makeText(getActivity(),json+"",Toast.LENGTH_LONG).show();
            try
            {
                new imageThread().start();
                //bm=new ImageDownloader(getActivity(),myimage,IMAGE_URL).execute().get();





                //if(!IMAGE_URL.equals("등록 안됨"))
                    //Glide.with(getActivity()).load(IMAGE_URL).thumbnail(0.2f).skipMemoryCache(true).into(myimage);

            }catch(Exception e)
            {
                e.printStackTrace();
            }


            /*
            ImageView myimage;
            TextView nickname,greeting;
            RadioGroup sexRadio;
            Spinner ageSpinner;
            */

            nickname.setText(nickname_.toString());
            greeting.setText(greeting_.toString());

            Toast.makeText(getActivity(),sex_.toString(),Toast.LENGTH_LONG).show();
            if(sex_.toString().equals("man"))
            {
                sexRadio.check(R.id.man);
            }
            else
            {
                sexRadio.check(woman);
            }

            int spinnerPosition=age_-15;
            ageSpinner.setSelection(spinnerPosition);



        }

    }

    private void uploadImage(){
        final ProgressDialog loading=ProgressDialog.show(getActivity(),"Uploading...",
                "Please Wait...",false,false);


        StringRequest stringRequest=new StringRequest(Request.Method.POST,"http://jw910911.vps.phps.kr/uploadprofile.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(getActivity(),s,Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),"회원정보 수정됨",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(getActivity(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                String image = getStringImage(bm);


                String nickname_ = nickname.getText().toString();
                String sex_;
                if(sexRadio.getCheckedRadioButtonId()==R.id.man)
                {
                    sex_="man";
                }else{
                    sex_="woman";
                }

                String age=ageSpinner.getSelectedItem().toString().substring(0,2);
                String greeting_=greeting.getText().toString();

                Map<String, String> params = new Hashtable<String, String>();

                params.put("Content-Type", "application/json; charset=utf-8");
                params.put("image", image);
                params.put("nickname",nickname_);
                params.put("sex", sex_);
                params.put("greeting",greeting_);
                params.put("age",age);
                SharedPreferences shared=getActivity().getSharedPreferences("loginvalue",MODE_PRIVATE);
                String id=shared.getString("id","");
                params.put("id",id);



                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }


    //비트맵을 받아서 JPEG포멧의 바이트 형식으로 아웃풋 스트림에 담아서 imageBytes에 저장하고 이 2진 바이트값을 base64인코딩을 해서 String으로 출력하는 함수
   public static String getStringImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos);

        byte[] imageBytes=baos.toByteArray();
        String encodedImage= Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }

    class imageThread extends Thread
    {
        Handler h=new Handler();

        @Override
        public void run() {
            try {

                bm = new ImageDownloader(getActivity(), myimage, IMAGE_URL).execute().get();

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        myimage.setImageBitmap(bm);
                    }
                });

            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }


    }


}


/*
    public void run() {
        try {
            Toast.makeText(getActivity(),"입력",Toast.LENGTH_LONG).show();
            URL url=new URL("http://jw910911.vps.phps.kr/uploads/60.png");
            URLConnection urlc=url.openConnection();
            BufferedInputStream bis=new BufferedInputStream(urlc.getInputStream());
            Bitmap bm= BitmapFactory.decodeStream(bis);
            bis.close();

            myimage.setImageBitmap(bm);

        } catch (Exception e) {
            e.printStackTrace();
        }



  */
