package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.render.Drawable
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse

@Suppress("unused", "KDocUnresolvedReference")
interface Element : ElementContainer, Drawable, ModifiableUserInteractionHandler {

    companion object : Element {
        override val modifier: Modifier = Modifier
        override val transform: Transform = Transform()
        override var visible: Boolean = false
        override val layoutData: Map<Any, Any> = emptyMap()
        override var parent: () -> Element = { this }
        override val screen: () -> Screen = { Screen.EMPTY }
        override var active: Boolean = false
        override val focused: Boolean = false
        override val onFocusedChanged: ((Boolean) -> Unit) = {}
        override val focusable: Boolean = false
        override var fixed: Boolean = false
        override var tip: Tip? = null
        override var render: (renderContext: RenderContext) -> Unit = { }
        override fun onRender(renderContext: RenderContext) = Unit
        override var renderBackground: (renderContext: RenderContext) -> Unit = {}
        override fun onRenderBackground(renderContext: RenderContext) = Unit
        override var renderOverlay: (renderContext: RenderContext) -> Unit = {}
        override fun onRenderOverlay(renderContext: RenderContext) = Unit
        override var dragging: Boolean = false
        override var init: () -> Unit = {}
        override var width: ElementDimension = 0.fixed
        override var height: ElementDimension = 0.fixed
        override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension = elementMeasureDimension
        override fun layout() = Unit
        override val elements: List<Element> = emptyList()
        override val renderElements: List<Element> = elements
        override val fixedElements: List<Element> = elements
        override val handleElements: List<Element> = elements
        override fun <T : Element> addElement(element: T): T = element
        override fun preElement(element: Element): Element? = null
        override fun nextElement(element: Element): Element? = null
        override fun elementIndexOf(element: Element): Int = -1
        override fun removeElement(element: Element): Boolean = false
        override fun removeElement(index: Int) = Unit
        override fun clearElements(predicate: (Element) -> Boolean) = Unit
        override var margin: Margin = Margin()
        override var padding: Padding = Padding()
        override fun contentBox(isWorld: Boolean): Box = Box.NULL
        override fun init() = Unit
        override var tick: () -> Unit = {}
        override var mouseMoveIn: (mouseX: Float, mouseY: Float) -> Unit = { _, _ -> }
        override var mouseMoveOut: (mouseX: Float, mouseY: Float) -> Unit = { _, _ -> }
        override var mouseMove: (mouseX: Float, mouseY: Float) -> Unit = { _, _ -> }
        override var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit = { _, _, _ -> }
        override var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit = { _, _, _ -> }
        override var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> Unit = { _, _, _, _, _ -> }
        override var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> Unit = { _, _, _ -> }
        override var keyPress: (keyCode: KeyCode) -> Unit = {}
        override var keyRelease: (keyCode: KeyCode) -> Unit = {}
        override var charTyped: (chr: Char) -> Unit = {}
        override fun onMouseMoveIn(mouseX: Float, mouseY: Float) = Unit
        override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit
        override fun tick() = Unit
    }

    val modifier: Modifier

    /**
     * 基础属性变换
     */
    override val transform: Transform

    override var visible: Boolean

    val layoutData: Map<Any, Any>

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
     * 固定元素，不会受到布局排列方法 [moe.forpleuvoir.ibukigourd.gui.base.layout.Layout.layout] 的位置调整
     */
    var fixed: Boolean

    var tip: Tip?

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
    override fun onMouseMove(mouseX: Float, mouseY: Float) {}

    /**
     * 鼠标点击
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse) {}

    /**
     * 鼠标释放
     * @param button Mouse
     * @param mouseX Float
     * @param mouseY Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse) {}

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
    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {}

    /**
     * 鼠标滚动
     * @param mouseX Float
     * @param mouseY Float
     * @param amount Float
     * @return 是否处理之后的同类操作
     */
    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float) {}

    /**
     * 按键按下
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    override fun onKeyPress(keyCode: KeyCode) {}

    /**
     * 按键释放
     * @param keyCode KeyCode
     * @return 是否处理之后的同类操作
     */
    override fun onKeyRelease(keyCode: KeyCode) {}

    /**
     * 字符输入
     * @param chr Char
     * @return 是否处理之后的同类操作
     */
    override fun onCharTyped(chr: Char) {}
}