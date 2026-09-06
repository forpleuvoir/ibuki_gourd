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
 * 当前缺省 3。
 */
val LocalSokitsuPixelScale: ProvidableCompositionLocal<Int> = staticCompositionLocalOf { 3 }

/**
 * 当前作用域内 [moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite] 的默认染色色板。
 *
 * 默认为 [ColorTone.Unspecified]，语义是"本作用域不干预，继续按组件 token 映射表 +
 * [LocalColorScheme] 解析"。可通过 [CompositionLocalProvider] 在子树内整体切换为
 * secondary / error / 自定义色板；组件 `Colors` 工厂处显式传参则优先级最高。
 *
 * 类型为非空 [ColorTone]，用 [ColorTone.Unspecified] 而非 null 表达"未指定"，
 * 从而与组件 `Colors` 工厂的默认参数共用一套语义。
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
     * 当前像素放大倍率（1 逻辑像素 → N×N 屏幕像素块）：
     * 来自全局 [SokitsuThemeMeta.pixelScale]（资源包可覆盖，缺省 3），见 [LocalSokitsuPixelScale]。
     */
    val pixelScale: Int
        @Composable @ReadOnlyComposable
        get() = SokitsuThemeMeta.pixelScale

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

/**
 * Sokitsu 主题入口。
 *
 * 颜色来源（优先级高 → 低）：
 * 1. 显式传入的 [colorScheme]（完全接管，逃生舱）；
 * 2. 全局 [SokitsuThemeMeta] 中 [darkTheme] 对应的亮/暗 section（资源包定义，缺槽回落内置工厂默认）。
 *
 * [pixelScale] 与组件默认尺寸/内边距同样来自全局 [SokitsuThemeMeta]，
 * 资源重载时整体刷新，无需重新进入界面。
 *
 * @param darkTheme 是否使用深色方案；默认探测**操作系统**的主题偏好
 *   （[systemDarkTheme]，取不到按浅色），也可显式传入（如测试屏的亮暗切换）
 * @param colorScheme 显式指定的配色方案；null = 由 meta 构建
 */
@Composable
fun SokitsuTheme(
    colorScheme: ColorScheme = SokitsuThemeMeta.colorScheme(systemDarkTheme()),
    typography: Typography = SokitsuTheme.typography,
    pixelScale: Int = SokitsuThemeMeta.pixelScale,
    content: @Composable () -> Unit,
) {
    val rememberedColors = remember { colorScheme.copy() }.apply { updateColorsFrom(colorScheme) }
    val selectionColors = rememberTextSelectionColors(rememberedColors)
    CompositionLocalProvider(
        LocalColorScheme provides rememberedColors,
        LocalContentAlpha provides ContentAlpha.high,
        LocalContentColor provides rememberedColors.onBackground.base,
        LocalIndication provides SokitsuIndicationNodeFactory,
        LocalTextSelectionColors provides selectionColors,
        LocalTypography provides typography,
        LocalSokitsuPixelScale provides pixelScale,
        LocalShadowLight provides Offset(-1f, -1f),
    ) {
        ProvideTextStyle(value = typography.body, content = content)
    }
}