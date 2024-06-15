package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.render.Drawable
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import kotlin.reflect.KClass

@moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
@Suppress("unused", "KDocUnresolvedReference")
interface Element : ElementContainer, Drawable, ModifiableUserInteractionHandler {

    companion object : Element {
        override val modifier: Modifier = Modifier
        override val transform: moe.forpleuvoir.ibukigourd.gui.base.Transform = moe.forpleuvoir.ibukigourd.gui.base.Transform()
        override var layer: Layer = DefaultLayer
        override var visible: Boolean = false
        override var parent: () -> Element = { this }
        override val screen: () -> Screen = { Screen.EMPTY }
        override var layout: Layout = object : Layout {
            override fun Element.layout() {}
            override fun Element.measureWidth(measureSpec: MeasureSpec): Float = 0f
            override fun Element.measureHeight(measureSpec: MeasureSpec): Float = 0f
        }
        override val layoutData: Map<KClass<out LayoutData>, LayoutData> = emptyMap()
        override var active: Boolean = false
        override var wasFocused: Boolean = false
        override val onFocusedChanged: ((Boolean) -> Unit) = {}
        override val focusable: Boolean = false
        override var fixed: Boolean = false
        override var wasMouseOver: Boolean = false
        override var tip: Tip? = null
        override fun setMeasureWidth(width: Float) = Unit
        override fun setMeasureHeight(height: Float) = Unit
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
        override fun onLayout() = Unit
        override fun onMeasureWidth(measureSpec: MeasureSpec): Float = 0f
        override fun onMeasureHeight(measureSpec: MeasureSpec): Float = 0f
        override val elements: List<Element> = emptyList()
        override val layoutElements: List<Element> = elements
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
        override var margin: moe.forpleuvoir.ibukigourd.gui.base.Margin = moe.forpleuvoir.ibukigourd.gui.base.Margin()
        override var padding: moe.forpleuvoir.ibukigourd.gui.base.Padding = moe.forpleuvoir.ibukigourd.gui.base.Padding()
        override fun contentBox(isWorld: Boolean): Box = Box.NULL
        override fun init() = Unit
        override var tick: () -> Unit = {}
        override var mouseEnter: (event: MouseEnterEvent) -> Unit = {}
        override var mouseLeave: (event: MouseLeaveEvent) -> Unit = {}
        override var mouseMove: (event: MouseMoveEvent) -> Unit = {}
        override var mouseClick: (event: MousePressEvent) -> Unit = {}
        override var focused: (event: FocusedEvent) -> Unit = {}
        override var mouseRelease: (event: MouseReleaseEvent) -> Unit = {}
        override var mouseDragging: (event: MouseDragEvent) -> Unit = {}
        override var mouseScrolling: (event: MouseScrollEvent) -> Unit = {}
        override var keyPress: (event: KeyPressEvent) -> Unit = {}
        override var keyRelease: (event: KeyReleaseEvent) -> Unit = {}
        override var charTyped: (event: CharTypedEvent) -> Unit = {}
        override fun onMouseEnter(event: MouseEnterEvent) = Unit
        override fun onMouseLeave(event: MouseLeaveEvent) = Unit
        override fun tick() = Unit
    }

    val modifier: Modifier

    /**
     * 基础属性变换
     */
    override val transform: moe.forpleuvoir.ibukigourd.gui.base.Transform

    val depth: Int get() = transform.depth

    var layer: Layer

    val layoutData: Map<KClass<out LayoutData>, LayoutData>

    override var visible: Boolean

    /**
     * 父元素
     */
    var parent: () -> Element

    /**
     *  当前元素所在的Screen
     */
    val screen: () -> Screen

    val layout: Layout

    /**
     * 是否为激活的元素
     */
    var active: Boolean

    /**
     * 是否为聚焦中的元素
     */
    var wasFocused: Boolean

    val onFocusedChanged: ((Boolean) -> Unit)?

    /**
     * 是否为可聚焦元素
     */
    val focusable: Boolean

    /**
     * 固定元素，不会受到布局排列方法 [moe.forpleuvoir.ibukigourd.gui.base.layout.Layout.layout] 的位置调整
     */
    var fixed: Boolean

    /**
     * 鼠标是否在此元素上
     */
    var wasMouseOver: Boolean

    var tip: Tip?

    fun fixed(x: Float, y: Float) {
        fixed = true
        transform.x = x
        transform.y = y
    }

    /**
     * 对元素宽度进行测量
     * @return 元素测量后的宽度加上[Margin.width]
     */
    fun onMeasureWidth(measureSpec: MeasureSpec): Float

