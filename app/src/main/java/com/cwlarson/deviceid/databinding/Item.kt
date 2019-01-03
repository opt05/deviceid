package com.cwlarson.deviceid.databinding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters

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

/**
 * Helper class for recyclerview items
 */
@Entity(tableName = "item",
        primaryKeys = ["title", "itemtype"])
@TypeConverters(ItemTypeConverter::class)
open class Item(
        @get:Bindable
        var title: String = "",
        @get:Bindable
        @TypeConverters(ItemTypeConverter::class)
        var itemtype : ItemType
) : BaseObservable() {
    @get:Bindable
    var subtitle: String? = null
    @get:Bindable
    @Embedded
    var chartitem : ChartItem? = null
    @get:Bindable
    @Embedded
    var unavailableitem : UnavailableItem? = null

    fun itemsTheSame(other: Any?): Boolean {
        return other is Item &&
                this.title == other.title &&
                this.itemtype == other.itemtype
    }

    override fun equals(other: Any?): Boolean {
        return other is Item &&
                this.title == other.title &&
                this.itemtype == other.itemtype &&
                this.unavailableitem == other.unavailableitem &&
                this.subtitle == other.subtitle &&
                this.chartitem == other.chartitem
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + itemtype.hashCode()
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (chartitem?.hashCode() ?: 0)
        result = 31 * result + (unavailableitem?.hashCode() ?: 0)
        return result
    }
}
