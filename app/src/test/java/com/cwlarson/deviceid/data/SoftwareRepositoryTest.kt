package com.cwlarson.deviceid.data

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.testutils.awaitItemFromList
import com.cwlarson.deviceid.testutils.shadows.ExceptionShadowActivityManager
import com.cwlarson.deviceid.testutils.shadows.MyShadowBuild
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild
import org.robolectric.shadows.ShadowSystemProperties
import org.robolectric.shadows.ShadowWebView
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class SoftwareRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var repository: SoftwareRepository

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = mockk()
        repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
    }

    @Test
    fun `Verify item list is returned when items method is called`() = runTest {
        repository.items().test {
            val item = awaitItem()
            assertEquals(R.string.software_title_android_version, item[0].title)
            assertEquals(R.string.software_title_patch_level, item[1].title)
            assertEquals(R.string.software_title_preview_sdk_int, item[2].title)
            assertEquals(R.string.software_title_device_build_version, item[3].title)
            assertEquals(R.string.software_title_build_baseband, item[4].title)
            assertEquals(R.string.software_title_build_kernel, item[5].title)
            assertEquals(R.string.software_title_build_date, item[6].title)
            assertEquals(R.string.software_title_build_number, item[7].title)
            assertEquals(R.string.software_title_build_board, item[8].title)
            assertEquals(R.string.software_title_build_bootloader, item[9].title)
            assertEquals(R.string.software_title_build_brand, item[10].title)
            assertEquals(R.string.software_title_build_device, item[11].title)
            assertEquals(R.string.software_title_build_display, item[12].title)
            assertEquals(R.string.software_title_build_fingerprint, item[13].title)
            assertEquals(R.string.software_title_build_hardware, item[14].title)
            assertEquals(R.string.software_title_build_host, item[15].title)
            assertEquals(R.string.software_title_build_tags, item[16].title)
            assertEquals(R.string.software_title_build_type, item[17].title)
            assertEquals(R.string.software_title_build_user, item[18].title)
            assertEquals(R.string.software_title_open_gl_version, item[19].title)
            assertEquals(R.string.software_title_google_play_services_version, item[20].title)
            assertEquals(R.string.software_title_google_play_services_install_date, item[21].title)
            assertEquals(R.string.software_title_google_play_services_updated_date, item[22].title)
            assertEquals(R.string.software_title_webview_version, item[23].title)
            awaitComplete()
        }
    }

    @Test
    fun `Verify sdk to version returned correct text when method is called`() {
        assertEquals("", 0.sdkToVersion())
        assertEquals("1.0", 1.sdkToVersion())
        assertEquals("1.1", 2.sdkToVersion())
        assertEquals("1.5", 3.sdkToVersion())
        assertEquals("1.6", 4.sdkToVersion())
        assertEquals("2.0", 5.sdkToVersion())
        assertEquals("2.0.1", 6.sdkToVersion())
        assertEquals("2.1", 7.sdkToVersion())
        assertEquals("2.2", 8.sdkToVersion())
        assertEquals("2.3", 9.sdkToVersion())
        assertEquals("2.3.3", 10.sdkToVersion())
        assertEquals("3.0", 11.sdkToVersion())
        assertEquals("3.1", 12.sdkToVersion())
        assertEquals("3.2", 13.sdkToVersion())
        assertEquals("4.0.1", 14.sdkToVersion())
        assertEquals("4.0.3", 15.sdkToVersion())
        assertEquals("4.1", 16.sdkToVersion())
        assertEquals("4.2", 17.sdkToVersion())
        assertEquals("4.3", 18.sdkToVersion())
        assertEquals("4.4", 19.sdkToVersion())
        assertEquals("4.4W", 20.sdkToVersion())
        assertEquals("5.0", 21.sdkToVersion())
        assertEquals("5.1", 22.sdkToVersion())
        assertEquals("6.0", 23.sdkToVersion())
        assertEquals("7.0", 24.sdkToVersion())
        assertEquals("7.1", 25.sdkToVersion())
        assertEquals("8.0", 26.sdkToVersion())
        assertEquals("8.1", 27.sdkToVersion())
        assertEquals("9.0", 28.sdkToVersion())
        assertEquals("10.0", 29.sdkToVersion())
        assertEquals("11.0", 30.sdkToVersion())
        assertEquals("12.0", 31.sdkToVersion())
        assertEquals("12.1", 32.sdkToVersion())
        assertEquals("13.0", 33.sdkToVersion())
        assertEquals("14.0", 34.sdkToVersion())
        assertEquals("", 35.sdkToVersion())
    }

    @Test
    fun `Verify codename to version returned correct text when method is called`() {
        assertEquals("", 0.getCodename())
        assertEquals("", 1.getCodename())
        assertEquals("", 2.getCodename())
        assertEquals("Cupcake", 3.getCodename())
        assertEquals("Donut", 4.getCodename())
        assertEquals("Eclair", 5.getCodename())
        assertEquals("Eclair", 6.getCodename())
        assertEquals("Eclair", 7.getCodename())
        assertEquals("Froyo", 8.getCodename())
        assertEquals("Gingerbread", 9.getCodename())
        assertEquals("Gingerbread", 10.getCodename())
        assertEquals("Honeycomb", 11.getCodename())
        assertEquals("Honeycomb", 12.getCodename())
        assertEquals("Honeycomb", 13.getCodename())
        assertEquals("Ice Cream Sandwich", 14.getCodename())
        assertEquals("Ice Cream Sandwich", 15.getCodename())
        assertEquals("Jelly Bean", 16.getCodename())
        assertEquals("Jelly Bean", 17.getCodename())
        assertEquals("Jelly Bean", 18.getCodename())
        assertEquals("KitKat", 19.getCodename())
        assertEquals("KitKat Watch", 20.getCodename())
        assertEquals("Lollipop", 21.getCodename())
        assertEquals("Lollipop", 22.getCodename())
        assertEquals("Marshmallow", 23.getCodename())
        assertEquals("Nougat", 24.getCodename())
        assertEquals("Nougat", 25.getCodename())
        assertEquals("Oreo", 26.getCodename())
        assertEquals("Oreo", 27.getCodename())
        assertEquals("Pie", 28.getCodename())
        assertEquals("", 29.getCodename())
        assertEquals("", 30.getCodename())
        assertEquals("", 31.getCodename())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when android version is available`() = runTest {
        ShadowBuild.setVersionRelease("1.0")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_android_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("1.0 (API level 28) Pie")
                ), awaitItemFromList(R.string.software_title_android_version)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when android version with an exception`() = runTest { }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when patch level is android M+`() = runTest {
        ShadowBuild.setVersionSecurityPatch("2000-01-01")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_patch_level,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("January 1, 2000")
                ), awaitItemFromList(R.string.software_title_patch_level)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when patch level is invalid date and is android M+`() = runTest {
        ShadowBuild.setVersionSecurityPatch("1/1/2000")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_patch_level,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("1/1/2000")
                ), awaitItemFromList(R.string.software_title_patch_level)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when patch level is below android M`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_patch_level,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
                ), awaitItemFromList(R.string.software_title_patch_level)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when patch level with an exception`() = runTest { }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N], shadows = [MyShadowBuild::class])
    fun `Returns text when preview sdk int is android N+`() = runTest {
        MyShadowBuild.setPreviewSdkInt(9)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_preview_sdk_int,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("9")
                ), awaitItemFromList(R.string.software_title_preview_sdk_int)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when preview sdk int is below android N`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_preview_sdk_int,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.N)
                ), awaitItemFromList(R.string.software_title_preview_sdk_int)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when preview sdk int with an exception`() = runTest { }

    @Test
    fun `Returns text when build version is Moto available`() = runTest {
        ShadowSystemProperties.override("ro.build.version.full", "test")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_device_build_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("test")
                ), awaitItemFromList(R.string.software_title_device_build_version)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build version is available`() = runTest {
        MyShadowBuild.setBuildVersion("test2")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_device_build_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("test2")
                ), awaitItemFromList(R.string.software_title_device_build_version)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build version with an exception`() = runTest { }

    @Test
    fun `Returns text when build baseband is available`() = runTest {
        ShadowBuild.setRadioVersion("this-is-a-radio-version")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_baseband,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("this-is-a-radio-version")
                ), awaitItemFromList(R.string.software_title_build_baseband)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build baseband with an exception`() = runTest { }

    @Test
    fun `Returns text when build kernel is available`() = runTest {
        System.setProperty("os.version", "this-is-a-kernel-version")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_kernel,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("this-is-a-kernel-version")
                ), awaitItemFromList(R.string.software_title_build_kernel)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build kernel with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build date is available`() = runTest {
        val date = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT")
            timeInMillis = 946684800000
        }
        MyShadowBuild.setBuildDate(date.timeInMillis)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_date,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text(SimpleDateFormat.getInstance().format(date.time))
                ), awaitItemFromList(R.string.software_title_build_date)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build date with an exception`() = runTest { }

    @Test
    fun `Returns text when build number is available`() = runTest {
        ShadowBuild.setId("something.12345.010")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_number,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("something.12345.010")
                ), awaitItemFromList(R.string.software_title_build_number)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build number with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build board is available`() = runTest {
        MyShadowBuild.setBuildBoard("powerline")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_board,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("powerline")
                ), awaitItemFromList(R.string.software_title_build_board)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build board with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build bootloader is available`() = runTest {
        MyShadowBuild.setBuildBootloader("hd8d-jd9-8303835")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_bootloader,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("hd8d-jd9-8303835")
                ), awaitItemFromList(R.string.software_title_build_bootloader)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build bootloader with an exception`() = runTest { }

    @Test
    fun `Returns text when build brand is available`() = runTest {
        ShadowBuild.setBrand("costco")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_brand,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("costco")
                ), awaitItemFromList(R.string.software_title_build_brand)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build brand with an exception`() = runTest { }

    @Test
    fun `Returns text when build device is available`() = runTest {
        ShadowBuild.setDevice("chicago")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_device,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("chicago")
                ), awaitItemFromList(R.string.software_title_build_device)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build device with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build display is available`() = runTest {
        MyShadowBuild.setBuildVersion("cosmos")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_display,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("cosmos")
                ), awaitItemFromList(R.string.software_title_build_display)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build display with an exception`() = runTest { }

    @Test
    fun `Returns text when build fingerprint is available`() = runTest {
        ShadowBuild.setFingerprint("something really long to show here")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_fingerprint,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("something really long to show here")
                ), awaitItemFromList(R.string.software_title_build_fingerprint)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build fingerprint with an exception`() = runTest { }

    @Test
    fun `Returns text when build hardware is available`() = runTest {
        ShadowBuild.setHardware("secure")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_hardware,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("secure")
                ), awaitItemFromList(R.string.software_title_build_hardware)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build hardware with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build host is available`() = runTest {
        MyShadowBuild.setBuildHost("total")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_host,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("total")
                ), awaitItemFromList(R.string.software_title_build_host)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build host with an exception`() = runTest { }

    @Test
    fun `Returns text when build tags is available`() = runTest {
        ShadowBuild.setTags("classify")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_tags,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("classify")
                ), awaitItemFromList(R.string.software_title_build_tags)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build tags with an exception`() = runTest { }

    @Test
    fun `Returns text when build type is available`() = runTest {
        ShadowBuild.setType("car")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_type,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("car")
                ), awaitItemFromList(R.string.software_title_build_type)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build type with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowBuild::class])
    fun `Returns text when build user is available`() = runTest {
        MyShadowBuild.setBuildUser("distinct")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_build_user,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("distinct")
                ), awaitItemFromList(R.string.software_title_build_user)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when build user with an exception`() = runTest { }

    @Test
    fun `Returns text when open gl version is available`() = runTest {
        shadowOf(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .setDeviceConfigurationInfo(ConfigurationInfo().apply { reqGlEsVersion = 0x20000 })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_open_gl_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("2.0")
                ), awaitItemFromList(R.string.software_title_open_gl_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when open gl version with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.ACTIVITY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_open_gl_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_open_gl_version)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowActivityManager::class])
    fun `Returns error when open gl version with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_open_gl_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_open_gl_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when play services version is available`() = runTest {
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                packageName = "com.google.android.gms"
                versionName = "fibre"
                longVersionCode = 0x20000000
            }
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("fibre (536870912)")
                ), awaitItemFromList(R.string.software_title_google_play_services_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services version with a null system service`() = runTest {
        shadowOf(context.packageManager).deletePackage("com.google.android.gms")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_google_play_services_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services version with an exception on Android 13+`() = runTest {
        val context: Context = mockk()
        val packageManager: PackageManager = mockk()
        val packageInfo: PackageInfo = mockk()
        every { context.packageManager } returns packageManager
        every {
            packageManager.getPackageInfo(any<String>(), any<PackageManager.PackageInfoFlags>())
        } returns packageInfo
        every { packageInfo.longVersionCode } throws NullPointerException("")
        val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_google_play_services_version)
            )
            awaitComplete()
        }
    }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Returns error when play services version with an exception on older Android`() = runTest {
        val context: Context = mockk()
        val packageManager: PackageManager = mockk()
        val packageInfo: PackageInfo = mockk()
        every { context.packageManager } returns packageManager
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        every { packageInfo.longVersionCode } throws NullPointerException("")
        val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_google_play_services_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when play services install date is available`() = runTest {
        val date = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT")
            timeInMillis = 946684800000
        }
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                packageName = "com.google.android.gms"
                firstInstallTime = date.timeInMillis
            }
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_install_date,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text(
                        DateFormat.getDateFormat(context).format(date.time)
                    )
                ), awaitItemFromList(R.string.software_title_google_play_services_install_date)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services install date with a null system service`() = runTest {
        shadowOf(context.packageManager).deletePackage("com.google.android.gms")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_install_date,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_google_play_services_install_date)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services install date with an exception on Android 13+`() =
        runTest {
            val context: Context = mockk()
            val packageManager: PackageManager = mockk()
            val packageInfo: PackageInfo = mockk()
            every { context.packageManager } returns packageManager
            every {
                packageManager.getPackageInfo(any<String>(), any<PackageManager.PackageInfoFlags>())
            } returns packageInfo
            val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.software_title_google_play_services_install_date,
                        itemType = ItemType.SOFTWARE,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.software_title_google_play_services_install_date)
                )
                awaitComplete()
            }
        }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Returns error when play services install date with an exception on older Android`() =
        runTest {
            val context: Context = mockk()
            val packageManager: PackageManager = mockk()
            val packageInfo: PackageInfo = mockk()
            every { context.packageManager } returns packageManager
            every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
            val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.software_title_google_play_services_install_date,
                        itemType = ItemType.SOFTWARE,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.software_title_google_play_services_install_date)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when play services update date is available`() = runTest {
        val date = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT")
            timeInMillis = 946684800000
        }
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                packageName = "com.google.android.gms"
                lastUpdateTime = date.timeInMillis
            }
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_updated_date,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text(
                        DateFormat.getDateFormat(context).format(date.time)
                    )
                ), awaitItemFromList(R.string.software_title_google_play_services_updated_date)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services update date with a null system service`() = runTest {
        shadowOf(context.packageManager).deletePackage("com.google.android.gms")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_google_play_services_updated_date,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_google_play_services_updated_date)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when play services update date with an exception on Android 13+`() =
        runTest {
            val context: Context = mockk()
            val packageManager: PackageManager = mockk()
            val packageInfo: PackageInfo = mockk()
            every { context.packageManager } returns packageManager
            every {
                packageManager.getPackageInfo(any<String>(), any<PackageManager.PackageInfoFlags>())
            } returns packageInfo
            val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.software_title_google_play_services_updated_date,
                        itemType = ItemType.SOFTWARE,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.software_title_google_play_services_updated_date)
                )
                awaitComplete()
            }
        }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Returns error when play services update date with an exception on older Android`() =
        runTest {
            val context: Context = mockk()
            val packageManager: PackageManager = mockk()
            val packageInfo: PackageInfo = mockk()
            every { context.packageManager } returns packageManager
            every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
            val repository = SoftwareRepository(dispatcherProvider, context, preferencesManager)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.software_title_google_play_services_updated_date,
                        itemType = ItemType.SOFTWARE,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.software_title_google_play_services_updated_date)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when web view version is available`() = runTest {
        ShadowWebView.setCurrentWebViewPackage(PackageInfo().apply {
            packageName = "android.webkit.WebViewUpdateService"
            versionName = "88.0.4324.96"
        })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_webview_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Text("88.0.4324.96")
                ), awaitItemFromList(R.string.software_title_webview_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when web view version with a null system web view`() = runTest {
        ShadowWebView.setCurrentWebViewPackage(null)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.software_title_webview_version,
                    itemType = ItemType.SOFTWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.software_title_webview_version)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when web view version with an exception`() = runTest { }
}