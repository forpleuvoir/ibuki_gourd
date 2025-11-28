package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.util.textRenderer
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

interface TextSizeSupplier {
    fun size(text: String?): SizeFloat
    fun size(text: FormattedText): SizeFloat
    fun size(text: FormattedCharSequence): SizeFloat

    fun width(text: String?): Float
    fun width(text: FormattedText): Float
    fun width(text: FormattedCharSequence): Float


    fun height(text: String?): Float
    fun height(text: FormattedText): Float
    fun height(text: FormattedCharSequence): Float


    companion object : TextSizeSupplier {
        override fun size(text: String?): SizeFloat = Size(
            textRenderer.splitter.stringWidth(text),
            textRenderer.lineHeight.toFloat()
        )

        override fun size(text: FormattedText): SizeFloat = Size(
            textRenderer.splitter.stringWidth(text),
            textRenderer.lineHeight.toFloat()
        )

        override fun size(text: FormattedCharSequence): SizeFloat = Size(
            textRenderer.splitter.stringWidth(text),
            textRenderer.lineHeight.toFloat()
        )

        override fun width(text: String?): Float =
            textRenderer.splitter.stringWidth(text)

        override fun width(text: FormattedText): Float =
            textRenderer.splitter.stringWidth(text)

        override fun width(text: FormattedCharSequence): Float =
            textRenderer.splitter.stringWidth(text)

        override fun height(text: String?): Float =
            textRenderer.lineHeight.toFloat()

        override fun height(text: FormattedText): Float =
            textRenderer.lineHeight.toFloat()

        override fun height(text: FormattedCharSequence): Float =
            textRenderer.lineHeight.toFloat()
    }
}

internal var textWidthSupplier: TextSizeSupplier = TextSizeSupplier

val String?.size: SizeFloat get() = textWidthSupplier.size(this)
val FormattedText.size: SizeFloat get() = textWidthSupplier.size(this)
val FormattedCharSequence.size: SizeFloat get() = textWidthSupplier.size(this)

val String?.width: Float get() = textWidthSupplier.width(this)
val FormattedText.width: Float get() = textWidthSupplier.width(this)
val FormattedCharSequence.width: Float get() = textWidthSupplier.width(this)

val String?.height: Float get() = textWidthSupplier.height(this)
val FormattedText.height: Float get() = textWidthSupplier.height(this)
val FormattedCharSequence.height: Float get() = textWidthSupplier.height(this)