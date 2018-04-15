package com.example.amia.zplayer.ControlUtil;

import android.content.Context;

import com.example.amia.zplayer.DAO.DownloadDao;
import com.example.amia.zplayer.DTO.MusicDownLoadInfo;
import com.example.amia.zplayer.R;
import com.example.amia.zplayer.util.ConvertStringCode;
import com.example.amia.zplayer.util.DownloadUtil;
import com.example.amia.zplayer.util.NetUtils;

import java.net.MalformedURLException;

/**
 * Created by Amia on 2018/3/18.
 */

public class SearchMusicJson {

    public String searchMusic(Context context, String ... searchCond) throws MalformedURLException {
        StringBuffer sb=getUrlString(context);
        sb.append(ConvertStringCode.toBase64Second(searchCond[0]));
        if(searchCond.length==2){
            sb.append("&pageNo=");
            sb.append(searchCond[1]);
        }
        String res=NetUtils.requestDataFromNet(context,sb.toString());
        return res;
    }

    private StringBuffer getUrlString(Context context){
        StringBuffer sb=new StringBuffer("http://");
        sb.append(context.getResources().getString(R.string.down_host));
        sb.append("findMusic?cond=");
        return sb;
    }

    public static void addToDownDatabase(Context context,MusicDownLoadInfo info){
        DownloadDao dao=new DownloadDao(context);
        dao.insert(info);
    }
}
