package com.cwlarson.deviceid.tabs

import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.sdkToVersion
import kotlinx.android.synthetic.main.recycler_header_view.view.*

typealias onItemClick = (item: Item, isLongClick: Boolean) -> Unit

class TabsAdapter(private val clickHandler: onItemClick) :
        ListAdapter<Item, TabsAdapter.CustomViewHolder>(DIFF_CALLBACK),
        HeaderInterface<TabsAdapter.HeaderViewHolder> {

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder =
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.recycler_header_view, parent, false))

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder) = holder.bind()

    companion object {
        private const val TEXT_VIEW_TYPE = 0
        private const val CHART_VIEW_TYPE = 1
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                    oldItem.title == newItem.title
                            && oldItem.titleFormatArgs?.contentEquals(newItem.titleFormatArgs
                            ?: emptyArray()) == true
                            && oldItem.itemType == newItem.itemType

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                    oldItem == newItem
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                if (viewType == CHART_VIEW_TYPE) R.layout.recycler_chart_view
                else R.layout.recycler_text_view, parent, false)
        return CustomViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int =
            if (getItem(position)?.subtitle is ItemSubtitle.Chart) CHART_VIEW_TYPE
            else TEXT_VIEW_TYPE

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        getItem(position)?.let { holder.bind(it, clickHandler) }
    }

    inner class CustomViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val top: View? = view.findViewById(R.id.top_layout)
        private val textTitle: TextView? = view.findViewById(R.id.item_title)
        private val textSubTitle: TextView = view.findViewById(R.id.item_subtitle)
        private val chartIcon: ImageView? = view.findViewById(R.id.left_icon)
        private val chartBar: ProgressBar? = view.findViewById(R.id.item_bar)
        fun bind(item: Item, clickHandler: onItemClick) {
            top?.setOnClickListener { clickHandler(item, false) }
            top?.setOnLongClickListener {
                clickHandler(item, true)
                true
            }
            textTitle?.text = item.getFormattedString(view.context)
            when (val sub = item.subtitle) {
                is ItemSubtitle.Text -> {
                    textSubTitle.text =
                            if (sub.data.isNullOrBlank())
                                view.context.getString(R.string.not_found)
                            else sub.data
                }
                is ItemSubtitle.Chart -> {
                    chartIcon?.setImageResource(sub.chart.chartDrawable)
                    chartBar?.apply {
                        progress = sub.chart.chartPercentage
                        isVisible = !sub.chart.chartSubtitle.isNullOrBlank()
                    }
                    textSubTitle.text =
                            if (sub.chart.chartSubtitle.isNullOrBlank())
                                view.context.getString(R.string.not_found)
                            else sub.chart.chartSubtitle
                }
                is ItemSubtitle.NoLongerPossible -> {
                    textSubTitle.text = view.context.getString(R.string.no_longer_possible,
                            sub.version.sdkToVersion())
                }
                is ItemSubtitle.NotPossibleYet -> {
                    textSubTitle.text = view.context.getString(R.string.not_possible_yet,
                            sub.version.sdkToVersion())
                }
                is ItemSubtitle.Permission -> {
                    textSubTitle.text = view.context.getString(R.string.permission_item_subtitle,
                            view.context.packageManager.getPermissionInfo(sub.androidPermission, 0)
                                    .loadLabel(view.context.packageManager))
                }
                else -> textSubTitle.text = view.context.getString(R.string.not_found)
            }
        }
    }

    inner class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            view.header_count.text = this@TabsAdapter.itemCount.toString()
        }
    }
}

class HeaderDecoration(private val adapter: TabsAdapter) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == 0) {
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
        header.layout(left, 0, right, header.measuredHeight)
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(child) == 0) {
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
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)
        return holder
    }
}

/**
 * The interface to assist the [HeaderDecoration] in creating the binding the header views
 * @param T the header view holder
 */
interface HeaderInterface<T : RecyclerView.ViewHolder> {
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
