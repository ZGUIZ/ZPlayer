package com.example.amia.zplayer.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.util.MusicListDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amia on 2018/3/23.
 */

public class DownloadDao {
    private MusicListDatabaseHelper databaseHelper;
    private Context context;
    public static final String TABLENAME="download";

    public DownloadDao(Context context){
        this.context=context;
        databaseHelper=new MusicListDatabaseHelper(context);
    }

    /**
     * 往数据库中插入数据
     * @param downLoadInfo
     */
    public void insert(MusicDownLoadInfo downLoadInfo){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        if(database.isOpen()){
            ContentValues values=new ContentValues();
            values.put("net_id",downLoadInfo.getNetId());
            values.put("music_name",downLoadInfo.getTitle());
            values.put("artist",downLoadInfo.getArtist());
            values.put("finish",1);
            values.put("net_uri",downLoadInfo.getNetUrl());
            values.put("uri", Environment.getExternalStorageDirectory().getPath()+"/ZPlayer/Music/"+downLoadInfo.getArtist()+" - "+downLoadInfo.getTitle()+".mp3");
            database.insert(TABLENAME,null,values);
        }
        database.close();
    }

    /**
     * 单首下载暂停
     * @param downLoadInfo
     */
    public void downLoadPause(MusicDownLoadInfo downLoadInfo){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        if (database.isOpen()){
            ContentValues values=new ContentValues();
            values.put("finish",0);
            database.update(TABLENAME,values,"net_id = ?",new String[]{String.valueOf(downLoadInfo.getNetId())});
        }
        database.close();
    }

    /**
     * 全部下载暂停
     */
    public void allDownLoadPause(){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        if (database.isOpen()){
            ContentValues values=new ContentValues();
            values.put("finish",0);
            database.update(TABLENAME,values,"finish = ?",new String[]{String.valueOf(1)});
        }
        database.close();
    }

    /**
     * 下载完成
     * @param downLoadInfo
     */
    public void downloadFinish(MusicDownLoadInfo downLoadInfo){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        if (database.isOpen()){
            ContentValues values=new ContentValues();
            values.put("finish",2);
            database.update(TABLENAME,values,"net_id = ?",new String[]{String.valueOf(downLoadInfo.getNetId())});
        }
        database.close();
    }

    /**
     * 删除数据
     * @param downLoadInfo
     */
    public void delete(MusicDownLoadInfo downLoadInfo){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        if(database.isOpen()){
            ContentValues values=new ContentValues();
            values.put("net_id",downLoadInfo.getId());
            database.delete(TABLENAME,"net_id = ?",new String[]{String.valueOf(downLoadInfo.getNetId())});
        }
        database.close();
    }

    /**
     * 查询所有数据
     * @return
     */
    public List<MusicDownLoadInfo> queryAll(){
        SQLiteDatabase database=databaseHelper.getReadableDatabase();
        List<MusicDownLoadInfo> lists=new ArrayList<>();
        if(database.isOpen()){
            Cursor cursor=database.query(TABLENAME,null,null,null,null,null,"d_id desc");
            if(cursor.getColumnCount()>0){
                cursor.moveToFirst();
                MusicDownLoadInfo musicDownLoadInfo;
                while(!cursor.isAfterLast()){
                    musicDownLoadInfo=new MusicDownLoadInfo();
                    musicDownLoadInfo.setId_list(cursor.getInt(cursor.getColumnIndex("d_id")));
                    musicDownLoadInfo.setNetId(cursor.getInt(cursor.getColumnIndex("net_id")));
                    musicDownLoadInfo.setTitle(cursor.getString(cursor.getColumnIndex("music_name")));
                    musicDownLoadInfo.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
                    musicDownLoadInfo.setStatus(cursor.getInt(cursor.getColumnIndex("finish"))==2?2:0);
                    musicDownLoadInfo.setNetUrl(cursor.getString(cursor.getColumnIndex("net_uri")));
                    musicDownLoadInfo.setUrl(cursor.getString(cursor.getColumnIndex("uri")));
                    lists.add(musicDownLoadInfo);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return lists;
    }

    public boolean isInList(int id){
        SQLiteDatabase database=databaseHelper.getReadableDatabase();
        if(database.isOpen()){
            Cursor cursor=database.query(TABLENAME,null,"net_id = ?",new String[]{String.valueOf(id)},null,null,null);
            if(cursor.getCount()>0){
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }
}
