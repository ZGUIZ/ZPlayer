package com.example.amia.zplayer.util;

import android.util.Log;

import com.example.amia.zplayer.DTO.LrcDownLoadInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Amia on 2018/3/8.
 */

public class NetUtils {

    public static String requestDataFromNet(String uri) throws MalformedURLException{
        URL url=new URL(uri);
        InputStreamReader inReader=null;
        BufferedReader bufReader=null;
        HttpURLConnection conn=null;
        StringBuffer sb=new StringBuffer();
        try{
            conn=(HttpURLConnection) url.openConnection();
            conn.setReadTimeout(50000);
            conn.setRequestMethod("GET");
            //conn.setDoInput(true);
            //conn.setRequestProperty("Content-Type", "application/json");
            //conn.connect();
            Log.i("NetUtils","ResponseCode:"+conn.getResponseCode());
            inReader=new InputStreamReader(conn.getInputStream());
            bufReader=new BufferedReader(inReader);
            String line=null;
            while((line=bufReader.readLine())!=null){
                sb.append(line);
            }
            Log.i("NetUtils",sb.toString());
            return sb.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                }
                if(inReader!=null){
                    inReader.close();
                }
                if(conn!=null){
                    conn.disconnect();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
