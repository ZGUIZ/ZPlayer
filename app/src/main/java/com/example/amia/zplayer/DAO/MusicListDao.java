package com.example.amia.zplayer.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.amia.zplayer.DTO.MusicList;
import com.example.amia.zplayer.util.MusicListDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Amia on 2017/8/24.
 */

public class MusicListDao {

    public static String table_name="musiclist_list";

    private MusicListDatabaseHelper dbhelper;
    private Context context;
    public MusicListDao(Context context){
        this.context=context;
        dbhelper=new MusicListDatabaseHelper(context);
    }

    /**
     * 用于向musiclist_list表中插入数据
     * @param musicList
     * @return 是否插入成功
     */
    public boolean insert(MusicList musicList){
        return insert(musicList.get_Name());
    }

    /**
     * 用于向musiclist_list表中插入数据
     * @param list_name 列表的名称
     * @return 是否插入成功
     */
    public boolean insert(String list_name){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            ContentValues contentValues=new ContentValues();
            //contentValues.put("_id",musicList.get_id());
            contentValues.put("list_name",list_name);
            contentValues.put("isDefault",0);
            long res=db.insert(MusicListDatabaseHelper.musilst_list,null,contentValues);
            if(res!=-1) {
                Log.i("MusicListDao","res="+res);
                flag = true;
            }
            db.close();
        }
        return flag;
    }

    /**
     * 用于获取musiclist_list表中所有信息
     * @return
     */
    public ArrayList<MusicList> queryAllList(){
        ArrayList<MusicList> array=null;
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        if(db.isOpen()){
            Cursor cursor=db.query(table_name,null,"isShow = '1'",null,null,null,null);
            if(cursor!=null&&cursor.getCount()>0){
                array=new ArrayList<>();
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    MusicList musicList=new MusicList();
                    musicList.set_id(cursor.getInt(0));
                    musicList.set_Name(cursor.getString(1));
                    array.add(musicList);
                    cursor.moveToNext();
                }
                cursor.close();
            }
            db.close();
        }
        return array;
    }

    /**
     * 用于获取musiclist_list表中非默认表
     * @return
     */
    public ArrayList<MusicList> queryListNotDefault(){
        ArrayList<MusicList> array=new ArrayList<>();
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        if(db.isOpen()){
            Cursor cursor=db.query(table_name,null,"isDefault = '0'",null,null,null,null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    MusicList musicList=new MusicList();
                    musicList.set_id(cursor.getInt(0));
                    musicList.set_Name(cursor.getString(1));
                    array.add(musicList);
                    cursor.moveToNext();
                }
                cursor.close();
            }
            db.close();
        }
        return array;
    }

    /**
     * 用于删除音乐列表
     * @param musicList 传递一个储存要删除列表信息的对象
     * @return
     */
    public boolean deleteList(MusicList musicList){
        return deleteList(musicList.get_id());
    }

    /**
     * 用于删除音乐列表
     * @param _id 传入要删除列表的ID
     * @return
     */
    public boolean deleteList(int _id){
        boolean flag=false;
        MusicOfListDao musicOfListDao=new MusicOfListDao(context);
        musicOfListDao.deleteMusicOfListByList(_id);
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            int res=db.delete(table_name,"_id = ?",new String[]{String.valueOf(_id)});


            if(res>0){
                flag=true;
            }
            db.close();

        }
        return flag;
    }

    /**
     * 用于更新音乐列表的信息
     * @param musicList
     * @return
     */
    public boolean alterListName(MusicList musicList){
        return alterListName(musicList.get_id(),musicList.get_Name());
    }

    /**
     * 用于更新音乐列表的表名
     * @param _id
     * @param name
     * @return
     */
    public boolean alterListName(int _id,String name){
        boolean flag=false;
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        if(db.isOpen()){
            ContentValues contentValues=new ContentValues();
            contentValues.put("list_name",name);
            int rowcount=db.update(table_name,contentValues,"_id = ?",new String[]{String.valueOf(_id)});
            if(rowcount>0){
                flag=true;
            }
            db.close();
        }
        return flag;
    }

    /**
     * 用于获取音乐列表的ID
     * @param list_name
     * @return
     */
    public int getList_id(String list_name){
        SQLiteDatabase db=dbhelper.getReadableDatabase();
        int list_id=-1;
        if(db.isOpen()){
            Cursor cursor=db.query(table_name,null,"list_name = ?",new String[]{list_name},null,null,null);
            if(cursor.getColumnCount()>0){
                cursor.moveToFirst();
                list_id=cursor.getInt(0);
                Log.i("MusicListDao","name="+list_name);
                Log.i("MusicListDao","_id="+list_id);
            }
        }
        return list_id;
    }

}
