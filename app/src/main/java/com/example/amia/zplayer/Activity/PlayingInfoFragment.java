package com.example.amia.zplayer.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amia.zplayer.R;

/**
 * Created by Amia on 2017/12/15.
 */

public class PlayingInfoFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER="section_number";
    public  PlayingInfoFragment(){
    }

    public static PlayingInfoFragment newInstance(int sectionNumber){
        PlayingInfoFragment fragment=new PlayingInfoFragment();
        Bundle args=new Bundle();
        args.putInt(ARG_SECTION_NUMBER,sectionNumber);
        fragment.setArguments(args);
        //Log.i("fragment",sectionNumber+"\t"+fragment.getId());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle saveInstanceState){
        int sectionNumber=getArguments().getInt(ARG_SECTION_NUMBER);
        View view=null;
        switch (sectionNumber){
            case 0:
                view=inflater.inflate(R.layout.playing_info_pager,container,false);
                break;
            case 1:
                view=inflater.inflate(R.layout.playing_lrc_pager,container,false);
                break;
        }
        return view;
    }
}
