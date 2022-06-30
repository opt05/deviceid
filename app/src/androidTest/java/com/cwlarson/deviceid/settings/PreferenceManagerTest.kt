package com.cwlarson.deviceid.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.test.platform.app.InstrumentationRegistry
import com.cwlarson.deviceid.androidtestutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.inject.Inject

@HiltAndroidTest
class PreferenceManagerTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val tmp = TemporaryFolder()

    @get:Rule(order = 2)
    val coroutineRule = CoroutineTestRule()

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var testObject: PreferenceManager

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(coroutineRule.dispatcher)
        ) { tmp.newFile("test_file.preferences_pb") }
        testObject = PreferenceManager(
            dispatcherProvider, InstrumentationRegistry.getInstrumentation().targetContext,
            dataStore
        )
    }

    @Test
    fun getFilters_HasValues_EmitsTrues() = runTest {
        dataStore.edit {
            it[booleanPreferencesKey("hide_unables")] = true
            it[intPreferencesKey("refresh_rate")] = 100
        }
        assertEquals(
            Filters(hideUnavailable = true, swipeRefreshDisabled = true),
            testObject.getFilters().first()
        )
    }

    @Test
    fun getFilters_HasValues_EmitsFalses() = runTest {
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
    @Test
    fun userPreferencesFlow_HasInvalidData_EmitsDefaultPreferences() = runTest {
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

    @Test
    fun userPreferencesFlow_HasSetValues_EmitsSameValues() = runTest {
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

    @Test
    fun darkTheme_ModeOff_EmitsFalse() = runTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_off" }
        assertEquals(false, testObject.darkTheme.first())
    }

    @Test
    fun darkTheme_ModeOn_EmitsTrue() = runTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_on" }
        assertEquals(true, testObject.darkTheme.first())
    }

    @Test
    fun darkTheme_ModeSystem_EmitsNull() = runTest {
        dataStore.edit { it[stringPreferencesKey("daynight_mode")] = "mode_system" }
        assertNull(testObject.darkTheme.first())
    }

    @Test
    fun darkTheme_Null_EmitsNull() = runTest {
        assertNull(testObject.darkTheme.first())
    }

    @Test
    fun setDarkTheme_NotNull_SavesValue() = runTest {
        testObject.setDarkTheme("mode_on")
        assertEquals(
            preferencesOf(stringPreferencesKey("daynight_mode") to "mode_on"),
            dataStore.data.first()
        )
    }

    @Test
    fun setDarkTheme_Null_DoesNotSaveValue() = runTest {
        testObject.setDarkTheme(null)
        assertNull(dataStore.data.first()[stringPreferencesKey("daynight_mode")])
    }

    @Test
    fun hideUnavailable_NotNull_EmitsValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("hide_unables")] = true }
        assertTrue(testObject.hideUnavailable.first())
    }

    @Test
    fun hideUnavailable_Null_EmitsFalse() = runTest {
        assertFalse(testObject.hideUnavailable.first())
    }

    @Test
    fun hideUnavailable_SetTrue_SavesTrue() = runTest {
        testObject.hideUnavailable(true)
        assertEquals(
            preferencesOf(booleanPreferencesKey("hide_unables") to true),
            dataStore.data.first()
        )
    }

    @Test
    fun hideUnavailable_SetFalse_SavesFalse() = runTest {
        testObject.hideUnavailable(false)
        assertEquals(
            preferencesOf(booleanPreferencesKey("hide_unables") to false),
            dataStore.data.first()
        )
    }

    @Test
    fun forceRefresh_DefaultValue_IsFalse() = runTest {
        assertFalse(testObject.forceRefresh.value)
    }

    @Test
    fun forceRefresh_SetValue_Toggles() = runTest {
        testObject.forceRefresh()
        assertTrue(testObject.forceRefresh.value)
    }

    @Test
    fun autoRefreshRate_NotNull_EmitsValue() = runTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 200 }
        assertEquals(200, testObject.autoRefreshRate.first())
    }

    @Test
    fun autoRefreshRate_Null_Emits0() = runTest {
        assertEquals(0, testObject.autoRefreshRate.first())
    }

    @Test
    fun authRefreshRateMillis_NotNull_EmitsValue() = runTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 1 }
        assertEquals(1000, testObject.autoRefreshRateMillis.first())
    }

    @Test
    fun authRefreshRateMillis_Null_Emits0() = runTest {
        assertEquals(0, testObject.autoRefreshRateMillis.first())
    }

    @Test
    fun authRefreshRateMillis_NotNull_AlwaysUsesLatestValue() = runTest {
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 1 }
        dataStore.edit { it[intPreferencesKey("refresh_rate")] = 2 }
        assertEquals(2000, testObject.autoRefreshRateMillis.first())
    }

    @Test
    fun autoRefreshRate_NotNull_SavesValue() = runTest {
        testObject.autoRefreshRate(300)
        assertEquals(
            preferencesOf(intPreferencesKey("refresh_rate") to 300),
            dataStore.data.first()
        )
    }

    @Test
    fun getSearchHistoryItems_WithFilterAndNotNull_EmitsValue() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test"), testObject.getSearchHistoryItems("test").first())
    }

    @Test
    fun getSearchHistoryItems_WithFilterAndNull_EmitsValue() = runTest {
        assertEquals(emptyList<String>(), testObject.getSearchHistoryItems("test").first())
    }

    @Test
    fun getSearchHistoryItems_WithoutFilterAndNotNull_EmitsValue() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test", "string"), testObject.getSearchHistoryItems().first())
    }

    @Test
    fun getSearchHistoryItems_WithoutFilterAndNull_EmitsValue() = runTest {
        assertEquals(emptyList<String>(), testObject.getSearchHistoryItems().first())
    }

    @Test
    fun getSearchHistoryItems_WithFilterAndNotNull_AlwaysUsesLatestValue() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]"
        }
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("string"), testObject.getSearchHistoryItems("string").first())
    }

    @Test
    fun getSearchHistoryItems_WithoutFilterAndNotNull_AlwaysUsesLatestValue() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\"]"
        }
        dataStore.edit {
            it[stringPreferencesKey("pref_search_history_data")] = "[\"test\", \"string\"]"
        }
        assertEquals(listOf("test", "string"), testObject.getSearchHistoryItems().first())
    }

    @Test
    fun saveSearchHistoryItem_NotNullAndSavingEnabled_SavesValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem("test")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to true,
                stringPreferencesKey("pref_search_history_data") to "[\"test\"]"
            ),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_NullAndSavingEnabled_DoesNotSaveValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem(null)
        assertEquals(
            preferencesOf(booleanPreferencesKey("pref_search_history") to true),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_BlankAndSavingEnabled_DoesNotSaveValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        testObject.saveSearchHistoryItem("")
        assertEquals(
            preferencesOf(booleanPreferencesKey("pref_search_history") to true),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_NotNullAndSavingDisabled_DoesNotSaveValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem("test")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false
            ),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_NullAndSavingDisabled_DoesNotSaveValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem(null)
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false
            ),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_BlankAndSavingDisabled_DoesNotSaveValue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        testObject.saveSearchHistoryItem("")
        assertEquals(
            preferencesOf(
                booleanPreferencesKey("pref_search_history") to false
            ),
            dataStore.data.first()
        )
    }

    @Test
    fun saveSearchHistoryItem_AddSecondItem_PrependsToTopOfList() = runTest {
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

    @Test
    fun saveSearchHistoryItem_Add11thItem_KeepsOnlyLast10() = runTest {
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

    @Test
    fun saveSearchHistoryItem_AddSameItem_MovesToTop() = runTest {
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

    @Test
    fun searchHistory_SetTrue_EmitsTrue() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = true }
        assertTrue(testObject.searchHistory.first())
    }

    @Test
    fun searchHistory_SetFalse_EmitsFalse() = runTest {
        dataStore.edit { it[booleanPreferencesKey("pref_search_history")] = false }
        assertFalse(testObject.searchHistory.first())
    }

    @Test
    fun searchHistory_NotSet_EmitsFalse() = runTest {
        assertFalse(testObject.searchHistory.first())
    }

    @Test
    fun searchHistory_SetTrue_SavesValueAndDoesNotClearData() = runTest {
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

    @Test
    fun searchHistory_SetFalse_SavesValueAndDoesClearData() = runTest {
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