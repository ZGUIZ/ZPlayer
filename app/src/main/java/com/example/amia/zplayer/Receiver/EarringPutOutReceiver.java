package com.example.amia.zplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.util.XmlReadWriteUtil;

import java.util.HashMap;

/**
 * 耳机拔出事件的接受者类
 * Created by Amia on 2017/8/3.
 */

public class EarringPutOutReceiver extends BroadcastReceiver {

    private MusicService ser;
    private XmlReadWriteUtil xmlReadWriteUtil;

    public EarringPutOutReceiver(MusicService context){
        ser=context;
        xmlReadWriteUtil=new XmlReadWriteUtil(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String extraStr=context.getResources().getString(R.string.extra_stop);
            HashMap<String,Boolean> setting=xmlReadWriteUtil.readSetting(context.getResources().getString(R.string.setting_file),new String[]{extraStr});
            boolean isExtraCheck=setting.get(extraStr);
            if(!isExtraCheck){
                return;
            }
            if (intent.getIntExtra("state", 0) == 0) {
                ser.pauseMusic();
                Intent intent1=new Intent();
                intent1.setAction("com.example.amia.musicplayer.musicservicepause");
                ser.sendBroadcast(intent1);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
