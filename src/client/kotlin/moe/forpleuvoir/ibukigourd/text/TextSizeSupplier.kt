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
    }
}

internal var textWidthSupplier: TextSizeSupplier = TextSizeSupplier

val String?.size: SizeFloat get() = textWidthSupplier.size(this)
val StringVisitable.size: SizeFloat get() = textWidthSupplier.size(this)
val OrderedText.size: SizeFloat get() = textWidthSupplier.size(this)

val String?.width: Float get() = size.width
val StringVisitable.width: Float get() = size.width
val OrderedText.width: Float get() = size.width

val String?.height: Float get() = size.height
val StringVisitable.height: Float get() = size.height
val OrderedText.height: Float get() = size.height