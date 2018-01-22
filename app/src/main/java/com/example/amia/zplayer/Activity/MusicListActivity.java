package com.example.amia.zplayer.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.ControlUtil.AddToListListener;
import com.example.amia.zplayer.ControlUtil.MusicListAcitvityUtils;
import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicList;
import com.example.amia.zplayer.DTO.MusicOfList;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.Receiver.CurrentPositionReceiver;
import com.example.amia.zplayer.Receiver.MusicInfoReceiver;
import com.example.amia.zplayer.Receiver.MusicPlayManager;
import com.example.amia.zplayer.Receiver.PauseMusicReceiver;
import com.example.amia.zplayer.Service.MusicService;
import com.example.amia.zplayer.util.ChangelateUtil;
import com.example.amia.zplayer.util.MusicResolverUtil;

import java.util.ArrayList;

public class MusicListActivity extends MusicAboutActivity implements View.OnClickListener {

    public static final String musiclistKey="mp3Infos";
    public static final String listnameKey="listname";
    private static final int msg_setadapter=0;
    private static final int msg_setinfo=1;
    private static final int request_del_code=101;

    protected MusicListDao musicListDao;
    protected MusicOfListDao musicOfListDao;

    protected int list_id;
    private boolean isAfLoCl;
    private  ArrayList<Mp3Info> checkedMus=new ArrayList<>();  //被勾选的音乐

    private ListView music_list;                //音乐列表组件
    private ArrayList<Mp3Info> mp3Infos;        //音乐信息列表
    private ImageButton love_ib;                //添加到喜欢列表按钮
    private ImageButton pausebutton;                 //暂停按钮
    private ProgressBar progressBar;            //进度条
    private static boolean isplay=false;        //正在播放
    private TextView list_title;                //列表名称组件
    private TextView title_tv;                  //音乐名称组件
    private TextView artist_tv;                 //演唱者名称组件
    private ImageView music_album;              //专辑图片组件
    private ImageButton backButton;             //返回按钮
    private TextView del_tv;                    //删除文本框

    private RelativeLayout bottom_tool_layout;  //长按后底部控制布局
    private LinearLayout play_layout;  //长按播放按钮布局
    private LinearLayout add_layout;   //长按添加按钮布局
    private LinearLayout del_layout;   //长按删除按钮布局
    private LinearLayout share_layout; //长按分享按钮布局
    private TextView cancel_tv;        //长按取消按钮
    private RelativeLayout all_sel_ll;   //全选布局管理器
    private TextView all_select_tv;     //全选
    private TextView all_cancle;     //全取消

    private LinearLayout bottom_layout;

    private MusListAdapter musListAdapter;      //音乐列表适配器

