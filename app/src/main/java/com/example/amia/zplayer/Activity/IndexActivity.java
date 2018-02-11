package com.example.amia.zplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.ControlUtil.MusicListAcitvityUtils;
import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.CurrentPositionReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PauseMusicReceiver;
import com.example.amia.zplayer.Service.MusicService;

public class IndexActivity extends MusicAboutActivity implements View.OnClickListener{

    private ViewPager pager;
    private SelectionPagerAdapter selectionAdapter;

    private View[] title_view;
    private ImageButton pausebutton;                      //暂停按钮
    private ImageButton love_ib;                     //添加到我喜欢按钮
    private ProgressBar progressBar;                //进度条
    private TextView title_tv;                  //音乐名称组件
    private TextView artist_tv;                 //演唱者名称组件
    private ImageView music_album;           //专辑图片组件

    private static PauseMusicReceiver pauseMusicReceiver;
    private static MusicServiceConnection musicServiceConnection;  //音乐服务连接
    private static MusInfoRec musInfoRec;          //音乐信息接收者
    private static CurrentPositionReceiver currentPositionReceiver;

    private MusicListDao musicListDao;
    private MusicOfListDao musicOfListDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        musicListDao=new MusicListDao(this);
        musicOfListDao=new MusicOfListDao(this);
        init();
        Intent intent=new Intent(this,MusicService.class);
        startService(intent);
    }

    private void init(){
        selectionAdapter=new SelectionPagerAdapter(getSupportFragmentManager());
        pager=findViewById(R.id.view_pager);
        pager.setAdapter(selectionAdapter);

        title_view=new View[selectionAdapter.getCount()*2];
        title_view[0]=findViewById(R.id.web_Button);
        title_view[1]=findViewById(R.id.web_view);
        title_view[2]=findViewById(R.id.list_button);
        title_view[3]=findViewById(R.id.list_view);
        title_view[0].setOnClickListener(this);
        title_view[2].setOnClickListener(this);
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

        LinearLayout bottomll=findViewById(R.id.con_bar);
        bottomll.setOnClickListener(this);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeTitleBtnColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /*设置标题按钮颜色*/
    private void changeTitleBtnColor(int position){
        for(int i=0;i<selectionAdapter.getCount();i++){
            if(i==position){
                ((TextView)title_view[i*2]).setTextColor(Color.RED);
                title_view[i*2+1].setVisibility(View.VISIBLE);
            }
            else {
                ((TextView) title_view[i * 2]).setTextColor(Color.BLACK);
                title_view[i * 2 + 1].setVisibility(View.INVISIBLE);
            }
        }
    }

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

    @Override
    protected void onStop(){
        musicPlayManager.subConnection();
        super.onStop();
        unregisterReceiver(pauseMusicReceiver);
        unregisterReceiver(musInfoRec);
        unregisterReceiver(currentPositionReceiver);
        unbindService(musicServiceConnection);
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

    private void continuePlayMusic(){
        musicPlayManager.resumePlay();
        isplay=true;
        musicPlayManager.setPlaying(isplay);
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
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

    class SelectionPagerAdapter extends FragmentPagerAdapter{

        SelectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position,IndexActivity.this);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "乐库";
                case 1:
                    return "音乐列表";
            }
            return null;
        }
    }

    private class MusicServiceConnection implements ServiceConnection{

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

    private class MusInfoRec extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Mp3Info mp3Info=(Mp3Info)intent.getSerializableExtra("mp3Info");
            setCurrentMusicInfo(mp3Info);
        }
    }

    private class CurrentPositReceiver extends CurrentPositionReceiver{

        @Override
        public void setProgressBar(int currentPosition) {
            progressBar.setProgress(currentPosition);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.web_Button:
                pager.setCurrentItem(0);
                break;
            case R.id.list_button:
                pager.setCurrentItem(1);
                break;
            case R.id.music_title:
            case R.id.music_artist:
            case R.id.music_album:
            case R.id.con_bar:
                //打开播放界面
                startPlayingActivity();
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
        }
    }

    private void startPlayingActivity(){
        Intent intent=new Intent(this,PlayingActivity.class);
        startActivity(intent);
    }
}
