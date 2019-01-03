package com.cwlarson.deviceid

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.cwlarson.deviceid.database.AppDatabase
import com.cwlarson.deviceid.database.MainActivityViewModel
import com.cwlarson.deviceid.databinding.ActivityMainBinding
import com.cwlarson.deviceid.util.TabsViewPagerAdapter

private const val ACTION_SEARCH = "com.cwlarson.deviceid.SEARCH"
internal const val PREFERENCE_HIDE_UNAVAILABLE = "hide_unables"

class MainActivity : PermissionsActivity() {
    private var adapter: TabsViewPagerAdapter? = null
    private var searchViewMenuItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var launchSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setSupportActionBar(binding.myToolbar)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        adapter = TabsViewPagerAdapter(supportFragmentManager, this)
        binding.viewpager.adapter = adapter
        //Prevent reloading of views on tab switching
        binding.viewpager.offscreenPageLimit = adapter?.count ?: 1
        // Give the TabLayout the ViewPager
        binding.tabs.setupWithViewPager(binding.viewpager)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && ACTION_SEARCH == intent.action) {
            //Shortcut launched for search
            if (searchViewMenuItem == null)
                launchSearch = true
            else {
                searchViewMenuItem?.expandActionView()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.base_menu, menu)
        inflater.inflate(R.menu.search_menu, menu)
        // Get checkable menu item value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        menu.findItem(R.id.action_hide_unables).isChecked = sharedPreferences.getBoolean(PREFERENCE_HIDE_UNAVAILABLE, false)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchViewMenuItem = menu.findItem(R.id.search)
        searchView = searchViewMenuItem?.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        if (launchSearch) {
            searchViewMenuItem?.expandActionView()
            launchSearch = false
        }
        val model = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        if (model.searchString?.isNotBlank() == true) {
            searchViewMenuItem?.expandActionView()
            searchView?.setQuery(model.searchString, false)
            if(model.searchFocus) searchView?.clearFocus()
        }
        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                model.searchString = query
                model.searchFocus = searchView?.hasFocus() ?: false
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                model.searchString = newText
                model.searchFocus = searchView?.hasFocus() ?: false
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hide_unables -> {
                item.isChecked = !item.isChecked
                PreferenceManager.getDefaultSharedPreferences(applicationContext).edit {
                    putBoolean(PREFERENCE_HIDE_UNAVAILABLE, item.isChecked)
                }
                // Refresh tabs due to data added/removed
                adapter?.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        adapter?.destroy()
        AppDatabase.destroyInstance()
        super.onDestroy()
    }
}
