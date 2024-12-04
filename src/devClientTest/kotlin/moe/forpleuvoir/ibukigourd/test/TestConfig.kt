package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.item.impl.DurationObject
import moe.forpleuvoir.ibukigourd.config.item.impl.durationObject
import moe.forpleuvoir.ibukigourd.config.item.impl.keyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.keyBindBoolean
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.item.impl.*
import kotlin.time.DurationUnit


@ModConfig("test")
object TestConfig : ClientModConfigManager(IbukiGourd.metadata, "${IbukiGourd.MOD_ID}_test") {

    var testInt = int("test_int", 0, 0, 233)

    var testLong = long("test_long", 0, 0, 233)

    var testFloat = float("test_float", 0f, 0f, 233f)

    var testDouble = double("test_double", 0.0, 0.0, 233.0)

    var testString = string("test_string", "test")

    var testBoolean = boolean("test_boolean", true)

    var testDirection = enum("test_direction", Direction.Top)

    var testColor = color("test_color", Colors.BLUE_LOTUS)

    var testDuration = durationObject("test_duration", DurationObject(30, DurationUnit.SECONDS))

    var testKeyBind = keyBind("test_key_bind", KeyBind {
        println("按下了测试按键")
    })

    var testKeyBindBoolean = keyBindBoolean("test_key_bind_boolean", KeyBind(Keyboard.H), false)

}