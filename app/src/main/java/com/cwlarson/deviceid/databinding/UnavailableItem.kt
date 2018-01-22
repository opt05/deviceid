package com.cwlarson.deviceid.databinding

import android.arch.persistence.room.Ignore
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.databinding.BaseObservable
import android.databinding.Bindable

enum class UnavailableType(val value: Int) {
    NOT_FOUND(1), NO_LONGER_POSSIBLE(2), NOT_POSSIBLE_YET(3),
    NEEDS_PERMISSION(4)
}
class UnavailableTypeConverter {
    @TypeConverter
    fun toUnavailableType(type: Int?): UnavailableType? = type?.let {
        UnavailableType.values().associateBy(UnavailableType::value)[it]
    }

    @TypeConverter
    fun toInt(type: UnavailableType?): Int? = type?.value
}

enum class UnavailablePermission(val value: Int) {
    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE(1)
}
class UnavailablePermissionConverter {
    @TypeConverter
    fun toUnavailablePermission(permission: Int?): UnavailablePermission? = permission?.let {
        UnavailablePermission.values().associateBy(UnavailablePermission::value)[it]
    }

    @TypeConverter
    fun toInt(permission: UnavailablePermission?): Int? = permission?.value
}

@TypeConverters(UnavailableTypeConverter::class, UnavailablePermissionConverter::class)
class UnavailableItem(
        @get:Bindable
        @TypeConverters(UnavailableTypeConverter::class)
        var unavailabletype : UnavailableType? = null,
        @get:Bindable
        var unavailablesupporttext : String? = null,
        @get:Bindable
        @TypeConverters(UnavailablePermissionConverter::class)
        var unavailablepermissioncode : UnavailablePermission? = null) : BaseObservable() {

        @Ignore
        constructor(type: UnavailableType, text: String) : this(unavailabletype = type,
                unavailablesupporttext = text, unavailablepermissioncode = null)

}