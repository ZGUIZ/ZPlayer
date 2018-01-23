package com.example.amia.zplayer.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.ControlUtil.MusicListAcitvityUtils;
import com.example.amia.zplayer.ControlUtil.PlayingActivityUtil;
import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.LrcEntity;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.MusicPlayStatus;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.CurrentPositionReceiver;
import com.example.amia.zplayer.Receiver.MusicInfoReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PauseMusicReceiver;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.util.ChangelateUtil;
import com.example.amia.zplayer.util.LrcResovler;
import com.example.amia.zplayer.util.MusicResolverUtil;
import com.example.amia.zplayer.util.WindowInfoMananger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayingActivity extends MusicAboutActivity implements View.OnClickListener{

    protected static PauseMusicReceiver headsetrPlugReceiver;//耳机事件监听者
    protected static CurrentPositReceiver currentPositReceiver;//当前播放进度接受者
    protected static MusInfoRec musInfoRec;//音乐信息接受者
    protected static MusicServiceConnection musicServiceConnection;
    protected static MusicPlayManager musicPlayManager;

    public static String getCurrentMp3InfoKey="currentMusic";

    private ViewPager viewPager;
    private TextView current_time_tv;
    private TextView acount_time_time;
    private SeekBar process_seekbar;
    private ImageButton pause_ib;
    private ImageButton mode_ib;
    private ImageButton love_ib;

    private ImageView music_album;
    private TextView title_tv;
    private TextView artist_tv;
    private ListView lrc_listView;//歌词列表
    private TextView null_lrc_tv;  //无歌词
    private SimpleAdapter lrcAdapter; //歌词适配器

    private PlayingPagerAdapter pagerAdapter;

    private MusicListDao musicListDao;
    private MusicOfListDao musicOfListDao;
    private boolean seeking;

    private ListView musicListView;          //音乐列表
    private HalfMusAdapter musListAdapter;   //音乐列表适配器
    private RelativeLayout half_muslist_rl;  //音乐列表整个布局
    private RelativeLayout half_lsit_area;   //音乐列表区域布局

    private List<Mp3Info> musList;     //当前播放列表
    private List<LrcEntity> lrcList;   //歌词列表
    private List<Map<String,Object>> lrcObject=new ArrayList<>();   //歌词适配SimpleAdapter转换的List
    private int curlrc;     //当前歌词所在位置
    //private boolean isDrag;  //是否被拖动

    private static WindowInfoMananger wim;
    private static Point point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        musicListDao=new MusicListDao(this);
        musicOfListDao=new MusicOfListDao(this);
        wim=new WindowInfoMananger(this);
        point=wim.getScreenWidthHight();
        Intent intent=new Intent(this,MusicService.class);
        startService(intent);
        init();
    }

    //首次加载音乐信息
    private void onLoadMusic(Mp3Info mp3Info){
        currentMp3Info=mp3Info;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(pagerAdapter==null||pagerAdapter.getFragment(0)==null){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message msg=handler.obtainMessage();
                msg.what=0;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    setCurrentMusicInfo(currentMp3Info);
                    break;
            }
        }
    };

    private void init(){
        ImageButton back_iv=findViewById(R.id.play_back_ib);
        back_iv.setOnClickListener(this);

        viewPager=findViewById(R.id.music_info_pager);
        pagerAdapter=new PlayingPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        current_time_tv=findViewById(R.id.currentPos);
        acount_time_time=findViewById(R.id.Duration_tv);
        process_seekbar=findViewById(R.id.music_seekbar);
        pause_ib=findViewById(R.id.play_pause_ib);
        pause_ib.setOnClickListener(this);
        findViewById(R.id.pre_ib).setOnClickListener(this);
        findViewById(R.id.next_ib).setOnClickListener(this);
        findViewById(R.id.music_list_ib).setOnClickListener(this);
        mode_ib=findViewById(R.id.play_mode_ib);
        mode_ib.setOnClickListener(this);
        love_ib=findViewById(R.id.play_love_ib);
        love_ib.setOnClickListener(this);
        findViewById(R.id.play_else).setOnClickListener(this);

        musicListView=findViewById(R.id.half_mus_list);

        half_muslist_rl=findViewById(R.id.half_mus_list_rl);
        half_muslist_rl.setTag(false);
        half_muslist_rl.setOnClickListener(this);
        half_lsit_area=findViewById(R.id.list_area_rl);

        process_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                current_time_tv.setText(ChangelateUtil.calTime(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking=false;
                int progress=seekBar.getProgress();
                curlrc=PlayingActivityUtil.firstfindCurrentLrc(lrcList,progress,currentMp3Info);
                musicPlayManager.setCurrentPosition(progress);
                lrcAdapter.notifyDataSetChanged();
            }
        });
    }

    //设置音乐列表的适配器
    private void setmusListAdatper(){
        if(musList==null){
            return;
        }
        HalfMusAdapter.HalfItemSetting setting=new ListItemSetting();
        musListAdapter=new HalfMusAdapter(PlayingActivity.this,musList,setting);
        musicListView.setAdapter(musListAdapter);
        musicListView.setDivider(null);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                play(i);
                resetLrc(musList.get(i));  //重置歌词
            }
        });
    }

    //播放音乐
    private void play(int position){
        Mp3Info mp3Info=musList.get(position);
        process_seekbar.setMax((int)mp3Info.getDuration());
        process_seekbar.setProgress(0);
        musicPlayManager.playMusic(musList,position);
        isplay=true;
        setPauseButtonIcon();
        curlrc=0;
        musListAdapter.notifyDataSetChanged();
    }

    //设置暂停按钮图标
    private void setPauseButtonIcon(){
        if (isplay){
            pause_ib.setImageDrawable(getResources().getDrawable(R.drawable.pause_button,null));
        }
        else{
            pause_ib.setImageDrawable(getResources().getDrawable(R.drawable.play_btton,null));
        }
    }

    @Override
    protected void onStart(){
        bindMusicService();
        registerMusicInfoReceiver();
        registerCurrentPositReceiver();
        registerHeadsetrPlugReceiver();
        super.onStart();
    }

    //绑定音乐服务
    private void bindMusicService(){
        musicServiceConnection=new MusicServiceConnection();
        Intent intent=new Intent(this, MusicService.class);
        bindService(intent,musicServiceConnection,BIND_AUTO_CREATE);
    }

    //注册耳机事件接受者
    private void registerHeadsetrPlugReceiver(){
        headsetrPlugReceiver=new PauseMusicReceiver(this);
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.amia.musicplayer.musicservicepause");
        registerReceiver(headsetrPlugReceiver,filter);
    }

    //注册音乐信息接受者
    private void registerMusicInfoReceiver(){
        musInfoRec=new MusInfoRec();
        IntentFilter filter=new IntentFilter();
        filter.addAction("amia.musicplayer.action.MusicChange");
        this.registerReceiver(musInfoRec,filter);
    }

    //注册当前进度接收者
    private void registerCurrentPositReceiver(){
        currentPositReceiver=new CurrentPositReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(currentPositReceiver.currentPositionActionName);
        registerReceiver(currentPositReceiver,filter);
    }

    //暂停并调用Service的暂停方法
    @Override
    void PauseMusic() {
        musicPlayManager.pauseMusic();
        PauseMusicFromService();
    }

    //暂停
    @Override
    public void PauseMusicFromService() {
        isplay=false;
        pause_ib.setImageDrawable(getResources().getDrawable(R.drawable.play_btton,null));
        setPauseButtonIcon();
    }
    //恢复播放音乐
    public void resumePlayMusic(){
        if(currentMp3Info==null){
            return;
        }
        isplay=true;
        musicPlayManager.resumePlay();
        pause_ib.setImageDrawable(getResources().getDrawable(R.drawable.pause_button,null));
        setPauseButtonIcon();
    }

    private void setMusicIcon(Mp3Info info){
        Bitmap bitmap=MusicResolverUtil.getMusicBitemp(this,info.getId(),info.getAlbum_id());
        PlayingActivityUtil.setAlbumView(music_album,bitmap,point);
    }

    @Override
    public void setCurrentMusicInfo(Mp3Info info) {
        if (info==null){
            return;
        }
        currentMp3Info=info;
        android.support.v4.app.Fragment fragment=pagerAdapter.getFragment(0);
        View view=fragment.getView();
        music_album=view.findViewById(R.id.album_icon);
        title_tv=view.findViewById(R.id.music_title_tv);
        artist_tv=view.findViewById(R.id.artist);

        setMusicIcon(info);
        int time=(int)info.getDuration();
        title_tv.setText(info.getTitle());
        artist_tv.setText(info.getArtist());
        process_seekbar.setMax(time);
        process_seekbar.setProgress(0);
        acount_time_time.setText(ChangelateUtil.calTime(time));

        int love_id=musicListDao.getList_id("我喜欢");
        boolean inlist=musicOfListDao.isInList(love_id,(int)info.getId());
        info.setInLove(inlist);
        setLoveButtonIcon();
        musListAdapter.notifyDataSetChanged();

        if(lrc_listView==null) {
            lrc_listView = findViewById(R.id.lrc_list_view);
            lrcAdapter = new LrcAdapter(this, lrcObject, R.layout.lrc_item_layout, new String[]{"lrc"}, new int[]{R.id.lrc_text}, new LrcCallBack());
            lrc_listView.setAdapter(lrcAdapter);
            lrc_listView.setDivider(null);
            null_lrc_tv = findViewById(R.id.nul_lrc);

            //当发生触摸事件时，3秒内不滚动到现在对应的歌词上
            lrc_listView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            DragCancelRunnable.setDrag(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            new Thread(new DragCancelRunnable()).start();
                            break;
                    }
                    return false;
                }
            });
        }

        //设置歌词
        lrcList= LrcResovler.getLrc(info.getTitle(),info.getArtist());   //获取歌词
        PlayingActivityUtil.firstsetLrc(lrcList,lrcObject,null_lrc_tv,lrc_listView);
        lrcAdapter.notifyDataSetChanged();

    }

    private void setLoveButtonIcon(){
        if(currentMp3Info.isInLove()){
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
        }
        else{
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.love, null));
        }
    }

    //设置歌词
    private void setLrc(int position){
        int temp=curlrc;
        curlrc=PlayingActivityUtil.getCurlrcSet(curlrc,lrcList,position);

        //如果歌词没变动则不需要改变
        if(temp!=curlrc) {
            lrcAdapter.notifyDataSetChanged();
        }

        //判断3秒内是否触摸过屏幕
        //如果触摸过，则不跳转对应的歌词上
        if(DragCancelRunnable.isDrag()){
            return;
        }
        if(curlrc-5>0){
            lrc_listView.setSelection(curlrc-5);
        }
        else{
            lrc_listView.setSelection(0);
        }
    }

    private void resetLrc(Mp3Info mp3Info){
        curlrc=0;
        lrcList=LrcResovler.getLrc(mp3Info.getTitle(),mp3Info.getArtist());
        if(lrcList==null||lrcList.size()==0){
            PlayingActivityUtil.setEmptyLrc(lrcObject,null_lrc_tv,lrc_listView);
            return;
        }
        PlayingActivityUtil.firstsetLrc(lrcList,lrcObject,null_lrc_tv,lrc_listView);
        lrcAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_back_ib:
                this.finish();
                break;
            case R.id.play_pause_ib:
                if(currentMp3Info==null){
                    Toast.makeText(this,"无播放的的音乐",Toast.LENGTH_SHORT).show();
                    break;
                }
                if(isplay){
                    PauseMusic();
                }
                else{
                    resumePlayMusic();
                }
                break;
            case R.id.pre_ib:
                playPreMus();
                break;
            case R.id.next_ib:
                playNextMus();
                break;
            case R.id.music_list_ib:
                PlayingActivityUtil.openOrCloseMusList(this,half_muslist_rl,half_lsit_area);
                break;
            case R.id.play_mode_ib:
                switchMode();
                break;
            case R.id.play_love_ib:
                if(currentMp3Info!=null&&currentMp3Info.isInLove()){
                    removeLove();
                }
                else {
                    addToLove();
                }
                break;
            case R.id.play_else:

                break;
            case R.id.half_mus_list_rl:
                PlayingActivityUtil.closeMusicList(this,half_muslist_rl,half_lsit_area);
                break;
        }
    }

    //播放上一首
    private void playPreMus(){
        musicPlayManager.lastMusic();
        isplay=true;
        setPauseButtonIcon();
    }

    //播放下一首
    private void playNextMus(){
        musicPlayManager.nextMusic();
        isplay=true;
        setPauseButtonIcon();
    }

    //切换播放模式
    private void switchMode(){
        MusicPlayStatus status=PlayingActivityUtil.switchMode(musicPlayManager.getStatus());
        musicPlayManager.setStatus(status);
        setModeButton(status);
    }

    private void setModeButton(){
        setModeButton(musicPlayManager.getStatus());
    }

    //设置按钮
    private void setModeButton(MusicPlayStatus status){
        switch (status){
            case listloop:
                mode_ib.setImageDrawable(getResources().getDrawable(R.drawable.listloop,null));
                break;
            case sequence:
                mode_ib.setImageDrawable(getResources().getDrawable(R.drawable.sequence,null));
                break;
            case singleloop:
                mode_ib.setImageDrawable(getResources().getDrawable(R.drawable.singleloop,null));
                break;
        }
    }

    //添加到我喜欢
    private void addToLove(){
        if(currentMp3Info==null){
            Toast.makeText(this,"无播放的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        currentMp3Info.setInLove(true);
        int love_id=musicListDao.getList_id("我喜欢");
        MusicListAcitvityUtils.addSingleMusicToList(musicOfListDao,love_id,(int)currentMp3Info.getId());
        love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
    }

    //从我喜欢移除
    private void removeLove(){
        if (currentMp3Info==null){
            Toast.makeText(this,"无播放的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        int love_id=musicListDao.getList_id("我喜欢");
        musicOfListDao.deleteMusicOfList(love_id,(int)currentMp3Info.getId());
        currentMp3Info.setInLove(false);
        setLoveButtonIcon();
    }

    protected void onStop(){
        musicPlayManager.subConnection();
        unregisterReceiver(musInfoRec);
        unregisterReceiver(currentPositReceiver);
        unbindService(musicServiceConnection);
        unregisterReceiver(headsetrPlugReceiver);
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        if((boolean)half_muslist_rl.getTag()){
            PlayingActivityUtil.openOrCloseMusList(this,half_muslist_rl,half_lsit_area);
            return;
        }
        super.onBackPressed();
    }

    //切换音乐时音乐信息接收者
    class MusInfoRec extends MusicInfoReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Mp3Info mp3Info=(Mp3Info) intent.getSerializableExtra("mp3Info");
            setCurrentMusicInfo(mp3Info);
            resetLrc(mp3Info);
        }
    }

    class MusicServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayManager=(MusicPlayManager) iBinder;
            musicPlayManager.addConnection();
            isplay=musicPlayManager.isPlaying();
            musList=musicPlayManager.getMusicList();   //获取播放列表
            setmusListAdatper();   //音乐列表适配器
            setModeButton();       //设置播放模式按钮
            onLoadMusic(musicPlayManager.getCurretnMp3Info());  //加载当前音乐
            setPauseButtonIcon();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }
    class CurrentPositReceiver extends CurrentPositionReceiver{

        @Override
        public void setProgressBar(int currentPosition) {
            if(seeking){
                return;
            }
            process_seekbar.setProgress(currentPosition);
            current_time_tv.setText(ChangelateUtil.calTime(currentPosition));
            if(lrcList!=null&&lrcList.size()>0) {
                setLrc(currentPosition);
            }
        }
    }

    /**
     * 回调设置播放音乐类
     */
    class ListItemSetting implements HalfMusAdapter.HalfItemSetting {

        @Override
        public void setListItem(HalfMusAdapter.Holder holder) {
            if (holder.id==currentMp3Info.getId()){
                holder.title.setTextColor(getResources().getColor(R.color.playingItem));
                holder.artist.setTextColor(getResources().getColor(R.color.playingItem));
                holder.half_item_rl.setBackgroundColor(Color.rgb(245,245,245));
            }
            else{
                holder.title.setTextColor(getResources().getColor(R.color.unplayItem));
                holder.artist.setTextColor(getResources().getColor(R.color.unplayItem));
                holder.half_item_rl.setBackgroundColor(Color.rgb(255,255,255));
            }
        }
    }
    class LrcCallBack implements LrcAdapter.LrcColorCallBack{

        @Override
        public void setLrcColor(LrcAdapter.Holder holder,int position) {
            if (position == curlrc) {
                holder.textView.setTextColor(Color.rgb(105, 105, 105));
                holder.textView.setTextSize(16f);
                ((LrcView)holder.textView).setFocuse(true);
            } else {
                holder.textView.setTextColor(Color.rgb(190, 190, 190));
                holder.textView.setTextSize(16f);
                ((LrcView)holder.textView).setFocuse(false);
            }
        }
    }
}

/**
 * 控制滚动歌词列表的时候不自动跳转到对应的歌词选项的线程
 */
class DragCancelRunnable implements Runnable{
    private static int count;
    private static boolean isDrag;
    @Override
    public void run() {
        try {
            count++;
            isDrag=true;
            Thread.sleep(3000);
            count--;
            if(count==0){
                isDrag=false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDrag() {
        return isDrag;
    }
    public static void setDrag(boolean Drag){
        isDrag=Drag;
    }
}