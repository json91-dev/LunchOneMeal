package com.example.user.lunchonemeal;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by user on 2017-06-02.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG="MyFirebaseIIDService";

    // [Strat refresh_token]


    @Override
    public void onTokenRefresh() {

        String token=FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Refreshed token"+token);

        SharedPreferences shared=getSharedPreferences("tokenstore",MODE_PRIVATE);
        SharedPreferences.Editor edit=shared.edit();
        edit.putString("token",token);
        edit.commit();

        sendRegistrationToServer(token);
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
