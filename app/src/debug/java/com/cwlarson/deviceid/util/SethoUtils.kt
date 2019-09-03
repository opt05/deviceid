package com.cwlarson.deviceid.util

import android.app.Application
import com.facebook.stetho.Stetho

fun Application.installStetho() {
    Stetho.initializeWithDefaults(this)
}