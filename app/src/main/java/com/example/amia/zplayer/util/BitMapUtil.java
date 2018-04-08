package com.example.amia.zplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Amia on 2017/8/6.
 */

public class BitMapUtil {

    /**
     * 根据路径获取位图
     * @param path
     * @return
     */
    public static Bitmap getBitmapFromPath(String path){
        File file=new File(path);
        if(file.exists()){
            byte[] buf=new byte[1024*512];
            Bitmap bitmap=null;
            try{
                FileInputStream fis=new FileInputStream(path);
                int len=fis.read(buf,0,buf.length);
                bitmap= BitmapFactory.decodeByteArray(buf,0,len);
                return bitmap;
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            return null;
        }
    }

    /**
     * 获取尺寸的位图
     * @param bitmap
     * @param x
     * @param y
     * @return
     */
    public static Bitmap getOrderSizeBitmap(Bitmap bitmap, int x, int y){
        float px=(float)x/bitmap.getWidth();
        float py=(float)y/bitmap.getHeight();
        Bitmap resultBitmap= Bitmap.createBitmap((int)(bitmap.getWidth()*px),(int)(bitmap.getHeight()*py),bitmap.getConfig());
        Canvas canvas =new Canvas(resultBitmap);
        Paint paint=new Paint();
        Matrix matrix=new Matrix();

        matrix.setScale(px,py);
        canvas.drawBitmap(bitmap,matrix,paint);
        return resultBitmap;
    }

    /**
     * 通过路径获取指定尺寸的位图
     * @param url
     * @param x
     * @param y
     * @return
     */
    public static Bitmap getOrderSizeBitmapByUrl(String url, int x, int y){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        Bitmap bitmap= BitmapFactory.decodeFile(url,options);
        int realWidth=options.outWidth;
        int realHieght=options.outHeight;
        Log.i("BitMapUtil","x="+x+";y="+y);
        int dx=x/realWidth;
        int dy=y/realHieght;
        int res=dx<dy?dx:dy;
        Log.i("BitMapUtil","结果比例"+res);
        options.inSampleSize=res;
        options.inJustDecodeBounds=false;
        bitmap= BitmapFactory.decodeFile(url,options);
        return bitmap;
    }

    /**
     * 保存位图
     * @param bitmap
     * @param saveName
     * @param context
     * @throws IOException
     */
    public static void saveBitmap(Bitmap bitmap, String saveName, Context context) throws IOException {
        File dir=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/pic");
        if(!dir.exists()){
            dir.mkdir();
        }
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/pic",saveName+".jpg");
        if(file.exists()){
            file.delete();
        }
        FileOutputStream outputStream;
        try{
            outputStream=new FileOutputStream(file);
            if(bitmap!=null&&bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)){
                outputStream.flush();
                outputStream.close();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        try{
            MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),saveName,null);
            file.delete();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static String getPicFilePath(String saveName, Context context){
        Cursor cursor= MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Images.Media.TITLE+" = ?",new String[]{saveName},null);
        String path=null;
        if(cursor!=null&&cursor.getCount()>0){
            cursor.moveToFirst();
            path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.i("BitMapUtil",path);
        }
        return path;
    }

    public static byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
    }
}
