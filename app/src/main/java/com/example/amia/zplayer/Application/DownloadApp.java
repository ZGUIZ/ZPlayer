package com.example.amia.zplayer.Application;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Amia on 2018/3/4.
 */

public class DownloadApp extends Application {
    public void onCreate(){
        super.onCreate();
        x.Ext.init(this);
    }
}
