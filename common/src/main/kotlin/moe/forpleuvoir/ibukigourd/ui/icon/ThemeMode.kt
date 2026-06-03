@file:Suppress("CheckReturnValue", "UnusedReceiverParameter")

package moe.forpleuvoir.ibukigourd.ui.icon

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.DarkMode: ImageVector
    get() {
        if (darkMode != null) {
            return darkMode!!
        }
        darkMode =
            ImageVector.Builder(
                name = "dark_mode",
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
                        moveTo(12f, 21f)
                        quadTo(8.23f, 21f, 5.61f, 18.39f)
                        quadTo(3f, 15.78f, 3f, 12f)
                        quadTo(3f, 8.55f, 5.25f, 6.01f)
                        reflectiveQuadTo(11f, 3.05f)
                        quadTo(11.33f, 3f, 11.58f, 3.14f)
                        reflectiveQuadToRelative(0.4f, 0.36f)
                        quadToRelative(0.15f, 0.22f, 0.16f, 0.52f)
                        reflectiveQuadTo(11.95f, 4.6f)
                        quadTo(11.53f, 5.25f, 11.31f, 5.97f)
                        reflectiveQuadTo(11.1f, 7.5f)
                        quadToRelative(0f, 2.25f, 1.57f, 3.82f)
                        reflectiveQuadTo(16.5f, 12.9f)
                        quadToRelative(0.78f, 0f, 1.54f, -0.22f)
                        reflectiveQuadTo(19.4f, 12.05f)
                        quadToRelative(0.27f, -0.17f, 0.56f, -0.16f)
                        reflectiveQuadToRelative(0.51f, 0.14f)
                        quadToRelative(0.25f, 0.13f, 0.39f, 0.38f)
                        reflectiveQuadTo(20.95f, 13f)
                        quadToRelative(-0.35f, 3.45f, -2.94f, 5.73f)
                        quadTo(15.43f, 21f, 12f, 21f)
                        close()
                        moveToRelative(0f, -2f)
                        quadToRelative(2.2f, 0f, 3.95f, -1.21f)
                        reflectiveQuadTo(18.5f, 14.63f)
                        quadToRelative(-0.5f, 0.13f, -1f, 0.2f)
                        reflectiveQuadToRelative(-1f, 0.08f)
                        quadToRelative(-3.07f, 0f, -5.24f, -2.16f)
                        quadTo(9.1f, 10.58f, 9.1f, 7.5f)
                        quadTo(9.1f, 7f, 9.18f, 6.5f)
                        reflectiveQuadToRelative(0.2f, -1f)
                        quadTo(7.43f, 6.3f, 6.21f, 8.05f)
                        reflectiveQuadTo(5f, 12f)
                        quadToRelative(0f, 2.9f, 2.05f, 4.95f)
                        reflectiveQuadTo(12f, 19f)
                        close()
                        moveTo(11.75f, 12.25f)
                        close()
                    }
                }
                .build()
        return darkMode!!
    }

private var darkMode: ImageVector? = null

