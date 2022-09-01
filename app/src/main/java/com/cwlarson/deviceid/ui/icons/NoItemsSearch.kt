package com.cwlarson.deviceid.ui.icons

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.material.MaterialTheme
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NoItemsSearchPreviewLight() = AppTheme {
    Image(imageVector = noItemsSearchIcon(), contentDescription = null)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun NoItemsSearchPreviewDark() = AppTheme {
    Image(imageVector = noItemsSearchIcon(), contentDescription = null)
}

@Composable
fun noItemsSearchIcon() = if (MaterialTheme.colors.isLight)
    Icons.Default.NoItemsSearchLight else Icons.Default.NoItemsSearchDark

@Suppress("unused")
val Icons.Filled.NoItemsSearchLight: ImageVector
    get() {
        if (_noItemsSearchLight != null) return _noItemsSearchLight!!
        _noItemsSearchLight = ImageVector.Builder(
            name = "Filled.NoItemsSearchLight", defaultWidth = 120.0.dp, defaultHeight = 120.0.dp,
            viewportWidth = 48.0f, viewportHeight = 48.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(33.0f, 42.0f)
                lineToRelative(-28.0f, 0.0f)
                lineToRelative(0.0f, -38.0f)
                lineToRelative(19.0f, 0.0f)
                lineToRelative(9.0f, 9.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(31.5f, 14.0f)
                lineToRelative(-8.5f, 0.0f)
                lineToRelative(0.0f, -8.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(34.5047f, 37.5805f)
                lineToRelative(1.9796f, -1.9796f)
                lineToRelative(8.484f, 8.484f)
                lineToRelative(-1.9796f, 1.9796f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(28.0f, 29.0f)
                moveToRelative(-11.0f, 0.0f)
                arcToRelative(
                    11.0f, 11.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = 22.0f, dy1 = 0.0f
                )
                arcToRelative(
                    11.0f, 11.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = -22.0f, dy1 = 0.0f
                )
            }
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(28.0f, 29.0f)
                moveToRelative(-9.0f, 0.0f)
                arcToRelative(
                    9.0f, 9.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = 18.0f, dy1 = 0.0f
                )
                arcToRelative(
                    9.0f, 9.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = -18.0f, dy1 = 0.0f
                )
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(36.8487f, 39.8797f)
                lineToRelative(1.9796f, -1.9796f)
                lineToRelative(6.1509f, 6.1509f)
                lineToRelative(-1.9796f, 1.9796f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(30.0f, 31.0f)
                horizontalLineToRelative(-9.7f)
                curveToRelative(0.4f, 1.6f, 1.3f, 3.0f, 2.5f, 4.0f)
                horizontalLineTo(30.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.3f, 27.0f)
                horizontalLineTo(30.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-7.3f)
                curveToRelative(-1.2f, 1.0f, -2.0f, 2.4f, -2.4f, 4.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.1f, 20.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(7.3f)
                curveToRelative(0.5f, -0.7f, 1.1f, -1.4f, 1.8f, -2.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(17.1f, 24.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.4f)
                curveToRelative(0.2f, -0.7f, 0.4f, -1.4f, 0.7f, -2.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(16.0f, 29.0f)
                curveToRelative(0.0f, -0.3f, 0.0f, -0.7f, 0.1f, -1.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.1f)
                curveTo(16.0f, 29.7f, 16.0f, 29.3f, 16.0f, 29.0f)
                close()
            }
            path(
                fill = SolidColor(Color.White), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.4f, 32.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.1f)
                curveToRelative(-0.3f, -0.6f, -0.5f, -1.3f, -0.7f, -2.0f)
                close()
            }
        }.build()
        return _noItemsSearchLight!!
    }

@Suppress("ObjectPropertyName")
private var _noItemsSearchLight: ImageVector? = null

@Suppress("unused")
val Icons.Filled.NoItemsSearchDark: ImageVector
    get() {
        if (_noItemsSearchDark != null) return _noItemsSearchDark!!
        _noItemsSearchDark = ImageVector.Builder(
            name = "Filled.NoItemsSearchDark", defaultWidth = 120.0.dp, defaultHeight = 120.0.dp,
            viewportWidth = 48.0f, viewportHeight = 48.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(33.0f, 42.0f)
                lineToRelative(-28.0f, 0.0f)
                lineToRelative(0.0f, -38.0f)
                lineToRelative(19.0f, 0.0f)
                lineToRelative(9.0f, 9.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(31.5f, 14.0f)
                lineToRelative(-8.5f, 0.0f)
                lineToRelative(0.0f, -8.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(34.5047f, 37.5805f)
                lineToRelative(1.9796f, -1.9796f)
                lineToRelative(8.484f, 8.484f)
                lineToRelative(-1.9796f, 1.9796f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(28.0f, 29.0f)
                moveToRelative(-11.0f, 0.0f)
                arcToRelative(
                    11.0f, 11.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = 22.0f, dy1 = 0.0f
                )
                arcToRelative(
                    11.0f, 11.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = -22.0f, dy1 = 0.0f
                )
            }
            path(
                fill = SolidColor(Color(0xFF03DAC6)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(28.0f, 29.0f)
                moveToRelative(-9.0f, 0.0f)
                arcToRelative(
                    9.0f, 9.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = 18.0f, dy1 = 0.0f
                )
                arcToRelative(
                    9.0f, 9.0f, 0.0f, isMoreThanHalf = true, isPositiveArc = true,
                    dx1 = -18.0f, dy1 = 0.0f
                )
            }
            path(
                fill = SolidColor(Color(0xFF018786)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(36.8487f, 39.8797f)
                lineToRelative(1.9796f, -1.9796f)
                lineToRelative(6.1509f, 6.1509f)
                lineToRelative(-1.9796f, 1.9796f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(30.0f, 31.0f)
                horizontalLineToRelative(-9.7f)
                curveToRelative(0.4f, 1.6f, 1.3f, 3.0f, 2.5f, 4.0f)
                horizontalLineTo(30.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.3f, 27.0f)
                horizontalLineTo(30.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-7.3f)
                curveToRelative(-1.2f, 1.0f, -2.0f, 2.4f, -2.4f, 4.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.1f, 20.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(7.3f)
                curveToRelative(0.5f, -0.7f, 1.1f, -1.4f, 1.8f, -2.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(17.1f, 24.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.4f)
                curveToRelative(0.2f, -0.7f, 0.4f, -1.4f, 0.7f, -2.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(16.0f, 29.0f)
                curveToRelative(0.0f, -0.3f, 0.0f, -0.7f, 0.1f, -1.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.1f)
                curveTo(16.0f, 29.7f, 16.0f, 29.3f, 16.0f, 29.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF121212)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.4f, 32.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.1f)
                curveToRelative(-0.3f, -0.6f, -0.5f, -1.3f, -0.7f, -2.0f)
                close()
            }
        }.build()
        return _noItemsSearchDark!!
    }

@Suppress("ObjectPropertyName")
private var _noItemsSearchDark: ImageVector? = null