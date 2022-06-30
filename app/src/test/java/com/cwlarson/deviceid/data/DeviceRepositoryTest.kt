package com.cwlarson.deviceid.data

import android.Manifest
import android.app.Application
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
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
import com.cwlarson.deviceid.testutils.shadows.*
import com.cwlarson.deviceid.util.AppPermission
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.extract
import org.robolectric.shadows.ShadowBuild

@RunWith(AndroidJUnit4::class)
class DeviceRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var repository: DeviceRepository

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = mock()
        repository = DeviceRepository(dispatcherProvider ,context, preferencesManager)
    }

    @Test
    fun `Verify item list is returned when items method is called`() = runTest {
        repository.items().test {
            val item = awaitItem()
            assertEquals(R.string.device_title_imei, item[0].title)
            assertEquals(R.string.device_title_model, item[1].title)
            assertEquals(R.string.device_title_serial, item[2].title)
            assertEquals(R.string.device_title_android_id, item[3].title)
            assertEquals(R.string.device_title_gsfid, item[4].title)
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when IMEI is below android O with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager).setDeviceId("test")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_imei,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("test")
                ), awaitItemFromList(R.string.device_title_imei)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns permission needed when IMEI is below android O with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager).setDeviceId("test")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.device_title_imei,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.device_title_imei)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when IMEI is below android O with an exception and permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.device_title_imei,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.device_title_imei)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns not possible when IMEI is above android N`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_imei,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
                ), awaitItemFromList(R.string.device_title_imei)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when device model is available and model starts with manufacturer`() =
        runTest {
            ShadowBuild.setManufacturer("manufacturer")
            ShadowBuild.setProduct("product")
            ShadowBuild.setModel("manufacturer model")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.device_title_model,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Text("Manufacturer model (product)")
                    ), awaitItemFromList(R.string.device_title_model)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when device model is available and model does not start with manufacturer`() =
        runTest {
            ShadowBuild.setManufacturer("manufacturer")
            ShadowBuild.setProduct("product")
            ShadowBuild.setModel("model")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.device_title_model,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Text("Manufacturer model (product)")
                    ), awaitItemFromList(R.string.device_title_model)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when device model is completely empty then returns blank`() = runTest {
        ShadowBuild.setManufacturer("")
        ShadowBuild.setProduct("")
        ShadowBuild.setModel("")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_model,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("")
                ), awaitItemFromList(R.string.device_title_model)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when device model is manufacturer empty then returns text`() = runTest {
        ShadowBuild.setManufacturer("")
        ShadowBuild.setProduct("product")
        ShadowBuild.setModel("model")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_model,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("Model (product)")
                ), awaitItemFromList(R.string.device_title_model)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when device model is manufacturer and model empty then returns text`() =
        runTest {
            ShadowBuild.setManufacturer("")
            ShadowBuild.setProduct("product")
            ShadowBuild.setModel("")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.device_title_model,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Text("(product)")
                    ), awaitItemFromList(R.string.device_title_model)
                )
                awaitComplete()
            }
        }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when device model with an exception`() = runTest { }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N], shadows = [MyShadowBuild::class])
    fun `Returns text when serial is below android O`() = runTest {
        MyShadowBuild.setSerial("RF1DB6K177Y")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_serial,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("RF1DB6K177Y")
                ), awaitItemFromList(R.string.device_title_serial)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns not possible when serial is above android N`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_serial,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
                ), awaitItemFromList(R.string.device_title_serial)
            )
            awaitComplete()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when serial with an exception`() = runTest { }

    @Test
    @Config(shadows = [MyShadowSecure::class])
    fun `Returns text when Android ID is available`() = runTest {
        MyShadowSecure.setAndroidID("1")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_android_id,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("1")
                ), awaitItemFromList(R.string.device_title_android_id)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when Android ID with an exception`() = runTest {
        val context = mock<Application>()
        whenever(context.contentResolver).thenThrow(NullPointerException())
        val repository = DeviceRepository(dispatcherProvider, context, preferencesManager)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_android_id,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.device_title_android_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [MyShadowContentResolver::class])
    fun `Returns text when GSFID is available`() = runTest {
        extract<MyShadowContentResolver>(
            ApplicationProvider.getApplicationContext<Application>()
                .contentResolver
        ).setGSFID("1814197643282848243")
        val repository = DeviceRepository(dispatcherProvider, context, preferencesManager)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_gsfid,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("192d5325003999f3")
                ), awaitItemFromList(R.string.device_title_gsfid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [MyShadowContentResolver::class])
    fun `Returns error when GSFID with unable to move to first`() = runTest {
        extract<MyShadowContentResolver>(
            ApplicationProvider.getApplicationContext<Application>()
                .contentResolver
        ).setGSFID(null)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_gsfid,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.device_title_gsfid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [MyShadowContentResolver::class])
    fun `Returns error when GSFID with column count less then two`() = runTest {
        extract<MyShadowContentResolver>(
            ApplicationProvider.getApplicationContext<Application>()
                .contentResolver
        ).setGSFID(null)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_gsfid,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.device_title_gsfid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [MyShadowContentResolver::class])
    fun `Returns error when GSFID with query is null`() = runTest {
        extract<MyShadowContentResolver>(
            ApplicationProvider.getApplicationContext<Application>()
                .contentResolver
        ).setGSFID(null)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_gsfid,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.device_title_gsfid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowContextImpl::class])
    fun `Returns error when GSFID with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.device_title_gsfid,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.device_title_gsfid)
            )
            awaitComplete()
        }
    }
}