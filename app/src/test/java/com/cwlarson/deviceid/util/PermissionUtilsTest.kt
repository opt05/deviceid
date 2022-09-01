package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class PermissionUtilsTest {

    private lateinit var context: Application

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Verify isGranted returns true when permission granted`() {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        assertTrue(context.isGranted(AppPermission.ReadPhoneState))
    }

    @Test
    fun `Verify isGranted returns false when permission not granted`() {
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        assertFalse(context.isGranted(AppPermission.ReadPhoneState))
    }

    @Test
    fun `Verify isGranted returns false when one permission granted and another not granted`() {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertFalse(
            context.isGranted(
                AppPermission.ReadPhoneState,
                AppPermission.AccessFineLocation
            )
        )
    }
}