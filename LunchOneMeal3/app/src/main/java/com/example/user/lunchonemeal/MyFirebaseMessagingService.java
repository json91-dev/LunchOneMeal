package com.example.user.lunchonemeal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.nhn.android.data.g.g;
import static com.nhn.android.data.g.h;

/**
 * Created by user on 2017-06-02.
 */

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{
    private static final String TAG="FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        sendNotification(remoteMessage.getData().get("message"));


        // 친구요청 메세지 처리하는 부분이다.
        if(remoteMessage.getData().get("senderid")!=null) {
            friendRequest(remoteMessage.getData().get("senderid"));
        }

        // 친고 요청 return 을 처리하는 부분이다.

        if(remoteMessage.getData().get("senderid_return")!=null)
        {
            friendRequestReturn(remoteMessage.getData().get("senderid_return"));
        }






    }

    private void  friendRequest(String senderid)
    {
        SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();
        SQLiteDatabase sqlite=h.getSQLiteDb();


        // 디비에 내 로그인 정보와 보낸사람의 정보가 존재하는가?
        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+shared.getString("id","")+"' " +
                "and friendid='"+senderid+"'",null);

        // 존재하지 않는다면 상태(0) 으로 친구 리스트 생성을 해라 상태 0 -> 친구 맺기
        if(c.getCount()==0)
        {
            sqlite.execSQL("insert into friendlist (loginid,friendid,statement) values" +
                    "('"+shared.getString("id","")+"','"+senderid+"',0)");

            Log.i("친구 목록 갱신",": OK");
        }
        // 존재한다면 이미 친구목록에 존재하므로 아무 작업도 하지 않는다.
        else
        {
            Log.i("이미 친구 목록에 존재",": OK");
        }
    }

    private void  friendRequestReturn(String senderid)
    {
        SharedPreferences shared=getSharedPreferences("loginvalue",MODE_PRIVATE);

        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();
        SQLiteDatabase sqlite=h.getSQLiteDb();


        // 디비에 내 로그인 정보와 보낸사람의 정보가 존재하는가?
        Cursor c=sqlite.rawQuery("select * from friendlist where loginid='"+shared.getString("id","")+"' " +
                "and friendid='"+senderid+"'",null);

        // 존재하지 않는다면 상태(1) 으로 친구 리스트 생성을 해라 상태 1 -> 친 구
        if(c.getCount()==0)
        {
            sqlite.execSQL("insert into friendlist (loginid,friendid,statement) values" +
                    "('"+shared.getString("id","")+"','"+senderid+"',1)");

            Log.i("친구가 되었다..!!",": OK");
        }
        // 존재한다면 친구의 상태를 상태(1)으로 바꾼다 . 상태 1 -> 친구
        else
        {
            sqlite.execSQL("update friendlist set statement=1 where loginid='"+shared.getString("id","")+"' and friendid='"+senderid+"'");
            Log.i("친구목록이 갱신되었다 !! ",": OK");
        }
    }






    private void sendNotification(String messageBody){
        Intent intent=new Intent(this,MainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder=new Notification.Builder(this)
                .setSmallIcon(R.mipmap.lunchimagee)
                .setContentTitle("친구 요청 메세지 도착")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager=
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0,notificationBuilder.build());

    }
}
