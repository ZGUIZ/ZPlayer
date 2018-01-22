package com.example.amia.zplayer.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Amia on 2017/9/24.
 */

public class ConvertStringCode {

    public static String toBase64(String content){
        try{
            content= Base64.encodeToString(content.getBytes("utf-8"), Base64.DEFAULT);
            content= URLEncoder.encode(content,"utf-8");
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return content;
    }

}
