package com.cwlarson.deviceid.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.cwlarson.deviceid.R

private val OpenSans = FontFamily(
    Font(R.font.opensans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.opensans_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.opensans_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.opensans_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.opensans_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.opensans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.opensans_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.opensans_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.opensans_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.opensans_semibolditalic, FontWeight.SemiBold, FontStyle.Italic)
)

val Ubuntu = FontFamily(
    Font(R.font.ubuntu_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.ubuntu_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.ubuntu_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.ubuntu_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.ubuntu_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.ubuntu_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.ubuntu_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.ubuntu_mediumitalic, FontWeight.Medium, FontStyle.Italic)
)

val Typography = Typography(defaultFontFamily = OpenSans)