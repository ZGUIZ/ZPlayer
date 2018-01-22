package com.example.amia.zplayer.util;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by Amia on 2017/12/5.
 */

public class DownloadUtil {
    public void downLoad(String url){
        RequestParams params=new RequestParams(url);
        params.setAutoRename(true);
        x.http().post(params,new Callback.ProgressCallback<File>(){
            @Override
            public void onSuccess(File result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }
}
