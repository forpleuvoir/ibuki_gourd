package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable

@Suppress("NOTHING_TO_INLINE")
object InputLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.input.$key", args = args)


    inline val pressToSetting get() = lang("press_to_setting")

    inline val releaseToSaveSetting get() = lang("release_to_save_setting")

    inline val keybindConflict get() = lang("keybind_conflict")

    val KeybindSetting = KeybindSettingLang

    object KeybindSettingLang {

        inline val env get() = lang("keybind_setting.env")

        inline val trigger get() = lang("keybind_setting.trigger")

        inline val passthrough get() = lang("keybind_setting.passthrough")

        inline val strict get() = lang("keybind_setting.strict")

        inline val longPressThreshold get() = lang("keybind_setting.long_press_threshold")

        inline val repeatInterval get() = lang("keybind_setting.repeat_interval")

    }

}