package com.example.amia.zplayer.Activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicOfList;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.util.MusicResolverUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    final static int setListMessage=0;

    private AutoCompleteTextView search_tv;
    private ImageButton search_btn;
    private ListView res_list;

    private int list_id;
    private List<Mp3Info> mp3Infos;
    private List<Mp3Info> resInfo=new ArrayList<>();

    private MusicResolverUtil musicResolverUtil=new MusicResolverUtil(SearchActivity.this);
    private MusicOfListDao musicOfListDao=new MusicOfListDao(SearchActivity.this);
    private MusListAdapter adapter=new MusListAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        list_id=(Integer) getIntent().getSerializableExtra(getResources().getString(R.string.LIST_ID));
        thread.start();
        init();
    }

    private void init(){
        search_tv=findViewById(R.id.sear_mus_tv);
        search_btn=findViewById(R.id.sear_res_ib);
        res_list=findViewById(R.id.res_list);

        search_btn.setOnClickListener(this);
        res_list.setAdapter(adapter);
        res_list.setOnItemClickListener(this);

        Toolbar toolbar=findViewById(R.id.res_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sear_res_ib:
                searchByName();
                break;
            default:
                closeForNull();
        }
    }

    private void closeForNull(){
        Intent intent=new Intent();
        setResult(RESULT_CANCELED,intent);
        this.finish();
    }

    private void searchByName(){
        resInfo.clear();
        String str=search_tv.getText().toString().trim();
        for(Mp3Info mp3Info:mp3Infos){
            if(mp3Info.getTitle().indexOf(str)!=-1||mp3Info.getArtist().indexOf(str)!=-1){
                resInfo.add(mp3Info);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setAutoCompAdapter(){
        String[] items=new String[mp3Infos.size()];
        for(int i=0;i<mp3Infos.size();i++){
            items[i]=mp3Infos.get(i).getTitle();
        }
        search_tv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items));
    }

    Thread thread=new Thread(new Runnable() {

        @Override
        public void run() {
            if(list_id==-1){
                mp3Infos=musicResolverUtil.getAllMp3Infos();
            }
            else {
                ArrayList<MusicOfList> arrayList = musicOfListDao.queryMusicByList(list_id, null);
                mp3Infos = musicResolverUtil.getMp3InfoById(arrayList);
            }
            Message msg=handler.obtainMessage();
            msg.what=setListMessage;
            handler.sendMessage(msg);
        }
    });

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case setListMessage:
                    setAutoCompAdapter();
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent=new Intent();
        intent.putExtra("musicId",(int)resInfo.get(i).getId());
        setResult(RESULT_OK,intent);
        this.finish();
    }

    class MusListAdapter extends BaseAdapter{

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
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view==null){
                view=getLayoutInflater().inflate(R.layout.half_mus_item_layout,null);
                holder=new ViewHolder();
                holder.title=view.findViewById(R.id.half_mus_title);
                holder.artist=view.findViewById(R.id.half_mus_artist);
                view.setTag(holder);
            }
            else{
                holder=(ViewHolder) view.getTag();
            }
            Mp3Info info=resInfo.get(i);
            holder.title.setText(info.getTitle());
            holder.artist.setText(info.getArtist());
            return view;
        }
    }

    class ViewHolder{
        TextView title;
        TextView artist;
    }
}
