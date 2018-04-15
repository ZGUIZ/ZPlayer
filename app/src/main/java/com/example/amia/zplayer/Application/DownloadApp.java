package com.example.amia.zplayer.Application;

import android.app.Application;
import android.util.Log;

import com.example.amia.zplayer.DAO.DownloadDao;
import com.example.amia.zplayer.util.DownloadUtil;

import org.xutils.x;

/**
 * Created by Amia on 2018/3/4.
 */

public class DownloadApp extends Application {
    public void onCreate(){
        super.onCreate();
        x.Ext.init(this);
    }

    public void onTerminate(){
        //Log.i("DownloadApp","onTerminate");
        DownloadUtil.pool.shutdown();  //关闭线程池
        DownloadDao dao=new DownloadDao(this);
        dao.allDownLoadPause();
        super.onTerminate();
    }
}
