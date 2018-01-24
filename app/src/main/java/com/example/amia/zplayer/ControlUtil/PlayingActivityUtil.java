package com.example.amia.zplayer.ControlUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amia on 2017/12/19.
 */

public class PlayingActivityUtil {

    public static void setAlbumView(ImageView albumView, Bitmap bitmap,Point point){
        if(bitmap==null){
            albumView.setImageResource(R.drawable.defaluticon);
            return;
        }
        int size=point.x < point.y ? point.x : point.y;
        Bitmap resbitmap= BitMapUtil.getOrderSizeBitmap(bitmap,size,size);
        albumView.setImageBitmap(resbitmap);
    }

    public static MusicPlayStatus switchMode(MusicPlayStatus status){
        switch (status){
            case listloop:
                status=MusicPlayStatus.sequence;
                break;
            case sequence:
                status=MusicPlayStatus.singleloop;
                break;
            case singleloop:
                status=MusicPlayStatus.listloop;
                break;
        }
        return status;
    }

    public static void closeMusicList(Context context, final View closeLayout, View view){
        Animation animation= AnimationUtils.loadAnimation(context,R.anim.push_buttom_out);
        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                closeLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void openMusiList(Context context,View relativeLayout,View view){
        relativeLayout.setVisibility(View.VISIBLE);
        Animation animation=AnimationUtils.loadAnimation(context,R.anim.push_bottom_in);
        view.startAnimation(animation);
    }

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

    public static int getCurlrcSet(int curset,List<LrcEntity> lrc,int position){
        //Log.i("getCurlrcSet","curset="+curset);
        if(curset<lrc.size()-1&&lrc.get(curset+1).getTime()<=position){
            curset++;
        }
        return curset;
    }

    public static void firstsetLrc(List<LrcEntity> lrc,List<Map<String,Object>> lrcObject, TextView null_lrc, ListView lrc_list_view){
        if(lrc==null||lrc.size()==0){
            return;
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
