package com.example.amia.zplayer.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.amia.zplayer.R;

/**
 * 自定义View 圆形进度条
 * Created by Amia on 2018/3/20.
 */

public class ProgressView extends View {

    private int duration;  //总长度
    private int progress;  //进度
    private int mWidth;
    private int mHeight;
    private Paint paint;


    public ProgressView(Context context) {
        super(context,null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        paint=new Paint();
    }

    private void init(AttributeSet attrs){
        mWidth=50;
        mHeight=50;
        if(attrs==null){
            return;
        }
        TypedArray array=getContext().obtainStyledAttributes(attrs, R.styleable.ProgressView);
        progress=array.getInteger(R.styleable.ProgressView_progress,0);
        duration=array.getInteger(R.styleable.ProgressView_duration,100);
        array.recycle();
    }

    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int height=MeasureSpec.getSize(widthMeasureSpec);
        int temp=measureSize(widthMode,width);
        mWidth=temp==0?mWidth:temp;
        temp=measureSize(heightMode,height);
        mHeight=temp==0?mHeight:temp;
        setMeasuredDimension(mWidth,mHeight);
    }

    private int measureSize(int mode,int defsize){
        int size=0;
        switch (mode){
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                size=defsize;
                break;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas){
        int center=getWidth()/2;
        int innerCircle=getWidth()/2-10;
        if(innerCircle<0){
            innerCircle=10;
        }
        int ringWidth=getWidth()/2;
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(191,191,191));
        canvas.drawCircle(center,center,ringWidth,paint);

        RectF rectF=new RectF(0,0,getWidth(),getWidth());
        paint.setColor(Color.rgb(77,182,172));
        float per=((float)progress)/duration;
        float sweep=360*per;
        canvas.drawArc(rectF,-90,sweep,true,paint);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10f);
        canvas.drawCircle(center,center,innerCircle,paint);

    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = (int)duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        if(progress<0){
            return;
        }
        this.progress = (int)progress;
        postInvalidate();
    }
}
