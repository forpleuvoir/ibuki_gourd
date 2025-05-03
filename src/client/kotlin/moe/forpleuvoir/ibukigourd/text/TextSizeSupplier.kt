package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.util.textRenderer
import net.minecraft.text.OrderedText
import net.minecraft.text.StringVisitable

interface TextSizeSupplier {
    fun size(text: String?): SizeFloat
    fun size(text: StringVisitable): SizeFloat
    fun size(text: OrderedText): SizeFloat

    fun width(text: String?): Float
    fun width(text: StringVisitable): Float
    fun width(text: OrderedText): Float


    fun height(text: String?): Float
    fun height(text: StringVisitable): Float
    fun height(text: OrderedText): Float


    companion object : TextSizeSupplier {
        override fun size(text: String?): SizeFloat = Size(
            textRenderer.textHandler.getWidth(text),
            textRenderer.fontHeight.toFloat()
        )

        override fun size(text: StringVisitable): SizeFloat = Size(
            textRenderer.textHandler.getWidth(text),
            textRenderer.fontHeight.toFloat()
        )

        override fun size(text: OrderedText): SizeFloat = Size(
            textRenderer.textHandler.getWidth(text),
            textRenderer.fontHeight.toFloat()
        )

        override fun width(text: String?): Float =
            textRenderer.textHandler.getWidth(text)

        override fun width(text: StringVisitable): Float =
            textRenderer.textHandler.getWidth(text)

        override fun width(text: OrderedText): Float =
            textRenderer.textHandler.getWidth(text)

        override fun height(text: String?): Float =
            textRenderer.fontHeight.toFloat()

        override fun height(text: StringVisitable): Float =
            textRenderer.fontHeight.toFloat()

        override fun height(text: OrderedText): Float =
            textRenderer.fontHeight.toFloat()
    }
}

internal var textWidthSupplier: TextSizeSupplier = TextSizeSupplier

val String?.size: SizeFloat get() = textWidthSupplier.size(this)
val StringVisitable.size: SizeFloat get() = textWidthSupplier.size(this)
val OrderedText.size: SizeFloat get() = textWidthSupplier.size(this)

val String?.width: Float get() = textWidthSupplier.width(this)
val StringVisitable.width: Float get() = textWidthSupplier.width(this)
val OrderedText.width: Float get() = textWidthSupplier.width(this)

val String?.height: Float get() = textWidthSupplier.height(this)
val StringVisitable.height: Float get() = textWidthSupplier.height(this)
val OrderedText.height: Float get() = textWidthSupplier.height(this)