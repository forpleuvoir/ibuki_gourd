package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.active
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.visible
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.ExpandableWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

class DropMenuWidget : ExpandableWidgetContainer(), RowLayout {

    //------------ Override ------------\\

    override var spacing: Float = 0f

    override val arrangement: Arrangement = Arrangement.SpaceBetween

    private var _layerRecord: GuiLayer = layer

    override fun onExpand() {
        expandedContent.active = true
        expandedContent.visible = true
        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_UP
        _layerRecord = layer
        layer = GuiLayer.pop
    }

    override fun onClose() {
        expandedContent.active = false
        expandedContent.visible = false
        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_DOWN
        layer = _layerRecord
    }


    //------------ Render ------------\\

    private var separatorColor: ARGBColor = Color(0xFFA0A0A0)

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            val texture = expandState.pick(WidgetTextures.DROP_MENU_EXPEND_BACKGROUND, WidgetTextures.DROP_MENU_BACKGROUND)
            val box =
                expandState.pick(transform.asWorldBox.copy(height = transform.height + expandedContent.transform.height + 1f), transform.asWorldBox)
            pushWidgetTexture(box, texture)

            expandState.isClosed {
                transform.asWorldBox.copy(
                    _arrow.transform.worldLeft - 3f, width = 1f, height = transform.height
                ).let {
                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_VERTICAL, separatorColor)
                }
            }

            expandState.isExpanded {
                //渲染分割线
                transform.asWorldBox.copy(
                    _arrow.transform.worldLeft - 3f, width = 1f, height = transform.height + expandedContent.transform.height + 1f
                ).let {
                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_VERTICAL, separatorColor)
                }
                transform.asWorldBox.copy(y = expandedContent.transform.worldTop - 1f, height = 1f).let {
                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_HORIZONTAL, separatorColor)
                }
            }
        }
    }

    //------------ Layout ------------\\

    internal val scope: DropMenuScope = DropMenuScope(this)

    override fun layoutableChildren(): List<Layoutable> =
        widgetChildren().filter { it != expandedContent }

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        super.measureChildren(measurables, constraints)
        val c = constraints.constraintAs(this.constraints)
        _scroller.constraints = _scroller.constraints.copy(minWidth = _arrow.transform.width + 3f, maxHeight = 0f)
        expandedContent.measure(Constraints.of(0f, c.maxWidth, 0f, mc.window.scaledHeight.toFloat()))
        _scroller.constraints = _scroller.constraints.copy(maxHeight = _list.transform.height)
        if (transform.width < expandedContent.wrappedWidth) {
            head.constraints = head.constraints.copy(
                minWidth = expandedContent.wrappedWidth,
                maxWidth = expandedContent.wrappedWidth
            )
            super.measureChildren(
                measurables,
                constraints.copy(
                    minWidth = expandedContent.wrappedWidth + padding.width,
                    maxWidth = expandedContent.wrappedWidth + padding.width + padding.width
                )
            )
        }
        return this
    }

    override fun layout(layoutables: List<Layoutable>) {
        super<RowLayout>.layout(layoutables)
        expandedContent.measure(
            Constraints.of(
                expandedContent.transform.width,
                expandedContent.transform.width,
                0f,
                mc.window.scaledHeight.toFloat() - transform.worldBottom
            )
        )
        expandedContent.layout()
        expandedContent.placeAt(padding.left, transform.height - padding.bottom / 2f + 1f)
    }

    //------------ DropMenu ------------\\

    fun init() {
        clearWidgetChildren()
        head = scope.column(Arrangement.SpaceBetween) {
            _content = column(modifier = Modifier then (scope.contentModifier(this)), content = scope.content)
            _arrow = icon(
                expandState.pick(WidgetTextures.DROP_MENU_ARROW_UP, WidgetTextures.DROP_MENU_ARROW_DOWN),
                modifier = Modifier.padding(horizontal = 1f)
            )
        }
        expandedContent = scope.run {
            column(
                arrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(vertical = 1f)
                    .mousePress {
                        this as ColumnWidget
                        this.onMousePress(it)
//                        it.tryUse { wasDragging }
                        it.tryUse { !wasMouseOver }.onSuccess { this@DropMenuWidget.toggle() }
                    }.renderHoveredOutlineBox(Colors.AQUA)
                    .mouseScrolling { event ->
                        this as IGWidget
                        onMouseScrolling(event)
                        event.tryUse(wasMouseOver).onSuccess { _scroller.scroller(event.verticalAmount) }
                    }
            ) {
                layer(GuiLayer.pop)
                active(expandState.isExpanded)
                visible(expandState.isExpanded)
                val spacing = 3f
                _list = list(
                    orientation = Orientation.Vertical,
                    spacing = spacing,
                    modifier = Modifier
                        .gravityStart()
                        .renderHoveredOutlineBox(Colors.ROSE)
                        .renderOverlay { ctx, _, _, _ ->
                            this as ListWidget
                            ctx.scissor(this@DropMenuWidget.transform.asWorldBox.copy(height = this@DropMenuWidget.transform.height + expandedContent.transform.height - 4f)) {
                                ctx.batchRenderTextureColored {
                                    widgetChildren().filter { it.visible }.forEach { child ->
                                        this@DropMenuWidget.transform.asWorldBox.copy(
                                            y = child.transform.worldBottom + spacing / 2 - .5f,
                                            height = 1f,
                                            width = this@DropMenuWidget.transform.width - _arrow.wrappedWidth - this@DropMenuWidget.padding.right
                                        ).let {
                                            pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_HORIZONTAL, separatorColor)
                                        }
                                    }
                                }
                            }
                        }
                        .mouseScrolling { event ->
                            this as ListWidget
                            onMouseScrolling(event)
                            event.tryUse(wasMouseOver).onSuccess { _scroller.scroller(event.verticalAmount) }
                        } then scope.itemsModifier(this),
                ) {
                    scope.itemsContent(this)
                }
                box(modifier = Modifier.width(5f))
                _scroller = scroller(
                    amountStep = { _list.widgetChildren().minOf { it.transform.height } / 2f },
                    totalAmount = { _list.totalAmount },
                    barProportion = { (_list.contentHeight / _list.totalContentSize).coerceIn(0f..1f) },
                    amountConsumer = {
                        _list.amounts = it
                    },
                    orientation = Orientation.Vertical,
                    modifier = Modifier
                        .gravityStart()
                        .margin(right = -1.5f)
                        .render { ctx, x, y, delta ->
                            this as ScrollerWidget
                            if (totalAmount() > 0f) onRender(ctx, x, y, delta)
                        }
                        .renderBackground { ctx, x, y, delta ->
                            this as ScrollerWidget
                            if (totalAmount() > 0f) onRenderBackground(ctx, x, y, delta)
                        }
                )
            }
        }
    }

    private lateinit var head: ColumnWidget

    private lateinit var _content: ColumnWidget

    private lateinit var _arrow: IconWidget

    private lateinit var expandedContent: WidgetContainerImpl

    private lateinit var _list: ListWidget

    private lateinit var _scroller: ScrollerWidget

    companion object {}

    data class DropMenuScope(
        private val dropMenu: DropMenuWidget,
    ) : GuiScope<DropMenuWidget>, LinearLayoutScope {

        override val linearLayout: LinearLayout = dropMenu

        override fun owner(): DropMenuWidget = dropMenu

        internal lateinit var itemsModifier: LinearLayoutScope. () -> Modifier

        internal lateinit var itemsContent: ListWidgetScope.() -> Unit

        fun items(modifier: LinearLayoutScope. () -> Modifier = { Modifier }, content: ListWidgetScope.() -> Unit) {
            itemsModifier = modifier
            itemsContent = content
        }

        internal lateinit var contentModifier: LinearLayoutScope. () -> Modifier

        internal lateinit var content: ColumnScope.() -> Unit

        fun content(modifier: LinearLayoutScope. () -> Modifier = { Modifier }, content: ColumnScope.() -> Unit) {
            contentModifier = modifier
            this.content = content
        }

        fun toggle() {
            dropMenu.toggle()
        }

        fun refresh() {
            dropMenu.init()
            dropMenu.screen()?.remeasure()
        }

        fun separatorColor(separatorColor: ARGBColor) {
            owner().separatorColor = separatorColor
        }

    }


}

typealias DropMenuScope = DropMenuWidget.DropMenuScope


fun GuiScope<out WidgetContainer>.dropMenu(
    modifier: Modifier = Modifier,
    scope: DropMenuScope.() -> Unit,
) = addWidgetChild(DropMenuWidget()) {
    this.scope.scope()
    init()
    Modifier.padding(4).then(modifier).foldInApply()
}
