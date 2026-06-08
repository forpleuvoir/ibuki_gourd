package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.nebula.common.color.Color

@Suppress("NOTHING_TO_INLINE")
object ColorLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.color.$key", args = args)

    inline val red get() = lang("red")

    inline val green get() = lang("green")

    inline val blue get() = lang("blue")

    inline val alpha get() = lang("alpha")

    inline val hue get() = lang("hue")

    inline val saturation get() = lang("saturation")

    inline val brightness get() = lang("brightness")

    inline fun clickCopyColor(color: Color) = lang("click_copy_color", color.hexStr)

    inline fun copyColorSuccess(color: Color) = lang("copy_success", color.hexStr)
}