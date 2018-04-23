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
    private static DownloadUtil downloadUtil;
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
            holder.context=context;
            holder.adapter=this;
            holder.title=view.findViewById(R.id.down_titile);
            holder.artist=view.findViewById(R.id.down_artist);
            holder.progressView=view.findViewById(R.id.down_progress_view);
            holder.progressView.setOnClickListener(holder);
            holder.downloadButton=view.findViewById(R.id.download_button);
            holder.downloadButton.setOnClickListener(holder);
            view.setTag(holder);
            holderList.add(holder);
        }
        else{
            holder=(Holder)view.getTag();
        }
        MusicDownLoadInfo musicDownLoadInfo=list.get(i);
        holder.info=musicDownLoadInfo;
        switch (musicDownLoadInfo.getStatus()){
            case 0:
            case 1:
                holder.downloadButton.setClickable(true);
                setProgressViewState(holder);
                break;
            case 2:
                holder.downloadButton.setClickable(false);
        }

        holder.title.setText(musicDownLoadInfo.getTitle());
        holder.artist.setText(musicDownLoadInfo.getArtist());
        setDownProgressInfo(holder);
        return view;
    }

    private void setProgressViewState(Holder holder){
        MusicDownLoadInfo info=holder.info;
        if(info==null||info.getStatus()==2){
            return;
        }
        boolean isLoading=DownloadUtil.isLoading(info);
        holder.progressView.setLoading(isLoading);
    }

    public List<Holder> getHolderList(){
        return holderList;
    }

    static class Holder implements View.OnClickListener{
        TextView title;
        TextView artist;
        ProgressView progressView;
        ImageButton downloadButton;
        MusicDownLoadInfo info;
        long progress;
        long duration;
        private Context context;
        private DownloadAdapter adapter;

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.down_progress_view:
                    downloadOrPause();
                    break;
                case R.id.download_button:
                    info.setStatus(1);
                    downloadUtil.downLoadMusic(info);
                    break;
            }
        }

        private void downloadOrPause(){
            //Log.i("Download","downloadOrPause\t"+progressView.isLoading());
            if(progressView.isLoading()){
                DownloadUtil.cancelDownload(info);
                info.setStatus(0);
                progressView.setLoading(false);
            }
            else{
                info.setStatus(1);
                DownloadUtil downloadUtil=new DownloadUtil(context);
                downloadUtil.downLoadMusic(info);
                progressView.setLoading(true);
            }
            adapter.notifyDataSetChanged();
        }
    }

    protected abstract void setDownProgressInfo(Holder holder);
}
