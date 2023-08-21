package com.cwlarson.deviceid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.cwlarson.deviceid.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts", providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
private val openSans = FontFamily(Font(GoogleFont("Open Sans"), provider))
private val ubuntu = FontFamily(Font(GoogleFont("Ubuntu"), provider))
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = ubuntu),
        displayMedium = displayMedium.copy(fontFamily = ubuntu),
        displaySmall = displaySmall.copy(fontFamily = ubuntu),
        headlineLarge = headlineLarge.copy(fontFamily = ubuntu),
        headlineMedium = headlineMedium.copy(fontFamily = ubuntu),
        headlineSmall = headlineSmall.copy(fontFamily = ubuntu),
        titleLarge = titleLarge.copy(fontFamily = ubuntu),
        titleMedium = titleMedium.copy(fontFamily = ubuntu),
        titleSmall = titleSmall.copy(fontFamily = ubuntu),
        bodyLarge = bodyLarge.copy(fontFamily = openSans),
        bodyMedium = bodyMedium.copy(fontFamily = openSans),
        bodySmall = bodySmall.copy(fontFamily = openSans),
        labelLarge = labelLarge.copy(fontFamily = openSans),
        labelMedium = labelMedium.copy(fontFamily = openSans),
        labelSmall = labelSmall.copy(fontFamily = openSans)
    )
}