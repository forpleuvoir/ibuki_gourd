package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreenDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.DialogAnimationDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.DialogComposeScreenDefaults
import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenAnimation
import moe.forpleuvoir.compose_minecraft.platform.screen.ScreenAnimationDefaults
import moe.forpleuvoir.compose_minecraft.platform.textinput.ComposeInputBridge
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.item.configKeyCode
import moe.forpleuvoir.ibukigourd.config.item.configVector2f
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent
import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.buildText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonColors
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.item.*
import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * IbukiGourd 自身配置。
 *
 * 原 Compose Desktop 离屏渲染 UI 相关的配置项（主题 / 屏幕 / Toast / 图集缓存 / 打开界面快捷键）
 * 已随 UI 迁移至 compose-minecraft 一并移除；当前先重建提示（Toast）组，
 * 其余按新 UI 落地进度逐步回归。
 */
object IGConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "config") {

    init {
        addConfig(Gui)
    }

    object Gui : ConfigGroup("gui") {

        init {
            addConfig(Toast)
            addConfig(Scroller)
            addConfig(Screen)
            addConfig(Dialog)
        }

        object Toast : ConfigGroup("toast") {

            /**
             * 提示的默认展示时长。
             *
             * 只作用于**未显式指定**时长的调用（`ToastHandler.show*` 的默认参数）；
             * `Duration.ZERO` 表示默认不自动消失。
             */
            var duration: Duration by configDuration("duration", 2.seconds, Duration.ZERO, 30.seconds)

            /**
             * 同屏可见条数上限。超出的提示（[moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastStrategy.Enqueue]）
             * 排队等待；多条同时可见时落点相同、相互重叠。
             */
            var maxVisible: Int by configInt("max_visible", 1, 1, 10)

            /**
             * 提示在窗口中的落点偏移，取值 `0f~1f`（相对窗口尺寸的百分比）：
             * `x` 越小越靠左，`0.5` 为水平居中；`y` 表示面板**中心**所在的窗口高度比例。
             */
            var offset: Vector2fc by configVector2f(
                "offset",
                Vector2f(0.5f, 0.85f),
                Vector2f(0f, 0f),
                Vector2f(1f, 1f),
            )
        }


        object Scroller : ConfigGroup("scroller") {

            val scrollMultiplier1 by configFloat("scroll_multiplier_1", 2.5f, 0f, 20f)

            val scrollMultiplier1KeyCode by configKeyCode("scroll_multiplier_1_key_code", Keyboard.LEFT_SHIFT)

            val scrollMultiplier2 by configFloat("scroll_multiplier_2", 5f, 0f, 20f)

            val scrollMultiplier2KeyCode by configKeyCode("scroll_multiplier_2_key_code", Keyboard.LEFT_CONTROL)

            val scrollMultiplier3 by configFloat("scroll_multiplier_3", 10f, 0f, 20f)

            val scrollMultiplier3KeyCode by configKeyCode("scroll_multiplier_3_key_code", Keyboard.LEFT_ALT)


            override fun init() {
                // 事件在 InputHandler 增删按键状态之前广播，此时尚未计入本次事件的键码，
                // 查询 InputHandler 会导致释放时倍率不回落；改为用事件自带的键码维护按下集合
                val pressedKeys = mutableSetOf<KeyCode>()
                KeyboardEvent.Pressed.register { context ->
                    pressedKeys.add(context.keyCode)
                    ComposeInputBridge.scaleFactor = scrollMultiplier(pressedKeys)
                }
                KeyboardEvent.Released.register { context ->
                    pressedKeys.remove(context.keyCode)
                    ComposeInputBridge.scaleFactor = scrollMultiplier(pressedKeys)
                }
                MouseEvent.Pressed.register { context ->
                    pressedKeys.add(context.keyCode)
                    ComposeInputBridge.scaleFactor = scrollMultiplier(pressedKeys)
                }
                MouseEvent.Released.register { context ->
                    pressedKeys.remove(context.keyCode)
                    ComposeInputBridge.scaleFactor = scrollMultiplier(pressedKeys)
                }
            }

            private fun scrollMultiplier(pressedKeys: Set<KeyCode>): Float {
                return when {
                    scrollMultiplier1KeyCode in pressedKeys -> scrollMultiplier1
                    scrollMultiplier2KeyCode in pressedKeys -> scrollMultiplier2
                    scrollMultiplier3KeyCode in pressedKeys -> scrollMultiplier3
                    else -> 1f
                }
            }

        }

