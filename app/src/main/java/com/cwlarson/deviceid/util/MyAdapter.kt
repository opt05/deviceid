package com.cwlarson.deviceid.util

import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cwlarson.deviceid.BR
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.RecyclerHeaderViewBinding

internal class MyAdapter(private val handler: ItemClickHandler) :
        PagedListAdapter<Item, MyAdapter.CustomViewHolder>(DIFF_CALLBACK),
        HeaderInterface<MyAdapter.HeaderViewHolder> {

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder =
            HeaderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.recycler_header_view, parent, false))

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder) = holder.bind()

    companion object {
        private const val TEXT_VIEW_TYPE = 0
        private const val CHART_VIEW_TYPE = 1
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                    oldItem.title == newItem.title && oldItem.itemType == newItem.itemType
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
            getItem(position)?.chartItem?.let { CHART_VIEW_TYPE } ?: TEXT_VIEW_TYPE

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        getItem(position)?.let { holder.bind(it, handler) }
    }

    internal inner class CustomViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item, handler: ItemClickHandler) {
            binding.setVariable(BR.item, item)
            binding.setVariable(BR.handler, handler)
            binding.executePendingBindings()
        }
    }

    internal inner class HeaderViewHolder(private val binding: RecyclerHeaderViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.count = this@MyAdapter.itemCount.toString()
            binding.executePendingBindings()
        }
    }
}

internal class HeaderDecoration(private val adapter: MyAdapter): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if(parent.getChildAdapterPosition(view) == 0) {
            outRect.top = getHeader(parent).itemView.height
        } else outRect.setEmpty()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            c.clipRect(left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }
        val header = getHeader(parent).itemView
        header.layout(left,0, right, header.measuredHeight)
        for(i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if(parent.getChildAdapterPosition(child) == 0) {
                c.save()
                val height = header.measuredHeight.toFloat()
                val top = child.top - height
                c.translate(0F, top)
                header.draw(c)
                c.restore()
                break
            }
        }
    }

    private fun getHeader(parent: RecyclerView): RecyclerView.ViewHolder {
        val holder = adapter.onCreateHeaderViewHolder(parent)
        val header = holder.itemView

        adapter.onBindHeaderViewHolder(holder)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth,
                View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight,
                View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.paddingLeft + parent.paddingRight,
                header.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom,
                header.layoutParams.height)
        header.measure(childWidth, childHeight)
        header.layout(0,0,header.measuredWidth, header.measuredHeight)
        return holder
    }
}

/**
 * The interface to assist the [HeaderDecoration] in creating the binding the header views
 * @param T the header view holder
 */
internal interface HeaderInterface<T: RecyclerView.ViewHolder> {
    /**
     * Creates a new header ViewHolder
     * @param parent the header's view parent
     * @return a view holder for the created view
    **/
    fun onCreateHeaderViewHolder(parent: ViewGroup): T
    /**
     * Updates the header view to reflect the header data for the given position
     * @param holder the header view holder
    **/
    fun onBindHeaderViewHolder(holder: T)
}
