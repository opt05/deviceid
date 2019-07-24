package com.cwlarson.deviceid.databinding

import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.TypeConverter

@Keep
enum class ItemType(val value: Int) {
    NONE(-1),
    DEVICE(1),NETWORK(2),SOFTWARE(3),HARDWARE(4)
}

class ItemTypeConverter {
    @TypeConverter
    fun toItemType(type: Int?): ItemType? = type?.let {
       ItemType.values().associateBy(ItemType::value)[it]
    }

    @TypeConverter
    fun toInt(type: ItemType?): Int? = type?.value
}

@Entity(tableName = "item",
        primaryKeys = ["title", "itemType"])
data class Item(
        @get:Bindable
        var title: String = "",
        @get:Bindable
        var itemType : ItemType,
        @get:Bindable
        var subtitle: String? = null,
        @get:Bindable
        @Embedded var chartItem : ChartItem? = null,
        @get:Bindable
        @Embedded var unavailableItem : UnavailableItem? = null
): BaseObservable()
