package com.example.amia.zplayer.util;

import com.example.amia.zplayer.DTO.Mp3Info;

import java.util.List;
import java.util.Random;

/**
 * Created by Amia on 2018/4/2.
 */

public class RandomIdUtil {
    public static int getRandomId(List<Mp3Info> infos){
        Random random=new Random();
        int id=-1;
        if(infos==null){
            return id;
        }
        boolean flag=true;
        while(flag){
            id=random.nextInt();
            flag=false;
            for(Mp3Info info:infos){
                if(info.getId()==id){
                    flag=true;
                    break;
                }
            }
        }
        return id;
    }
}
