package com.cwlarson.deviceid

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.ActivitySearchBinding
import com.cwlarson.deviceid.util.MyAdapter

class SearchActivity : PermissionsActivity(), OnSharedPreferenceChangeListener {
    private lateinit var binding : ActivitySearchBinding
    private lateinit var adapter : MyAdapter
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.setLifecycleOwner(this)
        adapter = MyAdapter(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
        binding.searchRecyclerview.recyclerView.adapter = adapter
        binding.searchRecyclerview.recyclerView.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if(intent!=null && Intent.ACTION_SEARCH == intent.action) {
            val query: String = intent.getStringExtra(SearchManager.QUERY)
            title = getString(R.string.search_activity_title, query)
            binding.model = ViewModelProviders.of(this)
                    .get(SearchItemsViewModel::class.java)
            binding.model?.setHideUnavailable(preferences.getBoolean(PREFERENCE_HIDE_UNAVAILABLE, false))
            binding.model?.getAllSearchItems(query)?.observe(this, Observer { itemModels ->
                adapter.submitList(itemModels)
                binding.model?.itemsCount?.value = itemModels?.size ?: 0
                binding.model?.isLoading?.value = false
                // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                binding.executePendingBindings()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            PREFERENCE_HIDE_UNAVAILABLE -> {
                binding.model?.setHideUnavailable(
                        sharedPreferences?.getBoolean(key, false) ?: false)
            }
        }
    }

}
