package moe.forpleuvoir.ibukigourd.mod.config

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
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.item.*
import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.time.Duration
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
    }
}
