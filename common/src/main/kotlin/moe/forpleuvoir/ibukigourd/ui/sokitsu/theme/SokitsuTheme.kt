package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ProvideTextStyle
import moe.forpleuvoir.ibukigourd.util.contrasting

/**
 * Sokitsu 像素放大器（Int）：渲染时 **1 个逻辑像素放大为 N×N 的屏幕像素块**。
 *
 * 与素材密度（[moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuLayerSprite.density]，
 * Int，@1x/@2x）正交：源图 1 像素最终占屏 = `pixelScale / sprite.density` 个屏幕像素。
 *
 * **必须为整数**（类型即约束）：非整数倍会导致纹素块大小不均、像素风边缘模糊。
 * 当前缺省 2。
 */
val LocalSokitsuPixelScale: ProvidableCompositionLocal<Int> = staticCompositionLocalOf { 2 }

/**
 * 当前作用域内 [moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite] 的默认染色色板。
 *
 * 默认为 [ColorTone.Unspecified]，语义是"本作用域不干预，继续按组件 token 映射表 +
 * [LocalColorScheme] 解析"。可通过 [CompositionLocalProvider] 在子树内整体切换为
 * secondary / error / 自定义色板；组件 `Colors` 工厂处显式传参则优先级最高。
 *
 * 类型为非空 [ColorTone]，用 [ColorTone.Unspecified] 而非 null 表达"未指定"，
 * 从而与组件 `Colors` 工厂的默认参数共用一套语义——旧版这里是 `ColorTone?`，
 * 调用点必须写 `LocalSokitsuTone.current ?: primary`，既多一层 null 判断，
 * 也无法表达"作用域显式要求回退到主题默认"这层意思。
 *
 * 它在组件 token 回退链中位于中间一级：
 * `调用点传参` > `本作用域` > `组件 token 表` > `主题槽位`。
 */
val LocalSokitsuTone: ProvidableCompositionLocal<ColorTone> =
    staticCompositionLocalOf { ColorTone.Unspecified }

object SokitsuTheme {

    val colorScheme: ColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalColorScheme.current

    /**
     * 当前像素放大倍率（1 逻辑像素 → N×N 屏幕像素块），见 [LocalSokitsuPixelScale]。
     */
    val pixelScale: Int
        @Composable @ReadOnlyComposable
        get() = LocalSokitsuPixelScale.current

    /**
     * 当前生效的选中/焦点外框指示色：
     * 取 [LocalSelectedOutlineColor] 提供的值；未指定（[Color.Unspecified]）时自动计算与**主色 base**
     * 对比度最大的醒目颜色（互补色相 + 明度对立，见 [moe.forpleuvoir.ibukigourd.util.contrasting]）。
     */
    val selectedOutlineColor: Color
        @Composable @ReadOnlyComposable
        get() = LocalSelectedOutlineColor.current.takeOrElse { colorScheme.primary.base.contrasting() }

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current

}

@Composable
fun SokitsuTheme(
    colorScheme: ColorScheme = SokitsuTheme.colorScheme,
    typography: Typography = SokitsuTheme.typography,
    pixelScale: Int = SokitsuTheme.pixelScale,
    content: @Composable () -> Unit,
) {
    val rememberedColors = remember { colorScheme.copy() }.apply { updateColorsFrom(colorScheme) }
    val selectionColors = rememberTextSelectionColors(rememberedColors)
    CompositionLocalProvider(
        LocalColorScheme provides rememberedColors,
        LocalContentAlpha provides ContentAlpha.high,
        LocalContentColor provides colorScheme.onBackground.base,
        LocalIndication provides SokitsuIndicationNodeFactory,
        LocalTextSelectionColors provides selectionColors,
        LocalTypography provides typography,
        LocalSokitsuPixelScale provides pixelScale,
        LocalShadowLight provides Offset(-1f, -1f),
    ) {
        ProvideTextStyle(value = typography.body, content = content)
    }
}