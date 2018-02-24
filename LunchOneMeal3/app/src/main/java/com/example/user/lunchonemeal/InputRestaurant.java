package com.example.user.lunchonemeal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.bitmap;
import static android.R.attr.inset;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.media.ExifInterface.ORIENTATION_NORMAL;
import static android.os.Build.ID;
import static android.provider.LiveFolders.INTENT;
import static com.example.user.lunchonemeal.R.id.enrollLocation;
import static com.example.user.lunchonemeal.R.id.res_images;
import static com.example.user.lunchonemeal.R.id.res_title;


/**
 * Created by user on 2017-04-19.
 */

public class InputRestaurant extends AppCompatActivity implements View.OnClickListener {

    ImageView res_images;
    private Uri imageUri;

    private String KEY_IMAGE="image";
    private String KEY_NAME="name";

    Bitmap bitmap;

    String UPLOAD_URL="http://jw910911.vps.phps.kr/upload.php";

    EditText res_title;
    EditText res_name,res_price,res_info;

    Button res_inputbutton;
    Button enrollLocation;

    String[] locationInfo=null; //StartActivityForResult로 받은 위치정보
    //타이틀 / 설명 / 구주소 / 도로명주소 / 전화번호 / 지도 x좌표 / 지도 y좌표 /링크

    //DB메니저로 장소정보 업데이트

    DB_Manager db_manager;

    RadioGroup radioG;
    RadioButton meal,desert;

    //curl 설정

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputrestaurant);

        res_name=(EditText)findViewById(R.id.res_restaurantname);
        res_price=(EditText)findViewById(R.id.res_price);
        res_info=(EditText)findViewById(R.id.res_info);

        radioG=(RadioGroup)findViewById(R.id.radio);
        meal=(RadioButton)findViewById(R.id.meal);
        desert=(RadioButton)findViewById(R.id.desert);


        Toast.makeText(getApplicationContext(),"onCreate호출",Toast.LENGTH_LONG).show();

        res_images=(ImageView)findViewById(R.id.res_images);
        res_images.setOnClickListener(this);
        res_title=(EditText)findViewById(R.id.res_title);

        res_inputbutton=(Button)findViewById(R.id.res_inputbutton);
        res_inputbutton.setOnClickListener(this);

        enrollLocation=(Button)findViewById(R.id.enrollLocation);
        enrollLocation.setOnClickListener(this);

        db_manager=new DB_Manager();
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);


        if(resultCode!=RESULT_OK)
            return;

        if(requestCode==1002&&data!=null)
        {

            //타이틀 / 설명 / 구주소 / 도로명주소 / 전화번호 / 지도 x좌표 / 지도 y좌표 /링크
            Toast.makeText(getApplicationContext(),"장소값 전달받음",Toast.LENGTH_LONG).show();
            locationInfo = data.getStringArrayExtra("parseResult");
            res_info.setText("tel : "+locationInfo[4]+"\nadd : "+locationInfo[3]);



        }

        if(requestCode==1001&&data!=null) {

            Uri FilePath= data.getData();//안드로이드 컨텐트 리졸버의 URI를 가져온다

            try {
                Toast.makeText(getApplicationContext(),""+FilePath,Toast.LENGTH_LONG).show();

                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),FilePath);
                res_images.setImageBitmap(bitmap);
                /*
                Bitmap image = BitmapFactory.decodeFile(imagePath);
                ExifInterface exif=new ExifInterface(imagePath);
                int exifOrientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                int exifDegree=exifOrientationToDegrees(exifOrientation);
                image=rotate(image,exifDegree);
                res_images.setImageBitmap(image);
                */

            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String getStringImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos);

        byte[] imageBytes=baos.toByteArray();
        String encodedImage= Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }



    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();
        return path;
    }

    private void uploadImage(){
        final ProgressDialog loading=ProgressDialog.show(this,"Uploading...",
                "Please Wait...",false,false);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                        db_manager.locationUpdate(locationInfo);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                String image = getStringImage(bitmap);

                String title = res_title.getText().toString();
                String price=res_price.getText().toString().trim();
                String name=res_name.getText().toString().trim();
                String information=res_info.getText().toString().trim();
                String kind;

                if(radioG.getCheckedRadioButtonId()==meal.getId())
                {
                    kind="meal";
                }else if(radioG.getCheckedRadioButtonId()==desert.getId())
                {
                    kind="desert";
                }else
                {
                    kind="cafe";
                }


                Map<String, String> params = new Hashtable<String, String>();

                params.put("Content-Type", "application/json; charset=utf-8");
                params.put("image", image);
                params.put("title", title);
                params.put("price",price);
                params.put("information",information);
                params.put("name",name);
                SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);
                params.put("id",shared.getString("id",""));
                params.put("kind",kind);

                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }



    private Bitmap rotate(Bitmap bmp,int value){
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(value);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);


        return resizedBitmap;
    }


    int exifOrientationToDegrees(int exifOrientation)
    {
        if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }else if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }






    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.res_images)
        {
            //Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            //startActivityForResult(intent,1001);
            Intent intent=new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent,"Select Picture"),1001);
        }else if(v.getId()==R.id.res_inputbutton)
        {
            if(res_info.getText().length()==0||res_name.getText().length()==0||res_price.getText().length()==0||res_title.getText().length()==0||bitmap==null)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(InputRestaurant.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage("공란의 정보를 모두 입력해주세요.");
                alert.show();
            }
            else if (locationInfo==null)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(InputRestaurant.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage("장소 등록을 해주세요.");
                alert.show();

            }

            else {
                uploadImage();
            }
        }
        else if(v.getId()==R.id.enrollLocation)
        {
            Intent i=new Intent(this,InputLocationActivity.class);
            startActivityForResult(i,1002);
        }
        else if(v.getId()==R.id.res_cancelbutton)
        {
            finish();
        }
    }
}
