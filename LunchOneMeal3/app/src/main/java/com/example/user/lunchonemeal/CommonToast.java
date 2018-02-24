package com.example.user.lunchonemeal;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.nhn.android.data.g.g;

/**
 * Created by user on 2017-06-18.
 */


public class CommonToast extends Toast {
    Context mContext;
    public CommonToast(Context context) {
        super(context);
        mContext = context;
    }

    public void showToast(String message_,int duration,String friendid){
        // http://developer.android.com/guide/topics/ui/notifiers/toasts.html
        LayoutInflater inflater;
        View v;
        if(false){
            Activity act = (Activity)mContext;
            inflater = act.getLayoutInflater();
            v = inflater.inflate(R.layout.toast_layout, null);
        }else{

            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.toast_layout, null);
        }

        TextView nickname = (TextView) v.findViewById(R.id.nickname);
        TextView message=(TextView)v.findViewById(R.id.message);
        ImageView image=(ImageView)v.findViewById(R.id.image);

        String imageurl_=null,nickname_=null;
        imageurl_=Global.urlHash.get(friendid);
        nickname_=Global.nicknameHash.get(friendid);

        if(imageurl_!=null&nickname_!=null) {

            nickname.setText(nickname_);
            message.setText(message_);
            Glide.with(mContext).load(imageurl_).override(250, 250).transform(new CircleTransform(mContext)).thumbnail(0.6f).into(image);
            show(this, v, duration);
        }
    }
    private void show(Toast toast, View v, int duration){
        toast.setGravity(Gravity.TOP, 0, 300);

        toast.setDuration(duration);
        toast.setView(v);
        toast.show();
    }
}