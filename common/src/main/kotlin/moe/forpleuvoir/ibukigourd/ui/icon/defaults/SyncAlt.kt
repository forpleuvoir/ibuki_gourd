package moe.forpleuvoir.ibukigourd.ui.icon.defaults

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons

@Suppress("CheckReturnValue")
val Icons.SyncAlt: ImageVector
    get() {
        if (syncAlt != null) {
            return syncAlt!!
        }
        syncAlt =
            ImageVector.Builder(
                name = "sync_alt",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(7f, 21f)
                        lineTo(2f, 16f)
                        lineTo(7f, 11f)
                        lineToRelative(1.43f, 1.4f)
                        lineTo(5.83f, 15f)
                        horizontalLineTo(21f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(5.83f)
                        lineToRelative(2.6f, 2.6f)
                        lineTo(7f, 21f)
                        close()
                        moveTo(17f, 13f)
                        lineTo(15.58f, 11.6f)
                        lineTo(18.18f, 9f)
                        horizontalLineTo(3f)
                        verticalLineTo(7f)
                        horizontalLineTo(18.18f)
                        lineTo(15.58f, 4.4f)
                        lineTo(17f, 3f)
                        lineToRelative(5f, 5f)
                        lineToRelative(-5f, 5f)
                        close()
                    }
                }
                .build()
        return syncAlt!!
    }

private var syncAlt: ImageVector? = null
