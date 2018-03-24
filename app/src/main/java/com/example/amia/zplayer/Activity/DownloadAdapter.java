package com.example.amia.zplayer.Activity;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.View.ProgressView;
import com.example.amia.zplayer.util.DownloadUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amia on 2018/3/23.
 */

public abstract class DownloadAdapter extends BaseAdapter {

    private List<MusicDownLoadInfo> list;
    private Context context;
    private DownloadUtil downloadUtil;
    private List<Holder> holderList=new ArrayList<>();

    public DownloadAdapter(Context context,List<MusicDownLoadInfo> list){
        this.list=list;
        this.context=context;
        downloadUtil=new DownloadUtil(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return (list.get(i)).getId_list();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if(view==null){
            view=View.inflate(context, R.layout.download_item_layout,null);
            holder=new Holder();
            holder.title=view.findViewById(R.id.down_titile);
            holder.artist=view.findViewById(R.id.down_artist);
            holder.progressView=view.findViewById(R.id.down_progress_view);
            holder.downloadButton=view.findViewById(R.id.download_button);
            holder.listener=new DownLoadClickListener();
            holder.downloadButton.setOnClickListener(holder.listener);
            view.setTag(holder);
            holderList.add(holder);
        }
        else{
            holder=(Holder)view.getTag();
        }
        MusicDownLoadInfo musicDownLoadInfo=list.get(i);
        switch (musicDownLoadInfo.getStatus()){
            case 0:
            case 1:
                holder.downloadButton.setClickable(true);
                break;
            case 2:
                holder.downloadButton.setClickable(false);
        }
        holder.listener.setI(i);
        holder.info=musicDownLoadInfo;
        holder.title.setText(musicDownLoadInfo.getTitle());
        holder.artist.setText(musicDownLoadInfo.getArtist());
        setDownProgressInfo(holder);
        return view;
    }

    public List<Holder> getHolderList(){
        return holderList;
    }

    static class Holder{
        TextView title;
        TextView artist;
        ProgressView progressView;
        ImageButton downloadButton;
        MusicDownLoadInfo info;
        DownLoadClickListener listener;
        long progress;
        long duration;
    }

    protected abstract void setDownProgressInfo(Holder holder);

    class DownLoadClickListener implements View.OnClickListener{

        private int i;

        public void setI(int i){
            this.i=i;
        }

        @Override
        public void onClick(View view) {
            Log.i("clickListener","onClick");
            MusicDownLoadInfo info=list.get(i);
            info.setStatus(1);
            downloadUtil.downLoadMusic(info);
        }
    }
}
