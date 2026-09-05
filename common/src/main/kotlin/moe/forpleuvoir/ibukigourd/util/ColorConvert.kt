package moe.forpleuvoir.ibukigourd.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

/**
 * Compose 颜色与 nebula 颜色的互转扩展。
 *
 * 两者均按 **0xAARRGGBB int** 中转，分量含义一致（都是 sRGB 直存），转换无损、无色彩空间换算。
 * 注意区分包名——本文件内 nebula 的 [NebulaColor] 以 import 别名引入，避免与 Compose [Color] 同名冲突。
 */

/**
 * [Color]（Compose，androidx.compose.ui.graphics）→ [NebulaColor]（nebula）。
 */
fun Color.toNebulaColor(): NebulaColor = NebulaColor.fromARGB(toArgb())

/**
 * [NebulaColor]（nebula）→ [Color]（Compose，androidx.compose.ui.graphics）。
 */
fun NebulaColor.toComposeColor(): Color = Color(argb)
