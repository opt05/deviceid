package com.cwlarson.deviceid

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.cwlarson.deviceid.database.AppDatabase
import com.cwlarson.deviceid.databinding.ActivityMainBinding
import com.cwlarson.deviceid.util.TabsViewPagerAdapter


class MainActivity : PermissionsActivity() {
    private var mAdapter: TabsViewPagerAdapter? = null
    private var mSearchViewMenuItem: MenuItem? = null
    private var mSearchView: SearchView? = null
    private var launchSearch: Boolean = false
    private var mSearchString: String? = null
    private var mSearchFocus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_MainTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setSupportActionBar(binding.myToolbar)

        if (savedInstanceState != null) {
            mSearchString = savedInstanceState.getString(SEARCH_KEY)
            mSearchFocus = savedInstanceState.getBoolean(SEARCH_FOCUS_KEY)
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mAdapter = TabsViewPagerAdapter(supportFragmentManager, this)
        binding.viewpager.adapter = mAdapter
        binding.viewpager.offscreenPageLimit = mAdapter?.count ?: 1 //Prevent reloading of
        // views on
        // tab switching
        // Give the TabLayout the ViewPager
        binding.tabs.setupWithViewPager(binding.viewpager)
        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mSearchView?.let {
            mSearchString = it.query.toString()
            mSearchFocus = it.hasFocus()
            outState.putString(SEARCH_KEY, mSearchString)
            outState.putBoolean(SEARCH_FOCUS_KEY, mSearchFocus)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && ACTION_SEARCH == intent.action) {
            //Shortcut launched for search
            if (mSearchViewMenuItem == null)
                launchSearch = true
            else {
                mSearchViewMenuItem?.expandActionView()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.base_menu, menu)
        inflater.inflate(R.menu.search_menu, menu)
        // Get checkable menu item value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        menu.findItem(R.id.action_hide_unables).isChecked = sharedPreferences.getBoolean("hide_unables", false)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchViewMenuItem = menu.findItem(R.id.search)
        mSearchView = mSearchViewMenuItem?.actionView as SearchView
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        if (launchSearch) {
            mSearchViewMenuItem?.expandActionView()
            launchSearch = false
        }
        if (!TextUtils.isEmpty(mSearchString)) {
            mSearchViewMenuItem?.expandActionView()
            mSearchView?.setQuery(mSearchString, false)
            if (!mSearchFocus) mSearchView?.clearFocus()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hide_unables -> {
                item.isChecked = !item.isChecked
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sharedPreferences.edit()
                editor.putBoolean("hide_unables", item.isChecked)
                editor.apply()
                // Refresh tabs due to data added/removed
                mAdapter?.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mAdapter?.destroy()
        AppDatabase.destroyInstance()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_SEARCH = "com.cwlarson.deviceid.SEARCH"
        private const val SEARCH_KEY = "SEARCH_KEY"
        private const val SEARCH_FOCUS_KEY = "SEARCH_FOCUS_KEY"
    }
}
