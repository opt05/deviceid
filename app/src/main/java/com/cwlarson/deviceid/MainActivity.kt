package com.cwlarson.deviceid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.ActivityMainBinding
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.util.SearchClickHandler
import kotlinx.coroutines.*
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        @Suppress("FieldCanBeLocal","unused")
        private const val TAG = "MainActivity"
    }
    private val topLevelDestinations = hashSetOf(R.id.tab_device_dest,
            R.id.tab_network_dest, R.id.tab_software_dest, R.id.tab_hardware_dest)
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var searchHistoryAdapter: SuggestionAdapter<String>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).also {
            it.registerOnSharedPreferenceChangeListener(this)
        }
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout
                .activity_main).apply {
            lifecycleOwner = this@MainActivity
            model = ViewModelProviders.of(this@MainActivity).get()
            setSupportActionBar(toolbar)
            model?.apply {
                contentInsetStartWithNavigationDefault = toolbar.contentInsetStartWithNavigation
                contentInsetEndDefault = toolbar.contentInsetEnd
                contentInsetStartDefault = toolbar.contentInsetStart
            }
        }
        findNavController(R.id.nav_host_fragment).apply {
            //Setup menu
            binding.menuToolbar.run {
                menuInflater.inflate(R.menu.base_menu, menu)
                setOnMenuItemClickListener { item ->
                    item.onNavDestinationSelected(this@apply) ||
                            super.onOptionsItemSelected(item)
                }
            }
            setupActionBarWithNavController(this@apply,
                    AppBarConfiguration.Builder(topLevelDestinations).build())
            binding.bottomNavigation.setupWithNavController(this@apply)
            addOnDestinationChangedListener { _, destination, _ ->
                binding.model?.hideSearchBar?.value = !topLevelDestinations.contains(destination
                        .id) && destination.id != R.id.search_fragment_dest
                binding.model?.hideBottomBar?.value = !topLevelDestinations.contains(destination
                        .id) || destination.id == R.id.search_fragment_dest
                (destination.id == R.id.search_fragment_dest).apply {
                    if (this) supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.model?.isSearchOpen?.value = this
                    if (!this && binding.searchBar.query.isNotBlank())
                        binding.searchBar.setQuery("", false)
                }
            }
            binding.searchHandler = SearchClickHandler(this, binding.searchBar)
        }
        val searchModel = ViewModelProviders.of(this).get<SearchItemsViewModel>()
        binding.searchBar.apply {
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if(!hasFocus && preferences.getBoolean(
                                getString(R.string.pref_search_history_key), false))
                    query?.let { saveSearchHistoryItem(it.toString()) }
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { saveSearchHistoryItem(it) }
                    searchModel.setSearchText(query)
                    binding.searchHandler?.onSearchSubmit(query)
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    //if(!preferences.getBoolean(getString(R.string.pref_search_history_key), false)) {
                        searchModel.setSearchText(query)
                       binding.searchHandler?.onSearchSubmit(query)
                    //}
                    return false
                }
            })
            // Load history items in searchview
            findViewById<SearchView.SearchAutoComplete?>(R.id.search_src_text)?.apply {
                @ColorRes val colorRes = TypedValue().run {
                    this@MainActivity.theme.resolveAttribute(R.attr.colorBackgroundFloating, this, true)
                    resourceId
                }
                setDropDownBackgroundResource(colorRes)
                searchHistoryAdapter = SuggestionAdapter(this@MainActivity,
                        R.layout.searchview_history_item,
                        getSearchHistoryItems(preferences), android.R.id.text1)
                @SuppressLint("RestrictedApi")
                threshold = 0
                setAdapter(searchHistoryAdapter)
                setOnItemClickListener { _, _, position, _ ->
                    setQuery(searchHistoryAdapter.getItem(position), true)
                }
            } ?: Log.wtf(TAG, "SearchView.SearchAutoComplete id has changed and requires maintenance")

        }
        launch(Dispatchers.IO) {
            (savedInstanceState == null && intent.action != Intent.ACTION_SEARCH).apply {
                if(this) delay(TimeUnit.SECONDS.toMillis(2))
                binding.model?.titleVisibility?.postValue(Pair(!this, View.GONE))
            }
        }
        if (savedInstanceState == null) {
            binding.model?.loadAllData(this@MainActivity)
            intent?.handle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE.value) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // GRANTED: Force new data updates
                binding.model?.loadAllData(this@MainActivity)
            } else {
                // DENIED: We do nothing (it is handled by the ViewAdapter)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handle()
    }

    private fun Intent.handle() {
        if(this.action == Intent.ACTION_SEARCH) {
            binding.searchBar.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
            }
            binding.searchHandler?.onSearchSubmitIntent()
        }
    }

    override fun onSupportNavigateUp(): Boolean  =
            findNavController(R.id.nav_host_fragment).navigateUp()

    private inner class SuggestionAdapter<String>(context: Context, resource: Int, objects: MutableList<String>, textViewResourceId: Int) :
            ArrayAdapter<String>(context, resource, textViewResourceId, objects) {
        private val items = ArrayList<String>(objects)
        private var filterItems = mutableListOf<String>()
        private var filter = object: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults =
                FilterResults().apply {
                    filterItems.clear()
                    filterItems.addAll(if(constraint != null) {
                        items.filter { s -> s.toString().contains(constraint, true) }
                    } else items)
                    values = filterItems
                    count = filterItems.size
                }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(objects.isNotEmpty()) clear()
                if(results != null && results.count > 0) {
                    addAll(filterItems)
                    notifyDataSetChanged()
                } else notifyDataSetInvalidated()
            }
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItem(position: Int): String? = filterItems[position]

        override fun getCount(): Int = filterItems.size

        override fun getFilter(): Filter = filter

        fun updateList(newList: List<String>?) {
            items.clear()
            newList?.let { items.addAll(it) }
            notifyDataSetChanged()
        }
    }

    private fun saveSearchHistoryItem(item: String) {
        if(preferences.getBoolean(getString(R.string.pref_search_history_key), false) && item.isNotBlank()) {
            val jsonString = JSONArray(getSearchHistoryItems(preferences).run {
                // Remove item in the list if already in history
                removeAll { s -> s == item }
                // Prepend item to top of list and remove older ones if more than 10 items
                (listOf(item).plus(this)).take(10)
            }).toString()
            preferences.edit {
                putString(getString(R.string.pref_search_history_data_key), jsonString)
            }
        }
    }

    private fun getSearchHistoryItems(preferences: SharedPreferences?) = mutableListOf<String>().apply {
        val json = JSONArray(preferences?.getString(getString(R.string.pref_search_history_data_key), "[]"))
        (0..(json.length() - 1)).forEach {
            add(json.get(it).toString())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == getString(R.string.pref_search_history_data_key))
            searchHistoryAdapter.updateList(getSearchHistoryItems(sharedPreferences))
        else if(key == getString(R.string.pref_search_history_key) &&
                sharedPreferences?.getBoolean(key, false) == false)
            sharedPreferences.edit { remove(getString(R.string.pref_search_history_data_key)) }
    }
}
