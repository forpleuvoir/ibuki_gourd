package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.Drawable
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.util.NextAction

@Suppress("unused", "KDocUnresolvedReference")
interface Element : ElementContainer, Drawable, ModifiableUserInteractionHandler {

    companion object {
        val EMPTY: Element = object : Element {
            override val transform: Transform = Transform()
            override val dimension: ElementDimension = ElementDimension(0f, 0f)
            override var widthWeight: Float = 0f
            override var heightWeight: Float = 0f
            override var visible: Boolean = false
            override var parent: () -> Element = { this }
            override val screen: () -> Screen = { Screen.EMPTY }
            override var active: Boolean = false
            override val focused: Boolean = false
            override val onFocusedChanged: ((Boolean) -> Unit) = { }
            override val focusable: Boolean = false
            override var priority: Int = 0
            override var fixed: Boolean = false
            override var tip: Tip? = null
            override var tick: () -> Unit = {}
            override var render: (renderContext: RenderContext) -> Unit = {}

            override fun onRender(renderContext: RenderContext) = Unit

            override var renderBackground: (renderContext: RenderContext) -> Unit = {}

            override fun onRenderBackground(renderContext: RenderContext) = Unit

            override var renderOverlay: (renderContext: RenderContext) -> Unit = {}

            override fun onRenderOverlay(renderContext: RenderContext) = Unit
            override var mouseMoveIn: (mouseX: Float, mouseY: Float) -> Unit = { _: Float, _: Float -> }

            override fun onMouseMoveIn(mouseX: Float, mouseY: Float) = Unit

            override var mouseMoveOut: (mouseX: Float, mouseY: Float) -> Unit = { _: Float, _: Float -> }

            override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit

            override var mouseMove: (Float, Float) -> NextAction = { _: Float, _: Float -> NextAction.Cancel }

            override var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction = { _: Float, _: Float, _: Mouse -> NextAction.Cancel }

            override var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction = { _: Float, _: Float, _: Mouse -> NextAction.Cancel }

            override var dragging: Boolean = false

            override var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> NextAction =
                { _: Float, _: Float, _: Mouse, _: Float, _: Float -> NextAction.Cancel }

            override var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> NextAction = { _: Float, _: Float, _: Float -> NextAction.Cancel }

            override var keyPress: (keyCode: KeyCode) -> NextAction = { NextAction.Cancel }
            override var keyRelease: (keyCode: KeyCode) -> NextAction = { NextAction.Cancel }

            override var charTyped: (chr: Char) -> NextAction = { NextAction.Cancel }
            override var init: () -> Unit = {}
            override var spacing: Float = 0f
            override var remainingWidth: Float = 0f
            override var remainingHeight: Float = 0f


            override val elements: List<Element> = emptyList()
            override val renderElements: List<Element> = emptyList()
            override val fixedElements: List<Element> = emptyList()
            override val handleElements: List<Element> = emptyList()

            override fun <T : Element> addElement(element: T): T = element

            override fun preElement(element: Element): Element? = null

            override fun nextElement(element: Element): Element? = null

            override fun elementIndexOf(element: Element): Int = -1

            override fun removeElement(element: Element): Boolean = false

            override fun removeElement(index: Int) = Unit
            override fun clearElements(predicate: (Element) -> Boolean) = Unit

            override val margin: Margin = Margin()
            override val padding: Padding = Padding()

            override fun margin(margin: Number) = Unit

            override fun margin(margin: Margin) = Unit

            override fun margin(left: Number, right: Number, top: Number, bottom: Number) = Unit

            override fun padding(padding: Number) = Unit

            override fun padding(padding: Padding) = Unit

            override fun padding(left: Number, right: Number, top: Number, bottom: Number) = Unit

            override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> = Rectangle.NULL

            override fun init() = Unit

            override fun tick() = Unit
        }
    }

    /**
     * 基础属性变换
     */
    override val transform: Transform

    /**
     *  基础属性尺寸,
     *  只是用于提供给transform长度和宽度
     *  重新排列元素时
     */
    val dimension: ElementDimension

    var widthWeight: Float

    var heightWeight: Float

    override var visible: Boolean

    /**
     * 父元素
     */
    var parent: () -> Element

    /**
     *  当前元素所在的Screen
     */
    val screen: () -> Screen

    /**
     * 是否为激活的元素
     */
    var active: Boolean

    /**
     * 是否为聚焦中的元素
     */
    val focused: Boolean

    val onFocusedChanged: ((Boolean) -> Unit)?

    /**
     * 是否为可聚焦元素
     */
    val focusable: Boolean

    /**
     * 处理优先级 越高越优先处理
     */
    var priority: Int

    /**
     * 固定元素，不会受到布局排列方法 [moe.forpleuvoir.ibukigourd.gui.base.layout.Layout.layout] 的位置调整
     */
    var fixed: Boolean

    var tip: Tip?

    /**
     * 渲染优先级 越高渲染层级越高
     */
    override val renderPriority: Int
        get() = priority

    fun fixed(x: Float, y: Float) {
        fixed = true
        transform.x = x
        transform.y = y
    }

    /**
     * 渲染元素
     * @param matrixStack MatrixStack
     * @param delta Float 距离上一帧数渲染时间
     */
    override var render: (renderContext: RenderContext) -> Unit

    /**
     * 渲染元素
     * @param renderContext MatrixStack
     * @param delta Float 距离上一帧数渲染时间
     */
    override fun onRender(renderContext: RenderContext)

    /**
     * 渲染背景
     * @param matrixStack MatrixStack
     * @param delta Float
     */
    var renderBackground: (renderContext: RenderContext) -> Unit

    /**
     * 渲染背景
     * @param matrixStack MatrixStack
     * @param renderContext Float
     */
    fun onRenderBackground(renderContext: RenderContext)

    /**
     * 渲染覆盖层
     * @param matrixStack MatrixStack
     * @param delta Float
     */
    var renderOverlay: (renderContext: RenderContext) -> Unit

    /**
     * 渲染覆盖层
     * @param renderContext MatrixStack
     * @param delta Float
     */
    fun onRenderOverlay(renderContext: RenderContext)

    /**
     * 鼠标移动
     * @param mouseX Float
     * @param mouseY Float
     */
    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction = NextAction.Cancel

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Cancel

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Cancel

    /**
     * 鼠标是否为拖拽中
     */
    var dragging: Boolean

    /**
     * 鼠标拖动
     * @param mouseX Float
     * @param mouseY Float
     * @param button Mouse
     * @param deltaX Float
     * @param deltaY Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction =
        NextAction.Cancel

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction = NextAction.Cancel

    /**
     * 按键按下
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    override fun onKeyPress(keyCode: KeyCode): NextAction = NextAction.Cancel

    /**
     * 按键释放
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    override fun onKeyRelease(keyCode: KeyCode): NextAction = NextAction.Cancel

    /**
     * 字符输入
     * @param chr Char
     * @return 是否处理之后的同类操作
     */
    override fun onCharTyped(chr: Char): NextAction = NextAction.Cancel
}