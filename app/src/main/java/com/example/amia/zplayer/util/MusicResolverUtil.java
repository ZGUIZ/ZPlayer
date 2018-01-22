package com.example.amia.zplayer.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicOfList;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Amia on 2017/8/7.
 */

public class MusicResolverUtil {

    protected Activity activity;

    public MusicResolverUtil(Activity activity){
        this.activity=activity;
    }

    /**
     * 该方法用于从数据库获取音乐信息
     * @return 所有系统数据库音乐信息
     */
    public ArrayList<Mp3Info> getAllMp3Infos(){
        Cursor cursor=activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        ArrayList<Mp3Info> mp3Infos=new ArrayList<>();
        for(int i=0;i<cursor.getCount();i++){
            Mp3Info mp3Info=new Mp3Info();
            cursor.moveToNext();
            long id=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            //Log.i("getMp3Infos: Title:",title);
            String url=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            long album_id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            mp3Info.setId(id);
            mp3Info.setTitle(title);
            mp3Info.setArtist(artist);
            mp3Info.setDuration(duration);
            mp3Info.setSize(size);
            mp3Info.setUrl(url);
            mp3Info.setAlbum_id(album_id);
            mp3Infos.add(mp3Info);
        }
        if(cursor!=null) {
            cursor.close();
        }
        return mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3InfoById(ArrayList<MusicOfList> arrayList){
        ArrayList<Mp3Info> array=new ArrayList<>();
        for(MusicOfList musicOfList:arrayList){
            Mp3Info mp3Info=getMp3InfoById(musicOfList.getId_music());
            array.add(mp3Info);
        }
        return array;
    }

    public Mp3Info getMp3InfoById(long id){
        Mp3Info mp3Info=null;
        Cursor cursor=activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Media._ID+" = ?",new String[]{String.valueOf(id)},null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            mp3Info=new Mp3Info();
            mp3Info.setId(id);
            mp3Info.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            mp3Info.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            mp3Info.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            mp3Info.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            mp3Info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
            mp3Info.setAlbum_id(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
        }

        if(cursor!=null){
            cursor.close();
        }
        return mp3Info;
    }

    /**
     * 该方法用于获取传入顺序表的音乐信息的专辑Bitmap图片,获取图片的KEY为音乐的ID
     * @param mp3Infos
     * @return
     */
    public HashMap<String,Bitmap> getAllAlbum(ArrayList<Mp3Info> mp3Infos){
        HashMap<String,Bitmap> bitMap=new HashMap<>();
        //Log.i("getAllAlbum","array size="+mp3Infos.size());
        for(Mp3Info mp3Info:mp3Infos){
            /*String Album_url=getAlbumArt(mp3Info.getAlbum_id());
            if(Album_url!=null) {
                mp3Info.setAlbum_url(Album_url);
                bitMap.put(String.valueOf(mp3Info.getId()), BitMapUtil.getBitmapFromPath(mp3Info.getAlbum_url()));
            }*/
            Bitmap bitmap=getMusicBitemp(activity,mp3Info.getId(),mp3Info.getAlbum_id());
            if(bitmap!=null){
                bitMap.put(String.valueOf(mp3Info.getId()),bitmap);
            }
        }
        return bitMap;
    }

    //获得专辑图片的地址
    public String getAlbumArt(long album_id){
        String url="content://media/external/audio/albums";
        String[] projection=new String[] {"album_art"};
        Cursor cur=activity.getContentResolver().query(Uri.parse(url+"/"+ String.valueOf(album_id)),projection,null,null,null);
        String album_art=null;
        if(cur.getCount()>0&&cur.getColumnCount()>0){
            cur.moveToNext();
            album_art=cur.getString(0);
        }
        cur.close();
        return album_art;
    }

    /**
     * 获得单首音乐的专辑图片
     * @param mp3Info
     * @return
     */
    @Deprecated
    public Bitmap getAlbumFormInfo(Mp3Info mp3Info){
        Bitmap bitmap=null;
        String album_url=getAlbumArt(mp3Info.getAlbum_id());
        if(album_url!=null){
            mp3Info.setAlbum_url(album_url);
            bitmap=BitMapUtil.getBitmapFromPath(album_url);
        }
        return bitmap;
    }


    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    /**
     * 将MP3里图片读取出来
     *
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    public static Bitmap getMusicBitemp(Context context, long songid,
                                        long albumid) {
        Bitmap bm = null;
// 专辑id和歌曲id小于0说明没有专辑、歌曲，并抛出异常
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException(
                    "Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                } else {
                    return null;
                }
            }
        } catch (FileNotFoundException ex) {
        }
        return bm;
    }

    public boolean deleteMusice(Mp3Info mp3Info){
        boolean flag=false;
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id= "+mp3Info.getId(), null);
            flag=true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

}
