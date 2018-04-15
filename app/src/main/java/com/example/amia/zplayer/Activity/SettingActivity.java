package com.example.amia.zplayer.Activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.amia.zplayer.R;
import com.example.amia.zplayer.util.XmlReadWriteUtil;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,View.OnClickListener{

    private Switch net_wifi,extra_stop;
    private XmlReadWriteUtil xmlReadWriteUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
    }

    private void init(){
        net_wifi=findViewById(R.id.net_on_wifi);
        extra_stop=findViewById(R.id.extra_stop);
        net_wifi.setOnCheckedChangeListener(this);
        extra_stop.setOnCheckedChangeListener(this);

        //查询设置
        xmlReadWriteUtil=new XmlReadWriteUtil(this);
        String visitNetMode=getResources().getString(R.string.visitNetMode);
        String extarStopStr=getResources().getString(R.string.extra_stop);
        String[] settingParam=new String[]{visitNetMode,extarStopStr};
        HashMap<String,Boolean> setting=xmlReadWriteUtil.readSetting(getResources().getString(R.string.setting_file),settingParam);

        net_wifi.setChecked(setting.get(visitNetMode));
        extra_stop.setChecked(setting.get(extarStopStr));

        setToolBar();
    }

    private void setToolBar(){
        Toolbar toolbar=findViewById(R.id.setting_toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(this);
    }

    private void writeSetting(){
        Boolean isWifiChecked=net_wifi.isChecked();
        Boolean isExtraChecked=extra_stop.isChecked();
        HashMap<String,Boolean> setting=new HashMap<>();
        setting.put(getResources().getString(R.string.visitNetMode),isWifiChecked);
        setting.put(getResources().getString(R.string.extra_stop),isExtraChecked);
        xmlReadWriteUtil.writeSetting(getResources().getString(R.string.setting_file),setting);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        writeSetting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            default:
                this.finish();
                break;
        }
    }
}
