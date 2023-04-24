package com.forpleuvoir.ibukigourd.mod.gui

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.config.ClientModConfigManager
import com.forpleuvoir.ibukigourd.config.ModConfig
import com.forpleuvoir.ibukigourd.config.ModConfigCategory
import com.forpleuvoir.ibukigourd.config.item.impl.ConfigMargin
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.nebula.common.util.minute
import com.forpleuvoir.nebula.common.util.plus
import com.forpleuvoir.nebula.common.util.second
import com.forpleuvoir.nebula.config.impl.AutoSaveConfigManager
import com.forpleuvoir.nebula.config.impl.ConfigCategoryImpl
import com.forpleuvoir.nebula.config.impl.HoconConfigManagerSerializer
import com.forpleuvoir.nebula.config.item.impl.ConfigDouble
import com.forpleuvoir.nebula.config.item.impl.ConfigInt
import java.util.*

@ModConfig("ibukigourd_theme")
object Theme : ClientModConfigManager(IbukiGourd.metadata, "theme"), HoconConfigManagerSerializer, AutoSaveConfigManager {

	override val period: Long = 5.minute

	override val starTime: Date = Date() + 30.second

	override fun init() {
		super<ClientModConfigManager>.init()
		super<AutoSaveConfigManager>.init()
	}

	object BUTTON : ModConfigCategory("button") {

		val HEIGHT by ConfigDouble("height", 1.0)

		val PADDING by ConfigMargin("padding", Margin(6))

	}

	object TIP : ConfigCategoryImpl("tip") {

		val DELAY by ConfigInt("delay", 12, minValue = 0)

		val PADDING by ConfigMargin("padding", Margin(6))

	}
}

