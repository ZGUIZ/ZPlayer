package com.example.amia.zplayer.util;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;

import java.text.DecimalFormat;

/**
 * Created by Amia on 2017/8/8.
 */

public class ChangelateUtil {

    public static String calTime(long time){
        long second=time/1000;
        long min=second/60;
        second=second%60;
        String res=null;
        if(min<10){
            res= String.valueOf("0"+min+":");
        }
        else{
            res= String.valueOf(min+":");
        }
        if(second<10){
            res=res+"0"+second;
        }
        else{
            res=res+second;
        }
        return res;
    }

    public static int calTime(String time){
        int t= Integer.parseInt(time.substring(0,time.indexOf(':')))*60+ Integer.parseInt(time.substring(time.indexOf(':')+1,time.indexOf('.')));
        t=t*1000+ Integer.parseInt(time.substring(time.indexOf('.')+1))*10;
        return t;
    }

    public static String calSize(long size){
        float size1=size/(float)1048576;
        DecimalFormat decimalFormat=new DecimalFormat("0.00");
        return decimalFormat.format(size1)+"M";
    }
}
