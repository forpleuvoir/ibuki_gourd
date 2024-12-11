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
import moe.forpleuvoir.nebula.config.container.ConfigContainerImpl
import moe.forpleuvoir.nebula.config.item.impl.*
import kotlin.time.DurationUnit


@ModConfig("test")
object TestConfig : ClientModConfigManager(IbukiGourd.metadata, "${IbukiGourd.MOD_ID}_test") {

    var testInt by int("test_int", 0, 0, 233)

    var testLong by long("test_long", 0, 0, 233)

    var testFloat by float("test_float", 0f, 0f, 233f)

    var testDouble by double("test_double", 0.0, 0.0, 233.0)

    var testString by string("test_string", "test")

    var testBoolean by boolean("test_boolean", true)

    var testDirection by enum("test_direction", Direction.Top)

    var testColor by color("test_color", Colors.BLUE_LOTUS)

    var testDuration by durationObject("test_duration", DurationObject(30, DurationUnit.SECONDS))

    var testKeyBind by keyBind("test_key_bind", KeyBind {
        println("按下了测试按键")
    })

    var testKeyBindBoolean by keyBindBoolean("test_key_bind_boolean", KeyBind(Keyboard.H), false)

    val testStringList by stringList(
        "test_string_list", listOf(
            "测试1", "测试2", "测试4"
        )
    )

    val testStringMap by stringMap(
        "test_string_map", mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "k3" to "k3"
        )
    )

    val nested = addConfig(Nested)

    object Nested : ConfigContainerImpl("nested") {
        var testInt by int("test_int", 0, 0, 233)
    }

}