package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

@Suppress("NOTHING_TO_INLINE")
object MiscLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.misc.$key", args = args)

    inline val copy get() = lang("copy")

    inline val cut get() = lang("cut")

    inline val paste get() = lang("paste")

    inline val selectAll get() = lang("select_all")

    inline val reset get() = lang("reset")

    inline val confirm get() = lang("confirm")

    inline val cancel get() = lang("cancel")

    inline val switchOn get() = lang("switch.on")

    inline val switchOff get() = lang("switch.off")

    fun switch(switch: Boolean) = if (switch) switchOn else switchOff

    fun coloredSwitch(switch: Boolean, onColor: Color = Colors.LIMEGREEN, offColor: Color = Colors.RED) =
        if (switch) switchOn.withColor(onColor) else switchOff.withColor(offColor)

    inline val unsupported get() = lang("unsupported")

    inline val notSpecified get() = lang("not_specified")

    inline val hasNothing get() = lang("has_nothing")

    inline val setting get() = lang("setting")

    inline val search get() = lang("search")

    inline val moveUp get() = lang("move_up")

    inline val moveDown get() = lang("move_down")

    inline val moveLeft get() = lang("move_left")

    inline val moveRight get() = lang("move_right")

    inline val add get() = lang("add")

    inline val remove get() = lang("remove")

    inline fun removeConfirm(content: Any) = lang("remove_confirm", content)

    inline val edit get() = lang("edit")

    inline val content get() = lang("content")

    fun <T : Comparable<*>> notInRange(value: T, minValue: T, maxValue: T) = lang("not_in_range", value, minValue, maxValue)

    inline fun copySuccess(obj: Any) = ColorLang.lang("copy_success", obj)

}