    private static PauseMusicReceiver pauseMusicReceiver;  //服务暂停事件接收者
    private static CurrentPositionReceiver currentPositionReceiver;  //播放进度接收者
    private static MusicServiceConnection musicServiceConnection;   //服务连接器
    private static MusInfoRec musInfoRec;          //音乐信息接收者

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        Intent intent=getIntent();
        list_id=intent.getIntExtra("list_id",-1);
        isAfLoCl=false;
        //启动服务
        Intent intent1=new Intent(this,MusicService.class);
        startService(intent1);
        init(intent);
        resolveMusicThread.start();
    }

    private void init(Intent intent){
        musicResolverUtil=new MusicResolverUtil(this);
        musicOfListDao=new MusicOfListDao(this);
        musicListDao=new MusicListDao(this);

        list_title=findViewById(R.id.list_name);
        list_title.setText(intent.getStringExtra(listnameKey));

        backButton=findViewById(R.id.backbutton);
        backButton.setOnClickListener(this);
        love_ib=findViewById(R.id.love_ib);
        love_ib.setOnClickListener(this);

        pausebutton=findViewById(R.id.startpause);
        pausebutton.setOnClickListener(this);
        title_tv=findViewById(R.id.music_title);
        title_tv.setSingleLine(true);
        title_tv.setEllipsize(TextUtils.TruncateAt.END);
        artist_tv=findViewById(R.id.music_artist);
        artist_tv.setSingleLine(true);
        artist_tv.setEllipsize(TextUtils.TruncateAt.END);
        music_album=findViewById(R.id.music_album);
        bottom_layout=findViewById(R.id.con_bar);
        bottom_layout.setOnClickListener(this);
        progressBar=findViewById(R.id.progressbar);
        music_list=findViewById(R.id.music_list);

        bottom_tool_layout=findViewById(R.id.bottom_tool_bar);

        play_layout=findViewById(R.id.next_ll);
        add_layout=findViewById(R.id.add_ll);
        del_layout=findViewById(R.id.del_ll);
        share_layout=findViewById(R.id.share_ll);
        play_layout.setOnClickListener(this);
        add_layout.setOnClickListener(this);
        del_layout.setOnClickListener(this);
        share_layout.setOnClickListener(this);

        cancel_tv=findViewById(R.id.cancle_tv);
        cancel_tv.setOnClickListener(this);
        all_sel_ll=findViewById(R.id.all_select);
        all_select_tv=findViewById(R.id.all_select_tv);
        all_cancle=findViewById(R.id.all_cancel);
        all_select_tv.setOnClickListener(this);
        all_cancle.setOnClickListener(this);

        del_tv=findViewById(R.id.del_tv);
        if(list_id==-1){
            del_tv.setText("删除");
        }
        else{
            del_tv.setText("移除");
        }
    }

    public void setListAdpter(){
        musListAdapter=new MusListAdapter();
        music_list.setDivider(null);
        music_list.setAdapter(musListAdapter);
        music_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAfLoCl){
                    return false;
                }
                isAfLoCl=true;
                Mp3Info mp3Info=mp3Infos.get(i);
                mp3Info.setSelected(true);
                checkedMus.add(mp3Info);
                musListAdapter.notifyDataSetChanged();
                bottom_layout.setVisibility(View.INVISIBLE);
                bottom_tool_layout.setVisibility(View.VISIBLE);
                all_sel_ll.setVisibility(View.VISIBLE);
                cancel_tv.setVisibility(View.VISIBLE);
                return true;
            }
        });
        music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                play((Mp3Info) musListAdapter.getItem(position),position);
            }
        });
    }

    private void play(Mp3Info mp3Info,int position){
        progressBar.setMax((int)mp3Info.getDuration());
        progressBar.setProgress(0);
        musicPlayManager.playMusic(mp3Infos,position);
        isplay=true;
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        musListAdapter.notifyDataSetChanged();
    }

    private void play(ArrayList<Mp3Info> mp3Infos){
        Mp3Info mp3Info=mp3Infos.get(0);
        progressBar.setMax((int)mp3Info.getDuration());
        progressBar.setProgress(0);
        musicPlayManager.playMusic(mp3Infos,0);
        isplay=true;
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        musListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backbutton:    //返回按钮
                MusicListActivity.this.finish();
                break;
            case R.id.startpause:   //暂停播放按钮
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
            case R.id.con_bar:
                //打开播放界面
                startPlayintActivity();
                break;
            case R.id.next_ll:
                playAfLongPress();
                break;
            case R.id.add_ll:
                addTo();
                break;
            case R.id.del_ll:
                delMusic();
                break;
            case R.id.share_ll:
                shareMusic();
                break;
            case R.id.cancle_tv:
                cancel_LongPress();
                break;
            case R.id.all_select_tv:
                selectAll();
                break;
            case R.id.all_cancel:
                cancelAll();
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

    private void startPlayintActivity(){
        Intent intent=new Intent(this,PlayingActivity.class);
        startActivity(intent);
    }

    private void addToLove(){
        if(currentMp3Info==null){
            Toast.makeText(this,"无播放的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        currentMp3Info.setInLove(true);
        int love_id=musicListDao.getList_id("我喜欢");
        MusicListAcitvityUtils.addSingleMusicToList(musicOfListDao,love_id,(int)currentMp3Info.getId());
        love_ib.setImageDrawable(getResources().getDrawable(R.drawable.loved,null));
        if(love_id==list_id){
            mp3Infos.add(currentMp3Info);
            currentMp3Info.setInLove(true);
            musListAdapter.notifyDataSetChanged();
        }
    }

    private void shareMusic(){
        MusicListAcitvityUtils.shareMusic(this,checkedMus);
        cancel_LongPress();
    }

    private void removeFromLove(){
        if (currentMp3Info==null){
            Toast.makeText(this,"无播放的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        int love_id=musicListDao.getList_id("我喜欢");
        musicOfListDao.deleteMusicOfList(love_id,(int)currentMp3Info.getId());
        currentMp3Info.setInLove(false);
        love_ib.setImageDrawable(getResources().getDrawable(R.drawable.love,null));
        if(love_id==list_id){
            for(int i=0;i<mp3Infos.size();i++){
                if(mp3Infos.get(i).getId()==currentMp3Info.getId()){
                    mp3Infos.remove(i);
                    break;
                }
            }
            musListAdapter.notifyDataSetChanged();
        }
    }

    //继续播放音乐
    private void continuePlayMusic(){
        musicPlayManager.resumePlay();
        isplay=true;
        musicPlayManager.setPlaying(isplay);
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
    }

    //长按后播放
    private void playAfLongPress(){
        if(checkedMus.size()<=0){
            Toast.makeText(this,"没有选中的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        play(MusicListAcitvityUtils.clone_list(checkedMus));
        cancel_LongPress();
    }

    private void addTo(){
        MusicListDao dao=new MusicListDao(this);
        ArrayList<MusicList> lists=dao.queryAllList();
        AddToListListener listener=new AddToListListener(this,checkedMus,lists){

            @Override
            protected void cancelSelect() {
                MusicListActivity.this.cancel_LongPress();
                setCurrentMusicInfo(currentMp3Info);
            }
        };
        AlertDialog.Builder builder=MusicListAcitvityUtils.createAddDialog(this,listener,lists);
        builder.show();
    }

    private void delMusic(){
        if(checkedMus.size()<=0){
            Toast.makeText(this,"没有选中的音乐！",Toast.LENGTH_SHORT).show();
            return;
        }
        if(list_id!=-1){
            MusicListAcitvityUtils.removeFromList(this,list_id,mp3Infos,checkedMus);
            musListAdapter.notifyDataSetChanged();
            cancel_LongPress();
        }
        else{
            //弹出确认对话框
            Intent intent=new Intent(this,ConfimDelActivity.class);
            intent.putExtra("MusSize",checkedMus.size());
            startActivityForResult(intent,request_del_code);
        }
    }
    //判断是否删除
    private void judle_del(boolean result){
        if(!result) {
            cancel_LongPress();
            return;
        }
        if (currentMp3Info!=null) {
            boolean flag=false;
            for(Mp3Info mp3Info:checkedMus){
                if(mp3Info.getId()==currentMp3Info.getId()){
                    flag=true;
                    break;
                }
            }
            if(flag) {
                if (mp3Infos.size() == checkedMus.size()) {
                    PauseMusic();
                    isplay=false;
                    musicPlayManager.stopMusic();
                    currentMp3Info=null;
                } else {
                    int index=0;
                    while(currentMp3Info.getId()!=mp3Infos.get(index).getId()){
                        index++;
                    }
                    Mp3Info info=mp3Infos.get((++index)%mp3Infos.size());
                    while(info.isSelected()) {
                        index = (index+1) % mp3Infos.size();
                        info=mp3Infos.get(index);
                    }
                    musicPlayManager.playMusic(mp3Infos, index);
                }
            }
        }
        MusicListAcitvityUtils.deleteMusicFromDisk(this, checkedMus, mp3Infos);
        musListAdapter.notifyDataSetChanged();
        musicPlayManager.setMusicList(mp3Infos);
        cancel_LongPress();
    }

    //全选按钮
    private void selectAll(){
        checkedMus.clear();
        for(Mp3Info info:mp3Infos){
            info.setSelected(true);
            checkedMus.add(info);
        }
        all_cancle.setVisibility(View.VISIBLE);
        all_select_tv.setVisibility(View.GONE);
        musListAdapter.notifyDataSetChanged();
    }

    //全取消
    private void cancelAll(){
        for(Mp3Info info:checkedMus){
            info.setSelected(false);
        }
        checkedMus.clear();
        all_cancle.setVisibility(View.GONE);
        all_select_tv.setVisibility(View.VISIBLE);
        musListAdapter.notifyDataSetChanged();
    }

    @Override
    void PauseMusic() {
        isplay=false;
        musicPlayManager.pauseMusic();
        PauseMusicFromService();
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
        super.onStart();
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

    //绑定服务
    private void onBindMusicService(){
        musicServiceConnection=new MusicServiceConnection();
        Intent intent=new Intent(this, MusicService.class);
        bindService(intent,musicServiceConnection,BIND_AUTO_CREATE);
    }

    //注册耳机事件监听者
    private void registerHeadsetPlugReceiver(){
        pauseMusicReceiver=new PauseMusicReceiver(this);
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.amia.musicplayer.musicservicepause");
        registerReceiver(pauseMusicReceiver,filter);
    }

    //注册播放进度接收者
    private void registerCurrentPositionReceiver(){
        currentPositionReceiver=new CurrentPositionReceiver() {
            @Override
            public void setProgressBar(int currentPosition) {
                progressBar.setProgress(currentPosition);
            }
        };
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(CurrentPositionReceiver.currentPositionActionName);
        this.registerReceiver(currentPositionReceiver,intentFilter);
    }

    //注册音乐信息接收者
    private void registerMusicrInfoReceiver(){
        musInfoRec=new MusInfoRec();
        IntentFilter filter=new IntentFilter();
        filter.addAction("amia.musicplayer.action.MusicChange");
        this.registerReceiver(musInfoRec,filter);
    }

    @Override
    public void PauseMusicFromService() {
        isplay=false;
        pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
    }

    @Override
    public void setCurrentMusicInfo(Mp3Info info) {
        if(info==null){
            return;
        }
        currentMp3Info=info;
        Bitmap bitmap=null;
        if(bitMap!=null){
            bitmap=bitMap.get(String.valueOf(info.getId()));
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
        setPausebuttonIcon();
        if(musListAdapter!=null&&musListAdapter.getCount()>0) {
            musListAdapter.notifyDataSetChanged();
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

    private void setPausebuttonIcon(){
        if(isplay){
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
        }
        else{
            pausebutton.setImageDrawable(getResources().getDrawable(R.drawable.play,null));
        }
    }

    @Override
    public void onBackPressed(){
        if(isAfLoCl){
           cancel_LongPress();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode!=RESULT_OK)
            return;
        switch (requestCode){
            case request_del_code:
                judle_del(data.getBooleanExtra("result",false));
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void cancel_LongPress(){
        backButton.setVisibility(View.VISIBLE);
        bottom_layout.setVisibility(View.VISIBLE);
        bottom_tool_layout.setVisibility(View.GONE);
        all_select_tv.setVisibility(View.VISIBLE);
        all_cancle.setVisibility(View.GONE);
        all_sel_ll.setVisibility(View.GONE);
        cancel_tv.setVisibility(View.GONE);
        isAfLoCl=false;
        musListAdapter.notifyDataSetChanged();
        for(Mp3Info info:checkedMus){
            info.setSelected(false);
        }
        checkedMus.clear();
    }

    /**
     * 解析该列表音乐的线程
     */
    Thread resolveMusicThread=new Thread(new Runnable() {
        @Override
        public void run() {
            MusicListDao musicListDao=new MusicListDao(MusicListActivity.this);
            if(list_id==-1){
                mp3Infos=musicResolverUtil.getAllMp3Infos();
            }
            else {
                ArrayList<MusicOfList> arrayList;
                if(musicListDao.getList_id("最近播放")==list_id){
                    arrayList = musicOfListDao.queryMusicByList(list_id, "desc");
                }
                else {
                    arrayList = musicOfListDao.queryMusicByList(list_id, "asc");
                }
                mp3Infos = musicResolverUtil.getMp3InfoById(arrayList);
            }
            bitMap = musicResolverUtil.getAllAlbum(mp3Infos);
            Message msg=list_handler.obtainMessage();
            msg.what=msg_setadapter;
            list_handler.sendMessage(msg);
        }
    });

    Handler list_handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case msg_setadapter:
                    setListAdpter();
                    (MusicListActivity.this.findViewById(R.id.list_progress)).setVisibility(View.GONE);//加载进度条消失
                    break;
                case msg_setinfo:
                    setCurrentMusicInfo(currentMp3Info);
                    break;
            }
        }
    };

    class MusListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mp3Infos.size();
        }

        @Override
        public Object getItem(int i) {
            return mp3Infos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            ViewHolder holder=null;
            if(convertview==null){
                convertview=View.inflate(MusicListActivity.this, R.layout.music_item_layout,null);
                holder=new ViewHolder();
                holder.divLine=convertview.findViewById(R.id.mus_list_div);
                holder.album_iv=convertview.findViewById(R.id.photo);
                holder.titile_tv=convertview.findViewById(R.id.title_tv);
                holder.titile_tv.setSingleLine(true);
                holder.titile_tv.setEllipsize(TextUtils.TruncateAt.END);
                holder.artist_tv=convertview.findViewById(R.id.Artist);
                holder.artist_tv.setSingleLine(true);
                holder.artist_tv.setEllipsize(TextUtils.TruncateAt.END);
                holder.time=convertview.findViewById(R.id.duration);
                holder.isSelect=convertview.findViewById(R.id.music_check);
                holder.listener=new MyClickListener(i);
                holder.isSelect.setOnClickListener(holder.listener);
                holder.playing=convertview.findViewById(R.id.isplaying);
                convertview.setTag(holder);
            }
            else{
                holder=(ViewHolder) convertview.getTag();
                holder.listener.setPosition(i);
            }
            if(i==0){
                holder.divLine.setVisibility(View.GONE);
            }
            else{
                holder.divLine.setVisibility(View.VISIBLE);
            }
            if(currentMp3Info!=null&&currentMp3Info.getId()==mp3Infos.get(i).getId()){
                holder.playing.setVisibility(View.VISIBLE);
                holder.titile_tv.setTextColor(getResources().getColor(R.color.playingItem));
                holder.artist_tv.setTextColor(getResources().getColor(R.color.playingItem));
                holder.time.setTextColor(getResources().getColor(R.color.playingItem));
            }
            else{
                holder.playing.setVisibility(View.GONE);
                holder.titile_tv.setTextColor(getResources().getColor(R.color.unplayItem));
                holder.artist_tv.setTextColor(getResources().getColor(R.color.unplayItem));
                holder.time.setTextColor(getResources().getColor(R.color.unplayItem));
            }
            Mp3Info mp3Info=mp3Infos.get(i);
            holder.titile_tv.setText(mp3Info.getTitle());
            holder.artist_tv.setText(mp3Info.getArtist());
            holder.time.setText(ChangelateUtil.calTime(mp3Info.getDuration()));
            Bitmap icon=bitMap.get(String.valueOf(mp3Info.getId()));
            if(icon!=null){
                holder.album_iv.setImageBitmap(icon);
            }
            else{
                if(currentMp3Info==mp3Infos.get(i)){
                    holder.playing.setVisibility(View.GONE);
                }
                holder.album_iv.setImageResource(R.drawable.defaluticon);
            }
            //判断是否长按之后，是否显示复选框
            if(isAfLoCl){
                holder.isSelect.setVisibility(View.VISIBLE);
                holder.playing.setVisibility(View.GONE);
                //设置复选框状态
                holder.isSelect.setChecked(mp3Infos.get(i).isSelected());
                backButton.setVisibility(View.GONE);
            }
            else{
                holder.isSelect.setVisibility(View.GONE);
            }
            return convertview;
        }
    }

    static class ViewHolder{
        View divLine;
        ImageView album_iv;
        TextView titile_tv;
        TextView artist_tv;
        TextView time;
        CheckBox isSelect;
        ImageView playing;
        MyClickListener listener;   //点击事件监听器
    }

    /**
     * 列表复选框点击事件监听者
     */
    private class MyClickListener implements View.OnClickListener{

        private int position;
        public MyClickListener(int position){
            this.position=position;
        }

        public void setPosition(int position){
            this.position=position;
        }

        @Override
        public void onClick(View view) {
            MusicListAcitvityUtils.list_Check_Single(position,mp3Infos,checkedMus);
        }
    }

    private class MusicServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayManager=(MusicPlayManager)iBinder;
            musicPlayManager.addConnection();
            isplay=musicPlayManager.isPlaying();
            setPausebuttonIcon();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private class MusInfoRec extends MusicInfoReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            final Mp3Info mp3Info=(Mp3Info)intent.getSerializableExtra("mp3Info");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (bitMap == null) {
                            Thread.sleep(200);
                        }
                        currentMp3Info=mp3Info;
                        Message msg=handler.obtainMessage();
                        msg.what=msg_setinfo;
                        list_handler.sendMessage(msg);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}