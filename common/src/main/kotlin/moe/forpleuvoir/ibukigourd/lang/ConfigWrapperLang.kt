package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable

@Suppress("NOTHING_TO_INLINE")
object ConfigWrapperLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.config_wrapper.$key", args = args)


    val pairFirst get() = lang("pair.first")

    val pairSecond get() = lang("pair.second")

    val mapKey get() = lang("map.key")

    val mapValue get() = lang("map.value")

    fun keyExists(key: Any) = lang("key_exists", key)

    val pressToSetting get() = lang("press_to_setting")

    val releaseToSaveSetting get() = lang("release_to_save_setting")

    val keybindConflict get() = lang("keybind_conflict")

    val move get() = lang("move")

    fun listConfigWrapperText(count: Int) = lang("list.text", count)

    fun mapConfigWrapperText(count: Int) = lang("map.text", count)

}