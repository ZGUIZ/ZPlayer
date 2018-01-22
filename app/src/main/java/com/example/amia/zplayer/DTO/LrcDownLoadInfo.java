package com.example.amia.zplayer.DTO;

/**
 * Created by Amia on 2017/10/2.
 */

public class LrcDownLoadInfo {

    private String music_name;
    private String artist;
    private int time;
    private String url;

    public String getMusic_name() {
        return music_name;
    }

    public void setMusic_name(String music_name) {
        this.music_name = music_name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
