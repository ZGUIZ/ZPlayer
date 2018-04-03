package com.example.amia.zplayer.ControlUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.amia.zplayer.DTO.LrcEntity;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.MusicPlayStatus;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.util.BitMapUtil;
import com.example.amia.zplayer.util.DownloadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amia on 2017/12/19.
 */

public class PlayingActivityUtil {

    /**
     * 设置专辑图片
     * @param albumView
     * @param bitmap
     * @param point
     */
    public static void setAlbumView(ImageView albumView, Bitmap bitmap,Point point){
        if(bitmap==null){
            albumView.setImageResource(R.drawable.defaluticon);
            return;
        }
        int size=point.x < point.y ? point.x : point.y;
        Bitmap resbitmap= BitMapUtil.getOrderSizeBitmap(bitmap,size,size);
        albumView.setImageBitmap(resbitmap);
    }

    /**
     * 分享音乐
     * @param context
     * @param mp3Info
     */
    public static void shareMusic(Context context,Mp3Info mp3Info){
        //Bitmap bitmap=bitMap.get(String.valueOf(mp3Info.getId()));
        File file=new File(mp3Info.getUrl());

        if(file.exists()) {
            Intent musicIntent = new Intent(Intent.ACTION_SEND);
            musicIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            musicIntent.putExtra(Intent.EXTRA_SUBJECT, "Share");
            //Log.i("MainActivity", "Path=" + file.getAbsolutePath());
            musicIntent.setType("*/*");
            context.startActivity(Intent.createChooser(musicIntent, "分享到"));
        }
    }

    /**
     * 切换播放模式
     * @param status
     * @return
     */
    public static MusicPlayStatus switchMode(MusicPlayStatus status){
        switch (status){
            case listloop:
                status=MusicPlayStatus.sequence;
                break;
            case sequence:
                status=MusicPlayStatus.singleloop;
                break;
            case singleloop:
                status=MusicPlayStatus.ramdom;
                break;
            case ramdom:
                status=MusicPlayStatus.listloop;
                break;
        }
        return status;
    }

    /**
     * 视图向下滑动退出
     * @param context
     * @param closeLayout
     * @param view
     */
    public static void closeMusicList(Context context, final View closeLayout, View view){
        Animation animation= AnimationUtils.loadAnimation(context,R.anim.push_buttom_out);
        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                closeLayout.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                closeLayout.setVisibility(View.GONE);
                closeLayout.setTag(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 视图向上滑动进入
     * @param context
     * @param relativeLayout
     * @param view
     */
    public static void openMusiList(Context context,View relativeLayout,View view){
        relativeLayout.setVisibility(View.VISIBLE);
        relativeLayout.setClickable(true);
        Animation animation=AnimationUtils.loadAnimation(context,R.anim.push_bottom_in);
        view.startAnimation(animation);
    }

    /**
     * 不确定进入或退出时使用该方法
     * 注意该tag必须保存有是否已经显示的标志
     * @param context
     * @param half_muslist_rl
     * @param half_lsit_area
     */
    public static void openOrCloseMusList(Context context,View half_muslist_rl,View half_lsit_area){
        if(!(boolean)half_muslist_rl.getTag()){
            half_muslist_rl.setTag(true);
            PlayingActivityUtil.openMusiList(context,half_muslist_rl,half_lsit_area);
        }
        else{
            half_muslist_rl.setTag(false);
            PlayingActivityUtil.closeMusicList(context,half_muslist_rl,half_lsit_area);
        }
    }

    /**
     * 设置歌词为空
     */
    public static void setEmptyLrc(List<Map<String,Object>> lrcObject, TextView null_lrc, ListView lrc_list_view){
        lrcObject.clear();
        null_lrc.setVisibility(View.VISIBLE);
        lrc_list_view.setVisibility(View.INVISIBLE);
    }

    /**
     * 获取当前歌词位置
     * @param curset
     * @param lrc
     * @param position
     * @return
     */
    public static int getCurlrcSet(int curset,List<LrcEntity> lrc,int position){
        //Log.i("getCurlrcSet","curset="+curset);
        if(curset<lrc.size()-1&&lrc.get(curset+1).getTime()<=position){
            curset++;
        }
        return curset;
    }

    /**
     * 第一次尝试设置歌词
     * 没找到歌词则从网络下载
     * @param lrc
     * @param lrcObject
     * @param null_lrc
     * @param lrc_list_view
     * @param context
     * @param trytime
     * @param info
     */
    public static void firstsetLrc(List<LrcEntity> lrc,List<Map<String,Object>> lrcObject, TextView null_lrc, ListView lrc_list_view,Context context,int trytime,Mp3Info info){
        if(lrc==null||lrc.size()==0){
            if(trytime==0) {
                DownloadUtil util=new DownloadUtil(context);
                util.tryDownloadLrc(info,trytime);
                trytime++;
                return;
            }
            else {
                return;
            }
        }
        null_lrc.setVisibility(View.INVISIBLE);
        lrc_list_view.setVisibility(View.VISIBLE);
        lrcObject.clear();
        for(int i=0;i<lrc.size();i++){
            HashMap<String,Object> temp=new HashMap<>();
            temp.put("lrc",lrc.get(i).getLrc());
            lrcObject.add(temp);
        }
        //lrcAdapter.notifyDataSetChanged();
    }

    /**
     * 第一次搜索歌词位置
     * @param lrc
     * @param time
     * @param mp3Info
     * @return
     */
    //开始寻找位置
    public static int firstfindCurrentLrc(List<LrcEntity> lrc, int time, Mp3Info mp3Info){
        if(lrc==null||lrc.size()<=0){
            return -1;
        }
        int position=getPossPosition(time,lrc.size(),mp3Info);//获得可能的歌词位置
        //Log.i("PlayingAct","position="+position);
        if(position>=lrc.size()-1){
            position=lrc.size()-2;
        }
        LrcEntity before=lrc.get(position);
        LrcEntity after=lrc.get(position+1);
        while(!(before.getTime()<=time&&after.getTime()>time)){
            if(after.getTime()<=time&&position<lrc.size()-1){
                position++;
            }
            else{
                if((before.getTime()>=time||position>lrc.size())){
                    position--;
                }
                if(position<=0){
                    return 0;
                }
            }
            if(position>=lrc.size()-1||lrc.get(position).getTime()==time){
                break;
            }
            before=lrc.get(position);
            after=lrc.get(position+1);

//            Log.i("PlayingAct","before="+before.getTime());
//            Log.i("PlayingAct","time="+time);
//            Log.i("PlayingAct","after="+after.getTime());
        }
        return position;
    }

    //计算相对可能的歌词位置
    private static int getPossPosition(int time,int size,Mp3Info currentMp3Info){
        float dur=(float)currentMp3Info.getDuration();
        return (int)((time/dur)*size);
    }
}
