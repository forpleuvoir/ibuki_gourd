//package moe.forpleuvoir.ibukigourd.gui.widget
//
//import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
//import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidget
//import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
//import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonThemes
//import moe.forpleuvoir.ibukigourd.util.NextAction
//import moe.forpleuvoir.nebula.common.color.ARGBColor
//import moe.forpleuvoir.nebula.common.color.Colors
//import moe.forpleuvoir.nebula.common.util.clamp
//import net.minecraft.client.gui.DrawContext
//import kotlin.math.abs
//
//open class Scroller(
//    length: Float,
//    thickness: Float = 10f,
//    var amountStep: () -> Float,
//    var totalAmount: () -> Float,
//    /**
//     * 滚动条所占总长度的百分比 Range(0f..1f)
//     */
//    var barLength: () -> Float,
//    private val Orientation: Orientation = Orientation.Vertical,
//    var color: () -> ARGBColor = { Colors.WHITE },
//    barColor: () -> ARGBColor = { Colors.WHITE },
//) : IGPressableWidget() {
//
//    val bar: Button = button(color = barColor, pressOffset = 0f, theme = ButtonThemes.ScrollerBar) {
//        fixed = true
//    }
//
//    init {
//
//        Orientation.peek(
//            {
//                transform.width = thickness
//                transform.height = length
//            }, {
//                transform.width = length
//                transform.height = thickness
//            }
//        )
//    }
//
//    var amount: Float
//        get() = (totalAmount() * progress).clamp(0f..totalAmount())
//        set(value) {
//            val fixedValue = value.clamp(0f..totalAmount())
//            val barPosition = scrollerLength * if (totalAmount() == 0f) 0f else fixedValue / totalAmount()
//            Orientation.peek(
//                {
//                    bar.transform.y = barPosition
//                }, {
//                    bar.transform.x = barPosition
//                })
//            amountReceiver?.invoke(fixedValue)
//        }
//
//    var amountReceiver: ((amount: Float) -> Unit)? = null
//
//    open val scrollerLength: Float
//        get() = Orientation.peek(
//            this.transform.height - (barLength() * this.transform.height),
//            this.transform.width - (barLength() * this.transform.width)
//        )
//
//    val barPositionRange: ClosedFloatingPointRange<Float> get() = 0f..scrollerLength
//
//    /**
//     * 进度 Range(0.0f..1.0f)
//     */
//    var progress: Float
//        set(value) {
//            val fixed = value.clamp(0f..1f)
//            amount = (totalAmount() * fixed).clamp(0f..totalAmount())
//        }
//        get() {
//            if (scrollerLength == 0f) return 1f
//            return (Orientation.peek({ bar.transform.y }, { bar.transform.x }) / scrollerLength).clamp(0f..1f)
//        }
//
//
//    private fun calcBarLength() {
//        Orientation.peek(
//            {
//                bar.transform.height = barLength() * this.transform.height
//            }, {
//                bar.transform.width = barLength() * this.transform.width
//            }
//        )
//    }
//
//    override fun onPress() {
//        TODO("Not yet implemented")
//    }
//
//    override fun tick() {
//        super.tick()
//        if (pressed && mouseHover() && visible) {
//            setFromMouse(screen().mousePosition.x, screen().mousePosition.y)
//        }
//    }
//
//    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
//        TODO("Not yet implemented")
//    }
//
//    protected open fun setFromMouse(mouseX: Float, mouseY: Float) {
//        if (bar.mouseHover()) return
//        Orientation.peek(
//            {
//                val a = mouseY - bar.transform.worldY
//                if (a > 0) {
//                    bar.transform.y = (bar.transform.y + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
//                } else {
//                    bar.transform.y = (bar.transform.y - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
//                }
//            },
//            {
//                val a = mouseX - bar.transform.worldX
//                if (a > 0) {
//                    bar.transform.x = (bar.transform.x + amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
//                } else {
//                    bar.transform.x = (bar.transform.x - amountStep().coerceAtMost(abs(a))).clamp(barPositionRange)
//                }
//            }
//        )
//        amountReceiver?.invoke(amount)
//    }
//
//    override fun onMouseDragging(event: MouseDragEvent): NextAction {
//        if (!active || !dragging || !visible || !bar.dragging) return NextAction.Continue
//        Orientation.peek(
//            {
//                bar.transform.y = (bar.transform.y + deltaY).clamp(barPositionRange)
//            }, {
//                bar.transform.x = (bar.transform.x + deltaX).clamp(barPositionRange)
//            }
//        )
//        amountReceiver?.invoke(amount)
//        return super.onMouseDragging()
//    }
//
//    override fun onMouseClick(event: MousePressEvent): NextAction {
//        if (!visible) return NextAction.Continue
//        mouseHover {
//            setFromMouse(mouseX, mouseY)
//        }
//        return super.onMouseClick()
//    }
//
//    override fun onMouseScrolling(event: MouseScrollEvent): NextAction {
//        mouseHover {
//            this@Scroller.amount -= amountStep() * amount
//        }
//        return super.onMouseScrolling()
//    }
//
//
//    override fun onRenderBackground(renderContext: RenderContext) {
//        calcBarLength()
//        renderTexture(renderContext.matrixStack, this.transform, SCROLLER_BACKGROUND, color())
//    }
//
//}
