package com.example.amia.zplayer.Activity;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.amia.zplayer.R;

/**
 * Created by Amia on 2018/4/23.
 */

public class AddListDialog extends Dialog {
    public AddListDialog(@NonNull Context context) {
        super(context);
    }

    public AddListDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{
        private String positiveButtonText;
        private AddListDialog addListDialog;
        private View layout;
        private EditText inputEditText;

        private View.OnClickListener positiveButtonClickListener;

        public Builder(Context context){
            addListDialog=new AddListDialog(context, R.style.Theme_AppCompat_Light_Dialog);
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout=inflater.inflate(R.layout.add_list_dialog_layout,null);
            addListDialog.addContentView(layout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        private void setDefaultPositiveListener(){
            this.positiveButtonClickListener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addListDialog.cancel();
                }
            };
        }

        public String getValue(){
            String value=inputEditText.getText().toString();
            inputEditText.setText("");
            return value;
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
            if(addListDialog!=null||addListDialog.isShowing()){
                addListDialog.cancel();
            }
        }

        public AddListDialog createDialog() {
            if (positiveButtonClickListener == null) {
                setDefaultPositiveListener();
            }
            layout.findViewById(R.id.positiveButton).setOnClickListener(positiveButtonClickListener);
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
            } else {
                ((Button) layout.findViewById(R.id.positiveButton)).setText("确定");
            }
            inputEditText=layout.findViewById(R.id.input_et);
            create();
            return addListDialog;
        }

        private void create(){
            addListDialog.setContentView(layout);
            addListDialog.setCancelable(true);
            addListDialog.setCanceledOnTouchOutside(true);
        }
    }
}
