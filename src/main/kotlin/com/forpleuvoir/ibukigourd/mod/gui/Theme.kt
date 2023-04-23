package com.forpleuvoir.ibukigourd.mod.gui

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.config.ClientModConfigManager
import com.forpleuvoir.ibukigourd.config.ModConfig
import com.forpleuvoir.nebula.common.util.minute
import com.forpleuvoir.nebula.common.util.plus
import com.forpleuvoir.nebula.common.util.second
import com.forpleuvoir.nebula.config.impl.AutoSaveConfigManager
import com.forpleuvoir.nebula.config.impl.ConfigCategoryImpl
import com.forpleuvoir.nebula.config.impl.HoconConfigManagerSerializer
import com.forpleuvoir.nebula.config.item.impl.ConfigDouble
import java.util.*

@ModConfig("ibukigourd_theme")
object Theme : ClientModConfigManager(IbukiGourd.MOD_ID, "theme"), HoconConfigManagerSerializer, AutoSaveConfigManager {

	override val period: Long = 5.minute

	override val starTime: Date = Date() + 30.second

	override fun init() {
		super<ClientModConfigManager>.init()
		super<AutoSaveConfigManager>.init()
	}

	object BUTTON : ConfigCategoryImpl("button") {

		var BUTTON_HEIGHT by ConfigDouble("button_height", 1.0)

		var BUTTON_PADDING_LEFT by ConfigDouble("button_padding_left", 6.0)

		var BUTTON_PADDING_RIGHT by ConfigDouble("button_padding_right", 6.0)

		var BUTTON_PADDING_TOP by ConfigDouble("button_padding_top", 6.0)

		var BUTTON_PADDING_BOTTOM by ConfigDouble("button_padding_bottom", 6.0)

	}
}

