package com.example.amia.zplayer.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.MusicPlayStatus;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.EarringPutOutReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PhoneCallListener;
import com.example.amia.zplayer.util.RandomIdUtil;
import com.example.amia.zplayer.util.XmlReadWriteUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicService extends Service {
    private static MediaPlayer mediaPlayer;
    private Mp3Info curretnMp3Info;    //当前播放的音乐
    private List<Mp3Info> musiclist;   //当前播放的列表
    private boolean isPlaying;         //是否正在播放
    private EarringPutOutReceiver headsetrPlugReceiver;
    private static int sche_time;

    private static int connCount=0;   //记录连接数量
    private static boolean isFirstOpen;
    private static int lastProgress;

    protected MusicListDao musicListDao;
    protected MusicOfListDao musicOfListDao;

    private TelephonyManager manager;//电话管理
    private PhoneCallListener pcl;//电话事件监听器

    public MusicPlayStatus status;//播放模式

    private static ExecutorService executorService;  //线程池
    private ExecutorService schemeStopPool;         //定时停止线程池

    private final static String tranTarget="amia.musicplayer.action.MusicChange";
    private final static String currentPositionActionName="com.example.amia.musicplayer.currentPosition";
    private final static String currentPositionKey="currentPosition";
    public final static String scheSotpKey="lastTimeToStop";

    private IBinder iBinder;

    public MusicService() {
        iBinder=new MusicPlayMangerEntity();
        isFirstOpen=true;

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
            schemeStopPool=Executors.newCachedThreadPool();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        sche_time=-1;
    }

    public void onCreate(){
        super.onCreate();
        registerPhoneCallReceiver();
        registerHeadsetPlugReceiver();
        readLastProgress();
    }

    private void readLastProgress(){
        XmlReadWriteUtil util=new XmlReadWriteUtil(this);
        Object[] res=util.readMusicList();
        musiclist= (List<Mp3Info>) res[0];
        if(musiclist.size()==0){
            return;
        }
        curretnMp3Info=musiclist.get((int)res[1]);
        lastProgress=(int)res[2];
        playAndPauseTo();
    }

    private void playAndPauseTo(){
        if(curretnMp3Info==null){
            isFirstOpen=false;
            return;
        }
        playerAndPauseTo(curretnMp3Info,lastProgress);
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
        if(connCount<=0&&isPlaying){
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
        mediaPlayer.pause();
        executorService.shutdown();
        schemeStopPool.shutdown();
        unregisterReceiver(headsetrPlugReceiver);
        saveProgress();
        mediaPlayer.stop();
        mediaPlayer.release();
        unregisiterPhoneCallReceiver();
        super.onDestroy();
    }

    /**
     * 保存播放进度
     */
    private void saveProgress(){
        if(musiclist==null||musiclist.isEmpty()||curretnMp3Info==null){
            return;
        }
        XmlReadWriteUtil util=new XmlReadWriteUtil(this);
        util.writeMusicList(musiclist,musiclist.indexOf(curretnMp3Info),mediaPlayer.getCurrentPosition());
    }

    private void playerAndPauseTo(Mp3Info mp3Info,int currentTime){
        if(mp3Info instanceof MusicDownLoadInfo){
            playAndPauseTo((MusicDownLoadInfo)mp3Info);
        }
        else {
            playAndPauseTo(mp3Info.getUrl(), currentTime);
        }
    }

    private void playAndPauseTo(String url, final int currentTime){
        try {
            File file=new File(url);
            Log.i("Service",url);
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (IllegalStateException e){
            e.printStackTrace();
            playAndPauseTo(url,currentTime);
        }
    }

    private void playAndPauseTo(MusicDownLoadInfo info){
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource("http://" + getResources().getString(R.string.down_host) + info.getNetUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(lastProgress);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void playMusic(final List<Mp3Info> mp3Infos, final int position){
        final Mp3Info mp3Info=mp3Infos.get(position);
        musiclist=mp3Infos;
        playMusic(mp3Info);
        isPlaying=true;
    }

    private void playMusic(Mp3Info mp3Info){
        isFirstOpen=false;
        if(musiclist.indexOf(mp3Info)==-1){
            musiclist=new Vector<>();
            musiclist.add(mp3Info);
        }
        curretnMp3Info=mp3Info;
        String url=curretnMp3Info.getUrl();
        if(mp3Info instanceof MusicDownLoadInfo){
            playNetMusic((MusicDownLoadInfo)mp3Info);
        }
        else {
            playMusic(url);
        }
    }

    private void playNetMusic(MusicDownLoadInfo info){
        isPlaying=true;
        sendMp3Info();
        try{
            if(mediaPlayer.isPlaying()){
                isPlaying=false;
                mediaPlayer.pause();
                mediaPlayer.seekTo(mediaPlayer.getDuration()-100);
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource("http://"+getResources().getString(R.string.down_host)+info.getNetUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    isPlaying=true;
                    mediaPlayer.start();
                    curretnMp3Info.setDuration(mediaPlayer.getDuration());
                    sendMp3Info();
                    sentCurrentTime();
                }
            });
        }
        catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"网络异常！",Toast.LENGTH_SHORT).show();
            playNetMusic(info);
        }
    }

    private void sendMp3Info(){
        Intent intent=new Intent();
        intent.setAction(tranTarget);
        intent.putExtra("mp3Info",curretnMp3Info);
        sendBroadcast(intent);
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
                isPlaying=false;
                mediaPlayer.pause();
                mediaPlayer.seekTo(mediaPlayer.getDuration()-100);
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying=true;
            sentCurrentTime();
            addToPlayedDatabase();  //添加到数据库
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
        //没有播放的时候没有必要发送广播
        if(!isPlaying){
            return;
        }

        executorService.submit(new Runnable() {
            private Intent intent=new Intent();
            @Override
            public void run() {
                intent.setAction(currentPositionActionName);
                int time=0;
                int duration=(int)curretnMp3Info.getDuration();
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

    public void pauseMusic(){
        //Log.i("Service","暂停");
        mediaPlayer.pause();
        isPlaying=false;
    }

    private void setNullMusic(){
        curretnMp3Info=null;
    }

    public void resumePlay(){
        isFirstOpen=false;
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
        if(musiclist==null){
            musiclist=new ArrayList<>();
        }
        if(mp3Info instanceof MusicDownLoadInfo){
            int set=MusicDownLoadInfo.isInList((MusicDownLoadInfo) mp3Info,musiclist);
            if(set!=-1){
                musiclist.remove(set);
            }
            if(mp3Info.getId()==0||mp3Info.getId()==-1){
                mp3Info.setId(RandomIdUtil.getRandomId(musiclist));
            }
        }
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

    private int getMusicLength(){
        if(!isPlaying&&curretnMp3Info!=null){
            return (int)curretnMp3Info.getDuration();
        }
        if(!isPlaying){
            return 0;
        }
        return mediaPlayer.getDuration();
    }

    private void removeMusic(List<Mp3Info> removeList){
        for(Mp3Info rinfo:removeList){
            Mp3Info temp=curretnMp3Info;
            if(curretnMp3Info.getId()==rinfo.getId()){   //如果当前播放的音乐即在移除的列表中
                if(musiclist.size()>1){    //如果没有其他音乐
                    nextMusic();
                }
                else{
                    stopMusic();
                }
                musiclist.remove(temp);
                continue;
            }
            else{   //如果当前播放的音乐不在移除列表中，则遍历移除
                for(int i=0;i<musiclist.size();i++){
                    Mp3Info info=musiclist.get(i);
                    if(rinfo.getId()==info.getId()){
                        musiclist.remove(info);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 设置定时播放时间
     * @param minute
     */
    private void setStopTime(int minute){
        final LocalBroadcastManager manager=LocalBroadcastManager.getInstance(this);
        final Intent intent=new Intent();
        intent.setAction(scheSotpKey);
        if(sche_time<=-1){
            sche_time=minute*60;
            schemeStopPool.submit(new Runnable() {
                @Override
                public void run() {
                    while (sche_time >= -1) {
                        //发送广播
                        intent.putExtra(scheSotpKey,sche_time);
                        manager.sendBroadcast(intent);
                        if (sche_time == 0) {
                            pauseMusic();
                            sche_time--;
                            return;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sche_time--;
                    }
                }
            });
        }
        else{
            sche_time=minute;
        }
    }

    /**
     * 停止定时播放
     */
    private void stopScheStop(){
        sche_time=-1;
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

        @Override
        public void removeMusic(List<Mp3Info> removeList) {
            MusicService.this.removeMusic(removeList);
        }

        @Override
        public void setStopTime(int minute) {
            MusicService.this.setStopTime(minute);
        }

        @Override
        public void stopScheStop() {
            MusicService.this.stopScheStop();
        }

    }

    class MusicOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            //Log.d("onCompletion", "播放结束");
            if(isFirstOpen){
                playAndPauseTo();
                return;
            }
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
                    case ramdom:
                        Random random=new Random();
                        int ne=random.nextInt(musiclist.size())%musiclist.size();
                        playMusic(musiclist,ne);
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
            if(isFirstOpen){
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        if(isFirstOpen){
                            isFirstOpen=false;
                            mediaPlayer.pause();
                        }
                    }
                });
                Intent progressIntent=new Intent();
                progressIntent.setAction(currentPositionActionName);
                progressIntent.removeExtra(currentPositionKey);
                progressIntent.putExtra(currentPositionKey,lastProgress);
                sendBroadcast(progressIntent);
                if(curretnMp3Info instanceof MusicDownLoadInfo){
                    return;
                }
                mediaPlayer.start();
                mediaPlayer.pause();
                mediaPlayer.seekTo(lastProgress);
            }
            if(connCount<=0){
                return;
            }
            intent.setAction(tranTarget);
            intent.putExtra("mp3Info",curretnMp3Info);
            sendBroadcast(intent);
        }
    }

}
