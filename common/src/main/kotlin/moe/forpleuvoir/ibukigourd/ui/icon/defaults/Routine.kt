package moe.forpleuvoir.ibukigourd.ui.icon.defaults

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons

@Suppress("CheckReturnValue")
 val Icons.Routine: ImageVector
    get() {
        if (routine != null) {
            return routine!!
        }
        routine =
            ImageVector.Builder(
                name = "routine",
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
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(8.44f, 12.43f)
                        quadTo(7.78f, 11.55f, 7.23f, 10.58f)
                        quadTo(7.1f, 10.93f, 7.06f, 11.29f)
                        reflectiveQuadTo(7.03f, 12f)
                        quadToRelative(0f, 2.07f, 1.45f, 3.53f)
                        quadTo(9.93f, 16.98f, 12f, 16.98f)
                        quadToRelative(0.35f, 0f, 0.71f, -0.05f)
                        reflectiveQuadToRelative(0.71f, -0.15f)
                        quadTo(12.45f, 16.23f, 11.58f, 15.56f)
                        reflectiveQuadTo(9.9f, 14.1f)
                        reflectiveQuadTo(8.44f, 12.43f)
                        close()
                        moveTo(12.03f, 19f)
                        quadToRelative(-1.4f, 0f, -2.68f, -0.52f)
                        quadTo(8.08f, 17.95f, 7.08f, 16.95f)
                        quadToRelative(-1f, -1f, -1.52f, -2.28f)
                        reflectiveQuadTo(5.03f, 12f)
                        quadToRelative(0f, -1.28f, 0.42f, -2.44f)
                        reflectiveQuadTo(6.7f, 7.45f)
                        quadTo(7.03f, 7.1f, 7.5f, 7.21f)
                        reflectiveQuadTo(8.18f, 7.82f)
                        quadTo(8.7f, 9.2f, 9.49f, 10.43f)
                        reflectiveQuadToRelative(1.84f, 2.28f)
                        reflectiveQuadToRelative(2.28f, 1.84f)
                        reflectiveQuadToRelative(2.6f, 1.31f)
                        quadToRelative(0.5f, 0.2f, 0.61f, 0.67f)
                        reflectiveQuadToRelative(-0.24f, 0.8f)
                        quadToRelative(-0.95f, 0.82f, -2.11f, 1.25f)
                        reflectiveQuadTo(12.03f, 19f)
                        close()
                        moveTo(17.6f, 14.2f)
                        quadTo(17.2f, 14.08f, 17.03f, 13.69f)
                        reflectiveQuadToRelative(-0.1f, -0.81f)
                        quadToRelative(0.23f, -1.2f, -0.15f, -2.36f)
                        reflectiveQuadTo(15.53f, 8.48f)
                        reflectiveQuadTo(13.51f, 7.24f)
                        reflectiveQuadTo(11.2f, 7.07f)
                        quadTo(10.78f, 7.15f, 10.4f, 6.97f)
                        reflectiveQuadTo(9.88f, 6.4f)
                        reflectiveQuadTo(9.91f, 5.63f)
                        reflectiveQuadTo(10.5f, 5.15f)
                        quadToRelative(1.73f, -0.38f, 3.45f, 0.11f)
                        reflectiveQuadToRelative(3.03f, 1.79f)
                        quadToRelative(1.27f, 1.27f, 1.77f, 3f)
                        reflectiveQuadToRelative(0.13f, 3.45f)
                        quadToRelative(-0.1f, 0.42f, -0.47f, 0.63f)
                        reflectiveQuadTo(17.6f, 14.2f)
                        close()
                        moveTo(12f, 3f)
                        quadTo(11.58f, 3f, 11.29f, 2.71f)
                        reflectiveQuadTo(11f, 2f)
                        verticalLineTo(1f)
                        quadTo(11f, 0.57f, 11.29f, 0.29f)
                        reflectiveQuadTo(12f, 0f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 1f)
                        verticalLineTo(2f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(12f, 3f)
                        close()
                        moveToRelative(0f, 21f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(11f, 23.43f, 11f, 23f)
                        verticalLineTo(22f)
                        quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                        reflectiveQuadTo(12f, 21f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 22f)
                        verticalLineToRelative(1f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(12f, 24f)
                        close()
                        moveTo(18.38f, 5.65f)
                        quadToRelative(-0.3f, -0.3f, -0.3f, -0.71f)
                        quadToRelative(0f, -0.41f, 0.3f, -0.71f)
                        lineToRelative(0.7f, -0.7f)
                        quadTo(19.35f, 3.25f, 19.76f, 3.25f)
                        reflectiveQuadToRelative(0.71f, 0.27f)
                        quadToRelative(0.3f, 0.3f, 0.3f, 0.71f)
                        reflectiveQuadToRelative(-0.3f, 0.71f)
                        lineToRelative(-0.7f, 0.7f)
                        quadToRelative(-0.3f, 0.3f, -0.7f, 0.3f)
                        quadToRelative(-0.4f, 0f, -0.7f, -0.3f)
                        close()
                        moveTo(3.53f, 20.48f)
                        quadToRelative(-0.3f, -0.3f, -0.3f, -0.71f)
                        reflectiveQuadToRelative(0.3f, -0.71f)
                        lineToRelative(0.7f, -0.7f)
                        quadToRelative(0.3f, -0.3f, 0.7f, -0.3f)
                        reflectiveQuadToRelative(0.7f, 0.3f)
                        reflectiveQuadToRelative(0.3f, 0.71f)
                        reflectiveQuadToRelative(-0.3f, 0.71f)
                        lineToRelative(-0.7f, 0.7f)
                        quadTo(4.65f, 20.75f, 4.24f, 20.75f)
                        quadToRelative(-0.41f, 0f, -0.71f, -0.27f)
                        close()
                        moveTo(22f, 13f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(21f, 12.43f, 21f, 12f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(22f, 11f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(24f, 12f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(23f, 13f)
                        horizontalLineTo(22f)
                        close()
                        moveTo(1f, 13f)
                        quadTo(0.58f, 13f, 0.29f, 12.71f)
                        quadTo(0f, 12.43f, 0f, 12f)
                        reflectiveQuadTo(0.29f, 11.29f)
                        reflectiveQuadTo(1f, 11f)
                        horizontalLineTo(2f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(3f, 12f)
                        reflectiveQuadTo(2.71f, 12.71f)
                        reflectiveQuadTo(2f, 13f)
                        horizontalLineTo(1f)
                        close()
                        moveToRelative(19.48f, 7.48f)
                        quadToRelative(-0.3f, 0.3f, -0.71f, 0.3f)
                        reflectiveQuadToRelative(-0.71f, -0.3f)
                        lineToRelative(-0.7f, -0.7f)
                        quadToRelative(-0.3f, -0.3f, -0.3f, -0.7f)
                        quadToRelative(0f, -0.4f, 0.3f, -0.7f)
                        reflectiveQuadToRelative(0.71f, -0.3f)
                        quadToRelative(0.41f, 0f, 0.71f, 0.3f)
                        lineToRelative(0.7f, 0.7f)
                        quadToRelative(0.27f, 0.28f, 0.27f, 0.69f)
                        quadToRelative(0f, 0.41f, -0.27f, 0.71f)
                        close()
                        moveTo(5.65f, 5.63f)
                        quadToRelative(-0.3f, 0.3f, -0.71f, 0.3f)
                        quadToRelative(-0.41f, 0f, -0.71f, -0.3f)
                        lineTo(3.53f, 4.93f)
                        quadTo(3.25f, 4.65f, 3.25f, 4.24f)
                        reflectiveQuadTo(3.53f, 3.52f)
                        quadToRelative(0.3f, -0.3f, 0.71f, -0.3f)
                        reflectiveQuadToRelative(0.71f, 0.3f)
                        lineToRelative(0.7f, 0.7f)
                        quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
                        quadToRelative(0f, 0.4f, -0.3f, 0.7f)
                        close()
                        moveTo(9.9f, 14.1f)
                        close()
                    }
                }
                .build()
        return routine!!
    }

private var routine: ImageVector? = null