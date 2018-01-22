package com.example.amia.zplayer.Activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.amia.zplayer.DTO.LrcEntity;
import com.example.amia.zplayer.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Amia on 2018/1/7.
 */

class LrcAdapter extends SimpleAdapter {

    private List<Map<String,String>> lrcs;
    private Context context;
    private LrcColorCallBack callBack;

    private int resource;
    public LrcAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,LrcColorCallBack callBack) {
        super(context, data, resource, from, to);
        this.resource=resource;
        this.context=context;
        this.callBack=callBack;
        this.lrcs=(List<Map<String,String>>)data;
    }

    @Override
    public int getCount(){
        return lrcs.size();
    }

    @Override
    public View getView(final int position, View converView, ViewGroup viewGroup) {
        View view = null;
        Holder holder=null;
        if (converView == null) {
            view = View.inflate(context, resource, null);
            TextView textView = view.findViewById(R.id.lrc_text);
            holder=new Holder();
            holder.textView=textView;
            view.setTag(holder);
        } else {
            view = converView;
            holder=(Holder)view.getTag();
        }
        callBack.setLrcColor(holder,position);
        if (position < lrcs.size()) {
            holder.textView.setText(lrcs.get(position).get("lrc"));
        }
        return view;
    }

    static class Holder{
        TextView textView;
    }

    interface LrcColorCallBack{
        void setLrcColor(Holder holder,int position);
    }
}
