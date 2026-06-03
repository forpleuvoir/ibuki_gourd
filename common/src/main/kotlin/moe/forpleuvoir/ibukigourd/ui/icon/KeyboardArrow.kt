@file:Suppress("CheckReturnValue", "UnusedReceiverParameter")

package moe.forpleuvoir.ibukigourd.ui.icon

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.KeyboardArrowUp: ImageVector
    get() {
        if (keyboardArrowUp != null) {
            return keyboardArrowUp!!
        }
        keyboardArrowUp =
            ImageVector.Builder(
                name = "keyboard_arrow_up",
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
                        moveTo(12f, 10.8f)
                        lineTo(8.1f, 14.7f)
                        quadTo(7.83f, 14.98f, 7.4f, 14.98f)
                        reflectiveQuadTo(6.7f, 14.7f)
                        reflectiveQuadTo(6.43f, 14f)
                        reflectiveQuadTo(6.7f, 13.3f)
                        lineTo(11.3f, 8.7f)
                        quadTo(11.6f, 8.4f, 12f, 8.4f)
                        reflectiveQuadToRelative(0.7f, 0.3f)
                        lineToRelative(4.6f, 4.6f)
                        quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
                        reflectiveQuadTo(17.3f, 14.7f)
                        reflectiveQuadToRelative(-0.7f, 0.28f)
                        reflectiveQuadTo(15.9f, 14.7f)
                        lineTo(12f, 10.8f)
                        close()
                    }
                }
                .build()
        return keyboardArrowUp!!
    }

private var keyboardArrowUp: ImageVector? = null

val Icons.KeyboardArrowDown: ImageVector
    get() {
        if (keyboardArrowDown != null) {
            return keyboardArrowDown!!
        }
        keyboardArrowDown =
            ImageVector.Builder(
                name = "keyboard_arrow_down",
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
                        moveTo(11.63f, 14.91f)
                        quadTo(11.45f, 14.85f, 11.3f, 14.7f)
                        lineTo(6.7f, 10.1f)
                        quadTo(6.43f, 9.82f, 6.43f, 9.4f)
                        quadTo(6.43f, 8.98f, 6.7f, 8.7f)
                        reflectiveQuadTo(7.4f, 8.42f)
                        reflectiveQuadTo(8.1f, 8.7f)
                        lineTo(12f, 12.6f)
                        lineTo(15.9f, 8.7f)
                        quadTo(16.18f, 8.42f, 16.6f, 8.42f)
                        reflectiveQuadTo(17.3f, 8.7f)
                        reflectiveQuadToRelative(0.27f, 0.7f)
                        reflectiveQuadTo(17.3f, 10.1f)
                        lineToRelative(-4.6f, 4.6f)
                        quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
                        reflectiveQuadTo(12f, 14.98f)
                        reflectiveQuadTo(11.63f, 14.91f)
                        close()
                    }
                }
                .build()
        return keyboardArrowDown!!
    }

private var keyboardArrowDown: ImageVector? = null

val Icons.KeyboardArrowRight: ImageVector
    get() {
        if (keyboardArrowRight != null) {
            return keyboardArrowRight!!
        }
        keyboardArrowRight =
            ImageVector.Builder(
                name = "keyboard_arrow_right",
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
                        moveTo(12.6f, 12f)
                        lineTo(8.7f, 8.1f)
                        quadTo(8.43f, 7.82f, 8.43f, 7.4f)
                        reflectiveQuadTo(8.7f, 6.7f)
                        reflectiveQuadTo(9.4f, 6.43f)
                        reflectiveQuadTo(10.1f, 6.7f)
                        lineToRelative(4.6f, 4.6f)
                        quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
                        reflectiveQuadTo(14.98f, 12f)
                        reflectiveQuadToRelative(-0.06f, 0.38f)
                        reflectiveQuadTo(14.7f, 12.7f)
                        lineToRelative(-4.6f, 4.6f)
                        quadTo(9.83f, 17.58f, 9.4f, 17.58f)
                        reflectiveQuadTo(8.7f, 17.3f)
                        quadTo(8.43f, 17.02f, 8.43f, 16.6f)
                        reflectiveQuadTo(8.7f, 15.9f)
                        lineTo(12.6f, 12f)
                        close()
                    }
                }
                .build()
        return keyboardArrowRight!!
    }

private var keyboardArrowRight: ImageVector? = null

val Icons.KeyboardArrowLeft: ImageVector
    get() {
        if (keyboardArrowLeft != null) {
            return keyboardArrowLeft!!
        }
        keyboardArrowLeft =
            ImageVector.Builder(
                name = "keyboard_arrow_left",
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
                        moveTo(10.8f, 12f)
                        lineToRelative(3.9f, 3.9f)
                        quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
                        quadToRelative(0f, 0.42f, -0.28f, 0.7f)
                        reflectiveQuadTo(14f, 17.58f)
                        reflectiveQuadTo(13.3f, 17.3f)
                        lineTo(8.7f, 12.7f)
                        quadTo(8.55f, 12.55f, 8.49f, 12.38f)
                        reflectiveQuadTo(8.43f, 12f)
                        reflectiveQuadTo(8.49f, 11.63f)
                        reflectiveQuadTo(8.7f, 11.3f)
                        lineTo(13.3f, 6.7f)
                        quadTo(13.58f, 6.43f, 14f, 6.43f)
                        reflectiveQuadTo(14.7f, 6.7f)
                        reflectiveQuadToRelative(0.28f, 0.7f)
                        reflectiveQuadTo(14.7f, 8.1f)
                        lineTo(10.8f, 12f)
                        close()
                    }
                }
                .build()
        return keyboardArrowLeft!!
    }

private var keyboardArrowLeft: ImageVector? = null