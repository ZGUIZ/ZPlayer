package com.example.amia.zplayer.util;

import android.content.Context;

/**
 * Created by Amia on 2017/10/30.
 */

public class DensityUtil {
    /**
     * dip换取像素px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue){
        try{
            final float scale=context.getResources().getDisplayMetrics().density;//获取屏幕分辨率
            return (int)(dpValue*scale+0.5f);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return (int)dpValue;
    }

    /**
     * 像素转换为dip
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue){
        try{
            final float scale=context.getResources().getDisplayMetrics().density;
            return (int)(pxValue/scale+0.5f);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return (int)pxValue;
    }
}
