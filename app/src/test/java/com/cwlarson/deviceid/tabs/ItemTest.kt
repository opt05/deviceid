package com.cwlarson.deviceid.tabs

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cwlarson.deviceid.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
        context = mockk()
    }

    @Test
    fun `Verify item title formatted string returns when args available and not composable`() {
        every { context.getString(0, "test") } returns "test test"
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
        every { context.getString(0) } returns "test"
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
            0f,0f, mockk(), "test"
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
        assertNull(ItemSubtitle.Permission(mockk()).getSubTitleText())
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
        val icon = mockk<ImageVector>()
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
        assertNull(ItemSubtitle.Permission(mockk()).getIcon())
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
            0f,0f, mockk(), null
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when chart type and subtitle is blank`() {
        assertNull(ItemSubtitle.Chart(ChartItem(
            0f,0f, mockk(), ""
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns 0 when chart type and axis both 0`() {
        assertEquals(0f, ItemSubtitle.Chart(ChartItem(
            0f,0f, mockk(), "test"
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns when chart type and axis 1 greater than 2`() {
        assertEquals(0.5f, ItemSubtitle.Chart(ChartItem(
            2f,1f, mockk(), "test"
        )).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns when chart type and axis 1 less than 2`() {
        assertEquals(0.5f, ItemSubtitle.Chart(ChartItem(
            1f,2f, mockk(), "test"
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
        assertNull(ItemSubtitle.Permission(mockk()).getChartPercentage())
    }

    @Test
    fun `Verify item chart percentage returns null when error type`() {
        assertNull(ItemSubtitle.Error.getChartPercentage())
    }
}