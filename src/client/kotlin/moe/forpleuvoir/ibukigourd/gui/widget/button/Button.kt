package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.extensions.asBox
import moe.forpleuvoir.ibukigourd.gui.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.render.enableBlend
import moe.forpleuvoir.ibukigourd.render.enableDepthTest
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.pick
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

open class Button(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    private val theme: ButtonTheme = ButtonThemes.Button2,
    onPress: (Button) -> Unit,
    narrationSupplier: NarrationSupplier = DEFAULT_NARRATION_SUPPLIER
) : ButtonWidget(x, y, width, height, message, { onPress(it as Button) }, narrationSupplier) {

    constructor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        message: Text,
        theme: ButtonTheme = ButtonThemes.Button2,
        onPress: (Button) -> Unit,
    ) : this(x, y, width, height, message, theme, onPress, DEFAULT_NARRATION_SUPPLIER)

    companion object {
        protected fun drawScrollableText(
            context: DrawContext,
            textRenderer: TextRenderer,
            text: Text,
            startX: Int,
            startY: Int,
            endX: Int,
            endY: Int,
            color: Int,
            shadow: Boolean = false
        ) {
            drawScrollableText(context, textRenderer, text, (startX + endX) / 2, startX, startY, endX, endY, color, shadow)
        }

        protected fun drawScrollableText(
            context: DrawContext,
            textRenderer: TextRenderer,
            text: Text,
            centerX: Int,
            startX: Int,
            startY: Int,
            endX: Int,
            endY: Int,
            color: Int,
            shadow: Boolean = false
        ) {
            val i = textRenderer.getWidth(text)
            val y = (startY + endY - 9) / 2 + 1
            val k = endX - startX
            if (i > k) {
                val l = i - k
                val d = Util.getMeasuringTimeMs().toDouble() / 1000.0
                val e = max(l.toDouble() * 0.5, 3.0)
                val f = sin((Math.PI / 2) * cos((Math.PI * 2) * d / e)) / 2.0 + 0.5
                val g = MathHelper.lerp(f, 0.0, l.toDouble())
                context.enableScissor(startX, startY, endX, endY)
                context.drawText(textRenderer, text, startX - g.toInt(), y, color, shadow)
                context.disableScissor()
            } else {
                val l = MathHelper.clamp(centerX, startX + i / 2, endX - i / 2)
                context.drawText(textRenderer, text, l - textRenderer.getWidth(text) / 2, y, color, shadow)
            }
        }

    }

    protected open var pressed: Boolean = false

    var xMargin: Int = 4

    override fun onPress() {
        pressed = true
        super.onPress()
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        pressed = false
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha)
        enableBlend()
        enableDepthTest()

        context.batchRenderTextureColored {
            context.drawWidgetTexture(asBox, status(theme.disabled, theme.idle, theme.hovered, theme.pressed))
        }

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        this.drawMessage(context, context.client.textRenderer, pressOrDisabled.pick(Colors.BLACK, Colors.BLACK_BEAN).argb)
    }

    override fun drawMessage(context: DrawContext, textRenderer: TextRenderer, color: Int) {
        this.drawScrollableText(context, textRenderer, xMargin, color)
    }

    override fun drawScrollableText(context: DrawContext, textRenderer: TextRenderer, xMargin: Int, color: Int) {
        val i: Int = this.x + xMargin
        val j: Int = this.x + this.getWidth() - xMargin
        drawScrollableText(context, textRenderer, this.message, i, this.y, j, this.y + this.getHeight(), color)
    }

    protected val pressOrDisabled: Boolean get() = this.active || pressed

    protected fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
        return if (active) {
            if (this.pressed) pressed
            else if (this.hovered || this.isFocused) hovered
            else idle
        } else disabled
    }

    protected inline fun <R> status(disabled: () -> R, idle: () -> R, hovered: () -> R, pressed: () -> R): R {
        return if (active) {
            if (this.pressed) pressed()
            else if (this.hovered || this.isFocused) hovered()
            else idle()
        } else disabled()
    }
}