package com.example.amia.zplayer.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.util.MusicResolverUtil;

import java.util.HashMap;

/**
 * Created by Amia on 2017/8/7.
 */

public abstract class MusicAboutActivity extends AppCompatActivity {

    protected MusicPlayManager musicPlayManager;//控制Service的IBinder对象

    protected Mp3Info currentMp3Info;
    protected static boolean isplay=false;
    protected HashMap<String,Bitmap> bitMap;          //专辑图哈希表,key为音乐信息的id

    protected static MusicResolverUtil musicResolverUtil;

    abstract void PauseMusic();
    public abstract void PauseMusicFromService();

    @Override
    protected void onStart(){
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(musicPlayManager==null){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Mp3Info mp3Info=musicPlayManager.getCurretnMp3Info();
                if(mp3Info!=null) {
                    currentMp3Info=mp3Info;
                    Message msg=handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    protected void startActivity(Class cls){
        Intent intent=new Intent(this,cls);
        startActivity(intent);
    }

    public abstract void setCurrentMusicInfo(Mp3Info info);

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            setCurrentMusicInfo(currentMp3Info);
        }
    };
}
