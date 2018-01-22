package com.example.amia.zplayer.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicList;
import com.example.amia.zplayer.DTO.MusicOfList;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.util.MusicResolverUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Amia on 2017/12/12.
 */

public class MusiclistListItemAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MusicList> musicLists;

    private MusicListDao musicListDao;
    private MusicOfListDao musicOfListDao;
    private MusicResolverUtil resolverUtil;
    private HashMap<Integer,Integer> list_icon;

    public MusiclistListItemAdapter(Context context, ArrayList<MusicList> musicLists){
        super();
        this.musicLists=musicLists;
        this.context=context;
        musicListDao=new MusicListDao(context);
        musicOfListDao=new MusicOfListDao(context);
        resolverUtil=new MusicResolverUtil((Activity)context);
    }

    public MusiclistListItemAdapter(Context context, ArrayList<MusicList> musicLists, HashMap<Integer,Integer> icon){
        super();
        this.musicLists=musicLists;
        this.context=context;
        musicListDao=new MusicListDao(context);
        musicOfListDao=new MusicOfListDao(context);
        resolverUtil=new MusicResolverUtil((Activity)context);
        this.list_icon=icon;
    }

    @Override
    public int getCount() {
        return musicLists.size();
    }

    @Override
    public Object getItem(int i) {
        return musicLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;
        if(view==null){
            view=View.inflate(context, R.layout.musiclists_list_item_layout,null);
            holder=new ViewHolder();
            holder.list_icon=view.findViewById(R.id.lists_icon);
            holder.list_name=view.findViewById(R.id.lists_name);
            holder.div_view=view.findViewById(R.id.div_view);
            view.setTag(holder);
        }
        else{
            holder=(ViewHolder) view.getTag();
        }
        MusicList musicList=musicLists.get(i);
        holder.list_name.setText(musicList.get_Name());
        if(i==musicLists.size()-1){
            holder.div_view.setVisibility(View.GONE);
        }
        else{
            holder.div_view.setVisibility(View.VISIBLE);
        }
        if(list_icon!=null){
            holder.list_icon.setImageResource(list_icon.get(musicList.get_id()));
            return view;
        }
        Bitmap bitmap=getListBitMap(musicList);
        if(bitmap==null){
            holder.list_icon.setImageResource(R.drawable.defaluticon);
        }
        else {
            holder.list_icon.setImageBitmap(bitmap);
        }
        return view;
    }

    static class ViewHolder{
        ImageView list_icon;
        TextView list_name;
        View div_view;
    }

    //获取动态列表的参数
    private Bitmap getListBitMap(MusicList list){
        Bitmap bitmap=null;
        MusicOfList musicOfList=musicOfListDao.getOneByList(list.get_id());
        if(musicOfList==null){
            return null;
        }
        Mp3Info mp3Info=resolverUtil.getMp3InfoById(musicOfList.getId_music());
        bitmap=resolverUtil.getMusicBitemp(context,mp3Info.getId(), mp3Info.getAlbum_id());
        return bitmap;
    }
}