    /**
     * 对元素高度进行测量
     * @return 元素测量后的高度加上[Margin.height]
     */
    fun onMeasureHeight(measureSpec: MeasureSpec): Float

    fun setMeasureWidth(width: Float)

    fun setMeasureHeight(height: Float)

    var tick: () -> Unit

    /**
     * 渲染元素
     * @param renderContext [RenderContext]
     */
    override var render: (renderContext: RenderContext) -> Unit

    /**
     * 渲染元素
     * @param renderContext [RenderContext]
     */
    override fun onRender(renderContext: RenderContext)

    /**
     * 渲染背景
     * @param renderContext [RenderContext]
     */
    var renderBackground: (renderContext: RenderContext) -> Unit

    /**
     * 渲染背景
     * @param renderContext [RenderContext]
     */
    fun onRenderBackground(renderContext: RenderContext)

    /**
     * 渲染覆盖层
     * @param renderContext [RenderContext]
     */
    var renderOverlay: (renderContext: RenderContext) -> Unit

    /**
     * 渲染覆盖层
     * @param renderContext [RenderContext]
     */
    fun onRenderOverlay(renderContext: RenderContext)

    /**
     * 鼠标进入
     * @param event MouseEnterEvent
     */
    override fun onMouseEnter(event: MouseEnterEvent) {}

    /**
     * 鼠标离开
     * @param event MouseLeaveEvent
     */
    override fun onMouseLeave(event: MouseLeaveEvent) {}

    /**
     * 鼠标移动
     * @param event MouseMoveEvent
     */
    override fun onMouseMove(event: MouseMoveEvent) {}

    /**
     * 鼠标点击
     * @param event MousePressEvent
     */
    override fun onMouseClick(event: MousePressEvent) {}

    /**
     * 获得焦点
     * @param event FocusedEvent
     */
    override fun onFocused(event: FocusedEvent) {}

    /**
     * 鼠标释放
     * @param event MouseReleaseEvent
     */
    override fun onMouseRelease(event: MouseReleaseEvent) {}

    /**
     * 鼠标是否为拖拽中
     */
    var dragging: Boolean

    /**
     * 鼠标拖动
     * @param event MouseDragEvent
     */
    override fun onMouseDragging(event: MouseDragEvent) {}

    /**
     * 鼠标滚动
     * @param event MouseScrollEvent
     */
    override fun onMouseScrolling(event: MouseScrollEvent) {}

    /**
     * 按键按下
     * @param event KeyPressEvent
     */
    override fun onKeyPress(event: KeyPressEvent) {}

    /**
     * 按键释放
     * @param event KeyReleaseEvent
     */
    override fun onKeyRelease(event: KeyReleaseEvent) {}

    /**
     * 字符输入
     * @param event CharTypedEvent
     */
    override fun onCharTyped(event: CharTypedEvent) {}


    /**
     * 下面为扩展函数
     */

    /**
     * Executes the use function on the current GUIEvent instance with the given Element.
     * @receiver GUIEvent The current GUIEvent instance.
     * @param element The Element to use.
     */
    fun GUIEvent.use() {
        this.use(this@Element)
    }

    /**
     * Tries to use the current GUIEvent instance with the given block of code.
     * If the GUIEvent can be used and the block returns true, the 'use' function is executed on the element, and true is returned.
     * If the GUIEvent cannot be used or the block returns false, false is returned.
     * @receiver GUIEvent The current GUIEvent instance.
     * @param block The block of code to be executed.
     * @return Result<Boolean> Success(true) if the event was used, Failure(false) otherwise.
     */
    fun GUIEvent.tryUse(block: () -> Boolean = { true }): Result<Boolean> {
        if (canUse(this@Element)) {
            if (block()) {
                this.use(this@Element)
                return Result.success(true)
            }
            return Result.failure(Exception("Block returned false."))
        }
        return Result.failure(Exception("Event cannot be used."))
    }

    /**
     * Determines if the GUIEvent instance can be used with the given Element.
     *
     * @return true if the GUIEvent can be used with the Element, false otherwise.
     */
    fun GUIEvent.canUse(): Boolean {
        return this.canUse(this@Element)
    }

    fun GUIEvent.canUse(block: () -> Unit) {
        this.canUse(this@Element, block)
    }

    fun GUIEvent.cantUse(): Boolean {
        return this.cantUse(this@Element)
    }

    fun GUIEvent.cantUse(block: () -> Unit) {
        this.canUse(this@Element, block)
    }

    fun RenderContext.canRender(): Boolean {
        return this.canRender(this@Element)
    }

    fun RenderContext.tryRender(block: RenderContext.() -> Unit) {
        this.tryRender(this@Element, block)
    }

}