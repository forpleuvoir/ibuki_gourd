package moe.forpleuvoir.ibukigourd.ui.icon.defaults

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons

@Suppress("CheckReturnValue")
val Icons.Expand: ImageVector
    get() {
        if (expand != null) {
            return expand!!
        }
        expand =
            ImageVector.Builder(
                name = "expand",
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
                        moveTo(5f, 22f)
                        quadTo(4.58f, 22f, 4.29f, 21.71f)
                        quadTo(4f, 21.43f, 4f, 21f)
                        reflectiveQuadTo(4.29f, 20.29f)
                        reflectiveQuadTo(5f, 20f)
                        horizontalLineTo(19f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 21f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(19f, 22f)
                        horizontalLineTo(5f)
                        close()
                        moveToRelative(6.63f, -3.49f)
                        quadTo(11.45f, 18.45f, 11.3f, 18.3f)
                        lineTo(8.7f, 15.7f)
                        quadTo(8.43f, 15.43f, 8.41f, 15.01f)
                        reflectiveQuadTo(8.7f, 14.3f)
                        quadTo(8.98f, 14.02f, 9.39f, 14.01f)
                        reflectiveQuadToRelative(0.71f, 0.26f)
                        lineTo(11f, 15.15f)
                        verticalLineTo(8.85f)
                        lineTo(10.1f, 9.73f)
                        quadTo(9.83f, 10f, 9.41f, 10f)
                        reflectiveQuadTo(8.7f, 9.7f)
                        quadTo(8.43f, 9.42f, 8.43f, 9f)
                        quadTo(8.43f, 8.57f, 8.7f, 8.3f)
                        lineTo(11.3f, 5.7f)
                        quadTo(11.45f, 5.55f, 11.63f, 5.49f)
                        reflectiveQuadTo(12f, 5.43f)
                        reflectiveQuadToRelative(0.38f, 0.06f)
                        reflectiveQuadTo(12.7f, 5.7f)
                        lineToRelative(2.6f, 2.6f)
                        quadToRelative(0.28f, 0.28f, 0.29f, 0.69f)
                        reflectiveQuadTo(15.3f, 9.7f)
                        quadTo(15.03f, 9.98f, 14.61f, 9.99f)
                        reflectiveQuadTo(13.9f, 9.73f)
                        lineTo(13f, 8.85f)
                        verticalLineToRelative(6.3f)
                        lineToRelative(0.9f, -0.88f)
                        quadTo(14.18f, 14f, 14.59f, 14f)
                        reflectiveQuadToRelative(0.71f, 0.3f)
                        quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
                        reflectiveQuadTo(15.3f, 15.7f)
                        lineToRelative(-2.6f, 2.6f)
                        quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
                        reflectiveQuadTo(12f, 18.58f)
                        reflectiveQuadTo(11.63f, 18.51f)
                        close()
                        moveTo(5f, 4f)
                        quadTo(4.58f, 4f, 4.29f, 3.71f)
                        reflectiveQuadTo(4f, 3f)
                        quadTo(4f, 2.57f, 4.29f, 2.29f)
                        reflectiveQuadTo(5f, 2f)
                        horizontalLineTo(19f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 3f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(19f, 4f)
                        horizontalLineTo(5f)
                        close()
                    }
                }
                .build()
        return expand!!
    }

private var expand: ImageVector? = null