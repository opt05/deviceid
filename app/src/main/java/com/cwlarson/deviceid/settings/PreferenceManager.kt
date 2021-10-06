package com.cwlarson.deviceid.settings

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cwlarson.deviceid.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class Filters(val hideUnavailable: Boolean, val swipeRefreshDisabled: Boolean)

data class UserPreferences(
    val hideUnavailable: Boolean = false,
    val autoRefreshRate: Int = 0,
    val darkTheme: String = "mode_system",
    val searchHistory: Boolean = false,
    val forceRefresh: Boolean = false
)

open class PreferenceManager @Inject constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val HIDE_UNAVAILABLE = booleanPreferencesKey("hide_unables")
        val DAYNIGHT_MODE = stringPreferencesKey("daynight_mode")
        val AUTO_REFRESH_RATE = intPreferencesKey("refresh_rate")
        val SEARCH_HISTORY_DATA = stringPreferencesKey("pref_search_history_data")
        val SEARCH_HISTORY = booleanPreferencesKey("pref_search_history")
    }

    private val preferences: Flow<Preferences> = dataStore.data.catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else throw exception
    }

    open fun getFilters(): Flow<Filters> =
        combineTransform(hideUnavailable, autoRefreshRate, forceRefresh) { hide, rate, refresh ->
            Timber.d("New filter: $hide / $rate / $refresh")
            emit(Filters(hide, rate > 0))
        }

    open val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else throw exception
    }.map { preferences ->
        UserPreferences(
            preferences[PreferencesKeys.HIDE_UNAVAILABLE] ?: false,
            preferences[PreferencesKeys.AUTO_REFRESH_RATE] ?: 0,
            preferences[PreferencesKeys.DAYNIGHT_MODE]
                ?: context.getString(R.string.pref_night_mode_system),
            preferences[PreferencesKeys.SEARCH_HISTORY] ?: false
        )
    }

    open val darkTheme: Flow<Boolean?>
        get() = preferences.map {
            when (it[PreferencesKeys.DAYNIGHT_MODE]) {
                context.getString(R.string.pref_night_mode_off) -> false
                context.getString(R.string.pref_night_mode_on) -> true
                else -> null
            }
        }

    open suspend fun setDarkTheme(newValue: String? = null) {
        val setValue = newValue?.let { v ->
            dataStore.edit { it[PreferencesKeys.DAYNIGHT_MODE] = v }
        } ?: preferences.map {
            it[PreferencesKeys.DAYNIGHT_MODE] ?: context.getString(R.string.pref_night_mode_system)
        }

        AppCompatDelegate.setDefaultNightMode(
            when (setValue) {
                context.getString(R.string.pref_night_mode_off) -> AppCompatDelegate.MODE_NIGHT_NO
                context.getString(R.string.pref_night_mode_on) -> AppCompatDelegate.MODE_NIGHT_YES
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        )
    }

    open val hideUnavailable: Flow<Boolean>
        get() = preferences.map { it[PreferencesKeys.HIDE_UNAVAILABLE] ?: false }

    open suspend fun hideUnavailable(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.HIDE_UNAVAILABLE] = value }
    }

    private val _forceRefresh = MutableStateFlow(false)
    open val forceRefresh
        get() = _forceRefresh.asStateFlow()

    open fun forceRefresh() {
        _forceRefresh.value = !_forceRefresh.value
    }

    open val autoRefreshRate: Flow<Int>
        get() = preferences.map { it[PreferencesKeys.AUTO_REFRESH_RATE] ?: 0 }

    @OptIn(ExperimentalCoroutinesApi::class)
    open val autoRefreshRateMillis: Flow<Long>
        get() = autoRefreshRate.mapLatest {
            TimeUnit.MILLISECONDS.convert(
                it.toLong(),
                TimeUnit.SECONDS
            )
        }

    open suspend fun autoRefreshRate(value: Int) {
        dataStore.edit { it[PreferencesKeys.AUTO_REFRESH_RATE] = value }
    }

    private val searchHistoryData: Flow<String?>
        get() = preferences.map { it[PreferencesKeys.SEARCH_HISTORY_DATA] }

    private suspend fun searchHistoryData(value: String?) {
        dataStore.edit {
            if (value == null) it.remove(PreferencesKeys.SEARCH_HISTORY_DATA)
            else it[PreferencesKeys.SEARCH_HISTORY_DATA] = value
        }
    }

    private fun JSONArray.toList(): List<String> = List(length(), this::getString)

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun getSearchHistoryItems(filter: String? = null): Flow<List<String>> =
        searchHistoryData.mapLatest { items ->
            try {
                JSONArray(items ?: "[]").toList().filter { item ->
                    filter?.let { item.contains(it, ignoreCase = true) } ?: true
                }
            } catch (e: Throwable) {
                emptyList()
            }
        }.flowOn(Dispatchers.IO)

    open suspend fun saveSearchHistoryItem(item: String?) {
        if (searchHistory.first() && item?.isNotBlank() == true) {
            val data = JSONArray(searchHistoryData.firstOrNull() ?: "[]").toList()
            searchHistoryData(JSONArray(data.toMutableList().run {
                // Remove item in the list if already in history
                removeAll { s -> s == item }
                // Prepend item to top of list and remove older ones if more than 10 items
                (listOf(item).plus(this)).take(10)
            }).toString())
        }
    }

    open val searchHistory: Flow<Boolean>
        get() = preferences.map { it[PreferencesKeys.SEARCH_HISTORY] ?: false }

    open suspend fun searchHistory(value: Boolean) {
        if (!value) searchHistoryData(null)
        dataStore.edit { it[PreferencesKeys.SEARCH_HISTORY] = value }
    }
}
