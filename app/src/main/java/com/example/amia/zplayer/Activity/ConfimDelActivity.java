package com.example.amia.zplayer.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.amia.zplayer.R;


public class ConfimDelActivity extends Activity implements View.OnClickListener,Animation.AnimationListener{

    private Button sure_btn;
    private Button cancel_btn;
    private RelativeLayout cancel_area_rl;
    private LinearLayout btnAre_ll;

    Animation outAnim;

    //保证动画被加载
    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confim_del);
        sure_btn=findViewById(R.id.sure_bt);
        cancel_btn=findViewById(R.id.cancel_bt);
        cancel_area_rl=findViewById(R.id.can_area_rl);
        sure_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        cancel_area_rl.setOnClickListener(this);
        Intent intent=getIntent();
        int size=intent.getIntExtra("MusSize",0);
        sure_btn.setText("删除"+size+"首歌曲");

        TypedArray activityStyle=getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        int windowAnmiationStyleResId=activityStyle.getResourceId(0,0);
        activityStyle.recycle();
        activityStyle=getTheme().obtainStyledAttributes(windowAnmiationStyleResId,new int[]{android.R.attr.activityCloseEnterAnimation,android.R.attr.activityCloseEnterAnimation});
        activityCloseEnterAnimation=activityStyle.getResourceId(0,0);
        activityCloseExitAnimation=activityStyle.getResourceId(1,0);
        activityStyle.recycle();

        btnAre_ll=findViewById(R.id.btnare_ll);
        Animation inAnimation= AnimationUtils.loadAnimation(this,R.anim.push_bottom_in);
        btnAre_ll.startAnimation(inAnimation);
    }

    @Override
    public void onClick(View view) {
        boolean result=false;
        switch (view.getId()){
            case R.id.sure_bt:
                result=true;
                break;
            case R.id.cancel_bt:
                break;
            case R.id.can_area_rl:
                break;
        }
        Intent intent=new Intent();
        intent.putExtra("result",result);
        setResult(RESULT_OK,intent);
        outAnim=AnimationUtils.loadAnimation(this,R.anim.push_buttom_out);
        outAnim.setAnimationListener(this);
        btnAre_ll.startAnimation(outAnim);
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation,activityCloseExitAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation==outAnim){
            this.finish();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
