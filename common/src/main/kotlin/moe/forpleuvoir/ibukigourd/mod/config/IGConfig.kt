package moe.forpleuvoir.ibukigourd.mod.config

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.materialkolor.dynamicColorScheme
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.item.configKeyCode
import moe.forpleuvoir.ibukigourd.config.item.configKeybind
import moe.forpleuvoir.ibukigourd.config.item.configVector2f
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig.Gui.Theme.colorSchemeSeed
import moe.forpleuvoir.ibukigourd.mod.ui.IbukiGourdScreen
import moe.forpleuvoir.ibukigourd.ui.configwrapper.BooleanConfigWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ColorSchemeConfigWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigRowWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.uiWrapper
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Palette
import moe.forpleuvoir.ibukigourd.ui.icon.default.Routine
import moe.forpleuvoir.ibukigourd.ui.icon.filled.DarkMode
import moe.forpleuvoir.ibukigourd.ui.icon.filled.LightMode
import moe.forpleuvoir.ibukigourd.ui.open
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.item.*
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.color
import org.joml.Vector2f
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object IGConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "config") {

    private val openScreen by configKeybind("open_screen", Keybind(Keyboard.I, Keyboard.G) {
        IbukiGourdScreen().open()
    })

    init {
        addConfig(Gui)
    }


    object Gui : ConfigGroup("gui") {
        init {
            addConfig(Theme)
            addConfig(Screen)
            addConfig(Toast)
            addConfig(Scroller)
//            addConfig(Cache)
        }

        val quickActionKeyCode by configKeyCode("quick_action_key_code", Keyboard.LEFT_SHIFT)

        val hideActionKeyCode by configKeyCode("hide_action_key_code", Keyboard.LEFT_ALT)

        object Theme : ConfigGroup("theme") {

            //用来刷新初始化的Toast的配色方案
            private var _state = false

            private fun refreshColorScheme() {
                colorScheme = dynamicColorScheme(colorSchemeSeed.toComposeColor, isDark = !lightMode)
                if (_state) {
                    ToastHandler.activeScheme = ToastHandler.defaultScheme
                    _state = true
                }
            }

            var lightMode by configBoolean("light_mode", true)
                .uiWrapper {
                    CompositionLocalProvider(ConfigRowWrapper.LocalIcon provides {
                        Icon(Icons.Routine, null, modifier = Modifier.size(32.dp))
                    }) {
                        BooleanConfigWrapper(it, { mode ->
                            Icon(if (mode) Icons.Filled.LightMode else Icons.Filled.DarkMode, null)
                        })
                    }
                }.apply {
                    observe { refreshColorScheme() }
                }

            /** 预设的 seed 颜色列表 */
            val colorSchemeSeeds by configList(
                "color_scheme_seeds",
                listOf(
                    Color.fromRGB(0x445E9E),
                    Color.fromRGB(0x6B4EC8),
                    Color.fromRGB(0x2E7D32),
                    Color.fromRGB(0xD2703B),
                    Color.fromRGB(0xC2185B),
                    Color.fromRGB(0x8647B3),
                ),
                Codec.color,
            ).uiWrapper {
                CompositionLocalProvider(ConfigRowWrapper.LocalIcon provides {
                    Icon(Icons.Palette, null, modifier = Modifier.size(32.dp))
                }) {
                    ColorSchemeConfigWrapper(
                        config = it,
                        selected = colorSchemeSeed,
                        isDark = !lightMode,
                        onSelect = { color -> colorSchemeSeed = color },
                    )
                }
            }

            /** 当前选中的 seed 色（隐藏，不渲染 UI） */
            var colorSchemeSeed by configColor("color_scheme_seed", Color.fromRGB(0x445E9E))
                .uiWrapper { }
                .apply {
                    observe { refreshColorScheme() }
                }

            var colorScheme: ColorScheme by mutableStateOf(dynamicColorScheme(colorSchemeSeed.toComposeColor, isDark = !lightMode))
        }

        object Screen : ConfigGroup("screen") {

            val fadeInOffset by configFloat("fade_in_offset", 0.1f, 0f, 1f)

            val fadeInDuration by configDuration("fade_in_duration", 150.milliseconds, Duration.ZERO, 2.seconds)

            val pauseGame by configBoolean("pause_game", false)

        }

        object Toast : ConfigGroup("toast") {

            var adaptiveColorScheme by configBoolean("adaptive_color_scheme", true)

            var lightMode by configBoolean("light_mode", true)
                .uiWrapper {
                    CompositionLocalProvider(ConfigRowWrapper.LocalIcon provides {
                        Icon(Icons.Routine, null, modifier = Modifier.size(32.dp))
                    }) {
                        BooleanConfigWrapper(it, { mode ->
                            Icon(if (mode) Icons.Filled.LightMode else Icons.Filled.DarkMode, null)
                        })
                    }
                }.apply {
                    observe { ToastHandler.activeScheme = dynamicColorScheme(colorSchemeSeed.toComposeColor, isDark = !this.getValue()) }
                }

            var offset by configVector2f("offset", Vector2f(0.5f, 0.85f), Vector2f(0f, 0f), Vector2f(1f, 1f))

        }

        object Scroller : ConfigGroup("scroller") {

            val scrollMultiplier1 by configFloat("scroll_multiplier_1", 2.5f, 0f, 20f)

            val scrollMultiplier1KeyCode by configKeyCode("scroll_multiplier_1_key_code", Keyboard.LEFT_SHIFT)

            val scrollMultiplier2 by configFloat("scroll_multiplier_2", 5f, 0f, 20f)

            val scrollMultiplier2KeyCode by configKeyCode("scroll_multiplier_2_key_code", Keyboard.LEFT_CONTROL)

            val scrollMultiplier3 by configFloat("scroll_multiplier_3", 10f, 0f, 20f)

            val scrollMultiplier3KeyCode by configKeyCode("scroll_multiplier_3_key_code", Keyboard.LEFT_ALT)

            fun applyScrollMultiplier(amount: Float): Float {
                return if (InputHandler.wasKeyPressed(scrollMultiplier1KeyCode)) {
                    amount * scrollMultiplier1
                } else if (InputHandler.wasKeyPressed(scrollMultiplier2KeyCode)) {
                    amount * scrollMultiplier2
                } else if (InputHandler.wasKeyPressed(scrollMultiplier3KeyCode)) {
                    amount * scrollMultiplier3
                } else {
                    amount
                }
            }

        }

//        object Cache : ConfigGroup("cache") {
//
//            val itemTextureCacheSize by configLong("cache_item_texture_cache_size", 1024 * 1024 * 256)
//
//        }

    }

}
