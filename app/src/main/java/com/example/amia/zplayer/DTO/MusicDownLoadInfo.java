package com.example.amia.zplayer.DTO;

/**
 * Created by Amia on 2018/3/4.
 */

public class MusicDownLoadInfo extends Mp3Info{
    private String downloadUrl;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
