package com.cwlarson.deviceid.appupdates

import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.cwlarson.deviceid.R
import org.junit.Test

class AppUpdateDialogTest {

    @Test
    fun dialogDisplaysData() {
        launchFragment<AppUpdateDialog>(
            themeResId = R.style.AppTheme,
            fragmentArgs = AppUpdateDialogArgs(
                android.R.string.VideoView_error_title,
                android.R.string.VideoView_error_text_unknown,
                android.R.string.VideoView_error_button
            ).toBundle()
        )
        onView(withText(android.R.string.VideoView_error_title)).check(matches(isDisplayed()))
        onView(withText(android.R.string.VideoView_error_text_unknown)).check(matches(isDisplayed()))
        onView(withText(android.R.string.VideoView_error_button)).check(matches(isDisplayed()))
    }

    @Test
    fun dialogButtonDismisses() {
        launchFragment<AppUpdateDialog>(
            themeResId = R.style.AppTheme,
            fragmentArgs = AppUpdateDialogArgs(
                android.R.string.VideoView_error_title,
                android.R.string.VideoView_error_text_unknown,
                android.R.string.VideoView_error_button
            ).toBundle()
        )
        onView(withText(android.R.string.VideoView_error_button)).perform(click())
        onView(withText(android.R.string.VideoView_error_button)).check(doesNotExist())
    }
}