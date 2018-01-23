package com.example.amia.zplayer.Activity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * 显示歌词的TextView
 * Created by Amia on 2018/1/23.
 */

public class LrcView extends android.support.v7.widget.AppCompatTextView {

    private boolean focuse;

    public LrcView(Context context) {
        super(context);
    }

    public LrcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused(){
        return focuse;
    }

    public void setFocuse(boolean focuse) {
        this.focuse = focuse;
    }
}
