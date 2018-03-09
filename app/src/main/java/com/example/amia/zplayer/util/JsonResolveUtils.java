package com.example.amia.zplayer.util;

import com.example.amia.zplayer.DTO.LrcDownLoadInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amia on 2018/3/8.
 */

public class JsonResolveUtils{
    public static List<Object> resolveJson(String result,Class type) throws JSONException {
        List<Object> list=new ArrayList<>();
        JSONObject jsonObject=new JSONObject(result);
        JSONArray jsonArray=jsonObject.getJSONArray("lrc");
        for(int i=0;i<jsonArray.length();i++){
            JSONObject object=jsonArray.getJSONObject(i);
            list.add(changToTargetType(object,type));
        }
        return list;
    }

    protected static Object changToTargetType(JSONObject jsonObject,Class type) throws JSONException{
        if(type.getName().equals(LrcDownLoadInfo.class.getName())){
            LrcDownLoadInfo info=new LrcDownLoadInfo();
            info.setId(jsonObject.getInt("id"));
            info.setMusic_name(jsonObject.getString("name"));
            info.setArtist(jsonObject.getString("artist"));
            info.setUrl(jsonObject.getString("url"));
            return info;
        }
        return null;
    }
}
