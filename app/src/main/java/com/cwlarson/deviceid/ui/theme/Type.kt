@file:OptIn(ExperimentalTextApi::class)

package com.cwlarson.deviceid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.cwlarson.deviceid.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts", providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
private val OpenSans = FontFamily(Font(GoogleFont("Open Sans"), provider))
private val Ubuntu = FontFamily(Font(GoogleFont("Ubuntu"), provider))
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Ubuntu),
        displayMedium = displayMedium.copy(fontFamily = Ubuntu),
        displaySmall = displaySmall.copy(fontFamily = Ubuntu),
        headlineLarge = headlineLarge.copy(fontFamily = Ubuntu),
        headlineMedium = headlineMedium.copy(fontFamily = Ubuntu),
        headlineSmall = headlineSmall.copy(fontFamily = Ubuntu),
        titleLarge = titleLarge.copy(fontFamily = Ubuntu),
        titleMedium = titleMedium.copy(fontFamily = Ubuntu),
        titleSmall = titleSmall.copy(fontFamily = Ubuntu),
        bodyLarge = bodyLarge.copy(fontFamily = OpenSans),
        bodyMedium = bodyMedium.copy(fontFamily = OpenSans),
        bodySmall = bodySmall.copy(fontFamily = OpenSans),
        labelLarge = labelLarge.copy(fontFamily = OpenSans),
        labelMedium = labelMedium.copy(fontFamily = OpenSans),
        labelSmall = labelSmall.copy(fontFamily = OpenSans)
    )
}