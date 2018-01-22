package com.example.amia.zplayer.util;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * Created by Amia on 2017/8/7.
 */

public class WindowInfoMananger {
    public AppCompatActivity activity;
    public WindowInfoMananger(AppCompatActivity act){
        activity=act;
    }
    public Point getScreenWidthHight(){
        WindowManager windowManager=activity.getWindowManager();
        Point point=new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point;
    }
}
