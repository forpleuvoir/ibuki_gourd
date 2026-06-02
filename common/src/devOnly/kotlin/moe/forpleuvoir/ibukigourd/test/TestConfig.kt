package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.comment
import moe.forpleuvoir.ibukigourd.config.item.configKeybind
import moe.forpleuvoir.ibukigourd.config.item.configPairList
import moe.forpleuvoir.ibukigourd.config.item.configToggleKeybind
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigSerde
import moe.forpleuvoir.nebula.config.item.*
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.enum
import net.minecraft.core.Direction
import kotlin.time.Duration.Companion.seconds

object TestConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "test") {

    var testInt by configInt("test_int", 256, 0, 999).apply {
        translateText = Text.literal("测试Int")
        comment = Text.literal("测试用的Int\n测试一下换行")
    }

    var testLong by configLong("test_long", 0, 0, 233).apply {
        translateText = Text.literal("测试Long")
        comment =
            Text.literal("我想试一下超长的描述信息啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊")
    }

    var testFloat by configFloat("test_float", 0f, 0f, 233f)

    var testDouble by configDouble("test_double", 0.0, 0.0, 233.0)

    var testString by configString("test_string", "test")

    var testBoolean by configBoolean("test_boolean", true)

    var testDirection by configEnum("test_direction", Direction.UP)

    var testColor by configColor("test_color", Colors.BLUE_LOTUS)

    var testDuration by configDuration("test_duration", 30.seconds, 0.seconds, 60.seconds)

    var testKeyBind by configKeybind("test_key_bind", Keybind {
        println("按下了测试按键")
    })

    var testKeyBindBoolean by configToggleKeybind("test_key_bind_boolean", false, Keybind(Keyboard.H))

    val testStringList by configList(
        "test_string_list", listOf(
            "测试1", "测试2", "测试4"
        ), Codec.string
    )

    val testStringMap by configMap(
        "test_string_map", mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "k3" to "k3"
        ), Codec.string
    )

    val testStringPairList by configPairList(
        "test_string_pair_list",
        listOf("k1" to "v1", "k2" to "v2", "k3" to "k3"),
        ConfigSerde.of(Codec.string),
        ConfigSerde.of(Codec.string)
    )

    val nested = addConfig(Nested)

    object Nested : ConfigGroup("nested") {

        val testKeyBind by configToggleKeybind("test_key_bind", false, Keybind {})

        var testInt by configInt("test_int", 0, 0, 233)

        init {
            repeat(15) {
                configInt("test_int_$it", it, 0, 233)
            }
        }
    }


    val nested2 = addConfig(Nested2)

    object Nested2 : ConfigGroup("nested2") {

        val testBoolean by configBoolean("test_key_bind", false)

        var testInt by configInt("test_int", 0, 0, 233)
    }
}