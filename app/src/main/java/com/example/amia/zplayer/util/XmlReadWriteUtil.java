package com.example.amia.zplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.amia.zplayer.R;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Amia on 2018/4/15.
 */

public class XmlReadWriteUtil {
    private Context context;
    public  XmlReadWriteUtil(Context context){
        this.context=context;
    }

    /**
     * 写设置文件
     * @param fileName
     * @param values
     */
    public void writeSetting(String fileName, HashMap<String,Boolean> values){
        if(fileName==null||fileName.isEmpty()||values.isEmpty()){
            return;
        }
        SharedPreferences sp=context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        Set<String> keys=values.keySet();
        for(String key:keys){
            editor.putBoolean(key,values.get(key));
        }
        editor.commit();
    }

    /**
     * 读设置文件
     * @param fileName
     * @param keys
     * @return
     */
    public HashMap<String,Boolean> readSetting(String fileName,String[] keys){
        HashMap<String,Boolean> values=new HashMap<>();
        SharedPreferences sp=context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        for(String key:keys){
            //默认值
            boolean def=true;
            if(key.equals(context.getResources().getString(R.string.visitNetMode))){
                def=false;
            }
            values.put(key,sp.getBoolean(key,def));
        }
        return values;
    }
}
