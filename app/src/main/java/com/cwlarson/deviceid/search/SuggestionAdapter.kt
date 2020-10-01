package com.cwlarson.deviceid.search

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

internal class SuggestionAdapter(context: Context, resource: Int, objects: List<String>,
                                 textViewResourceId: Int) :
        ArrayAdapter<String>(context, resource, textViewResourceId, objects) {
    private val items = ArrayList<String>(objects)
    private var filterItems = mutableListOf<String>()
    private var filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults =
                FilterResults().apply {
                    filterItems.clear()
                    filterItems.addAll(if (constraint != null) {
                        items.filter { s -> s.contains(constraint, true) }
                    } else items)
                    values = filterItems
                    count = filterItems.size
                }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (objects.isNotEmpty()) clear()
            if (results != null && results.count > 0) {
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