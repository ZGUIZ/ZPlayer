package com.example.amia.zplayer.Activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.R;

import java.util.List;

/**
 * Created by Amia on 2017/12/28.
 */

public class HalfMusAdapter extends BaseAdapter {

    private Context context;
    private List<Mp3Info> mp3Infos;
    private HalfItemSetting setting;
    public HalfMusAdapter(Context context, List<Mp3Info> mp3Infos,HalfItemSetting setting){
        this.context=context;
        this.mp3Infos=mp3Infos;
        this.setting=setting;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int i) {
        return mp3Infos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mp3Infos.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder=null;
        if(view==null){
            view=View.inflate(context, R.layout.half_mus_item_layout,null);
            holder=new Holder();
            holder.half_item_rl=view.findViewById(R.id.half_item_rl);
            holder.title=view.findViewById(R.id.half_mus_title);
            holder.artist=view.findViewById(R.id.half_mus_artist);
            holder.playing=view.findViewById(R.id.half_play_iv);
            view.setTag(holder);
        }
        else{
            holder=(Holder)view.getTag();
        }
        Mp3Info info=mp3Infos.get(i);
        holder.id=info.getId();
        holder.title.setText(info.getTitle());
        holder.title.setSingleLine(true);
        holder.title.setEllipsize(TextUtils.TruncateAt.END);
        holder.artist.setText(info.getArtist());
        holder.artist.setSingleLine(true);
        holder.artist.setEllipsize(TextUtils.TruncateAt.END);
        setting.setListItem(holder);
        return view;
    }

    static class Holder{
        long id;
        RelativeLayout half_item_rl;
        TextView title;
        TextView artist;
        ImageView playing;
    }

    interface HalfItemSetting{
        void setListItem(Holder holder);
    }
}
