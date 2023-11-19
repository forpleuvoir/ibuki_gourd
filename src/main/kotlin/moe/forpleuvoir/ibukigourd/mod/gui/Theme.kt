package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ModConfigCategory
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigMargin
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.item.impl.*
import moe.forpleuvoir.nebula.config.manager.AutoSaveConfigManager
import moe.forpleuvoir.nebula.config.persistence.ConfigManagerPersistence
import moe.forpleuvoir.nebula.config.persistence.JsonConfigManagerPersistence
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ModConfig("theme")
object Theme : ClientModConfigManager(IbukiGourd.metadata, "${IbukiGourd.MOD_ID}_theme"), ConfigManagerPersistence by JsonConfigManagerPersistence,
               AutoSaveConfigManager {

	override val initialDelay: Duration = 30.seconds

	override val period: Duration = 5.minutes


	override fun init() {
		super<ClientModConfigManager>.init()
		super<AutoSaveConfigManager>.init()
	}

	object BUTTON : ModConfigCategory("button") {

		val COLOR by ConfigColor("color", Colors.WHITE)

		val PRESS_OFFSET by ConfigFloat("press_offset", 1.0f)

		val PADDING by ConfigMargin("padding", Margin(6))

		val TEXTURE by ConfigEnum(
			"texture",
			ButtonThemes.Button2
		)

	}

	object TEXT_INPUT : ModConfigCategory("text_input") {

		val PADDING by ConfigMargin("padding", Margin(6))

		val SELECTED_COLOR by ConfigColor("selected_color", Color(0x00556f).alpha(0.45f))

		val CURSOR_COLOR by ConfigColor("cursor_color", Colors.BLACK)

		val BACKGROUND_SHADER_COLOR by ConfigColor("background_shader_color", Colors.WHITE)

		val TEXT_COLOR by ConfigColor("text_color", Color(0x303030))

		val HINT_COLOR by ConfigColor("hint_color", Color(0x707070))

	}

	object TEXT : ModConfigCategory("text") {

		val COLOR by ConfigColor("color", Colors.BLACK)

		val BACKGROUND_COLOR by ConfigColor("background_color", Colors.BLACK.alpha(0f))

		val SPACING by ConfigFloat("spacing", 1.0f)

		val SHADOW by ConfigBoolean("shadow", false)

		val RIGHT_TO_LEFT by ConfigBoolean("right_to_left", false)

	}

	object TIP : ModConfigCategory("tip") {

		val BACKGROUND_COLOR by ConfigColor("background_color", Colors.WHITE)

		val DELAY by ConfigInt("delay", 12, minValue = 0)

		val ARROW_OFFSET by ConfigMargin("arrow_offset", Margin(2))

		val PADDING by ConfigMargin("padding", Margin(6))

		val MARGIN by ConfigMargin("margin", Margin(7))

	}
}

