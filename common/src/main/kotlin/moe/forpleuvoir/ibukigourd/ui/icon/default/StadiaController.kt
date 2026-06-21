package moe.forpleuvoir.ibukigourd.ui.icon.default

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
val Icons.StadiaController: ImageVector
    get() {
        if (stadiaController != null) {
            return stadiaController!!
        }
        stadiaController =
            ImageVector.Builder(
                name = "stadia_controller",
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
                        moveTo(4.73f, 20f)
                        quadTo(3.23f, 20f, 2.16f, 18.93f)
                        reflectiveQuadTo(1.05f, 16.33f)
                        quadToRelative(0f, -0.22f, 0.02f, -0.45f)
                        reflectiveQuadTo(1.15f, 15.43f)
                        lineToRelative(2.1f, -8.4f)
                        quadTo(3.6f, 5.68f, 4.68f, 4.84f)
                        reflectiveQuadTo(7.13f, 4f)
                        horizontalLineToRelative(9.75f)
                        quadToRelative(1.38f, 0f, 2.45f, 0.84f)
                        quadToRelative(1.07f, 0.84f, 1.42f, 2.19f)
                        lineToRelative(2.1f, 8.4f)
                        quadToRelative(0.05f, 0.23f, 0.09f, 0.46f)
                        reflectiveQuadToRelative(0.04f, 0.46f)
                        quadToRelative(0f, 1.52f, -1.09f, 2.59f)
                        reflectiveQuadTo(19.28f, 20f)
                        quadToRelative(-1.05f, 0f, -1.95f, -0.55f)
                        quadToRelative(-0.9f, -0.55f, -1.35f, -1.5f)
                        lineTo(15.28f, 16.5f)
                        quadTo(15.15f, 16.25f, 14.9f, 16.13f)
                        reflectiveQuadTo(14.38f, 16f)
                        horizontalLineTo(9.63f)
                        quadTo(9.35f, 16f, 9.1f, 16.13f)
                        reflectiveQuadTo(8.73f, 16.5f)
                        lineToRelative(-0.7f, 1.45f)
                        quadTo(7.58f, 18.9f, 6.68f, 19.45f)
                        reflectiveQuadTo(4.73f, 20f)
                        close()
                        moveTo(4.8f, 18f)
                        quadToRelative(0.47f, 0f, 0.86f, -0.25f)
                        reflectiveQuadTo(6.25f, 17.08f)
                        lineToRelative(0.7f, -1.42f)
                        quadToRelative(0.38f, -0.78f, 1.1f, -1.21f)
                        reflectiveQuadTo(9.63f, 14f)
                        horizontalLineToRelative(4.75f)
                        quadToRelative(0.85f, 0f, 1.58f, 0.45f)
                        reflectiveQuadToRelative(1.12f, 1.2f)
                        lineToRelative(0.7f, 1.42f)
                        quadToRelative(0.2f, 0.43f, 0.59f, 0.68f)
                        reflectiveQuadTo(19.23f, 18f)
                        quadToRelative(0.7f, 0f, 1.2f, -0.46f)
                        reflectiveQuadToRelative(0.53f, -1.16f)
                        quadToRelative(0f, 0.02f, -0.05f, -0.47f)
                        lineTo(18.8f, 7.52f)
                        quadTo(18.63f, 6.85f, 18.1f, 6.43f)
                        reflectiveQuadTo(16.88f, 6f)
                        horizontalLineTo(7.13f)
                        quadTo(6.43f, 6f, 5.89f, 6.43f)
                        reflectiveQuadTo(5.2f, 7.52f)
                        lineTo(3.1f, 15.9f)
                        quadTo(3.05f, 16.05f, 3.05f, 16.35f)
                        quadToRelative(0f, 0.7f, 0.51f, 1.17f)
                        quadTo(4.08f, 18f, 4.8f, 18f)
                        close()
                        moveToRelative(9.41f, -7.29f)
                        quadTo(14.5f, 10.43f, 14.5f, 10f)
                        quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                        reflectiveQuadTo(13.5f, 9f)
                        reflectiveQuadTo(12.79f, 9.29f)
                        reflectiveQuadTo(12.5f, 10f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(13.5f, 11f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        close()
                        moveToRelative(2f, -2f)
                        quadTo(16.5f, 8.42f, 16.5f, 8f)
                        quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                        reflectiveQuadTo(15.5f, 7f)
                        reflectiveQuadTo(14.79f, 7.29f)
                        reflectiveQuadTo(14.5f, 8f)
                        quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                        reflectiveQuadTo(15.5f, 9f)
                        reflectiveQuadTo(16.21f, 8.71f)
                        close()
                        moveToRelative(0f, 4f)
                        quadTo(16.5f, 12.43f, 16.5f, 12f)
                        reflectiveQuadTo(16.21f, 11.29f)
                        reflectiveQuadTo(15.5f, 11f)
                        reflectiveQuadToRelative(-0.71f, 0.29f)
                        reflectiveQuadTo(14.5f, 12f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(15.5f, 13f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        close()
                        moveToRelative(2f, -2f)
                        quadTo(18.5f, 10.43f, 18.5f, 10f)
                        quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                        reflectiveQuadTo(17.5f, 9f)
                        reflectiveQuadTo(16.79f, 9.29f)
                        reflectiveQuadTo(16.5f, 10f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(17.5f, 11f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        close()
                        moveTo(9.04f, 12.29f)
                        quadTo(9.25f, 12.08f, 9.25f, 11.75f)
                        verticalLineToRelative(-1f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.33f, 0f, 0.54f, -0.21f)
                        reflectiveQuadTo(11f, 10f)
                        quadTo(11f, 9.67f, 10.79f, 9.46f)
                        reflectiveQuadTo(10.25f, 9.25f)
                        horizontalLineToRelative(-1f)
                        verticalLineToRelative(-1f)
                        quadTo(9.25f, 7.93f, 9.04f, 7.71f)
                        quadTo(8.83f, 7.5f, 8.5f, 7.5f)
                        quadTo(8.18f, 7.5f, 7.96f, 7.71f)
                        quadTo(7.75f, 7.93f, 7.75f, 8.25f)
                        verticalLineToRelative(1f)
                        horizontalLineToRelative(-1f)
                        quadToRelative(-0.32f, 0f, -0.54f, 0.21f)
                        reflectiveQuadTo(6f, 10f)
                        reflectiveQuadToRelative(0.21f, 0.54f)
                        reflectiveQuadToRelative(0.54f, 0.21f)
                        horizontalLineToRelative(1f)
                        verticalLineToRelative(1f)
                        quadToRelative(0f, 0.32f, 0.21f, 0.54f)
                        reflectiveQuadTo(8.5f, 12.5f)
                        quadToRelative(0.33f, 0f, 0.54f, -0.21f)
                        close()
                        moveTo(12f, 12f)
                        close()
                    }
                }
                .build()
        return stadiaController!!
    }

private var stadiaController: ImageVector? = null