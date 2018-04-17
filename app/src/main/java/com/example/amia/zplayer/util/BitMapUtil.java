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
import java.text.DecimalFormat;

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

    public static byte[] bitmapToByte(Bitmap bitmap) throws NullPointerException{
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
    }

    public static void saveToTemp(Bitmap bitmap,int listId){
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/temp/";
        File file=new File(path);
        if(!file.exists()){
            file.mkdir();
        }
        File saveFile=new File(path+listId);
        FileOutputStream outputStream=null;
        try {
            outputStream=new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getBitMapByNetId(int netId){
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/temp/"+netId;
        File file=new File(path);
        if(!file.exists()){
            return null;
        }
        return BitmapFactory.decodeFile(path);
    }

    public static void defAllBitmapTemp(){
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/temp/";
        File file=new File(path);
        String[] lrcList=file.list();
        File lrc=null;
        for(int i=0;i<lrcList.length;i++){
            lrc=new File(path+lrcList[i].trim());
            if (lrc.isFile()) {
                lrc.delete();
            }
        }
    }

    public static String getBimapTempSize(){
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZPlayer/temp/";
        File file=new File(path);
        if(file.exists()){
            DecimalFormat decimalFormat=new DecimalFormat(".00");
            long size=getFileSize(file);
            float res=size/1024f;
            if(res==0){
                return null;
            }
            if(res<1024){
                String str=decimalFormat.format(res);
                return str+" KB";
            }
            else{
                res=res/1024;
                String str=decimalFormat.format(res);
                return str+" MB";
            }
        }
        else{
            return null;
        }
    }

    private static long getFileSize(File file){
        if(file.isFile()){
            return file.length();
        }
        File[] childFile=file.listFiles();
        long total=0;
        if(childFile!=null){
            for(File child:childFile){
                total+=getFileSize(child);
            }
        }
        return total;
    }
}
