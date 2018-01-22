package com.example.amia.zplayer.Receiver;

import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.example.amia.zplayer.Service.MusicService;

/**
 * Created by Amia on 2017/8/16.
 */

public class PhoneCallListener extends PhoneStateListener {
    private MusicService musicService;
    private static boolean isPlayBefore;
    private static int currentVolume;
    private AudioManager audioManager;
    public PhoneCallListener(MusicService musicService){
        this.musicService=musicService;
        isPlayBefore=false;
        audioManager=(AudioManager) musicService.getSystemService(musicService.AUDIO_SERVICE);
        currentVolume=-1;
    }
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state){
            case TelephonyManager.CALL_STATE_IDLE:
                if(isPlayBefore) {
                    if(currentVolume!=-1) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    }
                    musicService.resumePlay();
                    currentVolume=-1;
                }
                isPlayBefore=false;
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                isPlayBefore=musicService.isPlaying();
                if(isPlayBefore) {
                    currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,15,1);
                    //musicService.pauseMusic();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                isPlayBefore=musicService.isPlaying();
                if(isPlayBefore) {
                    musicService.pauseMusic();
                }
        }
        super.onCallStateChanged(state, incomingNumber);
    }
}
