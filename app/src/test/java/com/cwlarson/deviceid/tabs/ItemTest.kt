package com.cwlarson.deviceid.tabs

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cwlarson.deviceid.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(
    instrumentedPackages = [
        // open issue: https://github.com/robolectric/robolectric/issues/6593
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
class ItemTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Application

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun `Verify item title formatted string returns when args available and not composable`() {
        whenever(context.getString(0, "test")).doReturn("test test")
        assertEquals(
            "test test", Item(
                0, ItemType.DEVICE, ItemSubtitle.Text(null), listOf("test")
            ).getFormattedString(context)
        )
    }

    @Test
    fun `Verify item title formatted string returns when args available and composable`() {
        composeTestRule.setContent {
            assertEquals(
                "Unable to retrieve test from this device", Item(
                    R.string.snackbar_not_found_adapter, ItemType.DEVICE,
                    ItemSubtitle.Text(null), listOf("test")
                ).getFormattedString()
            )
        }
    }

    @Test
    fun `Verify item title formatted string returns when args not available and not composable`() {
        whenever(context.getString(0)).doReturn("test")
        assertEquals(
            "test", Item(
                0, ItemType.DEVICE, ItemSubtitle.Text(null)
            ).getFormattedString(context)
        )
    }

    @Test
    fun `Verify item title formatted string returns when args not available and composable`() {
        composeTestRule.setContent {
            assertEquals(
                "Device Info", Item(
                    R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(null)
                ).getFormattedString()
            )
        }
    }

    @Test
    fun `Verify item subtitle text returns text when text type`() {
        assertEquals("test", ItemSubtitle.Text("test").getSubTitleText())
    }

    @Test
    fun `Verify item subtitle text returns text when chart type`() {
        assertEquals("test", ItemSubtitle.Chart(ChartItem(
            0f,0f, mock(), "test"
        )).getSubTitleText())
    }

    @Test
    fun `Verify item subtitle text returns null when no longer possible type`() {
        assertNull(ItemSubtitle.NoLongerPossible(0).getSubTitleText())
    }

    @Test
    fun `Verify item subtitle text returns null when not possible yet type`() {
        assertNull(ItemSubtitle.NotPossibleYet(0).getSubTitleText())
    }

    @Test
    fun `Verify item subtitle text returns null when permission type`() {
        assertNull(ItemSubtitle.Permission(mock()).getSubTitleText())
    }

    @Test
    fun `Verify item subtitle text returns null when error type`() {
        assertNull(ItemSubtitle.Error.getSubTitleText())
    }

    @Test
    fun `Verify item icon returns null when text type`() {
        assertNull(ItemSubtitle.Text("test").getIcon())
    }

    @Test
    fun `Verify item icon returns icon when chart type`() {
        val icon = mock<ImageVector>()
        assertEquals(icon, ItemSubtitle.Chart(ChartItem(
            0f,0f, icon, "test"
        )).getIcon())
    }

    @Test
    fun `Verify item icon returns null when no longer possible type`() {
        assertNull(ItemSubtitle.NoLongerPossible(0).getIcon())
    }

    @Test
    fun `Verify item icon returns null when not possible yet type`() {
        assertNull(ItemSubtitle.NotPossibleYet(0).getIcon())
    }

    @Test
    fun `Verify item icon returns null when permission type`() {
        assertNull(ItemSubtitle.Permission(mock()).getIcon())
    }

    @Test
    fun `Verify item icon returns null when error type`() {
        assertNull(ItemSubtitle.Error.getIcon())
    }

    @Test
    fun `Verify item chart percentage returns null when text type`() {
        assertNull(ItemSubtitle.Text("test").getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when chart type and subtitle is null`() {
        assertNull(ItemSubtitle.Chart(ChartItem(
            0f,0f, mock(), null
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when chart type and subtitle is blank`() {
        assertNull(ItemSubtitle.Chart(ChartItem(
            0f,0f, mock(), ""
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns 0 when chart type and axis both 0`() {
        assertEquals(0f, ItemSubtitle.Chart(ChartItem(
            0f,0f, mock(), "test"
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns when chart type and axis 1 greater than 2`() {
        assertEquals(0.5f, ItemSubtitle.Chart(ChartItem(
            2f,1f, mock(), "test"
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns when chart type and axis 1 less than 2`() {
        assertEquals(0.5f, ItemSubtitle.Chart(ChartItem(
            1f,2f, mock(), "test"
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when no longer possible type`() {
        assertNull(ItemSubtitle.NoLongerPossible(0).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when not possible yet type`() {
        assertNull(ItemSubtitle.NotPossibleYet(0).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when permission type`() {
        assertNull(ItemSubtitle.Permission(mock()).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when error type`() {
        assertNull(ItemSubtitle.Error.getChartPercentage())
    }
}