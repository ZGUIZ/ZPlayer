package com.example.amia.zplayer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.amia.zplayer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Amia on 2018/3/8.
 */

public class NetUtils {

    /**
     * 读取网页内容
     * @param uri
     * @return
     * @throws MalformedURLException
     */
    public static String requestDataFromNet(Context context,String uri) throws MalformedURLException{
        XmlReadWriteUtil xmlReadWriteUtil=new XmlReadWriteUtil(context);
        String visitMode=context.getResources().getString(R.string.visitNetMode);
        HashMap<String,Boolean> setting=xmlReadWriteUtil.readSetting(context.getResources().getString(R.string.setting_file),new String[]{visitMode});
        boolean isWifiVisit=setting.get(visitMode);
        if(isWifiVisit){
            if(!isWifiConnection(context)) {
                return "";
            }
        }

        URL url=new URL(uri);
        InputStreamReader inReader=null;
        BufferedReader bufReader=null;
        HttpURLConnection conn=null;
        StringBuffer sb=new StringBuffer();
        try{
            conn=(HttpURLConnection) url.openConnection();
            conn.setReadTimeout(50000);
            conn.setRequestMethod("GET");
            inReader=new InputStreamReader(conn.getInputStream());
            bufReader=new BufferedReader(inReader);
            String line;
            while((line=bufReader.readLine())!=null){
                sb.append(line);
            }
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

    /**
     * 从网络上加载图片
     * @param url
     * @return
     */
    public static Bitmap getURLImage(String url) throws IOException {
        Bitmap bitmap=null;
        //Log.i("NetUtils","ImageUrl="+url);
        URL conn_url=new URL(url);
        HttpURLConnection connection=(HttpURLConnection)conn_url.openConnection();
        connection.setConnectTimeout(40000);
        connection.setDoInput(true);
        connection.setUseCaches(true);
        connection.connect();
        InputStream is=connection.getInputStream();
        bitmap= BitmapFactory.decodeStream(is);
        return bitmap;
    }

    /**
     * 判断当前是否wifi连接
     * @param context
     * @return
     */
    public static boolean isWifiConnection(Context context){
        ConnectivityManager manager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo!=null&&networkInfo.isConnectedOrConnecting());
    }
}
