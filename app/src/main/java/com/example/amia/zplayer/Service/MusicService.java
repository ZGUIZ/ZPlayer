package com.example.amia.zplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.MusicPlayStatus;
import com.example.amia.zplayer.Receiver.EarringPutOutReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PhoneCallListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicService extends Service {
    private static MediaPlayer mediaPlayer;
    private Mp3Info curretnMp3Info;    //当前播放的音乐
    private List<Mp3Info> musiclist;   //当前播放的列表
    private boolean isPlaying;
    private EarringPutOutReceiver headsetrPlugReceiver;

    private static int connCount=0;   //记录连接数量

    protected MusicListDao musicListDao;
    protected MusicOfListDao musicOfListDao;

    private TelephonyManager manager;//电话管理
    private PhoneCallListener pcl;//电话事件监听器

    public MusicPlayStatus status;//播放模式

    private static ExecutorService executorService;  //线程池

    private final static String tranTarget="amia.musicplayer.action.MusicChange";
    private final static String currentPositionActionName="com.example.amia.musicplayer.currentPosition";
    private final static String currentPositionKey="currentPosition";

    private IBinder iBinder;

    public MusicService() {
        iBinder=new MusicPlayMangerEntity();

        musicListDao=new MusicListDao(this);
        musicOfListDao=new MusicOfListDao(this);
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MusicOnCompletionListener());
        mediaPlayer.setOnPreparedListener(new MusicOnPrepareListener());
        isPlaying=false;
        if(status==null){
            status=MusicPlayStatus.listloop;
        }

        //创建线程池
        try{
            executorService= Executors.newSingleThreadExecutor();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onCreate(){
        super.onCreate();
        registerPhoneCallReceiver();
        registerHeadsetPlugReceiver();
    }

    //注册耳机事件接收者
    private void registerHeadsetPlugReceiver(){
        headsetrPlugReceiver=new EarringPutOutReceiver(this);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetrPlugReceiver,intentFilter);
    }

    //注册电话事件监听器
    private void registerPhoneCallReceiver(){
        manager=(TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        pcl=new PhoneCallListener(this);
        manager.listen(pcl, PhoneStateListener.LISTEN_CALL_STATE);
    }

    //注销电话事件监听器
    private void unregisiterPhoneCallReceiver(){
        manager.listen(pcl, PhoneStateListener.LISTEN_NONE);
    }

    private void addConnection(){
        if(connCount<=0){
            sentCurrentTime();
        }
        connCount++;
        //Log.i("addConnection","connCount="+connCount);
    }

    private void subConnection(){
        connCount--;
        //Log.i("subConncetion","connCount="+connCount);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        //Log.i("MusicService","解除绑定");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        //Log.i("MusicServer","销毁：onDestory");
        executorService.shutdown();
        unregisterReceiver(headsetrPlugReceiver);
        mediaPlayer.stop();
        mediaPlayer.release();
        unregisiterPhoneCallReceiver();
        super.onDestroy();
    }

    private void playMusic(final List<Mp3Info> mp3Infos, final int position){
        final Mp3Info mp3Info=mp3Infos.get(position);
        musiclist=mp3Infos;
        playMusic(mp3Info);
        isPlaying=true;
    }

    private void playMusic(Mp3Info mp3Info){
        if(musiclist.indexOf(mp3Info)==-1){
            musiclist=new Vector<>();
            musiclist.add(mp3Info);
        }
        curretnMp3Info=mp3Info;
        String url=curretnMp3Info.getUrl();
        playMusic(url);
    }

    private void setPlayStatus(MusicPlayStatus status){
        this.status=status;
    }

    private MusicPlayStatus getPlayStatus(){
        return status;
    }

    private void playMusic(String url){
        try {
            File file=new File(url);
            //让广播进度的线程停止
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                mediaPlayer.seekTo(mediaPlayer.getDuration()-100);
                mediaPlayer.stop();
                isPlaying=false;
            }
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying=true;
            sentCurrentTime();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (IllegalStateException e){
            e.printStackTrace();
            playMusic(url);
        }

    }

    private void sentCurrentTime(){
        executorService.submit(new Runnable() {
            private Intent intent=new Intent();
            @Override
            public void run() {
                intent.setAction(currentPositionActionName);
                int time=0;
                int duration=mediaPlayer.getDuration();
                while(isPlaying()&&(duration-time)>500&&connCount>0){
                    time=mediaPlayer.getCurrentPosition();
                    //Log.i("Service","time="+time);
                    intent.removeExtra(currentPositionKey);
                    intent.putExtra(currentPositionKey,time);
                    sendBroadcast(intent);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Deprecated
    private void sentCrrentTime(){
        new Thread(new Runnable() {
            private Intent intent=new Intent();
            @Override
            public void run() {
                intent.setAction(currentPositionActionName);
                int time=0;
                int duration=mediaPlayer.getDuration();
                while(isPlaying()&&(duration-time)>500/*&&isScreenOn*/&&connCount>0){
                    time=mediaPlayer.getCurrentPosition();
                    //Log.i("Service","time="+time);
                    intent.removeExtra(currentPositionKey);
                    intent.putExtra(currentPositionKey,time);
                    sendBroadcast(intent);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void pauseMusic(){
        //Log.i("Service","暂停");
        mediaPlayer.pause();
        isPlaying=false;
    }

    private void setNullMusic(){
        curretnMp3Info=null;
    }

    public void resumePlay(){
        mediaPlayer.start();
        isPlaying=true;
        sentCurrentTime();
    }

    private void lastMusic(){
        int index = musiclist.indexOf(curretnMp3Info);
        int next = index - 1;
        if(next<0){
            next=next+musiclist.size();
        }
        playMusic(musiclist, next);
    }

    private void nextMusic(){
        int index = musiclist.indexOf(curretnMp3Info);
        int next = (index + 1) % musiclist.size();
        playMusic(musiclist, next);
    }

    private void addToPlayedDatabase(){
        Mp3Info mp3Info=getCurretnMp3Info();
        int list_id=musicListDao.getList_id("最近播放");
        musicOfListDao.deleteMusicOfList(list_id,(int)mp3Info.getId());
        musicOfListDao.insertMusic(list_id,(int)mp3Info.getId());
    }

    private void addToNext(Mp3Info mp3Info){
        if(musiclist!=null&&musiclist.size()!=0){
            if(curretnMp3Info!=null){
                int position=musiclist.indexOf(curretnMp3Info);
                musiclist.add(position+1,mp3Info);
            }
        }
        else{
            if(musiclist==null){
                musiclist=new ArrayList<>();
            }
            musiclist.add(mp3Info);
            playMusic(musiclist,0);
        }
    }

    private void stopMusic(){
        mediaPlayer.stop();
        isPlaying=false;
        setNullMusic();
    }

    private int getCurrentTime(){
        return mediaPlayer.getCurrentPosition();
    }

    private Mp3Info getCurretnMp3Info(){
        return curretnMp3Info;
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private void setCurrentPosition(int currentPosition){
        mediaPlayer.seekTo(currentPosition);
    }

    class MusicPlayMangerEntity extends Binder implements MusicPlayManager {

        @Override
        public void playMusic(List<Mp3Info> mp3Infos, int position) {
            MusicService.this.playMusic(mp3Infos,position);
        }

        @Override
        public void playMusic(Mp3Info mp3Info) {
            MusicService.this.playMusic(mp3Info);
        }

        @Override
        public void pauseMusic() {
            MusicService.this.pauseMusic();
        }

        @Override
        public void resumePlay() {
            MusicService.this.resumePlay();
        }

        @Override
        public int getCurrentPosition() {
            return getCurrentTime();
        }

        @Override
        public Mp3Info getCurretnMp3Info() {
            return MusicService.this.getCurretnMp3Info();
        }

        @Override
        public boolean isPlaying() {
            return MusicService.this.isPlaying();
        }

        @Override
        public void setPlaying(boolean playing) {
            MusicService.this.setPlaying(playing);
        }

        @Override
        public void setCurrentPosition(int currentPosition) {
            MusicService.this.setCurrentPosition(currentPosition);
        }

        @Override
        public void setMusicList(ArrayList<Mp3Info> mp3Infos) {
            musiclist=mp3Infos;
        }

        @Override
        public void nextMusic() {
            MusicService.this.nextMusic();
        }

        @Override
        public void lastMusic() {
            MusicService.this.lastMusic();
        }

        @Override
        public List<Mp3Info> getMusicList() {
            return musiclist;
        }

        @Override
        public void setStatus(MusicPlayStatus status) {
            MusicService.this.setPlayStatus(status);
        }

        @Override
        public MusicPlayStatus getStatus() {
            return MusicService.this.getPlayStatus();
        }

        @Override
        public void addToNext(Mp3Info mp3Info) {
            MusicService.this.addToNext(mp3Info);
        }

        @Override
        public void stopMusic() {
            MusicService.this.stopMusic();
        }

        @Override
        public void addConnection() {
            MusicService.this.addConnection();
        }

        @Override
        public void subConnection() {
            MusicService.this.subConnection();
        }

    }

    class MusicOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            //Log.d("onCompletion", "播放结束");
            isPlaying=false;
            if (curretnMp3Info != null) {
                switch (status){
                    case singleloop:
                        playMusic(curretnMp3Info);
                        break;
                    case listloop:
                        int index = musiclist.indexOf(curretnMp3Info);
                        //Log.i("MusicService","index="+index);
                        int next = (index + 1) % musiclist.size();
                        playMusic(musiclist, next);
                        break;
                    case sequence:
                    default:
                        int i = musiclist.indexOf(curretnMp3Info);
                        int n = i + 1;
                        if(n<musiclist.size()) {
                            playMusic(musiclist, n);
                        }
                        else{
                            mediaPlayer.stop();
                        }
                        break;
                }
            }
        }
    }

    class MusicOnPrepareListener implements MediaPlayer.OnPreparedListener{
        Intent intent =new Intent();
        @Override
        public void onPrepared(MediaPlayer mp) {
            //Log.i("MusicService","发送数据");
            if(connCount<=0){
                return;
            }
            intent.setAction(tranTarget);
            intent.putExtra("mp3Info",curretnMp3Info);
            sendBroadcast(intent);
            addToPlayedDatabase();
        }
    }

}