val Icons.LightMode: ImageVector
    get() {
        if (lightMode != null) {
            return lightMode!!
        }
        lightMode =
            ImageVector.Builder(
                name = "light_mode",
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
                        moveTo(14.13f, 14.13f)
                        quadTo(15f, 13.25f, 15f, 12f)
                        reflectiveQuadTo(14.13f, 9.88f)
                        reflectiveQuadTo(12f, 9f)
                        reflectiveQuadTo(9.88f, 9.88f)
                        reflectiveQuadTo(9f, 12f)
                        reflectiveQuadToRelative(0.88f, 2.13f)
                        reflectiveQuadTo(12f, 15f)
                        reflectiveQuadToRelative(2.13f, -0.88f)
                        close()
                        moveTo(8.46f, 15.54f)
                        quadTo(7f, 14.08f, 7f, 12f)
                        quadTo(7f, 9.92f, 8.46f, 8.46f)
                        reflectiveQuadTo(12f, 7f)
                        reflectiveQuadToRelative(3.54f, 1.46f)
                        reflectiveQuadTo(17f, 12f)
                        reflectiveQuadToRelative(-1.46f, 3.54f)
                        reflectiveQuadTo(12f, 17f)
                        quadTo(9.93f, 17f, 8.46f, 15.54f)
                        close()
                        moveTo(2f, 13f)
                        quadTo(1.58f, 13f, 1.29f, 12.71f)
                        quadTo(1f, 12.43f, 1f, 12f)
                        reflectiveQuadTo(1.29f, 11.29f)
                        reflectiveQuadTo(2f, 11f)
                        horizontalLineTo(4f)
                        quadToRelative(0.42f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(5f, 12f)
                        reflectiveQuadTo(4.71f, 12.71f)
                        reflectiveQuadTo(4f, 13f)
                        horizontalLineTo(2f)
                        close()
                        moveToRelative(18f, 0f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(19f, 12.43f, 19f, 12f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(20f, 11f)
                        horizontalLineToRelative(2f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(23f, 12f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(22f, 13f)
                        horizontalLineTo(20f)
                        close()
                        moveTo(11.29f, 4.71f)
                        quadTo(11f, 4.42f, 11f, 4f)
                        verticalLineTo(2f)
                        quadTo(11f, 1.57f, 11.29f, 1.29f)
                        reflectiveQuadTo(12f, 1f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 2f)
                        verticalLineTo(4f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(12f, 5f)
                        reflectiveQuadTo(11.29f, 4.71f)
                        close()
                        moveToRelative(0f, 18f)
                        quadTo(11f, 22.43f, 11f, 22f)
                        verticalLineTo(20f)
                        quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                        reflectiveQuadTo(12f, 19f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 20f)
                        verticalLineToRelative(2f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(12f, 23f)
                        reflectiveQuadTo(11.29f, 22.71f)
                        close()
                        moveTo(5.65f, 7.05f)
                        lineTo(4.58f, 6f)
                        quadTo(4.28f, 5.72f, 4.29f, 5.3f)
                        reflectiveQuadTo(4.58f, 4.57f)
                        quadTo(4.88f, 4.27f, 5.3f, 4.27f)
                        reflectiveQuadTo(6f, 4.57f)
                        lineTo(7.05f, 5.65f)
                        quadToRelative(0.27f, 0.3f, 0.27f, 0.7f)
                        reflectiveQuadTo(7.05f, 7.05f)
                        reflectiveQuadTo(6.36f, 7.34f)
                        reflectiveQuadTo(5.65f, 7.05f)
                        close()
                        moveTo(18f, 19.43f)
                        lineTo(16.95f, 18.35f)
                        quadToRelative(-0.27f, -0.3f, -0.27f, -0.71f)
                        quadToRelative(0f, -0.41f, 0.27f, -0.69f)
                        quadToRelative(0.28f, -0.3f, 0.69f, -0.29f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        lineTo(19.43f, 18f)
                        quadToRelative(0.3f, 0.27f, 0.29f, 0.7f)
                        reflectiveQuadToRelative(-0.29f, 0.73f)
                        quadToRelative(-0.3f, 0.3f, -0.73f, 0.3f)
                        quadToRelative(-0.42f, 0f, -0.7f, -0.3f)
                        close()
                        moveTo(16.95f, 7.05f)
                        quadTo(16.65f, 6.77f, 16.66f, 6.36f)
                        reflectiveQuadTo(16.95f, 5.65f)
                        lineTo(18f, 4.57f)
                        quadToRelative(0.28f, -0.3f, 0.7f, -0.29f)
                        quadToRelative(0.43f, 0.01f, 0.73f, 0.29f)
                        quadToRelative(0.3f, 0.3f, 0.3f, 0.73f)
                        quadToRelative(0f, 0.42f, -0.3f, 0.7f)
                        lineTo(18.35f, 7.05f)
                        quadToRelative(-0.3f, 0.27f, -0.7f, 0.27f)
                        reflectiveQuadTo(16.95f, 7.05f)
                        close()
                        moveTo(4.58f, 19.43f)
                        quadTo(4.28f, 19.13f, 4.28f, 18.7f)
                        reflectiveQuadTo(4.58f, 18f)
                        lineTo(5.65f, 16.95f)
                        quadToRelative(0.3f, -0.27f, 0.71f, -0.27f)
                        reflectiveQuadToRelative(0.69f, 0.27f)
                        quadToRelative(0.3f, 0.28f, 0.29f, 0.69f)
                        reflectiveQuadTo(7.05f, 18.35f)
                        lineTo(6f, 19.43f)
                        quadToRelative(-0.27f, 0.3f, -0.7f, 0.29f)
                        reflectiveQuadTo(4.58f, 19.43f)
                        close()
                        moveTo(12f, 12f)
                        close()
                    }
                }
                .build()
        return lightMode!!
    }

private var lightMode: ImageVector? = null
