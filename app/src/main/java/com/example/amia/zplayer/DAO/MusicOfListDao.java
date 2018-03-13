package com.example.amia.zplayer.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.amia.zplayer.DTO.MusicOfList;
import com.example.amia.zplayer.util.MusicListDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Amia on 2017/8/25.
 */

public class MusicOfListDao {
    public final static String tableName="MusicOfList";

    private MusicListDatabaseHelper dbhelper;

    public MusicOfListDao(Context context){
        dbhelper=new MusicListDatabaseHelper(context);
    }

    public boolean insertMusic(MusicOfList musicOfList){
        return insertMusic(musicOfList.getId_list(),musicOfList.getId_music());
    }

    public boolean insertMusic(int list_id,int music_id){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            ContentValues contentValues=new ContentValues();
            contentValues.put("list_id",list_id);
            contentValues.put("music_id",music_id);
            long row=db.insert(tableName,null,contentValues);
            if(row!=-1){
                flag=true;
            }
            db.close();
        }
        return flag;
    }

    public boolean deleteMusicOfList(MusicOfList musicOfList){
        return deleteMusicOfList(musicOfList.getId_list(),musicOfList.getId_music());
    }

    public boolean deleteMusicOfList(int list_id,int music_id){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            int rowcount=db.delete(tableName,"list_id= ? and music_id = ?",new String[]{String.valueOf(list_id), String.valueOf(music_id)});
            if(rowcount>0){
                flag=true;
            }
            db.close();
        }
        return flag;
    }

    public boolean deleteMusicOfListByList(int list_id){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            int rowcount=db.delete(tableName,"list_id= ?",new String[]{String.valueOf(list_id)});
            if(rowcount>0){
                flag=true;
            }
            db.close();
        }
        return flag;
    }

    public boolean deleteMusicOfListByid(int music_id){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            int rowcount=db.delete(tableName,"music_id= ?",new String[]{String.valueOf(music_id)});
            if(rowcount>=0){
                flag=true;
            }
            db.close();
        }
        return flag;
    }

    public ArrayList<MusicOfList> queryMusicByList(int list_id, String order){
        if(order==null||order==""){
            order="asc";
        }
        ArrayList<MusicOfList> array=new ArrayList<>();
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        if(order!=null){
            order="n_id "+order;
        }
        if(db.isOpen()){
            Cursor cursor=db.query(tableName,null,"list_id = ?",new String[]{String.valueOf(list_id)},null,null,order);
            if(cursor.getColumnCount()>0){
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    MusicOfList musicOfList=new MusicOfList();
                    //musicOfList.setId_list(cursor.getInt(1));
                    //musicOfList.setId_music(cursor.getInt(2));
                    musicOfList.setId_list(cursor.getInt(cursor.getColumnIndex("list_id")));
                    musicOfList.setId_music(cursor.getInt(cursor.getColumnIndex("music_id")));
                    array.add(musicOfList);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
        return array;
    }

    public boolean isInList(int list_id,int music_id){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        if(db.isOpen()){
            Cursor cursor=db.query(tableName,null,"list_id = ? and music_id = ?",new String[]{String.valueOf(list_id),String.valueOf(music_id)},null,null,null);
            if(cursor.getCount()>0){
                flag=true;
            }
        }
        return flag;
    }

    public MusicOfList getOneByList(int list_id){
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        MusicOfList musicOfList=null;
        if(db.isOpen()){
            Cursor cursor=db.query(tableName,null,"list_id = ? limit 1",new String[]{String.valueOf(list_id)},null,null,null);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                musicOfList=new MusicOfList();
                //musicOfList.setId_list(cursor.getInt(1));
                //musicOfList.setId_music(cursor.getInt(2));
                musicOfList.setId_list(cursor.getInt(cursor.getColumnIndex("list_id")));
                musicOfList.setId_music(cursor.getInt(cursor.getColumnIndex("music_id")));
            }
            cursor.close();
        }
        return musicOfList;
    }

}
