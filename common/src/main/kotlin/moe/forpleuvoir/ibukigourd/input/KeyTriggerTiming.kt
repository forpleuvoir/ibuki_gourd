package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.enum

enum class KeyTriggerTiming(val key: String) {

    /**
     * 按下瞬间触发（仅触发一次）
     */
    Press("press"),

    /**
     * 按住期间持续触发
     */
    WhilePressed("while_pressed"),

    /**
     * 长按达成瞬间触发（仅触发一次）
     */
    LongPress("long_press"),

    /**
     * 长按期间持续触发
     */
    WhileLongPressed("while_long_pressed"),

    /**
     * 松开瞬间触发
     */
    Release("release"),

    /**
     * 按下或松开时均触触发
     */
    PressAndRelease("press_and_release");

    val displayName: MutableText
        get() = Translatable("ibuki_gourd.input.key_trigger_mode.${key}", key)

    val comment: MutableText
        get() = Translatable("ibuki_gourd.input.key_trigger_mode.${key}.comment", key)


    companion object : Codec<KeyTriggerTiming> by Codec.enum<KeyTriggerTiming>()

}