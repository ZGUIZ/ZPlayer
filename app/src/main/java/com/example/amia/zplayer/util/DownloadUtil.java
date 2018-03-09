package com.example.amia.zplayer.util;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.amia.zplayer.Activity.PlayingActivity;
import com.example.amia.zplayer.DTO.LrcDownLoadInfo;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;

import org.json.JSONException;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Amia on 2017/12/5.
 */

public class DownloadUtil {

    private static ExecutorService pool= Executors.newFixedThreadPool(4);

    private String type;
    public static final String LRC="lrc";
    public static final String MUSIC="music";
    private Mp3Info downInfo;
    private Context context;

    public DownloadUtil(Context context){
        this.context=context;
    }

    public void downLoadOrderLrc(Mp3Info mp3Info,int num,int trytime){
        type=LRC;
        downInfo=mp3Info;
        final String url=getLrcUrl(mp3Info,trytime);
        submitLrcRunnable(url,num,trytime);
    }

    public void tryDownloadLrc(Mp3Info mp3Info,int trytime){
        type=LRC;
        downInfo=mp3Info;
        final String url=getLrcUrl(mp3Info,trytime);
       submitLrcRunnable(url,0,trytime);
    }

    private String getLrcUrl(Mp3Info mp3Info,int trytime){
        StringBuffer sb=new StringBuffer("http://"+context.getResources().getString(R.string.down_host));
        sb.append("findLrc?lrcName=");
        switch (trytime){
            case 0:
                sb.append(ConvertStringCode.toBase64(mp3Info.getTitle()).trim());
                sb.append("&artist=");
                sb.append(ConvertStringCode.toBase64(mp3Info.getArtist()).trim());
                break;
            case 1:
                sb.append(ConvertStringCode.toBase64Second(mp3Info.getTitle()).trim());
                sb.append("&artist=");
                sb.append(ConvertStringCode.toBase64Second(mp3Info.getArtist()).trim());
        }
        return sb.toString();
    }

    private void submitLrcRunnable(final String url,final int i,final int trytime){
        pool.submit(new Runnable() {
            @Override
            public void run() {
                Log.i("TestActivity",url);
                String result = null;
                try {
                    result = NetUtils.requestDataFromNet(url);
                    //Log.i("TestActivity",result);
                    List<Object> objects= JsonResolveUtils.resolveJson(result,LrcDownLoadInfo.class);
                    if(objects.size()>0) {
                        downLoadLrc((LrcDownLoadInfo) objects.get(i));
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    if(trytime==0) {
                        tryDownloadLrc(downInfo, 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(trytime==0) {
                        tryDownloadLrc(downInfo, 1);
                    }
                }
            }
        });
    }

    protected void downLoadLrc(LrcDownLoadInfo info){
        //Log.i("TestActivity","downLoadLrc");
        RequestParams params=new RequestParams("http://"+context.getResources().getString(R.string.down_host)+info.getUrl());
        params.setAutoRename(true);
        params.setSaveFilePath(Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/lrc/"+info.getArtist()+" - "+info.getMusic_name()+".lrc");
        downLoad(params);
    }

    public void downLoadMusic(MusicDownLoadInfo info){
        type=MUSIC;
        RequestParams params=new RequestParams(info.getDownloadUrl());
        params.setAutoRename(true);
        params.setSaveFilePath(Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/Music/"+info.getArtist()+" - "+info.getTitle()+".mp3");
        downLoad(params);
    }

    private void downLoad(RequestParams params){
        //Log.i("TestActivity","strat downLoad");
        x.http().post(params,new Callback.ProgressCallback<File>(){
            @Override
            public void onSuccess(File result) {
                Log.i("DownLoad","success");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("DownLoad","error");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("DownLoad","cancelled");
            }

            @Override
            public void onFinished() {
                if(type.equals(LRC)||context instanceof PlayingActivity){
                    ((PlayingActivity)context).setFirstLrc(downInfo);
                }
            }

            @Override
            public void onWaiting() {
                Log.e("DownLoad","waiting");
            }

            @Override
            public void onStarted() {
                Log.i("DownLoad","start");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.i("Download","total="+total+"\tcurrent="+current);
            }
        });
    }
}
