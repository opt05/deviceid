@file:JvmName("SystemUtils") // pretty name for utils class if called from
package com.cwlarson.deviceid.util

import android.content.Context
import android.support.annotation.WorkerThread
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.cwlarson.deviceid.databinding.Item
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext

fun calculateNoOfColumns(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return (dpWidth / 300).toInt()
}

fun calculateBottomSheetMaxWidth(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return if(dpWidth>750) 750 else ViewGroup.LayoutParams.MATCH_PARENT
}

abstract class DiffUtilAdapter<D : Item, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    protected var dataset: List<D> = listOf()
    private val diffCallback by lazy(LazyThreadSafetyMode.NONE) { ItemDiffCallback<D>() }
    private val eventActor = actor<List<D>>(newSingleThreadContext("DiffUtilAdapter"),
            capacity = Channel.CONFLATED) {
        for (list in channel) internalUpdate(list)
    }

    fun update (list: List<D>) = eventActor.offer(list)

    @WorkerThread
    private suspend fun internalUpdate(list: List<D>) {
        val finalList = prepareList(list)
        val result = DiffUtil.calculateDiff(diffCallback.apply { this.update(dataset, finalList) }, false)
        launch(UI) {
            dataset = finalList
            result.dispatchUpdatesTo(this@DiffUtilAdapter)
        }.join()
    }

    protected open fun prepareList(list: List<D>) : List<D> = ArrayList(list)

    private inner class ItemDiffCallback<T : Item> : DiffUtil.Callback() {
        lateinit var oldList: List<T>
        lateinit var newList: List<T>

        fun update(oldList: List<T>, newList: List<T>) {
            this.oldList = oldList
            this.newList = newList
        }

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].itemsTheSame(newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                newList[newItemPosition] == oldList[oldItemPosition]
    }
}