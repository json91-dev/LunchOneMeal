package com.example.user.lunchonemeal;

import android.app.ActionBar;
import android.app.Activity;




import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import static android.R.attr.fragment;
import static com.example.user.lunchonemeal.R.id.chat_listview;
import static com.example.user.lunchonemeal.R.id.nickname;
import static com.example.user.lunchonemeal.R.id.profile_image;

/**
 * Created by user on 2017-04-18.
 */

public class MainScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    NavigationView navigationView=null;
    Toolbar toolbar=null;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);






        FirebaseMessaging.getInstance();
        FirebaseInstanceId.getInstance().getToken();

        //Fragment_Home fragment=new Fragment_Home();
        Fragment_Sharing fragment=new Fragment_Sharing();

        android.support.v4.app.FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),InputRestaurant.class);
                startActivityForResult(i,1002);
            }
        });
        */
        DrawerLayout drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(
                this,drawer,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //네비게이션뷰의 프로필 설정하는 부분

        SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);
        String id=shared.getString("id","");

        SharedPreferences shared2=getSharedPreferences(id,MODE_PRIVATE);

        if(shared2.getString("filepath",null)!=null) {
            NavigationView navigationview = (NavigationView)findViewById(R.id.nav_view);
            View nav_header_view = navigationview.getHeaderView(0);

            TextView nickname = (TextView) nav_header_view.findViewById(R.id.nickname);
            TextView greeting = (TextView) nav_header_view.findViewById(R.id.greeting);
            de.hdodenhof.circleimageview.CircleImageView profile_image =
                    (de.hdodenhof.circleimageview.CircleImageView) nav_header_view.findViewById(R.id.profile_image);

            nickname.setText(shared2.getString("nickname", ""));
            greeting.setText(shared2.getString("greeting", ""));

            String filePath = shared2.getString("filepath", "");
            Bitmap bm = BitmapFactory.decodeFile(filePath);
            profile_image.setImageBitmap(bm);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1002)
        {
            Fragment_Home fragment=new Fragment_Home();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment);
            fragmentTransaction.commit();

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i=new Intent(getApplicationContext(),InputRestaurant.class);
        startActivityForResult(i,1002);
        return true;
    }

    //메뉴생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Log.d("메뉴 생성", "후후");
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_button) {
            //Set the fragment initially
            Fragment_Home fragment=new Fragment_Home();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment);
            fragmentTransaction.commit();
            // Handle the camera action
        }else if(id==R.id.profile_setting){
            Fragment_Profile fragment=new Fragment_Profile();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment);
            fragmentTransaction.commit();
            // Handle the camera action
        }else if(id==R.id.sharing){
            Fragment_Sharing fragment=new Fragment_Sharing();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment);
            fragmentTransaction.commit();
            // Handle the camera action
        }else if(id==R.id.setting){
            Fragment_Setting fragment=new Fragment_Setting();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment);
            fragmentTransaction.commit();
            // Handle the camera action
        }else if(id==R.id.logout)
        {

            Toast.makeText(getApplicationContext(),"Logout 됨",Toast.LENGTH_LONG).show();
            SharedPreferences mPref=getSharedPreferences("loginvalue",MODE_PRIVATE);
            SharedPreferences.Editor editor=mPref.edit();

            editor.putInt("loginvalue",0);
            editor.putString("id","");
            editor.commit();

            startActivity(new Intent(this,StartScreen.class));
            finish();

        }else{}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

