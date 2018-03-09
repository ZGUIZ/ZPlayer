package com.example.amia.zplayer.Activity;

import com.example.amia.zplayer.DTO.Mp3Info;

/**
 * Created by Amia on 2018/3/9.
 * PlayingActivity 对外开放的ui更新接口
 */

interface LrcShowedAct {
    /**
     * 更新歌词
     * @param info
     */
    void setFirstLrc(Mp3Info info);
}
