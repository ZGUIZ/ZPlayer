package com.example.amia.zplayer.Receiver;


import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.MusicPlayStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amia on 2017/8/5.
 */

public interface MusicPlayManager{

    /**
     * 该方法用于播放音乐
     * @param mp3Infos 播放的列表
     * @param position 播放第几首
     */
    void playMusic(List<Mp3Info> mp3Infos, int position);//播放音乐

    /**
     * 该方法用于播放音乐
     * @param mp3Info 播放的音乐信息
     */
    void playMusic(Mp3Info mp3Info);

    /**
     * 该方法用于暂停音乐
     */
    void pauseMusic();//暂停

    /**
     * 该方法用于继续播放音乐
     */
    void resumePlay();//恢复播放

    /**
     * 该方法用于获取当前的进度
     * @return
     */
    int getCurrentPosition();//获取当前进度

    /**
     * 该方法用于获取当前播放音乐的信息
     * @return 当前音乐信息
     */
    Mp3Info getCurretnMp3Info();//获得当前音乐信息

    /**
     * 该方法用于获取播放器是否正在播放
     * @return
     */
    boolean isPlaying();

    /**
     * 该方法用于设置播放器是否正在播放
     * @param playing
     */
    void setPlaying(boolean playing);

    /**
     * 该方法用于设置当前音乐播放位置
     * @param currentPosition
     */
    void setCurrentPosition(int currentPosition);

    /**
     * 该方法用于设置当前播放的列表
     * @param mp3Infos
     */
    void setMusicList(ArrayList<Mp3Info> mp3Infos);

    /**
     * 该方法用于播放下一首音乐
     */
    void nextMusic();//播放下一首

    /**
     * 该方法用于播放上一首音乐
     */
    void lastMusic();//播放上一首

    /**
     * 该方法用于获取当前播放音乐的列表
     * @return
     */
    List<Mp3Info> getMusicList();

    /**
     * 该方法用于设置播放的模式，如列表循环、列表顺序播放和单曲循环播放
     * @param status
     */
    void setStatus(MusicPlayStatus status);//设置播放模式

    /**
     * 该方法用于获取播放模式
     * @return
     */
    MusicPlayStatus getStatus();//获取播放模式

    /**
     * 该方法用于将音乐设置为下一首播放
     * @param mp3Info
     */
    void addToNext(Mp3Info mp3Info);//下一首播放

    /**
     * 该方法用于停止音乐播放
     */
    void stopMusic();

    /**
     * 添加连接计数
     */
    void addConnection();

    /**
     * 断开连接计数
     */
    void subConnection();

    /**
     * 获取当前音乐的长度
     * @return
     */
    //int getMusicLength();
}
