package com.example.amia.zplayer.DTO;

import java.util.List;

/**
 * Created by Amia on 2018/3/4.
 */

public class MusicDownLoadInfo extends Mp3Info{
    private int id_list;
    private int netId;
    private String netUrl; //网络地址
    private int bps;       //比特率
    private int reckTime;  //单位：秒
    private int status;   //0

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public String getNetUrl() {
        return netUrl;
    }

    public void setNetUrl(String netUrl) {
        this.netUrl = netUrl;
    }

    public int getBps() {
        return bps;
    }

    public void setBps(int bps) {
        this.bps = bps;
    }

    public int getReckTime() {
        return reckTime;
    }

    public void setReckTime(int reckTime) {
        this.reckTime = reckTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int download) {
        status = download;
    }

    public int getId_list() {
        return id_list;
    }

    public void setId_list(int id_list) {
        this.id_list = id_list;
    }

    public boolean equals(MusicDownLoadInfo musicDownLoadInfo){
        return netId==musicDownLoadInfo.getNetId();
    }

    public static int isInList(MusicDownLoadInfo info, List<Mp3Info> lists){
        for(int i=0;i<lists.size();i++){
            Mp3Info mp3Info=lists.get(i);
            if(mp3Info instanceof MusicDownLoadInfo){
                if(((MusicDownLoadInfo)mp3Info).getNetId()==info.getNetId()){
                    return i;
                }
            }
        }
        return -1;
    }
}
