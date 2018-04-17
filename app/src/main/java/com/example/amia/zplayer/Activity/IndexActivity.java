package com.example.amia.zplayer.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.CurrentPositionReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PauseMusicReceiver;
import com.example.amia.zplayer.Receiver.ScheStopReceiver;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.util.BitMapUtil;
import com.example.amia.zplayer.util.LrcResovler;

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
    private Button scheButton;               //设置定时结束的按钮

    private static PauseMusicReceiver pauseMusicReceiver;
    private static MusicServiceConnection musicServiceConnection;  //音乐服务连接
    private static MusInfoRec musInfoRec;          //音乐信息接收者
    private static CurrentPositionReceiver currentPositionReceiver;  //当前音乐进度接收者
    private static ScheReceiver scheReceiver;
    private static LocalBroadcastManager broadcastManager;

    private MusicListDao musicListDao;
    private MusicOfListDao musicOfListDao;

    private static final int REQUEST_PHONE_STATE=0;
    private static final int REQUEST_EXTERNAL_STORAGE=1;
    private static final int REQUEST_NET_STATE=2;

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
        //申请电话监听权限
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_PHONE_STATE);
        }

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE},REQUEST_NET_STATE);
        }

        //申请内存读写权限
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
        }

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

        ImageButton searchButton=findViewById(R.id.net_search_ib);
        searchButton.setOnClickListener(this);

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

        Toolbar toolbar=findViewById(R.id.tit_bar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        findViewById(R.id.exit).setOnClickListener(this);
        Button clearButton=findViewById(R.id.clear_ache_bt);
        clearButton.setOnClickListener(this);
        String str=BitMapUtil.getBimapTempSize();
        if(str!=null) {
            clearButton.setText("清除缓存(" + str + ")");
        }
        scheButton=findViewById(R.id.sche_stop_bt);
        scheButton.setOnClickListener(this);
        findViewById(R.id.setting).setOnClickListener(this);
    }

    /**
     * 退出应用
     */
    private void exitApp(){
        unbindService(musicServiceConnection);
        Intent intent=new Intent(this,MusicService.class);
        stopService(intent);
        this.finish();
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
        //设置暂停时间接受者
        registerScheStopReceiver();

        scheButton.setText("定时停止");
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
        broadcastManager.unregisterReceiver(scheReceiver);
        try {
            unbindService(musicServiceConnection);
        }
        catch (Exception e){
            e.printStackTrace();
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

    protected void registerScheStopReceiver(){
        scheReceiver=new ScheReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(MusicService.scheSotpKey);
        broadcastManager=LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(scheReceiver,filter);
    }

    protected void setScheLastTimeFromService(String time){
        if(time.equals("0 : 0")||time.equals("00 : 00")){
            scheButton.setText("定时停止");
            isSche=false;
            isplay=false;
            setPauseButtonIcon();
        }
        else{
            scheButton.setText("取消定时("+time+")");
            isSche=true;
        }
    }

    private void scheButtonClick(){
        if(isSche){
            stopScheTime();
        }
        else{
            setScheStopTime();
        }
    }

    private void setScheStopTime(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("您希望多少分钟后停止？");
        final EditText editText=new EditText(this);
        editText.setHint("输入停止时间（分钟）");
        editText.setSelectAllOnFocus(true);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int time = Integer.parseInt(editText.getText().toString().trim());
                    musicPlayManager.setStopTime(time);
                    isSche=true;
                }
                catch (Exception e){
                    Toast.makeText(IndexActivity.this,"输入错误!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void stopScheTime(){
        musicPlayManager.stopScheStop();
        isSche=false;
        scheButton.setText("定时停止");
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
        int length= (int) info.getDuration();
        progressBar.setMax(length);
        if(isplay){
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        }
        else{
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
        }

        int love_id=musicListDao.getList_id("我喜欢");
        boolean inlist=musicOfListDao.isInList(love_id,(int)currentMp3Info.getId());
        love_ib.setClickable(true);
        if(inlist){
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
            currentMp3Info.setInLove(true);
        }
        else{
            love_ib.setImageDrawable(getResources().getDrawable(R.drawable.love,null));
            currentMp3Info.setInLove(false);
            if(currentMp3Info instanceof MusicDownLoadInfo){
                love_ib.setClickable(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case REQUEST_PHONE_STATE:
                Toast.makeText(this,"申请权限失败，可能导致来电音乐无法暂停",Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_EXTERNAL_STORAGE:
                Toast.makeText(this,"申请权限失败！",Toast.LENGTH_SHORT).show();
                System.exit(0);
                break;
            case REQUEST_NET_STATE:
                Toast.makeText(this,"申请网络权限失败！",Toast.LENGTH_SHORT).show();
                System.exit(0);
                break;
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
        currentMp3Info.setInLove(true);
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
            case R.id.exit:
                exitApp();
                break;
            case R.id.clear_ache_bt:
                clearAche();
                break;
            case R.id.sche_stop_bt:
                scheButtonClick();
                break;
            case R.id.setting:
                super.startActivity(SettingActivity.class);
                break;
        }
    }
    private void clearAche(){
        LrcResovler.delAllLrc();
        BitMapUtil.defAllBitmapTemp();
        ((Button)findViewById(R.id.clear_ache_bt)).setText("清理缓存");
        Toast.makeText(this,"清除成功！",Toast.LENGTH_SHORT).show();
    }
}
