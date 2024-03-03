package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.trans
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

enum class KeyTriggerMode(val key: String) {

    //按键按下时触发一次
    OnPress("on_press"),

    //按键按住时重复触发
    OnPressed("on_pressed"),

    //按键长按时触发一次
    OnLongPress("on_long_press"),

    //按键长按时重复触发
    OnLongPressed("on_long_pressed"),

    //按键释放时触发一次
    OnRelease("on_release"),

    //按键按下和释放时都会触发一次
    BOTH("both");

    val displayName: Text
        get() = trans("ibuki_gourd.key_bind.trigger_mode.${key}")

    val description: Text
        get() = trans("ibuki_gourd.key_bind.trigger_mode.${key}.description")


    companion object : Deserializer<KeyTriggerMode> {

        @JvmStatic
        fun fromKey(key: String): KeyTriggerMode {
            return entries.first { it.key == key }
        }

        override fun deserialization(serializeElement: SerializeElement): KeyTriggerMode {
            return fromKey(serializeElement.asString)
        }

    }

}