package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
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
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors

class DropMenuWidget : ExpandableWidgetContainer(), RowLayout {

    //------------ Override ------------\\

    override var spacing: Float = 0f

    override val arrangement: Arrangement = Arrangement.SpaceBetween

    private var _layerRecord: GuiLayer = layer

    override fun onExpand() {
        expandedContent.active = true
        expandedContent.visible = true
        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_DOWN
        _layerRecord = layer
        layer = GuiLayer.pop
    }

    override fun onClose() {
        expandedContent.active = false
        expandedContent.visible = false
        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_UP
        layer = _layerRecord
    }


    //------------ Render ------------\\

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            val texture = expandState.pick(WidgetTextures.DROP_MENU_EXPEND_BACKGROUND, WidgetTextures.DROP_MENU_BACKGROUND)
            val box = expandState.pick({
                transform.asWorldBox.copy(height = transform.height + expandedContent.transform.height + _list.spacing)
            }, {
                //渲染分割线


                transform.asWorldBox
            })
            context.drawWidgetTexture(box(), texture)
        }
    }


    //------------ Layout ------------\\

    internal val scope: DropMenuScope = DropMenuScope(this)

    override fun layoutableChildren(): List<Layoutable> =
        widgetChildren().filter { it != expandedContent }

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        super.measureChildren(measurables, constraints)
        val c = constraints.constraintAs(this.constraints)
        expandedContent.measure(Constraints.of(0f, c.maxWidth, 0f, mc.window.scaledHeight.toFloat()))
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
                mc.window.scaledHeight.toFloat() - transform.worldBottom - _list.spacing
            )
        )
        expandedContent.layout()
        expandedContent.placeAt(padding.left, head.transform.bottom + _list.spacing)
    }

    //------------ DropMenu ------------\\

    private lateinit var head: ColumnWidget

    fun init() {
        head = scope.column(Arrangement.SpaceBetween, Modifier.renderBackground { ctx, _, _, _ ->
            this as IGWidget
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    ctx.boxOutline(transform.asWorldBox, Colors.AQUA)
                }
                ctx.box(transform.asWorldBox, Colors.AQUA.alpha(.25f))
            }
        }) {
            _content = box(scope._contentModifier(this), scope._content)
            _arrow = icon(WidgetTextures.DROP_MENU_ARROW_DOWN)
        }
        expandedContent = scope.run {
            column(
                arrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(0)
                    .mousePress {
                        this as IGWidget
                        this.onMousePress(it)
                        it.tryUse { wasDragging }
                        it.tryUse { !wasMouseOver }.onSuccess { this@DropMenuWidget.toggle() }
                    }
                    .renderBackground { ctx, _, _, _ ->
                        this as IGWidget
                        ctx.batchRenderBox {
                            if (wasMouseOver) {
                                ctx.boxOutline(transform.asWorldBox, Colors.AQUA)
                            }
                            ctx.box(transform.asWorldBox, Colors.AQUA.alpha(.25f))
                        }
                    }
            ) {
                layer(GuiLayer.pop)
                active(expandState.isExpanded)
                visible(expandState.isExpanded)
                _list = list(
                    orientation = Orientation.Vertical,
                    spacing = 2f,
                    content = scope._itemsContent,
                    modifier = Modifier
                        .fill()
                        .mouseScrolling {
                            this as ListWidget
                            if (this.wasMouseOver)
                                _scroller.scroller(it.verticalAmount)
                        } then scope._itemsModifier(this)
                )
                _scroller = scroller(
                    amountStep = { _list.widgetChildren().minOf { it.transform.height } / 2f },
                    totalAmount = { _list.totalAmount },
                    barProportion = { (_list.contentHeight / _list.totalContentSize).coerceIn(0f..1f) },
                    amountConsumer = {
                        _list.amounts = it
                    },
                    orientation = Orientation.Vertical,
                    modifier = Modifier
                        .width(9f)
                        .margin(left = 1f, top = 2f, right = -1f)
                )
            }
        }
    }

    private lateinit var _content: BoxWidget

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

        internal lateinit var _itemsModifier: LinearLayoutScope. () -> Modifier

        internal lateinit var _itemsContent: ListWidgetScope.() -> Unit

        fun items(modifier: LinearLayoutScope. () -> Modifier = { Modifier }, content: ListWidgetScope.() -> Unit) {
            _itemsModifier = modifier
            _itemsContent = content
        }

        internal lateinit var _contentModifier: LinearLayoutScope. () -> Modifier

        internal lateinit var _content: BoxScope.() -> Unit

        fun content(modifier: LinearLayoutScope. () -> Modifier = { Modifier }, content: BoxScope.() -> Unit) {
            _contentModifier = modifier
            _content = content
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
