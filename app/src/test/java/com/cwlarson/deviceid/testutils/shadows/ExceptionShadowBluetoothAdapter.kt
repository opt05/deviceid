package com.cwlarson.deviceid.testutils.shadows

import android.bluetooth.BluetoothAdapter
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowBluetoothAdapter

@Implements(BluetoothAdapter::class)
class ExceptionShadowBluetoothAdapter: ShadowBluetoothAdapter() {

    @Implementation
    override fun getAddress(): String { throw NullPointerException() }

    @Implementation
    override fun getName(): String { throw NullPointerException() }
}