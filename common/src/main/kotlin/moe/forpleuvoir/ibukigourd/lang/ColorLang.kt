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

    inline val value get() = lang("value")

    inline fun clickCopyColor(color: Color) = lang("click_copy_color", color.hexStr)

    inline val rightClickPasteColor get() = lang("right_click_paste_color")

    inline fun copyColorSuccess(color: Color) = lang("copy_success", color.hexStr)

    /**
     * 复制成功的提示；[text] 传入**实际复制到剪贴板的内容**（hex 或 HSV 三元组文本等），
     * 使提示与剪贴板里的内容一致。
     */
    inline fun copyTextSuccess(text: Any) = lang("copy_success", text)

    inline fun pasteColorSuccess(color: Color) = lang("paste_success", color.hexStr)

    inline val pasteColorFailed get() = lang("paste_failed")
}