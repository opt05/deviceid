package com.cwlarson.deviceid.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.test.junit4.createComposeRule
import com.cwlarson.deviceid.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IconsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_NoItems_light() {
        composeTestRule.setContent {
            AppTheme(darkTheme = false) {
                assertEquals(Icons.Default.NoItemsLight.name, noItemsIcon().name)
            }
        }
    }

    @Test
    fun test_NoItems_dark() {
        composeTestRule.setContent {
            AppTheme(darkTheme = true) {
                assertEquals(Icons.Default.NoItemsDark.name, noItemsIcon().name)
            }
        }
    }

    @Test
    fun test_NoItemsSearch_light() {
        composeTestRule.setContent {
            AppTheme(darkTheme = false) {
                assertEquals(Icons.Default.NoItemsSearchLight.name, noItemsSearchIcon().name)
            }
        }
    }

    @Test
    fun test_NoItemsSearch_dark() {
        composeTestRule.setContent {
            AppTheme(darkTheme = true) {
                assertEquals(Icons.Default.NoItemsSearchDark.name, noItemsSearchIcon().name)
            }
        }
    }
}