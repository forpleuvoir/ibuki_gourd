package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseEnterEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseLeaveEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseMoveEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.render.enableBlend
import moe.forpleuvoir.ibukigourd.render.enableDepthTest
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.Tick
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

open class IGButtonWidget(
    private val theme: ButtonTheme = ButtonThemes.Button2,
) : IGPressableWidget(), LinearLayout {


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
            val textWidth = textRenderer.getWidth(text)
            val y = (startY + endY - 9) / 2 + 1
            val width = endX - startX
            if (textWidth > width) {
                val l = textWidth - width
                val d = Util.getMeasuringTimeMs().toDouble() / 1000.0
                val e = max(l.toDouble() * 0.5, 3.0)
                val f = sin((Math.PI / 2) * cos((Math.PI * 2) * d / e)) / 2.0 + 0.5
                val g = MathHelper.lerp(f, 0.0, l.toDouble())
                context.enableScissor(startX, startY, endX, endY)
                context.drawText(textRenderer, text, startX - g.toInt(), y, color, shadow)
                context.disableScissor()
            } else {
                val l = MathHelper.clamp(centerX, startX + textWidth / 2, endX - textWidth / 2)
                context.drawText(textRenderer, text, l - textRenderer.getWidth(text) / 2, y, color, shadow)
            }
        }

    }

    //------------ IGButton ------------\\

    open var longPressTime: Tick = 20

    open var pressTickCounter: Tick = 0
        protected set

    override fun tick() {
        if (pressed) {
            pressTickCounter++
            if (longPressTime == pressTickCounter) {
                longPress(this)
            }
        } else if (pressTickCounter != 0L) {
            pressTickCounter = 0
        }
    }

    protected var onPress: (IGButtonWidget) -> Unit = {}
        private set

    protected var longPress: (IGButtonWidget) -> Unit = {}
        private set

    protected var onRelease: (IGButtonWidget) -> Unit = {}
        private set

    override fun onPress() {
        onPress(this)
    }

    override fun onRelease() {
        onRelease(this)
    }

    fun longPress(time: Tick, action: (IGButtonWidget) -> Unit): IGButtonWidget {
        longPressTime = time
        longPress = action
        return this
    }

    fun press(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onPress = action
        return this
    }

    fun release(action: (IGButtonWidget) -> Unit): IGButtonWidget {
        onRelease = action
        return this
    }

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        enableBlend()
        enableDepthTest()

        context.batchRenderTextureColored {
            context.drawWidgetTexture(transform.asWorldBox, status(theme.disabled, theme.idle, theme.hovered, theme.pressed))
        }
        context.batchRenderBox {
            if (wasMouseOver)
                context.boxOutline(contentBox(true), Colors.RED)

        }
//        this.drawMessage(context, context.client.textRenderer, pressedOrDisabled.pick(Colors.BLACK, Colors.BLACK_BEAN).argb)
    }

    protected open fun drawMessage(context: DrawContext, textRenderer: TextRenderer, color: Int) {
        this.drawScrollableText(context, textRenderer, color)
    }

    protected open fun drawScrollableText(context: DrawContext, textRenderer: TextRenderer, color: Int) {
        val content = contentBox(true)
        val left: Int = content.left.toInt()
        val right: Int = content.right.toInt()
        drawScrollableText(context, textRenderer, Literal("测试中"), left, this.transform.worldY.toInt(), right, this.transform.bottom.toInt(), color)
    }

    //------------ Measurable ------------\\

    override var parentData: Any? = null

    override var constraints: Constraints = Constraints()

    override val orientation: Orientation = Orientation.Horizontal

    override val alignment: (Orientation) -> Alignment = PlanarAlignment::Center

    override val widget: IGWidget
        get() = this

}


fun GuiScope<out WidgetContainer>.button(modifier: Modifier? = null, content: GuiScope<IGButtonWidget>.() -> Unit) =
    owner.addWidgetChild(IGButtonWidget().apply {
        padding = Padding(horizontal = 6, vertical = 6)
        GuiScope.create(this).content()
        modifier?.foldIn(Unit) { _, e ->
            e.tryApplyModify(this)
        }
    })