package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenBackgroundBlur

/**
 * 屏幕背景模糊模式：跟随原版「菜单背景模糊度」/ 不模糊 / 固定半径。
 *
 * [Fixed] 的半径由独立配置项给出，取值范围 1 ~ [ScreenBackgroundBlur.Fixed.MAX_RADIUS]。
 */
enum class BackgroundBlurMode { Vanilla, None, Fixed }

/**
 * 配置 → 运行时模糊策略（compose-minecraft 的 [ScreenBackgroundBlur]）。
 *
 * @param mode 配置里的模糊模式
 * @param radius [BackgroundBlurMode.Fixed] 使用的模糊半径（采样像素数）
 */
fun resolveBackgroundBlur(mode: BackgroundBlurMode, radius: Int): ScreenBackgroundBlur = when (mode) {
    BackgroundBlurMode.Vanilla -> ScreenBackgroundBlur.Vanilla
    BackgroundBlurMode.None    -> ScreenBackgroundBlur.None
    BackgroundBlurMode.Fixed   -> ScreenBackgroundBlur.Fixed(radius)
}
