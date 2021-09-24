package com.cwlarson.deviceid.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.test.platform.app.InstrumentationRegistry
import com.cwlarson.deviceid.androidtestutils.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferenceManagerTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var testObject: PreferenceManager

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestCoroutineScope()
        ) { tmp.newFile("test_file.preferences_pb") }
        testObject =
            PreferenceManager(InstrumentationRegistry.getInstrumentation().targetContext, dataStore)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getFilters_HasValues_EmitsTrues() = runBlockingTest {
        dataStore.edit {
            it[booleanPreferencesKey("hide_unables")] = true
            it[intPreferencesKey("refresh_rate")] = 100
        }
        assertEquals(
            Filters(hideUnavailable = true, swipeRefreshDisabled = true),
            testObject.getFilters().first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getFilters_HasValues_EmitsFalses() = runBlockingTest {
        dataStore.edit {
            it[booleanPreferencesKey("hide_unables")] = false
            it[intPreferencesKey("refresh_rate")] = 0
        }
        assertEquals(
            Filters(hideUnavailable = false, swipeRefreshDisabled = false),
            testObject.getFilters().first()
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @ExperimentalCoroutinesApi
    @Test
    fun userPreferencesFlow_HasInvalidData_EmitsDefaultPreferences() = runBlockingTest {
        tmp.newFile("test_file.preferences_pb").writeBytes(byteArrayOf(0))
        assertEquals(
            UserPreferences(
                hideUnavailable = false,
                autoRefreshRate = 0,
                darkTheme = "mode_system",
                searchHistory = false
            ),
            testObject.userPreferencesFlow.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun userPreferencesFlow_HasSetValues_EmitsSameValues() = runBlockingTest {
        dataStore.edit {
            it[booleanPreferencesKey("hide_unables")] = true
            it[intPreferencesKey("refresh_rate")] = 456
            it[stringPreferencesKey("daynight_mode")] = "mode_on"
            it[booleanPreferencesKey("pref_search_history")] = true
        }
        assertEquals(
            UserPreferences(
                hideUnavailable = true,
                autoRefreshRate = 456,
                darkTheme = "mode_on",
                searchHistory = true
            ),
            testObject.userPreferencesFlow.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun darkTheme_ModeOff_EmitsFalse() = runBlockingTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_off" }
        assertEquals(false, testObject.darkTheme.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun darkTheme_ModeOn_EmitsTrue() = runBlockingTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_on" }
        assertEquals(true, testObject.darkTheme.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun darkTheme_ModeSystem_EmitsNull() = runBlockingTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_system" }
        assertNull(testObject.darkTheme.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun darkTheme_Null_EmitsNull() = runBlockingTest {
        assertNull(testObject.darkTheme.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun setDarkTheme_NotNull_SavesValue() = runBlockingTest {
        testObject.setDarkTheme("mode_on")
        assertEquals(
            preferencesOf(stringPreferencesKey("daynight_mode") to "mode_on"),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun setDarkTheme_Null_DoesNotSaveValue() = runBlockingTest {
        testObject.setDarkTheme(null)
        assertNull(dataStore.data.first()[stringPreferencesKey("daynight_mode")])
    }

    @ExperimentalCoroutinesApi
    @Test
    fun hideUnavailable_NotNull_EmitsValue() = runBlockingTest {
        dataStore.edit { it[booleanPreferencesKey("hide_unables")] = true }
        assertTrue(testObject.hideUnavailable.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun hideUnavailable_Null_EmitsFalse() = runBlockingTest {
        assertFalse(testObject.hideUnavailable.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun hideUnavailable_SetTrue_SavesTrue() = runBlockingTest {
        testObject.hideUnavailable(true)
        assertEquals(
            preferencesOf(booleanPreferencesKey("hide_unables") to true),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun hideUnavailable_SetFalse_SavesFalse() = runBlockingTest {
        testObject.hideUnavailable(false)
        assertEquals(
            preferencesOf(booleanPreferencesKey("hide_unables") to false),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun forceRefresh_DefaultValue_IsFalse() = runBlockingTest {
        assertFalse(testObject.forceRefresh.value)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun forceRefresh_SetValue_Toggles() = runBlockingTest {
        testObject.forceRefresh()
        assertTrue(testObject.forceRefresh.value)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun autoRefreshRate_NotNull_EmitsValue() = runBlockingTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 200 }
        assertEquals(200, testObject.autoRefreshRate.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun autoRefreshRate_Null_Emits0() = runBlockingTest {
        assertEquals(0, testObject.autoRefreshRate.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun authRefreshRateMillis_NotNull_EmitsValue() = runBlockingTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 1 }
        assertEquals(1000, testObject.autoRefreshRateMillis.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun authRefreshRateMillis_Null_Emits0() = runBlockingTest {
        assertEquals(0, testObject.autoRefreshRateMillis.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun authRefreshRateMillis_NotNull_AlwaysUsesLatestValue() = runBlockingTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 1 }
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 2 }
        assertEquals(2000, testObject.autoRefreshRateMillis.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun autoRefreshRate_NotNull_SavesValue() = runBlockingTest {
        testObject.autoRefreshRate(300)
        assertEquals(
            preferencesOf(intPreferencesKey("refresh_rate") to 300),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithFilterAndNotNull_EmitsValue() = runBlocking {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test"), testObject.getSearchHistoryItems("test").first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithFilterAndNull_EmitsValue() = runBlocking {
        assertEquals(emptyList<String>(), testObject.getSearchHistoryItems("test").first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithoutFilterAndNotNull_EmitsValue() = runBlocking {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test", "string"), testObject.getSearchHistoryItems().first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithoutFilterAndNull_EmitsValue() = runBlocking {
        assertEquals(emptyList<String>(), testObject.getSearchHistoryItems().first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithFilterAndNotNull_AlwaysUsesLatestValue() = runBlocking {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]"
        }
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("string"), testObject.getSearchHistoryItems("string").first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getSearchHistoryItems_WithoutFilterAndNotNull_AlwaysUsesLatestValue() = runBlocking {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]"
        }
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test", "string"), testObject.getSearchHistoryItems().first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_NotNullAndSavingEnabled_SavesValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem("test")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to true,
                stringPreferencesKey("pref_search_history_data") to "[\"test\"]"),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_NullAndSavingEnabled_DoesNotSaveValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem(null)
        assertEquals(
            preferencesOf(booleanPreferencesKey("pref_search_history") to true),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_BlankAndSavingEnabled_DoesNotSaveValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem("")
        assertEquals(
            preferencesOf(booleanPreferencesKey("pref_search_history") to true),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_NotNullAndSavingDisabled_DoesNotSaveValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem("test")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_NullAndSavingDisabled_DoesNotSaveValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem(null)
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_BlankAndSavingDisabled_DoesNotSaveValue() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem("")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_AddSecondItem_PrependsToTopOfList() = runBlocking {
        dataStore.edit {
            it[booleanPreferencesKey("pref_search_history")] = true
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]"
        }
        testObject.saveSearchHistoryItem("aaa")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to true,
                stringPreferencesKey("pref_search_history_data") to "[\"aaa\",\"test\"]"
            ),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_Add11thItem_KeepsOnlyLast10() = runBlocking {
        dataStore.edit {
            it[booleanPreferencesKey("pref_search_history")] = true
            it[stringPreferencesKey("pref_search_history_data")] =
                "[\"voiceless\",\"playground\",\"key\",\"tongue\",\"notebook\"," +
                        "\"peck\",\"receipt\",\"fanatical\",\"horrible\",\"bright\"]"
        }
        testObject.saveSearchHistoryItem("round")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to true,
                stringPreferencesKey("pref_search_history_data") to
                        "[\"round\",\"voiceless\",\"playground\",\"key\",\"tongue\"," +
                        "\"notebook\",\"peck\",\"receipt\",\"fanatical\",\"horrible\"]"
            ),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveSearchHistoryItem_AddSameItem_MovesToTop() = runBlocking {
        dataStore.edit {
            it[booleanPreferencesKey("pref_search_history")] = true
            it[stringPreferencesKey("pref_search_history_data")] =
                "[\"voiceless\",\"playground\",\"key\",\"tongue\",\"notebook\"," +
                        "\"peck\",\"receipt\",\"fanatical\",\"horrible\",\"bright\"]"
        }
        testObject.saveSearchHistoryItem("receipt")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to true,
                stringPreferencesKey("pref_search_history_data") to
                        "[\"receipt\",\"voiceless\",\"playground\",\"key\",\"tongue\"," +
                        "\"notebook\",\"peck\",\"fanatical\",\"horrible\",\"bright\"]"
            ),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchHistory_SetTrue_EmitsTrue() = runBlockingTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        assertTrue(testObject.searchHistory.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchHistory_SetFalse_EmitsFalse() = runBlockingTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        assertFalse(testObject.searchHistory.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchHistory_NotSet_EmitsFalse() = runBlockingTest {
        assertFalse(testObject.searchHistory.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchHistory_SetTrue_SavesValueAndDoesNotClearData() = runBlockingTest {
        dataStore.edit { it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]" }
        testObject.searchHistory(true)
        assertEquals(
            preferencesOf(
                stringPreferencesKey("pref_search_history_data") to "[\"test\"]",
                booleanPreferencesKey("pref_search_history") to true
            ),
            dataStore.data.first()
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchHistory_SetFalse_SavesValueAndDoesClearData() = runBlockingTest {
        dataStore.edit { it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]" }
        testObject.searchHistory(false)
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false
            ),
            dataStore.data.first()
        )
    }
}