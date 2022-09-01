package com.cwlarson.deviceid.ui.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

val BlueGrey200 = Color(0xFF9BA3A7)
val BlueGrey500 = Color(0xFF37474f)
val BlueGrey700 = Color(0xFF263238)

val Teal200 = Color(0xFF03DAC6)
val Teal500 = Color(0xFF018786)

val Red200 = Color(0xFFCF6679)
val Red500 = Color(0xFFB00020)

val DarkGrey = Color(0xFF121212)

val Colors.navigationBackgroundSelected: Color
    get() = if (isLight) Color(0x1F37474F) else Color(0x1F9BA3A7)
val Colors.statusBarColor: Color
    get() = if(isLight) Color(0xFF263238) else Color(0xFF121212)
