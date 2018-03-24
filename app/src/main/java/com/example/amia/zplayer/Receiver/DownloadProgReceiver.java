package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Amia on 2018/3/21.
 */

public abstract class DownloadProgReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id=intent.getIntExtra("id",-1);
        long progress=intent.getLongExtra("progress",0);
        long duration=intent.getLongExtra("duration",100);
        displayProgress(id,progress,duration);
    }

    protected abstract void displayProgress(int id, long progress, long duration);
}
