package com.cwlarson.deviceid.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

open class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context,
                                                 private val preferences: SharedPreferences) {

    open fun setDefaultValues() {
        try {
            PreferenceManager.setDefaultValues(context, R.xml.pref_general, false)
            if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                PreferenceManager.setDefaultValues(context, R.xml.pref_testing_app_update, true)
        } catch (e: Throwable) {
            Timber.e("Unable to set default preferences")
        }
    }

    open fun setDarkTheme(newValue: Any? = null) {
        AppCompatDelegate.setDefaultNightMode(
                when (newValue
                        ?: preferences.getString(context.getString(R.string.pref_daynight_mode_key),
                                context.getString(R.string.pref_night_mode_system))) {
                    context.getString(R.string.pref_night_mode_off) -> AppCompatDelegate.MODE_NIGHT_NO
                    context.getString(R.string.pref_night_mode_on) -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                })
    }

    open var hideUnavailable: Boolean
        get() = preferences.getBoolean(context.getString(R.string.pref_hide_unavailable_key), false)
        set(value) = preferences.edit {
            putBoolean(context.getString(R.string
                    .pref_hide_unavailable_key), value)
        }

    @ExperimentalCoroutinesApi
    open fun observeHideUnavailable() =
            preferences.observeKey(context.getString(R.string.pref_hide_unavailable_key),
                    default = false, onlyChanges = true)

    open var autoRefreshRate: Int
        get() = preferences.getInt(context.getString(R.string.pref_auto_refresh_rate_key), 0)
        set(value) = preferences.edit {
            putInt(context.getString(R.string
                    .pref_auto_refresh_rate_key), value)
        }

    @ExperimentalCoroutinesApi
    open fun observerAutoRefreshRate() =
            preferences.observeKey(context.getString(R.string.pref_auto_refresh_rate_key),
                    default = 0, onlyChanges = true)

    private var searchHistoryData: String?
        get() = preferences.getString(context.getString(R.string.pref_search_history_data_key), "[]")
        set(value) = if (value == null)
            preferences.edit { remove(context.getString(R.string.pref_search_history_data_key)) }
        else preferences.edit {
            putString(context.getString(R.string
                    .pref_search_history_data_key), value)
        }

    open fun saveSearchHistoryItem(item: String?) {
        if (searchHistory && item?.isNotBlank() == true) {
            searchHistoryData = JSONArray(getSearchHistoryItems().toMutableList().run {
                // Remove item in the list if already in history
                removeAll { s -> s == item }
                // Prepend item to top of list and remove older ones if more than 10 items
                (listOf(item).plus(this)).take(10)
            }).toString()
        }
    }

    private fun JSONArray.toList(): List<String> = List(length(), this::getString)
    open fun getSearchHistoryItems(items: String? = searchHistoryData) =
            JSONArray(items).toList()

    @ExperimentalCoroutinesApi
    open fun observeSearchHistoryData() =
            preferences.observeKey(context.getString(R.string.pref_search_history_data_key), "[]").map {
                getSearchHistoryItems(it)
            }

    open var searchHistory: Boolean
        get() = preferences.getBoolean(context.getString(R.string.pref_search_history_key), false)
        set(value) {
            if (!value) searchHistoryData = null
            preferences.edit { putBoolean(context.getString(R.string.pref_search_history_key), value) }
        }

    open var useFakeUpdateManager: Boolean
        get() = preferences.getBoolean(context.getString(R.string.pref_use_fake_update_manager_key), false)
        set(value) = preferences.edit(commit = true) {
            putBoolean(context.getString(R.string
                    .pref_use_fake_update_manager_key), value)
        }

    @ExperimentalCoroutinesApi
    open fun observeUseFakeUpdateManager() =
            preferences.observeKey(context.getString(R.string.pref_use_fake_update_manager_key),
                    default = false, onlyChanges = true)

}

@ExperimentalCoroutinesApi
private inline fun <reified T> SharedPreferences.observeKey(key: String, default: T,
                                                            onlyChanges: Boolean = false,
                                                            dispatcher: CoroutineContext = Dispatchers.Default): Flow<T> {
    val flow: Flow<T> = callbackFlow {
        if (!onlyChanges) trySend(getItem(key, default))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (key == k) trySend(getItem(key, default))
        }

        registerOnSharedPreferenceChangeListener(listener)
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return flow.flowOn(dispatcher)
}

private inline fun <reified T> SharedPreferences.getItem(key: String, default: T): T {
    @Suppress("UNCHECKED_CAST")
    return when (default) {
        is String -> getString(key, default) as T
        is Int -> getInt(key, default) as T
        is Long -> getLong(key, default) as T
        is Boolean -> getBoolean(key, default) as T
        is Float -> getFloat(key, default) as T
        is Set<*> -> getStringSet(key, default as Set<String>) as T
        is MutableSet<*> -> getStringSet(key, default as MutableSet<String>) as T
        else -> throw IllegalArgumentException("generic type not handle ${T::class.java.name}")
    }
}