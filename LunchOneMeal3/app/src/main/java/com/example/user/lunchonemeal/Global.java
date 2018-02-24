package com.example.user.lunchonemeal;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by user on 2017-06-15.
 */

public class Global {

    private static boolean ReceiveFlag=true;
    private static boolean ParentReceiveFlag=true;
    public static String SocketIp ="115.71.233.49";
    public static DataInputStream dis;
    public static boolean isPopup=true;

    public static HashMap<String,String> urlHash=new HashMap<String,String>();
    public static HashMap<String,String> nicknameHash=new HashMap<String,String>();

    public static boolean SocketServiceFlag=true;


    public static int ResumeFlag=0;

    //teamnova 2G -> 192.168.1.129
    //KT Giga 2G -> 172.30.1.60
    //내 폰 -> 192.168.43.174
    //내 서버 -> 115.71.233.49
    //우리집 -> 192.168.0.6


    public static boolean getReceiveFlag() {
        return ReceiveFlag;
    }

    public static void setReceiveFlag(boolean receiveFlag) {
        ReceiveFlag = receiveFlag;
    }

    public static boolean getParentReceiveFlag() {
        return ParentReceiveFlag;
    }

    public static void setParentReceiveFlag(boolean parentReceiveFlag) {
        ParentReceiveFlag = parentReceiveFlag;
    }
}
