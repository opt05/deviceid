package com.cwlarson.deviceid.ui.icons

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.theme.isLight

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NoItemsPreviewLight() = AppTheme {
    Image(imageVector = noItemsIcon(), contentDescription = null)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun NoItemsPreviewDark() = AppTheme {
    Image(imageVector = noItemsIcon(), contentDescription = null)
}

@Composable
fun noItemsIcon() = if (isLight) Icons.Default.NoItemsLight else Icons.Default.NoItemsDark

@Suppress("unused")
val Icons.Filled.NoItemsLight: ImageVector
    get() {
        if (_noItemsLight != null) return _noItemsLight!!
        _noItemsLight = ImageVector.Builder(
            name = "Filled.NoItemsLight", defaultWidth = 120.0.dp, defaultHeight = 120.0.dp,
            viewportWidth = 48.0f, viewportHeight = 48.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(30.0f, 6.0f)
                lineToRelative(8.0f, 8.0f)
                lineToRelative(0.0f, 28.0f)
                lineToRelative(-28.0f, 0.0f)
                lineToRelative(0.0f, -36.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(29.0f, 7.5f)
                lineToRelative(7.5f, 7.5f)
                lineToRelative(-7.5f, 0.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(22.5f, 30.3f)
                curveToRelative(0.0f, -4.7f, 3.6f, -4.4f, 3.6f, -7.2f)
                curveToRelative(0.0f, -0.7f, -0.2f, -2.1f, -2.0f, -2.1f)
                curveToRelative(-2.0f, 0.0f, -2.1f, 1.6f, -2.1f, 2.0f)
                horizontalLineToRelative(-2.7f)
                curveToRelative(0.0f, -0.7f, 0.3f, -4.2f, 4.8f, -4.2f)
                curveToRelative(4.6f, 0.0f, 4.7f, 3.6f, 4.7f, 4.3f)
                curveToRelative(0.0f, 3.5f, -3.8f, 4.0f, -3.8f, 7.3f)
                horizontalLineToRelative(-2.5f)
                close()
                moveTo(22.3f, 33.8f)
                curveToRelative(0.0f, -0.2f, 0.0f, -1.5f, 1.5f, -1.5f)
                curveToRelative(1.4f, 0.0f, 1.5f, 1.3f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.4f, -0.2f, 1.4f, -1.5f, 1.4f)
                curveToRelative(-1.3f, 0.0f, -1.5f, -1.0f, -1.5f, -1.4f)
                close()
            }
        }.build()
        return _noItemsLight!!
    }

@Suppress("ObjectPropertyName")
private var _noItemsLight: ImageVector? = null

@Suppress("unused")
val Icons.Filled.NoItemsDark: ImageVector
    get() {
        if (_noItemsDark != null) return _noItemsDark!!
        _noItemsDark = ImageVector.Builder(
            name = "Filled.NoItemsDark", defaultWidth = 120.0.dp, defaultHeight = 120.0.dp,
            viewportWidth = 48.0f, viewportHeight = 48.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(30.0f, 6.0f)
                lineToRelative(8.0f, 8.0f)
                lineToRelative(0.0f, 28.0f)
                lineToRelative(-28.0f, 0.0f)
                lineToRelative(0.0f, -36.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(29.0f, 7.5f)
                lineToRelative(7.5f, 7.5f)
                lineToRelative(-7.5f, 0.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(22.5f, 30.3f)
                curveToRelative(0.0f, -4.7f, 3.6f, -4.4f, 3.6f, -7.2f)
                curveToRelative(0.0f, -0.7f, -0.2f, -2.1f, -2.0f, -2.1f)
                curveToRelative(-2.0f, 0.0f, -2.1f, 1.6f, -2.1f, 2.0f)
                horizontalLineToRelative(-2.7f)
                curveToRelative(0.0f, -0.7f, 0.3f, -4.2f, 4.8f, -4.2f)
                curveToRelative(4.6f, 0.0f, 4.7f, 3.6f, 4.7f, 4.3f)
                curveToRelative(0.0f, 3.5f, -3.8f, 4.0f, -3.8f, 7.3f)
                horizontalLineToRelative(-2.5f)
                close()
                moveTo(22.3f, 33.8f)
                curveToRelative(0.0f, -0.2f, 0.0f, -1.5f, 1.5f, -1.5f)
                curveToRelative(1.4f, 0.0f, 1.5f, 1.3f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.4f, -0.2f, 1.4f, -1.5f, 1.4f)
                curveToRelative(-1.3f, 0.0f, -1.5f, -1.0f, -1.5f, -1.4f)
                close()
            }
        }.build()
        return _noItemsDark!!
    }

@Suppress("ObjectPropertyName")
private var _noItemsDark: ImageVector? = null