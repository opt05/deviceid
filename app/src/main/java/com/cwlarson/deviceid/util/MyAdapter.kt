package com.cwlarson.deviceid.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cwlarson.deviceid.BR
import com.cwlarson.deviceid.PermissionsActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item

internal class MyAdapter(private val handler: PermissionsActivity) : PagedListAdapter<Item, MyAdapter.CustomViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val TEXT_VIEW_TYPE = 0
        private const val CHART_VIEW_TYPE = 1
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                    oldItem.itemsTheSame(newItem)
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                    oldItem == newItem
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layout = if (viewType == CHART_VIEW_TYPE)
            R.layout.recycler_chart_view else R.layout.recycler_text_view
        val viewDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context), layout, parent, false)
        return CustomViewHolder(viewDataBinding)
    }

    override fun getItemViewType(position: Int): Int =
            getItem(position)?.chartitem?.let { CHART_VIEW_TYPE } ?: TEXT_VIEW_TYPE

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, handler)
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            holder.clear()
        }
        getItem(position)?.let { holder.bind(it, handler) }
    }

    internal inner class CustomViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, activity: PermissionsActivity?) {
            binding.setVariable(BR.item, item)
            activity?.let {
                binding.setVariable(BR.handler, ItemClickHandler(it, item))
            }
            binding.executePendingBindings()
        }

        fun clear() {
            binding.setVariable(BR.item, null)
            binding.setVariable(BR.handler, null)
            binding.executePendingBindings()
        }
    }
}
