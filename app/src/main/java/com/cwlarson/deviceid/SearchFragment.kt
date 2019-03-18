package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.FragmentSearchBinding
import com.cwlarson.deviceid.util.HeaderDecoration
import com.cwlarson.deviceid.util.MyAdapter

class SearchFragment: Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        @Suppress("FieldCanBeLocal","unused")
        private const val TAG = "SearchFragment"
    }
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        preferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(this@SearchFragment) }
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater, R.layout.fragment_search,
                container, false).apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = MyAdapter(topLayout, activity)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.addItemDecoration(
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            recyclerView.addItemDecoration(HeaderDecoration(adapter), -1)
            lifecycleOwner = this@SearchFragment
            model = activity?.let {
                ViewModelProviders.of(it).get<SearchItemsViewModel>().apply {
                    setHideUnavailable(preferences.getBoolean(getString(R.string.pref_hide_unavailable_key), false))
                    searchItems.observe(this@SearchFragment, Observer { itemModels ->
                        adapter.submitList(itemModels)
                        itemsCount.value = itemModels?.size ?: 0
                        isLoading.value = false
                        // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                        executePendingBindings()
                    })
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            getString(R.string.pref_hide_unavailable_key) -> {
                binding.model?.setHideUnavailable(
                        sharedPreferences?.getBoolean(key, false) ?: false)
            }
        }
    }
}