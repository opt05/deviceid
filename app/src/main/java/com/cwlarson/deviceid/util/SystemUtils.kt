@file:JvmName("SystemUtils") // pretty name for utils class if called from
package com.cwlarson.deviceid.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun calculateNoOfColumns(context: Context?): Int {
    context?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 300).toInt()
    } ?: return 1
}

fun calculateBottomSheetMaxWidth(context: Context?): Int {
    context?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return if(dpWidth > 750) 750 else ViewGroup.LayoutParams.MATCH_PARENT
    } ?: return ViewGroup.LayoutParams.MATCH_PARENT
}

class WeakReferenceDelegate<T>(initialValue: T? = null) : ReadWriteProperty<Any, T?> {
    private var reference = WeakReference(initialValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T? = reference.get()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        reference = WeakReference(value)
    }
}

class SystemProperty(private val context: Context) {

    @Throws(NoSuchPropertyException::class)
    private fun getOrThrow(key: String): String {
        try {
            val classLoader = context.classLoader
            @SuppressLint("PrivateApi")
            val systemProperties = classLoader.loadClass("android.os.SystemProperties")
            val methodGet = systemProperties.getMethod("get", String::class.java)
            return methodGet.invoke(systemProperties, key) as String
        } catch (e: Exception) {
            throw NoSuchPropertyException(e)
        }

    }

    operator fun get(key: String): String? {
        return try {
            getOrThrow(key)
        } catch (e: NoSuchPropertyException) {
            null
        }

    }

    private inner class NoSuchPropertyException internal constructor(e: Exception) : Exception(e)

}