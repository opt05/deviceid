package com.cwlarson.deviceid.util

import android.app.Activity
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.TabFragment

class TabsViewPagerAdapter(fm: FragmentManager, activity: Activity) : FragmentPagerAdapter(fm) {
    private val tabTitles: Array<String>
    private val mTabReferenceMap = SparseArray<TabFragment>()

    init {
        val context = activity.applicationContext
        this.tabTitles = context.resources.getStringArray(R.array.tab_item_titles)
    }

    override fun getCount(): Int {
        return tabTitles.size
    }

    override fun getItem(position: Int): Fragment {
        val tab = TabFragment.newInstance(position)
        mTabReferenceMap.put(position, tab)
        return tab
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }

    fun destroy() {
        mTabReferenceMap.clear()
    }
}

