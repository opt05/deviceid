package com.cwlarson.deviceid.search

import com.cwlarson.deviceid.androidtestutils.launchFragmentInHiltContainer
import com.cwlarson.deviceid.di.AppModule
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.settings.SettingsFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun whenLoadingData_shouldBeInAlphabeticalOrder() = runBlocking {
        launchFragmentInHiltContainer<SettingsFragment>()
        /** Search for serial and compare the two results are in order
         * Hilt doesn't allow access to [FragmentScenario.onFragment()] **/
    }

}