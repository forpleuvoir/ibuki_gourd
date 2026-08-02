package moe.forpleuvoir.ibukigourd.ui.icon.defaults

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons

@Suppress("CheckReturnValue")
 val Icons.Compress: ImageVector
    get() {
        if (compress != null) {
            return compress!!
        }
        compress =
            ImageVector.Builder(
                name = "compress",
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
                        moveTo(5f, 14f)
                        quadTo(4.58f, 14f, 4.29f, 13.71f)
                        quadTo(4f, 13.43f, 4f, 13f)
                        reflectiveQuadTo(4.29f, 12.29f)
                        reflectiveQuadTo(5f, 12f)
                        horizontalLineTo(19f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 13f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(19f, 14f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 11f)
                        quadTo(4.58f, 11f, 4.29f, 10.71f)
                        quadTo(4f, 10.43f, 4f, 10f)
                        quadTo(4f, 9.57f, 4.29f, 9.29f)
                        reflectiveQuadTo(5f, 9f)
                        horizontalLineTo(19f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 10f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(19f, 11f)
                        horizontalLineTo(5f)
                        close()
                        moveToRelative(6.29f, 10.71f)
                        quadTo(11f, 21.43f, 11f, 21f)
                        verticalLineTo(18.8f)
                        lineToRelative(-0.9f, 0.9f)
                        quadTo(9.83f, 19.98f, 9.4f, 19.98f)
                        reflectiveQuadTo(8.7f, 19.7f)
                        quadTo(8.43f, 19.43f, 8.43f, 19f)
                        reflectiveQuadTo(8.7f, 18.3f)
                        lineToRelative(2.6f, -2.6f)
                        quadToRelative(0.15f, -0.15f, 0.32f, -0.21f)
                        reflectiveQuadTo(12f, 15.43f)
                        reflectiveQuadToRelative(0.38f, 0.06f)
                        reflectiveQuadTo(12.7f, 15.7f)
                        lineToRelative(2.6f, 2.6f)
                        quadToRelative(0.28f, 0.27f, 0.29f, 0.69f)
                        reflectiveQuadTo(15.3f, 19.7f)
                        quadToRelative(-0.28f, 0.28f, -0.69f, 0.29f)
                        reflectiveQuadTo(13.9f, 19.73f)
                        lineTo(13f, 18.85f)
                        verticalLineTo(21f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(12f, 22f)
                        reflectiveQuadTo(11.29f, 21.71f)
                        close()
                        moveTo(11.63f, 7.51f)
                        quadTo(11.45f, 7.45f, 11.3f, 7.3f)
                        lineTo(8.7f, 4.7f)
                        quadTo(8.43f, 4.42f, 8.43f, 4f)
                        quadTo(8.43f, 3.57f, 8.7f, 3.3f)
                        reflectiveQuadTo(9.4f, 3.02f)
                        reflectiveQuadTo(10.1f, 3.3f)
                        lineTo(11f, 4.2f)
                        verticalLineTo(2f)
                        quadTo(11f, 1.57f, 11.29f, 1.29f)
                        reflectiveQuadTo(12f, 1f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 2f)
                        verticalLineTo(4.2f)
                        lineTo(13.9f, 3.3f)
                        quadTo(14.18f, 3.02f, 14.6f, 3.02f)
                        reflectiveQuadTo(15.3f, 3.3f)
                        reflectiveQuadTo(15.58f, 4f)
                        quadToRelative(0f, 0.42f, -0.28f, 0.7f)
                        lineTo(12.7f, 7.3f)
                        quadTo(12.55f, 7.45f, 12.38f, 7.51f)
                        reflectiveQuadTo(12f, 7.57f)
                        reflectiveQuadTo(11.63f, 7.51f)
                        close()
                    }
                }
                .build()
        return compress!!
    }

private var compress: ImageVector? = null