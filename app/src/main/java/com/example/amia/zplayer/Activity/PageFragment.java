package com.example.amia.zplayer.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.MusicClassify;
import com.example.amia.zplayer.DTO.MusicList;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.util.BitMapUtil;
import com.example.amia.zplayer.util.JsonResolveUtils;
import com.example.amia.zplayer.util.NetUtils;
import com.example.amia.zplayer.util.WindowInfoMananger;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amia on 2017/8/22.
 */

public class PageFragment extends Fragment implements AdapterView.OnItemClickListener{

    private static final int SET_FIRST_FRAGEMENT=0;

    protected int divide;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Activity activity;

    protected MusicOfListDao musicOfListDao;//数据库处理类
    protected MusicListDao musicListDao;//数据库处理类
    protected ListView dynamic_lsit;
    protected ArrayList<MusicList> list_array;//存储列表

    protected ArrayList<MusicList> musicLists;  //音乐列表List
    protected List<Object> classifyList=new ArrayList<>();  //音乐类别List
    protected ClassifyAdapter adapter;

    protected View firstPageView;
    protected View secondPageView;

    private MusiclistListItemAdapter dynamic_item_adapter;

    public PageFragment(){
    }

    public static PageFragment newInstance(int sectionNumber, Activity act){
        activity=act;
        PageFragment pageFragment=new PageFragment();
        pageFragment.divide=sectionNumber;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){
        View view=null;
        switch (divide){
            case 0:
                view=layoutInflater.inflate(R.layout.web_music_pager,container,false);
                firstPageView=view;
                onCreateFirstView(view);
                break;
            case 1:
                view=layoutInflater.inflate(R.layout.my_list_pager,container,false);
                onCreateSecondView(view);
                secondPageView=view;
                break;
        }
        return view;
    }

