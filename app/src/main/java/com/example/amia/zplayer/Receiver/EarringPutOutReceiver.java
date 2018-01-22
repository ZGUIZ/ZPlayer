package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.amia.zplayer.Service.MusicService;

/**
 * Created by Amia on 2017/8/3.
 */

public class EarringPutOutReceiver extends BroadcastReceiver {

    private MusicService ser;

    public EarringPutOutReceiver(MusicService context){
        ser=context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getIntExtra("state", 0) == 0) {
                ser.pauseMusic();
                Intent intent1=new Intent();
                intent1.setAction("com.example.amia.musicplayer.musicservicepause");
                ser.sendBroadcast(intent1);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
