package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.amia.zplayer.Service.MusicService;

/**
 * Created by Amia on 2018/4/14.
 */

public abstract class ScheStopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int time=intent.getIntExtra(MusicService.scheSotpKey,0);
        setLastTime(calTime(time));
    }

    private String calTime(int time){
        int minute=time/60;
        int second=time%60;
        return minute+" : "+second;
    }

    protected abstract void setLastTime(String time);
}
