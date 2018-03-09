package com.example.amia.zplayer.util;

import android.os.Environment;

import com.example.amia.zplayer.DTO.LrcEntity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Amia on 2017/9/8.
 */

public class LrcResovler {
    private static String lrcPath= Environment.getExternalStorageDirectory().getAbsolutePath();
    private static ArrayList<String> readLrcFile(String musicName, String artist){
        ArrayList<String> lrc=new ArrayList<>();
        File dir=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer");
        if(!dir.exists()){
            dir.mkdir();
        }
        File dir2=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/lrc");
        if(!dir2.exists()){
            dir2.mkdir();
        }
        String stlrc=ConvertFileCode.converfile(String.valueOf(lrcPath+"/ZPlayer/lrc/"+artist+" - "+musicName+".lrc"));
        if(!stlrc.equals("")){
            StringBuffer buffer=new StringBuffer(stlrc);
            while (buffer.length()>0) {
                int set = buffer.indexOf("/n");
                String temp = buffer.substring(0, set);
                buffer.delete(0, set+2);
                if(temp.equals("")){
                    continue;
                }
                lrc.add(temp);
            }
        }
        return lrc;
    }

    /**
     * 该方法用于获取本地歌词
     * @param musicName
     * @param artist
     * @return
     */
    public static ArrayList<LrcEntity> getLrc(String musicName, String artist){
        ArrayList<LrcEntity> lrc=new ArrayList<>();
        ArrayList<String> alllrc=readLrcFile(musicName,artist);
        for(String str:alllrc){
            LrcEntity lrcEntity=new LrcEntity();
            try{
                Integer.parseInt(String.valueOf(str.charAt(1)));
                String time=str.substring(str.indexOf('[')+1,str.indexOf(']'));
                lrcEntity.setTime(ChangelateUtil.calTime(time));
                lrcEntity.setLrc(str.substring(str.indexOf(']')+1));
            }
            catch (NumberFormatException e){
                if(str.charAt(1)=='a') {
                    lrcEntity.setTime(0);
                }
                else{
                    lrcEntity.setTime(1000);
                }
                lrcEntity.setLrc(str.substring(str.indexOf(':')+1,str.indexOf(']')));
            }finally {
                lrc.add(lrcEntity);
            }
        }
        return lrc;
    }
}
