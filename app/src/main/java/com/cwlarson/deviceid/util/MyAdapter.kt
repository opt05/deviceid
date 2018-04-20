package com.cwlarson.deviceid.util

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cwlarson.deviceid.BR
import com.cwlarson.deviceid.PermissionsActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item

internal class MyAdapter(parentActivity: PermissionsActivity) : ListAdapter<Item, MyAdapter.CustomViewHolder>(
        object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.itemsTheSame(newItem)
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem})
{
    private var handler: PermissionsActivity? by WeakReferenceDelegate()

    init {
        handler = parentActivity
    }

    fun setItems(itemList: List<Item>?) {
        submitList(itemList)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val viewDataBinding: ViewDataBinding = if (viewType == CHART_VIEWTYPE)
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.recycler_chart_view, parent, false)
        else
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.recycler_text_view, parent, false)
        return CustomViewHolder(viewDataBinding)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).chartitem != null)
            CHART_VIEWTYPE
        else
            TEXT_VIEWTYPE
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(getItem(position), handler)
    }

    internal inner class CustomViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, activity: PermissionsActivity?) {
            binding.setVariable(BR.item, item)
            activity?.let {
                binding.setVariable(BR.handler, ItemClickHandler(it, item))
            }
            binding.executePendingBindings()
        }
    }

    companion object {
        private const val TEXT_VIEWTYPE = 0
        private const val CHART_VIEWTYPE = 1
    }
}
