package com.example.amia.zplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 写入退出时播放进度和播放列表
     * @param set 第几首音乐
     * @param currentTime
     * @param mp3Infos
     */
    public void writeMusicList(List<Mp3Info>mp3Infos,int set, int currentTime){
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+context.getResources().getString(R.string.list_file);
        File file=new File(path);
//        if(file.exists()){
//            file.delete();
//        }
        FileOutputStream outputStream=null;
        try {
            outputStream=new FileOutputStream(file);
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlSerializer serializer=factory.newSerializer();
            serializer.setOutput(outputStream,"utf-8");
            serializer.startDocument("utf-8",true);
            serializer.startTag(null,"musicList");
            for (int i = 0; i < mp3Infos.size(); i++) {
                Mp3Info mp3Info = mp3Infos.get(i);
                if(mp3Info instanceof MusicDownLoadInfo){
                    continue;
                }
                serializer.startTag(null,"music");
                serializer.startTag(null,"id");
                serializer.text(String.valueOf(mp3Info.getId()));
                serializer.endTag(null,"id");
                serializer.startTag(null,"title");
                serializer.text(mp3Info.getTitle());
                serializer.endTag(null,"title");
                serializer.startTag(null,"artist");
                serializer.text(mp3Info.getArtist());
                serializer.endTag(null,"artist");
                serializer.startTag(null,"duration");
                serializer.text(String.valueOf(mp3Info.getDuration()));
                serializer.endTag(null,"duration");
                serializer.startTag(null,"size");
                serializer.text(String.valueOf(mp3Info.getSize()));
                serializer.endTag(null,"size");
                serializer.startTag(null,"url");
                serializer.text(mp3Info.getUrl());
                serializer.endTag(null,"url");
                serializer.startTag(null,"album_id");
                serializer.text(String.valueOf(mp3Info.getAlbum_id()));
                serializer.endTag(null,"album_id");
                serializer.startTag(null,"album_url");
                if(mp3Info.getAlbum_url()==null){
                    mp3Info.setAlbum_url("");
                }
                serializer.text(mp3Info.getAlbum_url());
                serializer.endTag(null,"album_url");
                serializer.endTag(null,"music");
            }
            serializer.endTag(null,"musicList");

            if(mp3Infos.get(set) instanceof MusicDownLoadInfo){
                set=0;
                currentTime=0;
            }
            serializer.startTag(null,"set");
            serializer.text(String.valueOf(set));
            serializer.endTag(null,"set");
            serializer.startTag(null,"currentTime");
            serializer.text(String.valueOf(currentTime));
            serializer.endTag(null,"currentTime");
            serializer.endDocument();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取上次退出时的播放信息
     * @return Objcet[0] 音乐列表，Object[1] 第几首音乐，Object[2] 播放进度
     */
    public Object[] readMusicList(){
        Object[] result=new Object[3];
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+context.getResources().getString(R.string.list_file);
        File file=new File(path);
        FileInputStream inputStream=null;
        List<Mp3Info> mp3Infos=new ArrayList<>();
        int set=0;
        int currentTime=0;
        try{
            inputStream=new FileInputStream(file);
            XmlPullParser parser= Xml.newPullParser();
            parser.setInput(inputStream,"utf-8");
            int eventType=parser.getEventType();
            Mp3Info mp3Info=null;
            while(eventType!=XmlPullParser.END_DOCUMENT){
                String tagName=parser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals("music")){
                            mp3Info=new Mp3Info();
                        } else if(tagName.equals("id")){
                            mp3Info.setId(Long.parseLong(parser.nextText()));
                        } else if(tagName.equals("title")){
                            mp3Info.setTitle(parser.nextText());
                        } else if(tagName.equals("artist")){
                            mp3Info.setArtist(parser.nextText());
                        } else if(tagName.equals("duration")){
                            mp3Info.setDuration(Long.parseLong(parser.nextText()));
                        } else if(tagName.equals("size")){
                            mp3Info.setSize(Long.parseLong(parser.nextText()));
                        } else if(tagName.equals("url")){
                            mp3Info.setUrl(parser.nextText());
                        } else if(tagName.equals("album_id")){
                            mp3Info.setAlbum_id(Long.parseLong(parser.nextText()));
                        } else if(tagName.equals("album_url")){
                            mp3Info.setAlbum_url(parser.nextText());
                        } else if(tagName.equals("set")){
                            set=Integer.parseInt(parser.nextText());
                        } else if(tagName.equals("currentTime")){
                            currentTime=Integer.parseInt(parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(tagName.equals("music")){
                            mp3Infos.add(mp3Info);
                        }
                        break;
                    default:
                        break;
                }
                eventType=parser.next();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        result[0]=mp3Infos;
        result[1]=set;
        result[2]=currentTime;
        return result;
    }
}
