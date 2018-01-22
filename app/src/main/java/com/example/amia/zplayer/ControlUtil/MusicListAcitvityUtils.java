package com.example.amia.zplayer.ControlUtil;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.example.amia.zplayer.DAO.MusicListDao;
import com.example.amia.zplayer.DAO.MusicOfListDao;
import com.example.amia.zplayer.DTO.Mp3Info;
import com.example.amia.zplayer.DTO.MusicList;
import com.example.amia.zplayer.DTO.MusicOfList;
import com.example.amia.zplayer.util.MusicResolverUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Amia on 2017/12/10.
 */

public class MusicListAcitvityUtils {

    /**
     * 音乐列表多选框单选
     * @param position
     * @param mp3Infos
     * @param checkedMus
     */
    public static void list_Check_Single(int position, ArrayList<Mp3Info> mp3Infos,ArrayList<Mp3Info> checkedMus){
        Mp3Info mp3Info=mp3Infos.get(position);
        boolean isSelect=!mp3Info.isSelected();
        mp3Info.setSelected(isSelect);
        if(isSelect){
            checkedMus.add(mp3Info);
        }
        else{
            checkedMus.remove(mp3Info);
        }
    }

    /**
     * 克隆列表
     * @param checkedMus
     * @return
     */
    public static ArrayList<Mp3Info> clone_list(ArrayList<Mp3Info> checkedMus){
        return (ArrayList<Mp3Info>)checkedMus.clone();
    }

    /**
     * 从内存上删除多个音乐文件，该操作不可逆
     * @param context
     * @param mp3Infos 删除的信息
     * @pram currentList  当前的音乐列表
     * @return
     */
    public static boolean deleteMusicFromDisk(Activity context, ArrayList<Mp3Info> mp3Infos,ArrayList<Mp3Info> currentList){
        Boolean flag=true;
        for(Mp3Info mp3Info:mp3Infos){
            //Log.i("deleting","title:"+mp3Info.getTitle());
            flag=deleteMusicFromDisk(context,mp3Info);
            if(!flag){
                //Log.i("delete","false:"+mp3Info.getTitle());
                flag=false;
            }
            currentList.remove(mp3Info);
        }
        return flag;
    }
    /**
     * 从内存上删除音乐，该操作不可逆
     * @param context
     * @param mp3Info
     * @return
     */
    public static boolean deleteMusicFromDisk(Activity context, Mp3Info mp3Info){
        MusicResolverUtil musicResolverUtil=new MusicResolverUtil(context);
        MusicOfListDao musicOfListDao=new MusicOfListDao(context);
        boolean fl=false;
        try {
            File file = new File(mp3Info.getUrl());
            boolean flag=musicResolverUtil.deleteMusice(mp3Info);
            boolean deldb=musicOfListDao.deleteMusicOfListByid((int)mp3Info.getId());
            boolean delete = file.delete();
            if(!flag){
                throw new RuntimeException("数据库删除失败");
            }
            else {
                if(delete&&deldb) {
                    fl = true;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return fl;
    }

    /**
     * 从列表上移除音乐
     * @param context
     * @param list_id
     * @param mp3Infos
     * @param checkedMus
     */
    public static void removeFromList(Context context,int list_id,ArrayList<Mp3Info> mp3Infos,ArrayList<Mp3Info> checkedMus){
        MusicOfListDao musicOfListDao=new MusicOfListDao(context);
        for(Mp3Info mp3Info:checkedMus){
            mp3Infos.remove(mp3Info);
            musicOfListDao.deleteMusicOfList(list_id,(int)mp3Info.getId());
        }
    }

    public static AlertDialog.Builder createAddDialog(Context context,AddToListListener listListener,ArrayList<MusicList> musicLists){
        //MusicListDao musicListDao=new MusicListDao(context);
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("添加至...");
        //ArrayList<MusicList> musicLists=musicListDao.queryAllList();
        String[] items=new String[musicLists.size()];
        for(int i=0;i<musicLists.size();i++){
            MusicList musicList=musicLists.get(i);
            items[i]=musicList.get_Name();
        }
        builder.setItems(items, listListener);
        return builder;
    }

    public static boolean addMusicToList(Context context,int list_id,ArrayList<Mp3Info> infos){
        boolean flag=true;
        MusicOfListDao musicOfListDao=new MusicOfListDao(context);
        for(Mp3Info mp3Info:infos){
            boolean sflag;
            sflag=addSingleMusicToList(musicOfListDao,list_id,(int)mp3Info.getId());
            if(!sflag){
                flag=false;
            }
        }
        return flag;
    }

    public static boolean addSingleMusicToList(Context context,int list_id,int mus_id){
        MusicOfListDao dao=new MusicOfListDao(context);
        return addSingleMusicToList(dao,list_id,mus_id);
    }

    public static boolean addSingleMusicToList(MusicOfListDao dao, int list_id, int mus_id){
        return dao.insertMusic(list_id,mus_id);
    }

    public static void shareMusic(Context context,ArrayList<Mp3Info> infos){
        ArrayList<File> files=new ArrayList<>();
        Intent intent=new Intent(Intent.ACTION_SEND);
        for(Mp3Info info:infos){
            File file=new File(info.getUrl());
            if(file.exists()){
                files.add(file);
            }
        }
        intent.putExtra(Intent.EXTRA_STREAM,files);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Share");
        intent.setType("*/*");
        context.startActivity(Intent.createChooser(intent,"分享到"));
    }

    private static void shareMusic(Context context,Mp3Info mp3Info){
        //Bitmap bitmap=bitMap.get(String.valueOf(mp3Info.getId()));
        File file=new File(mp3Info.getUrl());

        if(file.exists()) {
            Intent musicIntent = new Intent(Intent.ACTION_SEND);
            musicIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            musicIntent.putExtra(Intent.EXTRA_SUBJECT, "Share");
            //Log.i("MainActivity", "Path=" + file.getAbsolutePath());
            musicIntent.setType("*/*");
            context.startActivity(Intent.createChooser(musicIntent, "分享到"));
        }
    }
}
