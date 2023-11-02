package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ModConfigCategory
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigMargin
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.plus
import moe.forpleuvoir.nebula.config.item.impl.*
import moe.forpleuvoir.nebula.config.manager.AutoSaveConfigManager
import moe.forpleuvoir.nebula.config.persistence.JsonConfigManagerPersistence
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ModConfig("theme")
object Theme : ClientModConfigManager(IbukiGourd.metadata, "${IbukiGourd.MOD_ID}_theme"), JsonConfigManagerPersistence, AutoSaveConfigManager {

    override val initialDelay: Duration = 30.seconds

    override val period: Duration = 5.minutes


    override fun init() {
        super<ClientModConfigManager>.init()
        super<AutoSaveConfigManager>.init()
    }

    object BUTTON : ModConfigCategory("button") {

        val COLOR by ConfigColor("color", Colors.WHITE)

        val HEIGHT by ConfigDouble("height", 1.0)

        val PADDING by ConfigMargin("padding", Margin(6))

        val TEXTURE by ConfigEnum(
            "texture",
            ButtonThemes.Button2
        )

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

