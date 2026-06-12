package moe.forpleuvoir.ibukigourd.mod

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.item.configKeyCode
import moe.forpleuvoir.ibukigourd.config.item.configKeybind
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.ui.configwrapper.BooleanConfigWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.uiWrapper
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.filled.DarkMode
import moe.forpleuvoir.ibukigourd.ui.icon.filled.LightMode
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.item.configBoolean
import moe.forpleuvoir.nebula.config.item.configDuration
import moe.forpleuvoir.nebula.config.item.configFloat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object IGConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "config") {

    private val openScreen by configKeybind("open_screen", Keybind(Keyboard.I, Keyboard.G) {
        //TODO open Screen
    })

    init {
        addConfig(Gui)
    }


    object Gui : ConfigGroup("gui") {

        var lightMode by configBoolean("light_mode", true)
            .uiWrapper {
                BooleanConfigWrapper(it, { mode ->
                    Icon(if (mode) Icons.Filled.LightMode else Icons.Filled.DarkMode, null)
                })
            }.apply {
                observe {
                    colorScheme = if (it.getValue()) lightColorScheme() else darkColorScheme()
                }
            }

        var colorScheme: ColorScheme by mutableStateOf(if (lightMode) lightColorScheme() else darkColorScheme())

        init {
            addConfig(Screen)
            addConfig(Scroller)
        }

        object Screen : ConfigGroup("screen") {

            val fadeInOffset by configFloat("fade_in_offset", 0.1f, 0f, 1f)

            val fadeInDuration by configDuration("fade_in_duration", 150.milliseconds, Duration.ZERO, 2.seconds)

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

    }

}