    private void onCreateFirstView(View view){
        RecyclerView recyclerView=view.findViewById(R.id.classify_rv);
        GridLayoutManager manager=new GridLayoutManager(activity,3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new ClassifyAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MusicClassify classify= (MusicClassify) classifyList.get(position);
                Intent intent=new Intent(activity,NetMusListActivity.class);
                intent.putExtra("classify",classify);
                Bitmap bitmap=(view.findViewById(R.id.classify_icon)).getDrawingCache();
                intent.putExtra("bitmap",BitMapUtil.bitmapToByte(bitmap));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                String res;
                try {
                    res= NetUtils.requestDataFromNet(getResources().getString(R.string.findAllClassify));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    res=null;
                }
                Message msg=handler.obtainMessage();
                Bundle bundle=new Bundle();
                bundle.putString("result",res);
                msg.setData(bundle);
                msg.what=SET_FIRST_FRAGEMENT;
                handler.sendMessage(msg);
            }
        }).start();
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SET_FIRST_FRAGEMENT:
                    Bundle bundle=msg.getData();
                    setFirstFragement(bundle.getString("result"));
                    break;
            }
        }
    };

    protected void setFirstFragement(String result){
        if(result==null||result==""){
            Toast.makeText(activity,"网络连接异常！",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            classifyList= JsonResolveUtils.resolveJson(result, MusicClassify.class);
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity,"网络连接异常！",Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void onCreateSecondView(View view){
        ListView static_list=view.findViewById(R.id.music_list_list);
        musicListDao=new MusicListDao(activity);
        MusiclistListItemAdapter static_item_adapter;
        ArrayList<MusicList> staticLists=new ArrayList<>();
        HashMap<Integer,Integer> list_icon=new HashMap<>();
        MusicList list=new MusicList();
        list.set_Name("本地音乐");
        list.set_id(-1);
        staticLists.add(list);
        list_icon.put(-1,R.drawable.locamusic);
        list=new MusicList();
        list.set_Name("我喜欢");
        list.set_id(musicListDao.getList_id("我喜欢"));
        staticLists.add(list);
        list_icon.put(list.get_id(),R.drawable.love);
        list=new MusicList();
        list.set_Name("最近播放");
        list.set_id(musicListDao.getList_id("最近播放"));
        staticLists.add(list);
        list_icon.put(list.get_id(),R.drawable.usedplay);
        list=new MusicList();
        list.set_Name("下载管理");
        list.set_id(musicListDao.getList_id("下载管理"));
        staticLists.add(list);
        list_icon.put(list.get_id(),R.drawable.download);
        static_item_adapter=new MusiclistListItemAdapter(activity,staticLists,list_icon);
        static_list.setAdapter(static_item_adapter);
        setListHeight(static_list,static_item_adapter);

        static_list.setDivider(null);
        static_list.setOnItemClickListener(this);
        LinearLayout addlistButton=view.findViewById(R.id.add_list);
        addlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle("请输入列表的名称：");
                final EditText editText=new EditText(activity);
                builder.setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title=editText.getText().toString().trim();
                        if(title!=null&&!title.equals("")) {
                            add_toMusicList_List(title);
                            MusicListDao musicListDao = new MusicListDao(activity);
                            list_array = musicListDao.queryListNotDefault();
                        }
                        else{
                            Toast.makeText(activity,"请确保输入列表名称不为空！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });
        dynamic_lsit=view.findViewById(R.id.dynamic_list);

        musicLists=musicListDao.queryListNotDefault();
        dynamic_item_adapter=new MusiclistListItemAdapter(activity,musicLists);
        dynamic_lsit.setDivider(null);
        dynamic_lsit.setAdapter(dynamic_item_adapter);
        setListHeight(dynamic_lsit,dynamic_item_adapter);
        dynamic_lsit.setOnItemClickListener(this);

        //列表长按事件
        dynamic_lsit.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final MusicList musicList=musicLists.get(position);
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle(musicList.get_Name());
                String[] selections=new String[]{"更改列表名称","删除列表"};
                builder.setItems(selections, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                changeListName(musicList,position);
                                break;
                            case 1:
                                deleteList(musicList,position);
                                break;
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    protected void changeListName(final MusicList musicList, final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setTitle("请输入列表名：");
        final EditText editText=new EditText(activity);
        editText.setText(musicList.get_Name());
        editText.setSelectAllOnFocus(true);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name=editText.getText().toString();
                musicList.set_Name(name);
                changeName(musicList,position);
            }
        });
        builder.show();
    }

    private void changeName(MusicList musicList,int position){
        boolean flag=false;
        if(activity!=null) {
            MusicListDao musicListDao = new MusicListDao(activity);
            flag=musicListDao.alterListName(musicList);
            musicLists.get(position).set_Name(musicList.get_Name());
            dynamic_item_adapter.notifyDataSetChanged();
        }
        if(!flag){
            Toast.makeText(activity,"更名失败！",Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteList(final MusicList musicList, final int position){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setTitle("删除");
        builder.setMessage("确定删除\""+musicList.get_Name()+"\"?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MusicListDao musicListDao=new MusicListDao(activity);
                boolean flag=musicListDao.deleteList(musicList);
                if(flag){
                    musicLists.remove(position);
                    dynamic_item_adapter.notifyDataSetChanged();
                    Toast.makeText(activity,"删除成功！",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(activity,"删除失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    //动态设置ListView的高度
    private void setListHeight(ListView list, ListAdapter adapter){
        ViewGroup.LayoutParams layoutParams=list.getLayoutParams();
        layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
        list.measure(0,0);
        layoutParams.height=adapter.getCount()*list.getMeasuredHeight();
        list.setLayoutParams(layoutParams);
    }

    private void add_toMusicList_List(String title){
        boolean flag=musicListDao.insert(title);
        MusicList musicList=new MusicList();
        musicList.set_Name(title);
        musicList.set_id(musicListDao.getList_id(title));
        if(flag){
            Map<String,Object> dyna_map=new HashMap<>();
            dyna_map.put("listname",title);
            dyna_map.put("listicon",R.drawable.defaluticon);
            musicLists.add(musicList);
            dynamic_item_adapter.notifyDataSetChanged();
            setListHeight(dynamic_lsit,dynamic_item_adapter);
        }
        else{
            Toast.makeText(activity,"该列表已经存在",Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.dynamic_list:
                clickDymaList(i);
                break;
            case R.id.music_list_list:
                clickStaticList(i);
                break;
        }
    }

    private void clickStaticList(int position){
        Intent intent=new Intent(activity, MusicListActivity.class);
        int list_id;
        switch (position){
            case 0:
                intent.putExtra(MusicListActivity.listnameKey,"本地音乐");
                intent.putExtra("list_id",-1);
                break;
            case 1:
                musicOfListDao=new MusicOfListDao(activity);
                musicListDao=new MusicListDao(activity);
                list_id=musicListDao.getList_id("我喜欢");
                intent.putExtra(MusicListActivity.listnameKey,"我喜欢");
                intent.putExtra("list_id",list_id);
                break;
            case 2:
                musicOfListDao=new MusicOfListDao(activity);
                musicListDao=new MusicListDao(activity);
                list_id=musicListDao.getList_id("最近播放");
                intent.putExtra(MusicListActivity.listnameKey,"最近播放");
                intent.putExtra("list_id",list_id);
                break;
            case 3:
                //musicOfListDao=new MusicOfListDao(activity);
                musicListDao=new MusicListDao(activity);
                list_id=musicListDao.getList_id("下载管理");
                intent=new Intent(activity,DownloadActivity.class);
                intent.putExtra(MusicListActivity.listnameKey,"下载管理");
                intent.putExtra("list_id",list_id);
                break;
        }
        activity.startActivity(intent);
    }

    private void clickDymaList(int position){
        Intent intent=new Intent(activity,MusicListActivity.class);
        musicOfListDao=new MusicOfListDao(activity);
        MusicList musicList=musicLists.get(position);
        intent.putExtra(MusicListActivity.listnameKey,musicList.get_Name());
        intent.putExtra("list_id",musicList.get_id());
        activity.startActivity(intent);
    }

    class ClassifyAdapter extends RecyclerView.Adapter<ClassifyAdapter.ClassifyHolder>{

        private OnItemClickListener mOnItemClickListener;

        private List<ClassifyHolder> holderList=new ArrayList<>();

        @Override
        public ClassifyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ClassifyHolder holder=new ClassifyHolder(LayoutInflater.from(activity).inflate(R.layout.classify_item_layout,parent,false));
            holderList.add(holder);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ClassifyHolder holder, int position) {
            final MusicClassify classify=(MusicClassify)classifyList.get(position);
            holder.class_id=classify.getId();
            holder.title_tv.setText(classify.getName());
            holder.icon_iv.setDrawingCacheEnabled(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Bundle bundle=new Bundle();
                        bundle.putInt("class_id",holder.class_id);
                        Bitmap bitmap=NetUtils.getURLImage("http://"+getResources().getString(R.string.down_host)+classify.getIconurl());
                        Message msg=bitmapHandler.obtainMessage();
                        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                        bundle.putByteArray("bitmap",outputStream.toByteArray());
                        msg.setData(bundle);
                        bitmapHandler.sendMessage(msg);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
            if(mOnItemClickListener!=null){
                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos=holder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(holder.relativeLayout,pos);
                    }
                });
                holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int pos=holder.getLayoutPosition();
                        mOnItemClickListener.onItemLongClick(holder.relativeLayout,pos);
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return classifyList.size();
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener){
            mOnItemClickListener=onItemClickListener;
        }

        class ClassifyHolder extends RecyclerView.ViewHolder{
            int class_id;
            RelativeLayout relativeLayout;
            ImageView icon_iv;
            TextView title_tv;

            public ClassifyHolder(View itemView) {
                super(itemView);
                relativeLayout=itemView.findViewById(R.id.classify_rl);
                icon_iv=itemView.findViewById(R.id.classify_icon);
                title_tv=itemView.findViewById(R.id.classify_name_tv);
            }
        }

        Handler bitmapHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                WindowInfoMananger wim=new WindowInfoMananger((AppCompatActivity) activity);
                Point point=wim.getScreenWidthHight();
                int height=point.x/3-10;
                Bundle bundle=msg.getData();
                try {
                    int class_id = bundle.getInt("class_id");
                    byte[] bitmapBytes = bundle.getByteArray("bitmap");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    for (ClassifyHolder holder : holderList) {
                        if (holder.class_id == class_id) {
                            bitmap = BitMapUtil.getOrderSizeBitmap(bitmap, height, height);
                            holder.icon_iv.setImageBitmap(bitmap);
                        }
                    }
                }
                catch (Exception e){
                    Toast.makeText(activity,"网络连接异常！",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
