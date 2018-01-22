package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.amia.zplayer.Activity.MusicAboutActivity;

/**
 * Created by Amia on 2017/8/8.
 */

public class PauseMusicReceiver extends BroadcastReceiver {
    MusicAboutActivity activity;
    public PauseMusicReceiver(MusicAboutActivity activity){
        this.activity=activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        activity.PauseMusicFromService();
    }
}
