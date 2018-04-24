package com.example.amia.zplayer.util;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.amia.zplayer.Activity.PlayingActivity;
import com.example.amia.zplayer.DAO.DownloadDao;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Amia on 2017/12/5.
 */

public class DownloadUtil {

    public static ExecutorService pool= Executors.newFixedThreadPool(4);
    private static HashMap<Integer,Callback.Cancelable> cancelableHashMap=new HashMap<>();

    public static final String PROGRESS_ACTION="com.example.amia.zplayer.util.downloadprogress";

    private String type;
    public static final String LRC="lrc";
    public static final String MUSIC="music";
    private Mp3Info downInfo;
    private Context context;

    private DownloadDao downloadDao;

    public DownloadUtil(Context context){
        this.context=context;
        downloadDao=new DownloadDao(context);
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
                String result = null;
                try {
                    result = NetUtils.requestDataFromNet(context,url);
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
        RequestParams params=new RequestParams("http://"+context.getResources().getString(R.string.down_host)+info.getUrl());
        params.setAutoRename(true);
        params.setSaveFilePath(Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/lrc/"+info.getArtist()+" - "+info.getMusic_name()+".lrc");
        downLoad(params);
    }

    public void downLoadMusic(MusicDownLoadInfo info){
        type=MUSIC;
        downInfo=info;
        RequestParams params=new RequestParams("http://"+context.getResources().getString(R.string.down_host)+info.getNetUrl());
        params.setAutoRename(true);
        params.setSaveFilePath(Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/Music/"+info.getArtist()+" - "+info.getTitle()+".mp3");
        downLoad(params);
    }

    private void downLoad(RequestParams params){
        final Callback.Cancelable cancelable=x.http().post(params,new Callback.ProgressCallback<File>(){
            LocalBroadcastManager manager=LocalBroadcastManager.getInstance(context);
            @Override
            public void onSuccess(File result) {
                Log.i("DownLoad","Success");

                if(type.equals(LRC)&&context instanceof PlayingActivity){
                    ((PlayingActivity)context).setFirstLrc(downInfo);
                }

                if(type.equals(MUSIC)) {
                    File file=new File(Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/Music/"+downInfo.getArtist()+" - "+downInfo.getTitle()+".mp3");
                    Uri fileUri=Uri.fromFile(file);
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(fileUri);
                    context.sendBroadcast(intent);
                    downloadDao.downloadFinish((MusicDownLoadInfo) downInfo);
                    removeCancelable();

                    Intent intent1 = new Intent();
                    intent1.setAction(PROGRESS_ACTION);
                    intent1.putExtra("id", ((MusicDownLoadInfo) downInfo).getNetId());
                    intent1.putExtra("progress", 100L);
                    intent1.putExtra("duration", 100L);
                    //发送本地广播
                    manager.sendBroadcast(intent);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("DownLoad","error");
                removeCancelable();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("DownLoad","cancelled");
            }

            @Override
            public void onFinished() {
                Log.i("DownLoad","finish");
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
                //Log.i("Download","total="+total+"\tcurrent="+current);
                Intent intent=new Intent();
                intent.setAction(PROGRESS_ACTION);
                intent.putExtra("id",((MusicDownLoadInfo)downInfo).getNetId());
                intent.putExtra("progress",current);
                intent.putExtra("duration",total);
                //发送本地广播
                manager.sendBroadcast(intent);
            }
        });
        if(downInfo instanceof MusicDownLoadInfo){
            cancelableHashMap.put(((MusicDownLoadInfo)downInfo).getNetId(),cancelable);
        }
    }

    //移除对应的Cancelable
    private void removeCancelable(){
        cancelableHashMap.remove(((MusicDownLoadInfo)downInfo).getNetId());
    }

    //移除对应的Cancelable
    private static void removeCancelable(MusicDownLoadInfo info){
        cancelableHashMap.remove(info.getNetId());
    }

    /**
     * 取消下载
     * @param info
     */
    public static void cancelDownload(MusicDownLoadInfo info){
        if(info==null){
            return;
        }
        Callback.Cancelable cancelable=cancelableHashMap.get(info.getNetId());
        removeCancelable(info);
        cancelable.cancel();
    }

    /**
     * 判断是否正在下载
     * @param info
     * @return
     */
    public static boolean isLoading(MusicDownLoadInfo info){
        Callback.Cancelable cancelable=cancelableHashMap.get(info.getNetId());
        return cancelable!=null;
    }

    public static boolean isPausing(Context context,MusicDownLoadInfo info,DownloadDao dao){
        String path=Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/Music/"+info.getArtist()+" - "+info.getTitle()+".mp3.tmp";
        File file=new File(path);
        if(dao.isInList(info.getNetId())&&file.exists()){
            return true;
        }
        else{
            return false;
        }
    }
}
