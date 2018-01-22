package com.cwlarson.deviceid.database

import android.content.Context
import android.text.TextUtils
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import java.lang.ref.WeakReference

class ItemAdder internal constructor(context: Context, database: AppDatabase) {
    private var context = WeakReference<Context>(null)
    private var db: AppDatabase

    init {
        this.context = WeakReference(context)
        this.db = database
    }

    fun addItems(item: Item?) {
        if(context.get()==null || item==null) return
        if (TextUtils.isEmpty(item.subtitle) && item.unavailableitem == null) {
            item.unavailableitem = UnavailableItem(UnavailableType.NOT_FOUND,
                    context.get()!!.getString(R.string.not_found))
        } else if(TextUtils.isEmpty(item.subtitle) && item.unavailableitem != null) {
            if(item.unavailableitem?.unavailabletype == UnavailableType.NO_LONGER_POSSIBLE) {
                item.unavailableitem?.unavailablesupporttext =
                        context.get()?.resources?.getString(R.string.no_longer_possible, item.unavailableitem?.unavailablesupporttext)
            } else if (item.unavailableitem?.unavailabletype == UnavailableType.NOT_POSSIBLE_YET) {
                item.unavailableitem?.unavailablesupporttext =
                        context.get()?.resources?.getString(R.string.not_possible_yet, item.unavailableitem?.unavailablesupporttext)
            }
        }
        db.itemDao().insertItems(item)
    }
}
