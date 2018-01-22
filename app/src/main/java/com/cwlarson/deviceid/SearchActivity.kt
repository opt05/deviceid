package com.cwlarson.deviceid

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.ActivitySearchBinding
import com.cwlarson.deviceid.util.MyAdapter

class SearchActivity : PermissionsActivity() {
    lateinit var binding : ActivitySearchBinding
    private lateinit var mAdapter : MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.isLoading = true
        mAdapter = MyAdapter(this)
        binding.searchRecyclerview?.recyclerView?.adapter = mAdapter
        binding.searchRecyclerview?.recyclerView?.addItemDecoration(
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
            val mModel: SearchItemsViewModel = ViewModelProviders.of(this)
                    .get(SearchItemsViewModel::class.java)
            mModel.getAllSearchItems(query).observe(this, Observer { itemModels ->
                mAdapter.setItems(itemModels)
                binding.itemsCount = itemModels?.size ?: 0
                binding.isLoading = false
                // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                binding.executePendingBindings()
            })
        }
    }

}
