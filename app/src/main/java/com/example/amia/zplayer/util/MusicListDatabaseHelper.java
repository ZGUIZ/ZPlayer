package com.example.amia.zplayer.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;

/**
 * Created by Amia on 2017/8/16.
 */

public class MusicListDatabaseHelper extends SQLiteOpenHelper {

    public static final String lastPlayListName="last_list";


    private String createMusicList_list="create table "+ MusicListDao.table_name+"(_id integer primary key, list_name varchar(20) unique, isDefault int default '0',isShow int default '1')";
    private String createMusicOfList="create table "+ MusicOfListDao.tableName+"(n_id integer primary key, list_id int,music_id int, foreign key(list_id) references "+ MusicListDao.table_name+"(_id))";
    public static String musilst_list="musiclist_list";
    public static String DataBaseName="ZMusicPlayerDatabase";
    public static int DataBaseVersion=1;

    public MusicListDatabaseHelper(Context context) {
        super(context, DataBaseName, null, DataBaseVersion);
        //this.DatabaseName=DataBaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createMusicList_list);
        db.execSQL(createMusicOfList);
        db.execSQL("create unique index music_list_not_repeat on MusicOfList(list_id,music_id)");

        ContentValues contentValues=new ContentValues();
        //contentValues.put("_id",1);
        contentValues.put("list_name","最近播放");
        contentValues.put("isDefault",1);
        contentValues.put("isShow",0);
        db.insert(MusicListDao.table_name,null,contentValues);

        contentValues=new ContentValues();
        //contentValues.put("_id",2);
        contentValues.put("list_name","下载管理");
        contentValues.put("isDefault",1);
        contentValues.put("isShow",0);
        db.insert(MusicListDao.table_name,null,contentValues);

        contentValues=new ContentValues();
        //contentValues.put("_id",3);
        contentValues.put("list_name","我喜欢");
        contentValues.put("isDefault",1);
        db.insert(MusicListDao.table_name,null,contentValues);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
