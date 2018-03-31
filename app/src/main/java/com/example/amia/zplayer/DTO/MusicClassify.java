package com.example.amia.zplayer.DTO;

import java.io.Serializable;

/**
 * Created by Amia on 2018/3/28.
 */

public class MusicClassify implements Serializable{
    private int id;
    private String name;
    private String iconurl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconurl() {
        return iconurl;
    }

    public void setIconurl(String iconurl) {
        this.iconurl = iconurl;
    }
}
