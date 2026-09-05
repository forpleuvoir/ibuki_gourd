package moe.forpleuvoir.ibukigourd.util

import androidx.compose.ui.graphics.Color

/**
 * 计算接收者的**鲜艳互补色**：色相旋转 180°、满饱和、0.95 明度。
 *
 * "很明显" = 醒目的互补色相 + 高饱和 + 高明度，恒定输出鲜艳色——
 * **不做明度对比决策**（若按"与底色亮度差最大"选色，底色偏亮时会输出近黑的暗色，
 * 看起来就像计算错误）。
 *
 * 典型用途：选中/焦点描边、强调框等需要从底色中醒目突出的颜色。
 *
 * 示例：
 * ```
 * val outline = theme.colors.primary.base.contrasting()
 * ```
 */
fun Color.contrasting(): Color {
    val (h, _, _) = toHsv()
    return hsvToColor((h + 0.5f) % 1f, 1f, 0.95f)
}

/**
 * 保留色相，重设饱和度与明度（"色相继承"派生色）。
 *
 * 用于从主色派生同色系衍生色：低饱和表面色（M3 surfaceVariant 思路）、
 * 高饱和强调色等——色相与主色一致，仅调整彩度/明度。
 */
fun Color.withHsv(saturation: Float, value: Float): Color {
    val (h, _, _) = toHsv()
    return hsvToColor(h, saturation, value)
}

private fun Color.toHsv(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val d = max - min
    val h = if (d == 0f) 0f
    else when (max) {
        red -> ((green - blue) / d).mod(6f)
        green -> ((blue - red) / d + 2f).mod(6f)
        else -> ((red - green) / d + 4f).mod(6f)
    } / 6f
    val s = if (max == 0f) 0f else d / max
    return Triple(h, s, max)
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val i = (h * 6f).toInt() % 6
    val f = h * 6f - (h * 6f).toInt()
    val p = v * (1f - s)
    val q = v * (1f - f * s)
    val t = v * (1f - (1f - f) * s)
    return when (i) {
        0 -> Color(v, t, p)
        1 -> Color(q, v, p)
        2 -> Color(p, v, t)
        3 -> Color(p, q, v)
        4 -> Color(t, p, v)
        else -> Color(v, p, q)
    }
}