        /**
         * 普通屏幕（compose-minecraft 的 `ComposeScreen`）的进出场动画与屏幕默认值。
         *
         * 全部绑定到平台的 [ScreenAnimationDefaults] / [ComposeScreenDefaults]，
         * 改动对**之后新建**的屏幕生效（已打开的屏幕沿用开屏时读到的值）。
         */
        object Screen : ConfigGroup("screen") {

            /** 进出场时长（0 = 立即到位）。 */
            val fadeInDuration by configDuration(
                "fade_in_duration",
                220.milliseconds,
                Duration.ZERO,
                5.seconds,
            ).apply {
                observe { ScreenAnimationDefaults.durationMillis = it.getValue().inWholeMilliseconds.toInt() }
            }

            /**
             * 进场起点 / 出场终点的位移量（屏高比例）：0 = 只淡入淡出，正数 = 自下而上滑入。
             */
            val fadeInOffset by configFloat("fade_in_offset", 0.08f, 0f, 1f)
                .apply { observe { ScreenAnimationDefaults.slideFraction = it.getValue() } }

            /** 是否在位移的同时淡入淡出。 */
            val fade by configBoolean("fade", true)
                .apply { observe { ScreenAnimationDefaults.fade = it.getValue() } }

            /** 是否启用进出场动画；关闭后开屏即显示、请求关闭即关屏。 */
            val animationEnabled by configBoolean("animation_enabled", true).apply {
                observe {
                    ComposeScreenDefaults.animation =
                        if (it.getValue()) ScreenAnimation.Default else ScreenAnimation.None
                }
            }

            /** 新开的屏幕是否默认停画世界（需要世界当背景的屏可逐屏覆盖）。 */
            val disableWorldRender by configBoolean("disable_world_render", false)
                .apply { observe { ComposeScreenDefaults.disableWorldRenderByDefault = it.getValue() } }

            // TODO 缓动(easing)：ScreenAnimationDefaults.easing 是任意 Easing 对象，
            //  现有配置类型无法承载，待专用配置项（预设枚举 / 三次贝塞尔四点）落地后在此绑定。

            override fun init() {
                super.init()
                applyToDefaults()
            }

            private fun applyToDefaults() {
                ScreenAnimationDefaults.durationMillis = fadeInDuration.inWholeMilliseconds.toInt()
                ScreenAnimationDefaults.slideFraction = fadeInOffset
                ScreenAnimationDefaults.fade = fade
                ComposeScreenDefaults.animation =
                    if (animationEnabled) ScreenAnimation.Default else ScreenAnimation.None
                ComposeScreenDefaults.disableWorldRenderByDefault = disableWorldRender
            }
        }

        /**
         * 对话框屏幕（compose-minecraft 的 `DialogComposeScreen`）的进出场动画与对话框默认值。
         *
         * 全部绑定到平台的 [DialogAnimationDefaults] / [DialogComposeScreenDefaults]，
         * 改动对**之后新建**的对话框生效。
         */
        object Dialog : ConfigGroup("dialog") {

            /** 遮罩色（`0xAARRGGBB`，默认黑 40% 不透明度）。 */
            val scrimColor by configColor("scrim_color", NebulaColor.fromARGB(0x66000000))
                .apply { observe { DialogAnimationDefaults.scrimColor = it.getValue().toComposeColor() } }

            /** 进出场时长（0 = 立即到位）。 */
            val fadeInDuration by configDuration(
                "fade_in_duration",
                200.milliseconds,
                Duration.ZERO,
                5.seconds,
            ).apply {
                observe { DialogAnimationDefaults.durationMillis = it.getValue().inWholeMilliseconds.toInt() }
            }

            /** 内容进场起始缩放 / 出场目标缩放（1 = 不缩放）。 */
            val initialScale by configFloat("initial_scale", 0.8f, 0f, 2f)
                .apply { observe { DialogAnimationDefaults.initialScale = it.getValue() } }

            /** 新建的对话框是否停画世界（背后是原版屏或世界时通常保持关闭）。 */
            val disableWorldRender by configBoolean("disable_world_render", false)
                .apply { observe { DialogComposeScreenDefaults.disableWorldRender = it.getValue() } }

            // TODO 缓动(easing)：DialogAnimationDefaults.easing 是任意 Easing 对象，
            //  现有配置类型无法承载，待专用配置项（预设枚举 / 三次贝塞尔四点）落地后在此绑定。

            override fun init() {
                super.init()
                applyToDefaults()
            }

            private fun applyToDefaults() {
                DialogAnimationDefaults.scrimColor = scrimColor.toComposeColor()
                DialogAnimationDefaults.durationMillis = fadeInDuration.inWholeMilliseconds.toInt()
                DialogAnimationDefaults.initialScale = initialScale
                DialogComposeScreenDefaults.disableWorldRender = disableWorldRender
            }
        }
    }
}
