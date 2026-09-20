package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.item.*
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.math.*
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.nebula.config.config
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigSerde
import moe.forpleuvoir.nebula.config.item.*
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.duration
import moe.forpleuvoir.nebula.serialization.codec.enum
import net.minecraft.client.gui.Font
import net.minecraft.core.Direction
import kotlin.time.Duration.Companion.seconds

object TestConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "test") {

    var testInt by configInt("test_int", 256, 0, 1024).apply {
        translateText = Text.literal("测试Int")
        translateComment = Text.literal("测试用的Int\n测试一下换行")
    }

    var testLong by configLong("test_long", 0, 0, 233).apply {
        translateText = Text.literal("测试Long")
        translateComment =
            Text.literal("我想试一下超长的描述信息啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊")
    }

    var testFloat by configFloat("test_float", 0f, 0f, 233f)

    var testDouble by configDouble("test_double", 0.0, 0.0, 233.0)

    var testString by configString("test_string", "test")

    var testBoolean by configBoolean("test_boolean", true)

    var testDirection by configEnum("test_direction", Direction.UP)

    var testDisplayeMode by configEnum("test_display_mode", Font.DisplayMode.NORMAL)

    var testColor by configColor("test_color", Colors.BLUE_LOTUS)

    var testDuration by configDuration("test_duration", 30.seconds, 0.seconds, 60.seconds)

    var testDuration2 by configDuration("test_duration2", 30.seconds)

    var testBezier by config("test_bezier", CubicBezier.Standard, CubicBezier)

    /** 没有任何 wrapper 注册的类型：用于验证兜底行「暂不支持」（见 `UnspecifiedConfigWrapper`）。 */
    var testChar by configChar("test_char", 'a')

    var testKeyBind by configKeybind("test_key_bind", Keybind {
        println("按下了测试按键")
    })

    var testKeyBindBoolean by configToggleKeybind("test_key_bind_boolean", false, Keybind(Keyboard.H))

    var testKeyBindBoolean2 by configToggleKeybind("test_key_bind_boolean2", false, Keybind(Keyboard.J))

    var testKeyBindBoolean3 by configToggleKeybind("test_key_bind_boolean3", false, Keybind(Keyboard.K))

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

    /** 元素为枚举：验证列表元素控件分发里的「枚举 → 下拉」。 */
    val testEnumList by configList("test_enum_list", listOf(Direction.NORTH, Direction.SOUTH), Codec.enum())

    /** 元素为时长：验证「时长 → 时长框」。 */
    val testDurationList by configList("test_duration_list", listOf(1.seconds, 2.seconds), Codec.duration)

    /** 元素为曲线：验证「CubicBezier → 行内曲线画布」。 */
    val testBezierList by configList("test_bezier_list", listOf(CubicBezier.Standard, CubicBezier.EaseInOut), CubicBezier)

    /** 元素为**整数对**：验证对列表不再假设分量是字符串。 */
    val testIntPairList by configList("test_int_pair_list", listOf(1 to 2, 3 to 4), Codec.pair(Codec.int, Codec.int))

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

        object Nested2 : ConfigGroup("nested2") {

            val testBoolean by configBoolean("test_key_bind", false)

            var testInt by configInt("test_int", 0, 0, 233)

            val n = addConfig(Nested2)

            object Nested2 : ConfigGroup("nested2") {

                val testBoolean by configBoolean("test_key_bind", false)

                var testInt by configInt("test_int", 0, 0, 233)
            }
        }

        init {
            repeat(15) {
                configInt("test_int_$it", it, 0, 233)
            }
            addConfig(Nested2)
        }
    }


    val nested2 = addConfig(Nested2)

    object Nested2 : ConfigGroup("nested2") {

        val testBoolean by configBoolean("test_key_bind", false)

        var testInt by configInt("test_int", 0, 0, 233)
    }

    val vector = addConfig(Vector)

    object Vector : ConfigGroup("vector") {
        val vector2i by configVector2i("vector2i", Vector2i(0f, 0f), Vector2i(-6666f, -6666f), Vector2i(6666f, 6666f))
        val vector2f by configVector2f("vector2f", Vector2f(0f, 0f), Vector2f(-6666f, -6666f), Vector2f(6666f, 6666f))
        val vector2d by configVector2d("vector2d", Vector2d(0f, 0f), Vector2d(-6666f, -6666f), Vector2d(6666f, 6666f))

        val vector3i by configVector3i("vector3i", Vector3i(0f, 0f), Vector3i(-6666f, -6666f, -6666f), Vector3i(6666f, 6666f, 6666f))
        val vector3f by configVector3f("vector3f", Vector3f(0f, 0f), Vector3f(-6666f, -6666f, -6666f), Vector3f(6666f, 6666f, 6666f))
        val vector3d by configVector3d("vector3d", Vector3d(0f, 0f), Vector3d(-6666f, -6666f, -6666f), Vector3d(6666f, 6666f, 6666f))
    }
}