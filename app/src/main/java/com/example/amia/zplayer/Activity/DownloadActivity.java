package com.example.amia.zplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;

import com.example.amia.zplayer.DAO.DownloadDao;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.DownloadProgReceiver;
import com.example.amia.zplayer.util.DownloadUtil;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends MusicListActivity {

    private DownListAdapter adapter;
    private List<MusicDownLoadInfo> infos=new ArrayList<>();
    private BroadcastReceiver receiver;
    private LocalBroadcastManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        registerProgressReceiver();
        findViewById(R.id.search_ib).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy(){
        manager.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void startResolveMusicThread(){
        resolveMusicThread.start();
    }

    @Override
    public void setListAdpter(){
        adapter=new DownListAdapter(this,infos);
        music_list.setDivider(null);
        music_list.setAdapter(adapter);
        music_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAfLoCl){
                    return false;
                }
                isAfLoCl=true;
                MusicDownLoadInfo mp3Info=infos.get(i);
                mp3Info.setSelected(true);
                checkedMus.add(mp3Info);
                adapter.notifyDataSetChanged();
                bottom_layout.setVisibility(View.INVISIBLE);
                bottom_tool_layout.setVisibility(View.VISIBLE);
                list_title.setVisibility(View.GONE);
                all_sel_ll.setVisibility(View.VISIBLE);
                cancel_tv.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.GONE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
            }
        });
    }

    private void registerProgressReceiver(){
        receiver=new DownLoadReceiver();
        IntentFilter filter=new IntentFilter(DownloadUtil.PROGRESS_ACTION);
        manager= LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(receiver,filter);
    }

    class DownListAdapter extends DownloadAdapter{
        public DownListAdapter(Context context, List<MusicDownLoadInfo> list) {
            super(context, list);
        }

        @Override
        protected void setDownProgressInfo(Holder holder) {
            switch (holder.info.getStatus()){
                case 0:
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.progressView.setVisibility(View.GONE);
                    holder.downloadButton.setImageDrawable(getResources().getDrawable(R.drawable.download_button,null));
                    holder.downloadButton.setClickable(true);
                    break;
                case 1:
                    holder.downloadButton.setVisibility(View.INVISIBLE);
                    holder.progressView.setVisibility(View.VISIBLE);
                    holder.progressView.setDuration(holder.duration);
                    holder.progressView.setProgress(holder.progress);
                    break;
                case 2:
                    //Log.i("NetMusic","status=2");
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.progressView.setVisibility(View.GONE);
                    holder.downloadButton.setImageDrawable(getResources().getDrawable(R.drawable.finish,null));
                    holder.downloadButton.setClickable(false);
                    break;
            }
        }
    }

    protected void adapterDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    Thread resolveMusicThread=new Thread(new Runnable() {
        @Override
        public void run() {
            DownloadDao dao=new DownloadDao(DownloadActivity.this);
            infos=dao.queryAll();
            Message msg=list_handler.obtainMessage();
            msg.what=msg_setadapter;
            list_handler.sendMessage(msg);
        }
    });

    class DownLoadReceiver extends DownloadProgReceiver {
        @Override
        protected void displayProgress(int id, long progress, long duration) {
            List<DownloadAdapter.Holder> holderList=adapter.getHolderList();
            for(DownloadAdapter.Holder holder:holderList){
                if(holder.info.getNetId()==id){
                    if(progress==duration){
                        holder.progressView.setProgress(progress);
                        holder.progressView.setDuration(duration);
                        holder.info.setStatus(2);
                        adapterDataSetChanged();
                        return;
                    }
                    if(holder.info.getStatus()!=1) {
                        holder.info.setStatus(1);
                        adapterDataSetChanged();
                    }
                    holder.progressView.setDuration(duration);
                    holder.progressView.setProgress(progress);
                    return;
                }
            }
            //如果没有在当前显示的item中
            if(progress==duration){
                //设置标志下载完毕
                for(Object object:infos){
                    MusicDownLoadInfo info=(MusicDownLoadInfo)object;
                    if(info.getNetId()==id){
                        info.setStatus(2);
                        break;
                    }
                }
            }
        }
    }
}
