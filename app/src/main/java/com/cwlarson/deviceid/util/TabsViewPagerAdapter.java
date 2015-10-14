package com.cwlarson.deviceid.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.TabFragment;

public class TabsViewPagerAdapter extends FragmentPagerAdapter {
    private final String[] tabTitles;

    public TabsViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.tabTitles = context.getResources().getStringArray(R.array.tab_item_titles);
    }

    @Override
    public int getItemPosition(Object object) {
        //return super.getItemPosition(object);
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}

