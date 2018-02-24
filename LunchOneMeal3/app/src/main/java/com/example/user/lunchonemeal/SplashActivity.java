package com.example.user.lunchonemeal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by user on 2017-04-09.
 */

public class SplashActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },3000);
    }
    
}
