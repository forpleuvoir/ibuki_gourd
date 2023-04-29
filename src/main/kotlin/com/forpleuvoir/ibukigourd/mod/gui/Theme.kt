package com.forpleuvoir.ibukigourd.mod.gui

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.config.ClientModConfigManager
import com.forpleuvoir.ibukigourd.config.ModConfig
import com.forpleuvoir.ibukigourd.config.ModConfigCategory
import com.forpleuvoir.ibukigourd.config.item.impl.ConfigMargin
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.nebula.common.color.Colors
import com.forpleuvoir.nebula.common.util.minute
import com.forpleuvoir.nebula.common.util.plus
import com.forpleuvoir.nebula.common.util.second
import com.forpleuvoir.nebula.config.impl.AutoSaveConfigManager
import com.forpleuvoir.nebula.config.impl.HoconConfigManagerSerializer
import com.forpleuvoir.nebula.config.item.impl.ConfigBoolean
import com.forpleuvoir.nebula.config.item.impl.ConfigColor
import com.forpleuvoir.nebula.config.item.impl.ConfigDouble
import com.forpleuvoir.nebula.config.item.impl.ConfigInt
import java.util.*

@ModConfig("theme")
object Theme : ClientModConfigManager(IbukiGourd.metadata, "theme"), HoconConfigManagerSerializer, AutoSaveConfigManager {

	override val period: Long = 5.minute

	override val starTime: Date = Date() + 30.second

	override fun init() {
		super<ClientModConfigManager>.init()
		super<AutoSaveConfigManager>.init()
	}

	object BUTTON : ModConfigCategory("button") {

		val COLOR by ConfigColor("color", Colors.WHITE)

		val HEIGHT by ConfigDouble("height", 1.0)

		val PADDING by ConfigMargin("padding", Margin(6))

	}

	object TEXT : ModConfigCategory("text") {

		val COLOR by ConfigColor("color", Colors.BLACK)

		val BACKGROUND_COLOR by ConfigColor("background_color", Colors.BLACK.alpha(0f))

		val SPACING by ConfigDouble("spacing", 1.0)

		val SHADOW by ConfigBoolean("shadow", false)

		val RIGHT_TO_LEFT by ConfigBoolean("rightToLeft", false)

	}

	object TIP : ModConfigCategory("tip") {

		val BACKGROUND_COLOR by ConfigColor("background_color", Colors.WHITE)

		val DELAY by ConfigInt("delay", 12, minValue = 0)

		val ARROW_OFFSET by ConfigMargin("arrow_offset", Margin(2))

		val PADDING by ConfigMargin("padding", Margin(6))

		val MARGIN by ConfigMargin("margin", Margin(7))

	}
}

