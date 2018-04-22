package com.example.amia.zplayer.Activity;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import com.example.amia.zplayer.R;

/**
 * Created by Amia on 2018/4/22.
 */

public class ScheTimeDialog extends Dialog {

    public ScheTimeDialog(@NonNull Context context) {
        super(context);
    }

    public ScheTimeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{
        private String positiveButtonText;
        private String negativeButtonText;
        private ScheTimeDialog scheTimeDialog;
        private View layout;
        private int minValue;
        private int maxValue;

        private NumberPicker numberPicker;

        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;

        public Builder(Context context){
            minValue=1;
            maxValue=60;
            scheTimeDialog=new ScheTimeDialog(context, R.style.Theme_AppCompat_Light_Dialog);
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout=inflater.inflate(R.layout.sche_dialog_layout,null);
            scheTimeDialog.addContentView(layout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        private void setDefaultNegativeListener(){
            this.negativeButtonClickListener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scheTimeDialog.cancel();
                }
            };
        }

        private void setDefaultPositiveListener(){
            this.positiveButtonClickListener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scheTimeDialog.cancel();
                }
            };
        }

        public int getValue(){
            return numberPicker.getValue();
        }

        public Builder setNegativeButton(String negativeButtonText, View.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, View.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(View.OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        public void cancleDialog(){
            if(scheTimeDialog!=null||scheTimeDialog.isShowing()){
                scheTimeDialog.cancel();
            }
        }

        public ScheTimeDialog createDialog() {
            if (positiveButtonClickListener == null) {
                setDefaultPositiveListener();
            }
            if(negativeButtonClickListener == null){
                setDefaultNegativeListener();
            }
            layout.findViewById(R.id.positiveButton).setOnClickListener(positiveButtonClickListener);
            layout.findViewById(R.id.negativeButton).setOnClickListener(negativeButtonClickListener);
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
            } else {
                ((Button) layout.findViewById(R.id.positiveButton)).setText("确定");
            }
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
            } else {
                ((Button) layout.findViewById(R.id.negativeButton)).setText("取消");
            }
            numberPicker=layout.findViewById(R.id.number_picker);
            numberPicker.setValue(minValue);
            numberPicker.setMinValue(minValue);
            numberPicker.setMaxValue(maxValue);
            create();
            return scheTimeDialog;
        }

        private void create(){
            scheTimeDialog.setContentView(layout);
            scheTimeDialog.setCancelable(true);
            scheTimeDialog.setCanceledOnTouchOutside(true);
        }

        public ScheTimeDialog setMinValue(int minValue){
            this.minValue=minValue;
            return scheTimeDialog;
        }

        public ScheTimeDialog setMaxValue(int maxValue){
            this.maxValue=maxValue;
            return scheTimeDialog;
        }
    }
}
