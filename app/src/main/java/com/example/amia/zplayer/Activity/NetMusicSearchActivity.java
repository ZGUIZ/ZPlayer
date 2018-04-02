package com.example.amia.zplayer.Activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.ControlUtil.SearchMusicJson;
import com.example.amia.zplayer.DAO.DownloadDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.DownloadProgReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.View.ProgressView;
import com.example.amia.zplayer.util.DownloadUtil;
import com.example.amia.zplayer.util.JsonResolveUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetMusicSearchActivity extends MusicAboutActivity implements View.OnClickListener{

    final static int SETLISTMESSAGE=0;

    private AutoCompleteTextView search_tv;

   private LocalBroadcastManager manager;
   private DownLoadReceiver downLoadReceiver;

   private ListView res_list;
    private NetMusicAdapter adapter=new NetMusicAdapter();

    private List<Object> resInfo=new ArrayList<>();
    private List<Holder> holderList=new ArrayList<>();

    private String reachStr;//输入的查询条件
    private ExecutorService pool;
    private ProgressBar progressBar;  //歌曲搜索进度条
    private DownloadDao downloadDao;

    private static MusicServiceConnection musicServiceConnection;   //服务连接器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        pool=Executors.newSingleThreadExecutor();
        downloadDao=new DownloadDao(this);
        init();
        onBindMusicService();
    }

    //绑定服务
    private void onBindMusicService(){
        musicServiceConnection=new MusicServiceConnection();
        Intent intent=new Intent(this, MusicService.class);
        bindService(intent,musicServiceConnection,BIND_AUTO_CREATE);
    }

    protected void init(){
        search_tv=findViewById(R.id.sear_mus_tv);
        ImageButton search_btn=findViewById(R.id.sear_res_ib);
        res_list=findViewById(R.id.res_list);
        progressBar=findViewById(R.id.net_music_prog);

        search_btn.setOnClickListener(this);

        res_list.setAdapter(adapter);

        Toolbar toolbar=findViewById(R.id.res_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(this);

        IntentFilter filter=new IntentFilter(DownloadUtil.PROGRESS_ACTION);
        manager=LocalBroadcastManager.getInstance(this);
        downLoadReceiver=new DownLoadReceiver();
        manager.registerReceiver(downLoadReceiver,filter);
    }

    private void searchMusic(){
        resInfo.clear();
        reachStr=search_tv.getText().toString();
        if(reachStr.trim().equals("")){
            Toast.makeText(this,"请输入要查找的关键字",Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        pool.submit(new Runnable() {
            @Override
            public void run() {
                SearchMusicJson search=new SearchMusicJson();
                try {
                    String res=search.searchMusic(NetMusicSearchActivity.this,reachStr);
                    resInfo=JsonResolveUtils.resolveJson(res,MusicDownLoadInfo.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    resInfo.clear();
                }
                Message msg=handler.obtainMessage();
                msg.what=SETLISTMESSAGE;
                handler.sendMessage(msg);
            }
        });
    }

    protected void download(final Holder holder){
        final MusicDownLoadInfo info=holder.info;
        if(downloadDao.isInList(info.getNetId())){
            AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("确认下载？").setMessage("你的下载列表中已经存在该音乐，是否继续或重新下载？").create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    downloadDao.delete(info);
                    download(info);
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            alertDialog.show();
        }
        else {
            download(info);
        }
    }

    private void download(MusicDownLoadInfo info){
        DownloadUtil util=new DownloadUtil(this);
        util.downLoadMusic(info);
        info.setStatus(1);
        SearchMusicJson.addToDownDatabase(this,info);
    }

    @Override
    protected void onDestroy(){
        pool.shutdown();  //关闭线程池
        manager.unregisterReceiver(downLoadReceiver);
        unbindService(musicServiceConnection);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sear_res_ib:
                searchMusic();
                break;
            default:
                this.finish();
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SETLISTMESSAGE:
                    progressBar.setVisibility(View.GONE);
                    isResultEmpty();
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void isResultEmpty(){
        if(resInfo==null||resInfo.isEmpty()){
            Toast.makeText(this,"没有找到对应的结果！",Toast.LENGTH_SHORT).show();
        }
    }

    private void tryListen(Holder holder){
        musicPlayManager.addToNext(holder.info);
        musicPlayManager.playMusic(holder.info);
        setCurrentMusicInfo(holder.info);
        super.startActivity(PlayingActivity.class);
    }

    @Override
    void PauseMusic() {
    }

    @Override
    public void PauseMusicFromService() {
    }

    @Override
    public void setCurrentMusicInfo(Mp3Info info) {
    }

    class NetMusicAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return resInfo.size();
        }

        @Override
        public Object getItem(int i) {
            return resInfo.get(i);
        }

        @Override
        public long getItemId(int i) {
            return ((MusicDownLoadInfo)resInfo.get(i)).getNetId();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Holder holder;
            if(view==null){
                view=View.inflate(NetMusicSearchActivity.this,R.layout.net_item_layout,null);
                holder=new Holder();
                holder.listen=view.findViewById(R.id.net_item_rl);
                holder.title=view.findViewById(R.id.net_title_tv);
                holder.artist=view.findViewById(R.id.net_artist_tv);
                holder.download=view.findViewById(R.id.download_ib);
                holder.progressView=view.findViewById(R.id.down_progress);
               // ListItemClickListener listener=new ListItemClickListener();
                //holder.listener=listener;
                holder.listen.setOnClickListener(holder);
                holder.download.setOnClickListener(holder);
                view.setTag(holder);
                holderList.add(holder);
            }
            else{
                holder=(Holder) view.getTag();
            }
            //holder.listener.setInfo(i,holder);
            MusicDownLoadInfo info=(MusicDownLoadInfo)resInfo.get(i);
            holder.net_id=info.getNetId();
            holder.title.setText(info.getTitle());
            holder.artist.setText(info.getArtist());
            holder.info= (MusicDownLoadInfo) resInfo.get(i);
            switch (holder.info.getStatus()){
                case 0:
                    holder.download.setVisibility(View.VISIBLE);
                    holder.progressView.setVisibility(View.GONE);
                    holder.download.setImageDrawable(getResources().getDrawable(R.drawable.download_button,null));
                    holder.download.setClickable(true);
                    break;
                case 1:
                    holder.download.setVisibility(View.INVISIBLE);
                    holder.progressView.setVisibility(View.VISIBLE);
                    holder.progressView.setDuration(holder.duration);
                    holder.progressView.setProgress(holder.progress);
                    break;
                case 2:
                    //Log.i("NetMusic","status=2");
                    holder.download.setVisibility(View.VISIBLE);
                    holder.progressView.setVisibility(View.GONE);
                    holder.download.setImageDrawable(getResources().getDrawable(R.drawable.finish,null));
                    holder.download.setClickable(false);
                    break;
            }

            return view;
        }
    }

    class Holder implements View.OnClickListener{
        int net_id;
        TextView title;
        TextView artist;
        RelativeLayout listen;
        ImageButton download;
        ProgressView progressView;
        //ListItemClickListener listener;
        long progress;
        long duration;
        MusicDownLoadInfo info;

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.net_item_rl:
                    tryListen(this);
                    break;
                case R.id.download_ib:
                    download(this);
                    break;
            }
        }
    }
/*
    class ListItemClickListener implements View.OnClickListener{
        private int i;
        private Holder holder;
        public void setInfo(int i,Holder holder){
            this.i=i;
            this.holder=holder;
        }
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.net_item_rl:
                    tryListen(holder);
                    Log.i("onClick","tryListen");
                    break;
                case R.id.download_ib:
                    download(i,holder);
                    break;
            }
        }
    }
*/
    class DownLoadReceiver extends DownloadProgReceiver{

        @Override
        protected void displayProgress(int id, long progress, long duration) {
            for(Holder holder:holderList){
                if(holder.net_id==id){
                    if(progress==duration){
                        //Log.i("DownLoadRec","equal");
                        holder.progress=progress;
                        holder.duration=duration;
                        holder.info.setStatus(2);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    holder.info.setStatus(1);
                    holder.progress=progress;
                    holder.duration=duration;
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
            //如果没有在当前显示的item中
            if(progress==duration){
                //设置标志下载完毕
                for(Object object:resInfo){
                    MusicDownLoadInfo info=(MusicDownLoadInfo)object;
                    if(info.getNetId()==id){
                        info.setStatus(2);
                        break;
                    }
                }
            }
        }
    }

    private class MusicServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayManager=(MusicPlayManager)iBinder;
            isplay=musicPlayManager.isPlaying();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }
}
