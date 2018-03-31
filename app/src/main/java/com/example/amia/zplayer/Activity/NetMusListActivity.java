package com.example.amia.zplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.ControlUtil.MusicListAcitvityUtils;
import com.example.amia.zplayer.ControlUtil.SearchMusicJson;
import com.example.amia.zplayer.DAO.DownloadDao;
import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicClassify;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.CurrentPositionReceiver;
import com.example.amia.zplayer.Receiver.DownloadProgReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PauseMusicReceiver;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.View.ProgressView;
import com.example.amia.zplayer.util.DownloadUtil;
import com.example.amia.zplayer.util.JsonResolveUtils;
import com.example.amia.zplayer.util.NetUtils;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class NetMusListActivity extends MusicAboutActivity implements View.OnClickListener{

    private static final int SET_LIST=0;

    private static PauseMusicReceiver pauseMusicReceiver;
    private static MusicServiceConnection musicServiceConnection;  //音乐服务连接
    private static MusInfoRec musInfoRec;          //音乐信息接收者
    private static CurrentPositionReceiver currentPositionReceiver;
    private DownLoadReceiver downLoadReceiver;
    private LocalBroadcastManager manager;

    private ImageButton pausebutton;         //暂停按钮
    private ImageButton love_ib;             //添加到我喜欢按钮
    private ProgressBar progressBar;         //进度条
    private TextView title_tv;               //音乐名称组件
    private TextView artist_tv;              //演唱者名称组件
    private ImageView music_album;           //专辑图片组件
    private ProgressBar waitProgress;        //正在查找进度条
    private NetMusicAdapter adapter;

    private List<Object> resInfo=new ArrayList<>();  //查询结果
    private List<Holder> holderList=new ArrayList<>();

    private MusicListDao musicListDao;
    private MusicOfListDao musicOfListDao;
    private DownloadDao downloadDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_mus_list);
        musicListDao=new MusicListDao(this);
        musicOfListDao=new MusicOfListDao(this);
        downloadDao=new DownloadDao(this);
        Intent findIntent=getIntent();
        init(findIntent);
        Intent intent=new Intent(this,MusicService.class);
        startService(intent);
        registerDownloadReceiver();
    }

    protected void init(Intent intent){
        MusicClassify classify= (MusicClassify) intent.getSerializableExtra("classify");
        startSearchThread(classify);

        progressBar=findViewById(R.id.progressbar);
        title_tv=findViewById(R.id.music_title);
        artist_tv=findViewById(R.id.music_artist);
        pausebutton=findViewById(R.id.startpause);
        love_ib=findViewById(R.id.love_ib);
        music_album=findViewById(R.id.music_album);
        title_tv.setOnClickListener(this);
        title_tv.setSingleLine(true);
        title_tv.setEllipsize(TextUtils.TruncateAt.END);
        artist_tv.setOnClickListener(this);
        artist_tv.setSingleLine(true);
        artist_tv.setEllipsize(TextUtils.TruncateAt.END);
        pausebutton.setOnClickListener(this);
        music_album.setOnClickListener(this);
        love_ib.setOnClickListener(this);

        waitProgress=findViewById(R.id.wait_progress);
        adapter=new NetMusicAdapter();
        ListView listView=findViewById(R.id.net_music_list);
        listView.setDivider(null);
        listView.setAdapter(adapter);

        ((TextView)findViewById(R.id.net_list_name)).setText(classify.getName());
        setToolBar();
    }

    protected void setToolBar(){
        Toolbar toolbar=findViewById(R.id.net_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(this);
    }

    protected void startSearchThread(final MusicClassify classify){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res=null;
                try {
                    Log.i("Thread","id="+classify.getId());
                    res= NetUtils.requestDataFromNet(getResources().getString(R.string.queryMusicInList)+classify.getId());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Message msg=handler.obtainMessage();
                msg.what=SET_LIST;
                msg.obj=res;
                handler.sendMessage(msg);
            }
        }).start();
    }

    protected void resolveMusicInfo(String json){
        waitProgress.setVisibility(View.GONE);
        if(json==null||json.trim().equals("")){
            findViewById(R.id.no_res).setVisibility(View.VISIBLE);
            return;
        }
        try {
            resInfo= JsonResolveUtils.resolveJson(json,MusicDownLoadInfo.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SET_LIST:
                    resolveMusicInfo((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onStart(){
        //暂停接收者
        registerHeadsetPlugReceiver();
        //绑定音乐服务
        onBindMusicService();
        //音乐信息接受者
        registerMusicrInfoReceiver();
        //播放进度接收者
        registerCurrentPositionReceiver();
        //设置暂停按钮图标
        setPauseButtonIcon();
        super.onStart();
    }

    @Override
    protected void onResume(){
        if(musicPlayManager!=null) {
            isplay = musicPlayManager.isPlaying();
        }
        setPauseButtonIcon();
        super.onResume();
    }

    @Override
    protected void onStop(){
        musicPlayManager.subConnection();
        super.onStop();
        unregisterReceiver(pauseMusicReceiver);
        unregisterReceiver(musInfoRec);
        unregisterReceiver(currentPositionReceiver);
        unbindService(musicServiceConnection);
    }

    //设置暂停按钮的图标
    private void setPauseButtonIcon(){
        if(isplay){
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
            return;
        }
        if(musicPlayManager!=null&&musicPlayManager.isPlaying()){
            isplay=true;
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        }
        else{
            isplay=false;
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
        }
    }

    //绑定音乐服务
    private void onBindMusicService(){
        musicServiceConnection=new MusicServiceConnection();
        Intent intent=new Intent(this, MusicService.class);
        bindService(intent,musicServiceConnection,BIND_AUTO_CREATE);
    }

    //注册音乐信息的接受者
    private void registerMusicrInfoReceiver(){
        musInfoRec=new MusInfoRec();
        IntentFilter filter=new IntentFilter();
        filter.addAction("amia.musicplayer.action.MusicChange");
        this.registerReceiver(musInfoRec,filter);
    }

    //绑定耳机事件接受者
    private void registerHeadsetPlugReceiver(){
        pauseMusicReceiver=new PauseMusicReceiver(this);
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.amia.musicplayer.musicservicepause");
        this.registerReceiver(pauseMusicReceiver,filter);
    }

    //注册播放进度接收者
    private void registerCurrentPositionReceiver(){
        currentPositionReceiver=new CurrentPositReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(CurrentPositionReceiver.currentPositionActionName);
        this.registerReceiver(currentPositionReceiver,filter);
    }

    /**
     * 注册下载进度接受者
     */
    private void registerDownloadReceiver(){
        downLoadReceiver=new DownLoadReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(DownloadUtil.PROGRESS_ACTION);
        manager=LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(downLoadReceiver,filter);
    }

    @Override
    void PauseMusic() {
        isplay=false;
        musicPlayManager.pauseMusic();
        PauseMusicFromService();
    }

    @Override
    public void PauseMusicFromService() {
        isplay=false;
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
    }

    @Override
    public void setCurrentMusicInfo(Mp3Info info) {
        currentMp3Info=info;
        Bitmap bitmap=null;
        if(bitMap!=null) {
            bitmap = bitMap.get(String.valueOf(info.getId()));
        }
        if(bitmap!=null){
            music_album.setImageBitmap(bitmap);
        }
        else{
            bitmap=musicResolverUtil.getMusicBitemp(this,info.getId(),info.getAlbum_id());
            if(bitmap!=null){
                music_album.setImageBitmap(bitmap);
            }
            else{
                music_album.setImageResource(R.drawable.defaluticon);
            }
        }
        artist_tv.setText(info.getArtist());
        title_tv.setText(info.getTitle());
        progressBar.setMax((int)info.getDuration());
        if(isplay){
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        }
        else{
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
        }

        int love_id=musicListDao.getList_id("我喜欢");
        boolean inlist=musicOfListDao.isInList(love_id,(int)currentMp3Info.getId());
        if(inlist){
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
            currentMp3Info.setInLove(true);
        }
        else{
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.love,null));
            currentMp3Info.setInLove(false);
        }
    }

    @Override
    protected void onDestroy(){
        manager.unregisterReceiver(downLoadReceiver);
        super.onDestroy();
    }

    /**
     * 添加到我喜欢
     */
    private void addToLove(){
        if(currentMp3Info==null){
            Toast.makeText(this,"无播放的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }

        MusicListAcitvityUtils.addSingleMusicToList(musicOfListDao,musicListDao.getList_id("我喜欢"),(int)currentMp3Info.getId());
        love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
    }

    /**
     * 从我喜欢中移除
     */
    private void removeFromLove(){
        musicOfListDao.deleteMusicOfList(musicListDao.getList_id("我喜欢"),(int)currentMp3Info.getId());
        currentMp3Info.setInLove(false);
        love_ib.setImageDrawable(getResources().getDrawable(R.drawable.love,null));
    }

    private void continuePlayMusic(){
        musicPlayManager.resumePlay();
        isplay=true;
        musicPlayManager.setPlaying(isplay);
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.music_title:
            case R.id.music_artist:
            case R.id.music_album:
            case R.id.con_bar:
                //打开播放界面
                super.startActivity(PlayingActivity.class);
                break;
            case R.id.startpause:
                if(currentMp3Info==null){
                    Toast.makeText(this,"无播放的的音乐",Toast.LENGTH_SHORT).show();
                    break;
                }
                if(isplay){
                    PauseMusic();
                }
                else{
                    continuePlayMusic();
                }
                break;
            case R.id.love_ib:
                //判断是否在列表中
                if(currentMp3Info!=null&&currentMp3Info.isInLove()){
                    removeFromLove();
                }
                else {
                    addToLove();
                }
                break;
            case R.id.net_search_ib:
                super.startActivity(NetMusicSearchActivity.class);
                break;
            default:
                finish();
        }
    }

    private class MusicServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayManager=(MusicPlayManager)iBinder;
            musicPlayManager.addConnection();
            isplay=musicPlayManager.isPlaying();
            setPauseButtonIcon();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private class MusInfoRec extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Mp3Info mp3Info=(Mp3Info)intent.getSerializableExtra("mp3Info");
            setCurrentMusicInfo(mp3Info);
        }
    }

    class CurrentPositReceiver extends CurrentPositionReceiver {

        @Override
        public void setProgressBar(int currentPosition) {
            progressBar.setProgress(currentPosition);
        }
    }

    class NetMusicAdapter extends BaseAdapter {

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
                view=View.inflate(NetMusListActivity.this,R.layout.net_item_layout,null);
                holder=new Holder();
                holder.title=view.findViewById(R.id.net_title_tv);
                holder.artist=view.findViewById(R.id.net_artist_tv);
                holder.listen=view.findViewById(R.id.try_listen_ib);
                holder.download=view.findViewById(R.id.download_ib);
                holder.progressView=view.findViewById(R.id.down_progress);
                ListItemClickListener listener=new ListItemClickListener();
                holder.listener=listener;
                holder.listen.setOnClickListener(listener);
                holder.download.setOnClickListener(listener);
                view.setTag(holder);
                holderList.add(holder);
            }
            else{
                holder=(Holder) view.getTag();
            }
            holder.listener.setInfo(i,holder);
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

    protected void download(int i,final Holder holder){
        final MusicDownLoadInfo info=(MusicDownLoadInfo)resInfo.get(i);
        if(downloadDao.isInList(info.getNetId())){
            AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("确认下载？").setMessage("你的下载列表中已经存在该音乐，是否继续或重新下载？").create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    downloadDao.delete(info);
                    download(holder);
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
            download(holder);
        }
    }

    private void download(Holder holder){
        DownloadUtil util=new DownloadUtil(this);
        util.downLoadMusic(holder.info);
        holder.info.setStatus(1);
        SearchMusicJson.addToDownDatabase(this,holder.info);
    }

    class Holder{
        int net_id;
        TextView title;
        TextView artist;
        ImageButton listen;
        ImageButton download;
        ProgressView progressView;
        ListItemClickListener listener;
        long progress;
        long duration;
        MusicDownLoadInfo info;
    }

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
                case R.id.try_listen_ib:

                    break;
                case R.id.download_ib:
                    download(i,holder);
                    break;
            }
        }
    }

    class DownLoadReceiver extends DownloadProgReceiver {

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
}