package com.example.user.lunchonemeal;

import android.provider.BaseColumns;

import static android.R.string.no;

/**
 * Created by user on 2017-06-01.
 */

public final class DataBases{
    public static final class CREATE_TABLE implements BaseColumns {

        public static final String firnedlist_CREATE="CREATE TABLE friendlist " +
                "(_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "loginid VARCHAR(20) NOT NULL, " +
                "friendid VARCHAR(20) NOT NULL," +
                "statement INTEGER NOT NULL);";

        public static final String talk_CREATE="CREATE TABLE talk (" +
                "_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "loginid VARCHAR(20)  NOT NULL," +
                "friendid VARCHAR(20)  NOT NULL," +
                "nickname VARCHAR(30) NOT NULL,"+
                "imageurl VARCHAR(100) NULL,"+
                "messagetype INTEGER  NOT NULL," +
                "message VARCHAR(150)  NULL," +
                "readcheck INTEGER  default '0' NULL,"+
                "time TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NOT NULL,"+
                "chatimageurl varchar(100) NULL "+
                ");";
    }
}
