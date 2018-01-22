package com.example.amia.zplayer.DTO;


import java.io.Serializable;


public class Mp3Info  implements Serializable {

    private long id;
    private String title;
    private String Artist;
    private long Duration;
    private long Size;
    private String url;
    private long album_id;
    private String album_url;

    private boolean isSelected;
    private boolean isInLove;

    public void setAlbum_id(long album_id){
        this.album_id=album_id;
    }
    public long getAlbum_id(){
        return album_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public long getDuration() {
        return Duration;
    }

    public void setDuration(long duration) {
        Duration = duration;
    }

    public long getSize() {
        return Size;
    }

    public void setSize(long size) {
        Size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlbum_url() {
        return album_url;
    }

    public void setAlbum_url(String album_url) {
        this.album_url = album_url;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isInLove() {
        return isInLove;
    }

    public void setInLove(boolean inLove) {
        isInLove = inLove;
    }
}
