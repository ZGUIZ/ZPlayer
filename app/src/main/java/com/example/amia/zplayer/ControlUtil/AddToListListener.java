package com.example.amia.zplayer.ControlUtil;

import android.content.Context;
import android.content.DialogInterface;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicList;

import java.util.ArrayList;

/**
 * Created by Amia on 2017/12/11.
 */

public abstract class AddToListListener implements DialogInterface.OnClickListener {
    private Context context;
    private ArrayList<Mp3Info> mp3Infos;
    private ArrayList<MusicList> musicLists;

    public AddToListListener(Context context,ArrayList<Mp3Info> infos,ArrayList<MusicList> lists){
        this.context=context;
        mp3Infos=infos;
        musicLists=lists;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        MusicListAcitvityUtils.addMusicToList(context,musicLists.get(i).get_id(),mp3Infos);
        cancelSelect();
    }

    protected abstract void cancelSelect();
}
