package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Amia on 2017/8/8.
 */

public abstract class CurrentPositionReceiver extends BroadcastReceiver {

    public final static String currentPositionActionName="com.example.amia.musicplayer.currentPosition";
    private final static String currentPositionKey="currentPosition";
    @Override
    public void onReceive(Context context, Intent intent) {
        int currenttime=intent.getIntExtra(currentPositionKey,0);
        setProgressBar(currenttime);
    }
    public abstract void setProgressBar(int currentPosition);
}
