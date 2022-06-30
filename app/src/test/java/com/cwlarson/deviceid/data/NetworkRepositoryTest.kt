package com.cwlarson.deviceid.data

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccInfo
import android.telephony.euicc.EuiccManager
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.extract
import org.robolectric.shadows.ShadowWifiInfo
import java.net.InetAddress

@Ignore("Timeouts")
@RunWith(AndroidJUnit4::class)
class NetworkRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var repository: NetworkRepository

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = mock()
        repository = NetworkRepository(dispatcherProvider, context, preferencesManager)
    }

    @Test
    fun `Verify item list is returned when items method is called`() = runTest {
        repository.items().test {
            val item = awaitItem()
            assertEquals(R.string.network_title_device_software_version, item[0].title)
            assertEquals(R.string.network_title_wifi_mac, item[1].title)
            assertEquals(R.string.network_title_wifi_bssid, item[2].title)
            assertEquals(R.string.network_title_wifi_ssid, item[3].title)
            assertEquals(R.string.network_title_wifi_frequency, item[4].title)
            assertEquals(R.string.network_title_wifi_hidden_ssid, item[5].title)
            assertEquals(R.string.network_title_wifi_ip_address, item[6].title)
            assertEquals(R.string.network_title_wifi_link_speed, item[7].title)
            assertEquals(R.string.network_title_wifi_tx_link_speed, item[8].title)
            assertEquals(R.string.network_title_wifi_network_id, item[9].title)
            assertEquals(R.string.network_title_wifi_passpoint_fqdn, item[10].title)
            assertEquals(R.string.network_title_wifi_passpoint_friendly_name, item[11].title)
            assertEquals(R.string.network_title_wifi_rssid, item[12].title)
            assertEquals(R.string.network_title_wifi_signal_level, item[13].title)
            assertEquals(R.string.network_title_wifi_hostname, item[14].title)
            assertEquals(R.string.network_title_wifi_canonical_hostname, item[15].title)
            assertEquals(R.string.network_title_bluetooth_mac, item[16].title)
            assertEquals(R.string.network_title_bluetooth_hostname, item[17].title)
            assertEquals(R.string.network_title_manufacturer_code, item[18].title)
            assertEquals(R.string.network_title_nai, item[19].title)
            assertEquals(R.string.network_title_phone_count, item[20].title)
            assertEquals(R.string.network_title_sim_serial, item[21].title)
            assertEquals(R.string.network_title_sim_operator, item[22].title)
            assertEquals(R.string.network_title_sim_country, item[23].title)
            assertEquals(R.string.network_title_sim_state, item[24].title)
            assertEquals(R.string.network_title_phone_number, item[25].title)
            assertEquals(R.string.network_title_voicemail_number, item[26].title)
            assertEquals(R.string.network_title_cell_network_name, item[27].title)
            assertEquals(R.string.network_title_cell_network_type, item[28].title)
            assertEquals(R.string.network_title_cell_network_class, item[29].title)
            assertEquals(R.string.network_title_esim_id, item[30].title)
            assertEquals(R.string.network_title_esim_enabled, item[31].title)
            assertEquals(R.string.network_title_esim_os_version, item[32].title)
            assertEquals(R.string.network_title_concurrent_voice_data_supported, item[33].title)
            assertEquals(R.string.network_title_data_roaming_enabled, item[34].title)
            assertEquals(R.string.network_title_hearing_aid_supported, item[35].title)
            assertEquals(R.string.network_title_multi_sim_supported, item[36].title)
            assertEquals(R.string.network_title_rtt_supported, item[37].title)
            assertEquals(R.string.network_title_sms_capable, item[38].title)
            assertEquals(R.string.network_title_voice_capable, item[39].title)
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when software version with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_device_software_version,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text(null)
                ), awaitItemFromList(R.string.network_title_device_software_version)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns permission needed when software version with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_device_software_version,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_device_software_version)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when software version with an exception and permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_device_software_version,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_device_software_version)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns error when software version with a null system service and permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_device_software_version,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_device_software_version)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns text when wifi mac is below android M`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setMacAddress("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("test")
                ), awaitItemFromList(R.string.network_title_wifi_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns not possible when wifi mac is above android M`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
                ), awaitItemFromList(R.string.network_title_wifi_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi mac with an exception and is below android M`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns error when wifi mac with a null system service and is below android M`() =
        runTest {
            shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_mac,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_mac)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns text when wifi bssid is below android P`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setBSSID("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_bssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("test")
                ), awaitItemFromList(R.string.network_title_wifi_bssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns permission needed when wifi bssid is above android O with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_bssid,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.AccessFineLocation)
                    ), awaitItemFromList(R.string.network_title_wifi_bssid)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when wifi bssid is above android O with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setBSSID("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_bssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("test")
                ), awaitItemFromList(R.string.network_title_wifi_bssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi bssid is above android O with an exception with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_bssid,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_bssid)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns error when wifi bssid with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_bssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_bssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns error when wifi bssid is below android P with an exception`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_bssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_bssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns text when wifi ssid is below android P`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setSSID("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("\"test\"")
                ), awaitItemFromList(R.string.network_title_wifi_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns permission needed when wifi ssid is above android O with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_ssid,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.AccessFineLocation)
                    ), awaitItemFromList(R.string.network_title_wifi_ssid)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when wifi ssid is above android O with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setSSID("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("\"test\"")
                ), awaitItemFromList(R.string.network_title_wifi_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi ssid is above android O with an exception with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_ssid,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_ssid)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi ssid is below android P with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi ssid with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi frequency is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setFrequency(54)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_frequency,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("54${WifiInfo.FREQUENCY_UNITS}")
                ), awaitItemFromList(R.string.network_title_wifi_frequency)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi frequency with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_frequency,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_frequency)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi frequency with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_frequency,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_frequency)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi hidden ssid is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setSSID("test")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hidden_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("false")
                ), awaitItemFromList(R.string.network_title_wifi_hidden_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi hidden ssid with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hidden_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_hidden_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi hidden ssid with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hidden_ssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_hidden_ssid)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi ip address is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                @Suppress("BlockingMethodInNonBlockingContext")
                shadowOf(this).setInetAddress(InetAddress.getByName("8.8.8.8"))
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ip_address,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("8.8.8.8")
                ), awaitItemFromList(R.string.network_title_wifi_ip_address)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi ip address with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ip_address,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_ip_address)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi ip address with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_ip_address,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_ip_address)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi link speed is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setLinkSpeed(34)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_link_speed,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("34${WifiInfo.LINK_SPEED_UNITS}")
                ), awaitItemFromList(R.string.network_title_wifi_link_speed)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi link speed with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_link_speed,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_link_speed)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi link speed with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_link_speed,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_link_speed)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], shadows = [MyShadowWifiInfo::class])
    fun `Returns text when wifi tx link speed is available and is above Android P`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                extract<MyShadowWifiInfo>(this).setTxLinkSpeedMbps(10)
            })

        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_tx_link_speed,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("10${WifiInfo.LINK_SPEED_UNITS}")
                ), awaitItemFromList(R.string.network_title_wifi_tx_link_speed)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi tx link speed with an exception and is above Android P`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_tx_link_speed,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_tx_link_speed)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when wifi tx link speed with a null system service and is above Android P`() =
        runTest {
            shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_tx_link_speed,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_tx_link_speed)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns not possible when wifi tx link speed is below android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_tx_link_speed,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_wifi_tx_link_speed)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi network id is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setNetworkId(87)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_network_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("87")
                ), awaitItemFromList(R.string.network_title_wifi_network_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi network id with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_network_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_network_id)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi network id with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_network_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_network_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], shadows = [MyShadowWifiInfo::class])
    fun `Returns text when wifi passpoint is available and is above Android P`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                extract<MyShadowWifiInfo>(this).setPasspointFqdn("http://www.google.com/index.html")
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_passpoint_fqdn,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("http://www.google.com/index.html")
                ), awaitItemFromList(R.string.network_title_wifi_passpoint_fqdn)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi passpoint with an exception and is above Android P`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_passpoint_fqdn,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_passpoint_fqdn)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when wifi passpoint with a null system service and is above Android P`() =
        runTest {
            shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_passpoint_fqdn,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_passpoint_fqdn)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns not possible when wifi passpoint is below android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_passpoint_fqdn,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_wifi_passpoint_fqdn)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], shadows = [MyShadowWifiInfo::class])
    fun `Returns text when wifi passpoint name is available and is above Android P`() =
        runTest {
            shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                    extract<MyShadowWifiInfo>(this).setPasspointProviderFriendlyName("Google")
                })
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_passpoint_friendly_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("Google")
                    ), awaitItemFromList(R.string.network_title_wifi_passpoint_friendly_name)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi passpoint name with a null system service and is above Android P`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_passpoint_friendly_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_passpoint_friendly_name)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when wifi passpoint name with an exception and is above Android P`() =
        runTest {
            shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_wifi_passpoint_friendly_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_wifi_passpoint_friendly_name)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns not possible when wifi passpoint name is below android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_passpoint_friendly_name,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_wifi_passpoint_friendly_name)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi rssi is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setRssi(66)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_rssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("66")
                ), awaitItemFromList(R.string.network_title_wifi_rssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi rssi with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_rssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_rssid)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi rssi with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_rssid,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_rssid)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns text when wifi signal level is available and is above Android Q`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setRssi(-80)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_signal_level,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("0%")
                ), awaitItemFromList(R.string.network_title_wifi_signal_level)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns text when wifi signal level is available and is below Android R`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                shadowOf(this).setRssi(-80)
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_signal_level,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("99%")
                ), awaitItemFromList(R.string.network_title_wifi_signal_level)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi signal level with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_signal_level,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_signal_level)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when wifi signal level with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.WIFI_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_signal_level,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_signal_level)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi hostname is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                @Suppress("BlockingMethodInNonBlockingContext")
                shadowOf(this).setInetAddress(InetAddress.getByName("142.250.69.196"))
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("sea30s08-in-f4.1e100.net")
                ), awaitItemFromList(R.string.network_title_wifi_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi hostname with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowContextImpl::class])
    fun `Returns error when wifi hostname with a null system service`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when wifi canonical hostname is available`() = runTest {
        shadowOf(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .setConnectionInfo(ShadowWifiInfo.newInstance().apply {
                @Suppress("BlockingMethodInNonBlockingContext")
                shadowOf(this).setInetAddress(InetAddress.getByName("142.250.69.196"))
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_canonical_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("sea30s08-in-f4.1e100.net")
                ), awaitItemFromList(R.string.network_title_wifi_canonical_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowWifiInfo::class])
    fun `Returns error when wifi canonical hostname with an exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_canonical_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_canonical_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowContextImpl::class])
    fun `Returns error when wifi canonical hostname with a null system service`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_wifi_canonical_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_wifi_canonical_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns text when bluetooth mac is below android M`() = runTest {
        shadowOf((context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter)
            .setAddress("00:00:00:00:00:00")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("00:00:00:00:00:00")
                ), awaitItemFromList(R.string.network_title_bluetooth_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns not possible when bluetooth mac is above android N`() = runTest {
        shadowOf((context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter)
            .setAddress("00:00:00:00:00:00")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
                ), awaitItemFromList(R.string.network_title_bluetooth_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(
        sdk = [Build.VERSION_CODES.LOLLIPOP_MR1],
        shadows = [ExceptionShadowBluetoothAdapter::class]
    )
    fun `Returns error when bluetooth mac with an exception and is below M`() = runTest {
        shadowOf((context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter)
            .setAddress("00:00:00:00:00:00")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_mac,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_bluetooth_mac)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns error when bluetooth mac with a null system service and is below M`() =
        runTest {
            shadowOf(context).removeSystemService(Context.BLUETOOTH_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_bluetooth_mac,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_bluetooth_mac)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when bluetooth hostname is available with permission granted and above Android S`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_bluetooth_hostname,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("DefaultBluetoothDeviceName")
                    ), awaitItemFromList(R.string.network_title_bluetooth_hostname)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when bluetooth hostname is available with permission not granted and above Android S`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_bluetooth_hostname,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.AccessBluetoothConnect)
                    ), awaitItemFromList(R.string.network_title_bluetooth_hostname)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns text when bluetooth hostname is available and below Android S`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("DefaultBluetoothDeviceName")
                ), awaitItemFromList(R.string.network_title_bluetooth_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowBluetoothAdapter::class])
    fun `Returns error when bluetooth hostname with an exception`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_bluetooth_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when bluetooth hostname with a null system service`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        shadowOf(context).removeSystemService(Context.BLUETOOTH_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_bluetooth_hostname,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_bluetooth_hostname)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when manufacturer code is above android P`() = runTest {
        extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setManufacturerCode("12345678")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_manufacturer_code,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("12345678")
                ), awaitItemFromList(R.string.network_title_manufacturer_code)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns not possible when manufacturer code is below android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_manufacturer_code,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_manufacturer_code)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when manufacturer code with an exception and is above P`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_manufacturer_code,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_manufacturer_code)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when manufacturer code with a null system service and is above P`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_manufacturer_code,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_manufacturer_code)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when nai is android P and permissions granted`() = runTest {
        extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setNai("user@realm")
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_nai,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("user@realm")
                ), awaitItemFromList(R.string.network_title_nai)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [MyShadowTelephonyManager::class])
    fun `Returns permission needed when nai is android P and permissions not granted`() =
        runTest {
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNai("user@realm")
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_nai,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_nai)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when nai is below android P`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_nai,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
                ), awaitItemFromList(R.string.network_title_nai)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns not possible when nai is above android P`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_nai,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.P)
                ), awaitItemFromList(R.string.network_title_nai)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when nai with an exception and is android P with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_nai,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_nai)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns error when nai with a null system service and is android P with permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_nai,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_nai)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when phone count is available and is above android Q`() = runTest {
        extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setActiveModemCount(5)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_count,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("5")
                ), awaitItemFromList(R.string.network_title_phone_count)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when phone count with exception and is above android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_count,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_phone_count)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns error when phone count with a null system service and is above android Q`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_phone_count,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_phone_count)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns text when phone count is available and is above android N`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setPhoneCount(5)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_count,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("5")
                ), awaitItemFromList(R.string.network_title_phone_count)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when phone count with exception and is above android N`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_count,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_phone_count)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns error when phone count with a null system service and is above android N`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_phone_count,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_phone_count)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when phone count is below android M`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_count,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
                ), awaitItemFromList(R.string.network_title_phone_count)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when sim serial is available and is below android Q`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimSerialNumber("89900123450004598765")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_serial,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("89900123450004598765")
                ), awaitItemFromList(R.string.network_title_sim_serial)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns not available when sim serial is above android P`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimSerialNumber("89900123450004598765")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_serial,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_sim_serial)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when sim serial with exception and is below android Q`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_serial,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_serial)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns error when sim serial with a null system service and is below android Q`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_sim_serial,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_sim_serial)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when sim operator name is available`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimOperatorName("Tribble")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_operator,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Tribble")
                ), awaitItemFromList(R.string.network_title_sim_operator)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when sim operator name with an exception`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimOperatorName("Tribble")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_operator,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_operator)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when sim operator name with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_operator,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_operator)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim country is available`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimCountryIso("IE")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_country,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("IE")
                ), awaitItemFromList(R.string.network_title_sim_country)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when sim country with an exception`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimOperatorName("IE")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_country,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_country)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when sim country with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_country,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_country)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is absent`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_ABSENT)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("No SIM card is available in the device")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is network locked`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_NETWORK_LOCKED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Locked: requires a network PIN to unlock")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is pin required`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_PIN_REQUIRED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Locked: requires the user's SIM PIN to unlock")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is puk required`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_PUK_REQUIRED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Locked: requires the user's SIM PUK to unlock")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is ready`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_READY)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Ready")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is not ready`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_NOT_READY)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Not ready")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is perm disabled`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_PERM_DISABLED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Permanently disabled")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is unknown`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_UNKNOWN)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Network unknown")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is card io error`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_CARD_IO_ERROR)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Error, present but faulty")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is card restricted`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_CARD_RESTRICTED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Restricted, present but not usable due to carrier restrictions")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when sim state is other`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_UNKNOWN)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Network unknown")
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when sim state with an exception`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setSimState(TelephonyManager.SIM_STATE_UNKNOWN)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when sim state with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sim_state,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sim_state)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns text when phone number is available with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setLine1Number("+1-800-867-5309")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_phone_number,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("+1-800-867-5309")
                ), awaitItemFromList(R.string.network_title_phone_number)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns permissions needed when phone number is available with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setLine1Number("+1-800-867-5309")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_phone_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_phone_number)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when phone number with an exception with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setLine1Number("+1-800-867-5309")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_phone_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_phone_number)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns error when phone number with a null system service with permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_phone_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_phone_number)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when voicemail number is available with permissions granted`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setVoiceMailNumber("+1-800-999-9999")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_voicemail_number,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("+1-800-999-9999")
                ), awaitItemFromList(R.string.network_title_voicemail_number)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns permissions needed when voicemail number is available with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setVoiceMailNumber("+1-800-999-9999")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_voicemail_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_voicemail_number)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when voicemail number with an exception with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setVoiceMailNumber("+1-800-999-9999")
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_voicemail_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_voicemail_number)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns error when voicemail number with a null system service with permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_voicemail_number,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_voicemail_number)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when cell network name is available`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setNetworkOperatorName("Android Wireless")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_name,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("Android Wireless")
                ), awaitItemFromList(R.string.network_title_cell_network_name)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when cell network name with an exception`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setVoiceMailNumber("Android Wireless")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_name,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_cell_network_name)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when cell network name with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_name,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_cell_network_name)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is gprs and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_GPRS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("GPRS")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is gprs and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_GPRS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("GPRS")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is edge and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("EDGE")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is edge and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("EDGE")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is umts and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_UMTS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("UMTS")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is umts and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_UMTS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("UMTS")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is hsdpa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSDPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is hsdpa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSDPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is hsupa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSUPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is hsupa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSUPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is hspa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is hspa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSPA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is cdma and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is cdma and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_CDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is evdo 0 and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. 0")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is evdo 0 and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. 0")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is evdo a and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. A")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is evdo a and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. A")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is evdo b and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. B")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is evdo b and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - EvDo rev. B")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is 1xrtt and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - 1xRTT")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is 1xrtt and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - 1xRTT")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is lte and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("LTE")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is lte and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("LTE")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is ehrpd and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - eHRPD")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is ehrpd and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("CDMA - eHRPD")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is iden and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_IDEN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("iDEN")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is iden and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_IDEN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("iDEN")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is hspap and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSPA+")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is hspap and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("HSPA+")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is gsm and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_GSM)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("GSM")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is gsm and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_GSM)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("GSM")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is td scdma and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_TD_SCDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("TD_SCDMA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is td scdma and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_TD_SCDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("TD_SCDMA")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is iwlan and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_IWLAN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("IWLAN")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is iwlan and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_IWLAN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("IWLAN")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network type is 5g and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("5G")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network type is 5g and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("5G")
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns null when cell network type is other and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text(null)
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns null when cell network type is other and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text(null)
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns permissions needed when cell network type is available with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_type,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_cell_network_type)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when cell network type with an exception and is below android N`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_cell_network_name)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when cell network type with an exception and is android N+`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setDataNetworkType(TelephonyManager.NETWORK_TYPE_NR)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_name,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_cell_network_name)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when cell network type with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_type,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_cell_network_type)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is gprs and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_GPRS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is gprs and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_GPRS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is edge and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is edge and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is umts and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_UMTS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is umts and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_UMTS)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is hsdpa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is hsdpa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is hsupa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is hsupa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is hspa and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is hspa and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSPA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is cdma and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is cdma and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_CDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is evdo 0 and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is evdo 0 and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is evdo a and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is evdo a and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is evdo b and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is evdo b and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is 1xrtt and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is 1xrtt and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is lte and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("4G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is lte and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("4G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is ehrpd and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is ehrpd and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is iden and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_IDEN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is iden and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_IDEN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is hspap and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is hspap and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is gsm and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_GSM)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is gsm and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_GSM)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("2G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is td scdma and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_TD_SCDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is td scdma and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_TD_SCDMA)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("3G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is iwlan and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_IWLAN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("4G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is iwlan and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_IWLAN)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("4G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when cell network class is 5g and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("5G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when cell network class is 5g and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("5G")
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns null when cell network class is other and is below android N with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text(null)
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns null when cell network class is other and is android N+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text(null)
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns permissions needed when cell network class is available with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(9999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_class,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_cell_network_class)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when cell network class with an exception and is below android N`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            @Suppress("DEPRECATION")
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_cell_network_name)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when cell network class with an exception and is android N+`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setDataNetworkType(TelephonyManager.NETWORK_TYPE_NR)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_cell_network_name,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_cell_network_name)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns error when cell network class with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_cell_network_class,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_cell_network_class)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when concurrent voice data is available and is android O+`() = runTest {
        extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setIsConcurrentVoiceAndDataSupported(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_concurrent_voice_data_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_concurrent_voice_data_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns not available when concurrent voice data is below android O`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_concurrent_voice_data_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O)
                ), awaitItemFromList(R.string.network_title_concurrent_voice_data_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when concurrent voice data with exception and is android O+`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_concurrent_voice_data_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_concurrent_voice_data_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `Returns error when concurrent voice data with a null system service and is android O+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_concurrent_voice_data_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_concurrent_voice_data_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when data roaming data is available and is android Q+ with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setIsDataRoamingEnabled(true)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_data_roaming_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("true")
                    ), awaitItemFromList(R.string.network_title_data_roaming_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns permission needed when data roaming is below android Q+ with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_data_roaming_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_data_roaming_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns not available when data roaming data is below android Q`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_data_roaming_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                    ), awaitItemFromList(R.string.network_title_data_roaming_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when data roaming data with exception and is android Q+ with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_data_roaming_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_data_roaming_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when data roaming data with a null system service and is android Q+ with permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_data_roaming_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_data_roaming_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns text when hearing aid supported is available and is android M+`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setHearingAidCompatibilitySupported(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_hearing_aid_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_hearing_aid_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not available when hearing aid supported is below android M`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setHearingAidCompatibilitySupported(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_hearing_aid_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
                ), awaitItemFromList(R.string.network_title_hearing_aid_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when hearing aid supported with exception and is android M+`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_hearing_aid_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_hearing_aid_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `Returns error when hearing aid supported with a null system service and is android M+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_hearing_aid_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_hearing_aid_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when multi sim supported is allowed and is android Q+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setIsMultiSimSupported(TelephonyManager.MULTISIM_ALLOWED)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("Supports multiple SIMs")
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when multi sim supported is not supported and is android Q+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setIsMultiSimSupported(TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_HARDWARE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("Device does not support multiple SIMs")
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns text when multi sim supported is not supported by carrier and is android Q+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setIsMultiSimSupported(TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_CARRIER)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text("Device does support multiple SIMS but restricted by carrier")
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [MyShadowTelephonyManager::class])
    fun `Returns null when multi sim supported is else and is android Q+ with permissions`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setIsMultiSimSupported(999999999)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Text(null)
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns permission needed when multi sim supported is android Q+ with permissions not granted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1], shadows = [MyShadowTelephonyManager::class])
    fun `Returns not available when multi sim supported is below android Q`() = runTest {
        extract<MyShadowTelephonyManager>(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setIsMultiSimSupported(TelephonyManager.MULTISIM_ALLOWED)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_multi_sim_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                ), awaitItemFromList(R.string.network_title_multi_sim_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when multi sim supported with exception and is android Q+ with permissions granted`() =
        runTest {
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when multi sim supported with a null system service and is android Q+ with permissions granted`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_multi_sim_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_multi_sim_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when rtt supported is available and is android Q+`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setRttSupported(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_rtt_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_rtt_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when rtt supported is available and is below android Q`() =
        runTest {
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setRttSupported(true)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_rtt_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                    ), awaitItemFromList(R.string.network_title_rtt_supported)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when rtt supported with exception and is android Q+`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_rtt_supported,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_rtt_supported)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns error when rtt supported with a null system service and is android Q+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_rtt_supported,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_rtt_supported)
                )
                awaitComplete()
            }
        }

    @Test
    fun `Returns text when sms supported is available`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setIsSmsCapable(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sms_capable,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_sms_capable)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowTelephonyManager::class])
    fun `Returns error when sms supported with exception`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sms_capable,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sms_capable)
            )
            awaitComplete()
        }
    }

    @Test
    fun `Returns error when sms supported with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_sms_capable,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_sms_capable)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns text when voice capable is available and is android L_MR1+`() = runTest {
        shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .setVoiceCapable(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_voice_capable,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_voice_capable)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `Returns not possible when voice capable is available and is below android L_MR1`() =
        runTest {
            shadowOf(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .setVoiceCapable(true)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_voice_capable,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.LOLLIPOP_MR1)
                    ), awaitItemFromList(R.string.network_title_voice_capable)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(
        sdk = [Build.VERSION_CODES.LOLLIPOP_MR1],
        shadows = [ExceptionShadowTelephonyManager::class]
    )
    fun `Returns error when sms supported with exception and is android L_MR1+`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_voice_capable,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_voice_capable)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns error when sms supported with a null system service and is android L_MR1+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.TELEPHONY_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_voice_capable,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_voice_capable)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when esim id available and is android P+`() = runTest {
        shadowOf(context.getSystemService(Context.EUICC_SERVICE) as EuiccManager)
            .setEid("1234567890")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("1234567890")
                ), awaitItemFromList(R.string.network_title_esim_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when esim id available and is below android P`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
                ), awaitItemFromList(R.string.network_title_esim_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowEuiccManager::class])
    fun `Returns error when esim id with exception and is android P+`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_id,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_esim_id)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns error when esim id with a null system service and is android P+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.EUICC_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_esim_id,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_esim_id)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns text when esim enabled available and is android P+`() = runTest {
        shadowOf(context.getSystemService(Context.EUICC_SERVICE) as EuiccManager)
            .setIsEnabled(true)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_enabled,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("true")
                ), awaitItemFromList(R.string.network_title_esim_enabled)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when esim enabled available and is below android P`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_enabled,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
                ), awaitItemFromList(R.string.network_title_esim_enabled)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowEuiccManager::class])
    fun `Returns error when esim enabled with exception and is android P+`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_enabled,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_esim_enabled)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns error when esim enabled with a null system service and is android P+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.EUICC_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_esim_enabled,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_esim_enabled)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [MyShadowEuiccManager::class])
    fun `Returns text when esim os version available and is android P+`() = runTest {
        extract<MyShadowEuiccManager>(context.getSystemService(Context.EUICC_SERVICE) as EuiccManager)
            .setEuiccInfo(EuiccInfo("19041.1110"))
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_os_version,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text("19041.1110")
                ), awaitItemFromList(R.string.network_title_esim_os_version)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [MyShadowEuiccManager::class])
    fun `Returns null when esim os version not available and is android P+`() = runTest {
        extract<MyShadowEuiccManager>(context.getSystemService(Context.EUICC_SERVICE) as EuiccManager)
            .setEuiccInfo(null)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_os_version,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Text(null)
                ), awaitItemFromList(R.string.network_title_esim_os_version)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `Returns not possible when esim os version available and is below android P`() =
        runTest {
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_esim_os_version,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
                    ), awaitItemFromList(R.string.network_title_esim_os_version)
                )
                awaitComplete()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P], shadows = [ExceptionShadowEuiccManager::class])
    fun `Returns error when esim os version with exception and is android P+`() = runTest {
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.network_title_esim_os_version,
                    itemType = ItemType.NETWORK,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.network_title_esim_os_version)
            )
            awaitComplete()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `Returns error when esim os version with a null system service and is android P+`() =
        runTest {
            shadowOf(context).removeSystemService(Context.EUICC_SERVICE)
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.network_title_esim_os_version,
                        itemType = ItemType.NETWORK,
                        subtitle = ItemSubtitle.Error
                    ), awaitItemFromList(R.string.network_title_esim_os_version)
                )
                awaitComplete()
            }
        }
}