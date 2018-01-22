package com.example.amia.zplayer.Activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Created by Amia on 2017/12/18.
 */

class PlayingPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> array;
    public PlayingPagerAdapter(FragmentManager fm) {
        super(fm);
        array=new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=PlayingInfoFragment.newInstance(position);
        array.put(position,fragment);
        if(array.get(position)==null){
            Log.i("FragmentAdapter","null:  id="+position);
        }
        Log.i("FragmentAdapter","id="+position);
        return fragment;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container,int position){
        Fragment fragment=(Fragment)super.instantiateItem(container,position);
        array.put(position,fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public Fragment getFragment(int position){
        return array.get(position);
    }
}
