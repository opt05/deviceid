package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.TabFragment;

public class TabsViewPagerAdapter extends FragmentPagerAdapter {
    private final String[] tabTitles;
    private final SparseArray<TabFragment> mTabReferenceMap = new SparseArray<>();

    public TabsViewPagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        Context context = activity.getApplicationContext();
        this.tabTitles = context.getResources().getStringArray(R.array.tab_item_titles);
    }

    public void update() {
        notifyDataSetChanged();
    }

    public TabFragment getFragment(int key){
        return mTabReferenceMap.get(key);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        TabFragment tab = TabFragment.newInstance(position);
        mTabReferenceMap.put(position,tab);
        return tab;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    public void destroy() {
        mTabReferenceMap.clear();
    }
}

