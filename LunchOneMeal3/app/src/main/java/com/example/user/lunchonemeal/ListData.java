package com.example.user.lunchonemeal;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

import static com.example.user.lunchonemeal.R.id.mDate;
import static com.nhn.android.data.g.i;
import static com.nhn.android.maps.maplib.f.d;

/**
 * Created by user on 2017-04-22.
 */

public class ListData {

    public String imageurl;

    public String mTitle;

    public String mDate;

    public String resname;

    public String resinfo;

    public int no;

    public double distance;

    public String kind;

    public String id;

    String getDistance ()
    {
        if(distance>=1000)
        {
            double dis=distance/1000;
            dis=Double.parseDouble(String.format("%.1f",dis));
            return dis+"km";
        }
        else
        {
            int dis=(int)distance;
            dis=Math.round(dis/10)*10;
            return dis+"m";

        }
    }






    public static final Comparator<ListData> distanceComparator=new Comparator<ListData>() {

        @Override
        public int compare(ListData a1, ListData a2) {

            if(a1.distance<=a2.distance)
            {
                return -1;
            }
            else{
                return 1;
            }
        }
    };

}
