//package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu
//
//import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
//import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
//import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.scissor
//import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
//import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
//import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
//import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
//import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
//import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
//import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
//import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
//import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
//import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
//import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
//import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
//import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnLayoutScope
//import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
//import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.active
//import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
//import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.layer
//import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.visible
//import moe.forpleuvoir.ibukigourd.gui.base.scope.RowLayoutScope
//import moe.forpleuvoir.ibukigourd.gui.base.widget.ExpandableWidgetContainer
//import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
//import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
//import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
//import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
//import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
//import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
//import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
//import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconWidget
//import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
//import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
//import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnWidget
//import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ListWidget
//import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowList
//import moe.forpleuvoir.ibukigourd.task.scheduleStartTick
//import moe.forpleuvoir.ibukigourd.util.mc
//import moe.forpleuvoir.nebula.common.color.ARGBColor
//import moe.forpleuvoir.nebula.common.color.Color
//import moe.forpleuvoir.nebula.common.color.Colors
//
//class DropMenuWidget : ExpandableWidgetContainer(), RowLayout {
//
//    //------------ Override ------------\\
//
//    override val arrangement: Arrangement.Vertical = Arrangement.SpaceBetween
//
//    override val alignment: Alignment.Horizontal = Alignment.CenterHorizontally
//
//    private var _layerRecord: GuiLayer = layer
//
//    override fun onExpand() {
//        expandedContent.active = true
//        expandedContent.visible = true
//        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_UP
//        _layerRecord = layer
//        layer = GuiLayer.pop
//    }
//
//    override fun onClose() {
//        expandedContent.active = false
//        expandedContent.visible = false
//        _arrow.iconTexture = WidgetTextures.DROP_MENU_ARROW_DOWN
//        layer = _layerRecord
//    }
//
//    override fun hoveredWidget(layer: GuiLayer): IGWidget? {
//        for (child in widgetChildren()) {
//            if (child is WidgetContainer && child.wasMouseOver) {
//                child.hoveredWidget(layer)?.let { return it }
//                return child.takeIf { it.layer == layer }
//            }
//            if (child.wasMouseOver && child.layer == layer) {
//                return child
//            }
//        }
//        return null
//    }
//
//    //------------ Render ------------\\
//
//    private var separatorColor: ARGBColor = Color(0xFFCCCCCC)
//
//    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
//        context.batchRenderTextureColored {
//            val texture = expandState.pick(WidgetTextures.DROP_MENU_EXPEND_BACKGROUND, WidgetTextures.DROP_MENU_BACKGROUND)
//            val box =
//                expandState.pick(transform.asWorldBox.copy(height = transform.height + expandedContent.transform.height + 1f), transform.asWorldBox)
//            pushWidgetTexture(box, texture)
//
//            expandState.isClosed {
//                transform.asWorldBox.copy(
//                    _arrow.transform.worldLeft - 3f, width = 1f, height = transform.height
//                ).let {
//                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_VERTICAL, separatorColor)
//                }
//            }
//
//            expandState.isExpanded {
//                //渲染分割线
//                transform.asWorldBox.copy(
//                    _arrow.transform.worldLeft - 3f, width = 1f, height = transform.height + expandedContent.transform.height + 1f
//                ).let {
//                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_VERTICAL, separatorColor)
//                }
//                transform.asWorldBox.copy(y = expandedContent.transform.worldTop - 1f, height = 1f).let {
//                    pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_HORIZONTAL, separatorColor)
//                }
//            }
//        }
//    }
//
//    //------------ Layout ------------\\
//
//    internal val scope: DropMenuScope = DropMenuScope(this)
//
//    override fun layoutableChildren(): List<Layoutable> =
//        widgetChildren().filter { it != expandedContent }
//
//    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
//        super.measureChildren(measurables, constraints)
//        val c = constraints.constraintAs(this.constraints)
//        _scroller.constraints = _scroller.constraints.copy(minWidth = _arrow.transform.width + 3f, maxHeight = 0f)
//        expandedContent.measure(Constraints.of(0f, c.maxWidth, 0f, mc.window.scaledHeight.toFloat()))
//        if (!_list.constraints.widthRange.isEmpty()) {
//            _list.constraints = _list.constraints.copy(minWidth = _list.transform.width, maxWidth = _list.transform.width)
//        }
//        _scroller.constraints = _scroller.constraints.copy(maxHeight = _list.transform.height)
//        if (transform.width < expandedContent.wrappedWidth) {
//            head.constraints = head.constraints.copy(
//                minWidth = expandedContent.wrappedWidth,
//                maxWidth = expandedContent.wrappedWidth
//            )
//            super.measureChildren(
//                measurables,
//                constraints.copy(
//                    minWidth = expandedContent.wrappedWidth + padding.width,
//                    maxWidth = expandedContent.wrappedWidth + padding.width + padding.width
//                )
//            )
//        }
//        return this
//    }
//
//    override fun layout(layoutables: List<Layoutable>) {
//        super<RowLayout>.layout(layoutables)
//        expandedContent.measure(
//            Constraints.of(
//                expandedContent.transform.width,
//                expandedContent.transform.width,
//                0f,
//                mc.window.scaledHeight.toFloat() - transform.worldBottom
//            )
//        )
//        expandedContent.layout()
//        expandedContent.placeAt(padding.left, transform.height - padding.bottom / 2f + 1f)
//    }
//
//    //------------ DropMenu ------------\\
//
//    fun init() {
//        clearWidgetChildren()
//        head = scope.Column(horizontalArrangement = Arrangement.SpaceBetween) {
//            _content = Column(modifier = Modifier then (scope.contentModifier(this)), content = scope.content)
//            _arrow = Icon(
//                expandState.pick(WidgetTextures.DROP_MENU_ARROW_UP, WidgetTextures.DROP_MENU_ARROW_DOWN),
//                modifier = Modifier.padding(horizontal = 1f)
//            )
//        }
//        expandedContent = scope.run {
//            Column(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier
//                    .padding(vertical = 1f)
//                    .mousePress {
//                        this as ColumnWidget
//                        this.onMousePress(it)
////                        it.tryUse { wasDragging }
//                        it.tryUse { !wasMouseOver }.onSuccess { this@DropMenuWidget.toggle() }
//                    }.renderHoveredOutlineBox(Colors.AQUA)
//                    .mouseScrolling { event ->
//                        this as IGWidget
//                        onMouseScrolling(event)
//                        event.tryUse(wasMouseOver).onSuccess { _scroller.scroller(event.verticalAmount) }
//                    }
//            ) {
//                layer(GuiLayer.pop)
//                active(expandState.isExpanded)
//                visible(expandState.isExpanded)
//                val spacing = 3f
//                _list = RowList(
//                    spacing = spacing,
//                    modifier = Modifier
//                        .align(Alignment.CenterVertically)
//                        .renderHoveredOutlineBox(Colors.ROSE)
//                        .renderOverlay { ctx, _, _, _ ->
//                            this as ListWidget
//                            ctx.scissor(this@DropMenuWidget.transform.asWorldBox.copy(height = this@DropMenuWidget.transform.height + expandedContent.transform.height - 4f)) {
//                                ctx.batchRenderTextureColored {
//                                    widgetChildren().filter { it.visible }.forEach { child ->
//                                        this@DropMenuWidget.transform.asWorldBox.copy(
//                                            y = child.transform.worldBottom + spacing / 2 - .5f,
//                                            height = 1f,
//                                            width = this@DropMenuWidget.transform.width - _arrow.wrappedWidth - this@DropMenuWidget.padding.right
//                                        ).let {
//                                            pushWidgetTexture(it, WidgetTextures.DROP_MENU_SEPARATOR_HORIZONTAL, separatorColor)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        .mouseScrolling { event ->
//                            this as ListWidget
//                            onMouseScrolling(event)
//                            event.tryUse(wasMouseOver).onSuccess { _scroller.scroller(event.verticalAmount) }
//                        } then scope.itemsModifier(this),
//                ) {
//                    scope.itemsContent(this)
//                }
//                Box(modifier = Modifier.width(5f))
//                _scroller = Scroller(
//                    amountStep = { _list.widgetChildren().minOf { it.transform.height } / 2f },
//                    totalAmount = { _list.totalAmount },
//                    barProportion = { (_list.contentHeight / _list.totalSpace).coerceIn(0f..1f) },
//                    amountConsumer = {
//                        _list.amount = it
//                    },
//                    orientation = Orientation.Vertical,
//                    modifier = Modifier
//                        .align(Alignment.CenterVertically)
//                        .margin(right = -1.5f)
//                        .render { ctx, x, y, delta ->
//                            this as ScrollerWidget
//                            if (totalAmount() > 0f) onRender(ctx, x, y, delta)
//                        }
//                        .renderBackground { ctx, x, y, delta ->
//                            this as ScrollerWidget
//                            if (totalAmount() > 0f) onRenderBackground(ctx, x, y, delta)
//                        }
//                )
//            }
//        }
//    }
//
//    private lateinit var head: ColumnWidget
//
//    private lateinit var _content: ColumnWidget
//
//    private lateinit var _arrow: IconWidget
//
//    private lateinit var expandedContent: WidgetContainerImpl
//
//    private lateinit var _list: ListWidget
//
//    private lateinit var _scroller: ScrollerWidget
//
//    companion object {}
//
//    data class DropMenuScope(
//        private val dropMenu: DropMenuWidget,
//    ) : GuiScope<DropMenuWidget>, RowLayoutScope {
//
//        override fun owner(): DropMenuWidget = dropMenu
//
//        internal lateinit var itemsModifier: RowLayoutScope. () -> Modifier
//
//        internal lateinit var itemsContent: RowLayoutScope.() -> Unit
//
//        fun items(modifier: RowLayoutScope. () -> Modifier = { Modifier }, content: RowLayoutScope.() -> Unit) {
//            itemsModifier = modifier
//            itemsContent = content
//        }
//
//        internal lateinit var contentModifier: RowLayoutScope. () -> Modifier
//
//        internal lateinit var content: ColumnLayoutScope.() -> Unit
//
//        fun content(modifier: RowLayoutScope. () -> Modifier = { Modifier }, content: ColumnLayoutScope.() -> Unit) {
//            contentModifier = modifier
//            this.content = content
//        }
//
//        fun toggle() {
//            dropMenu.toggle()
//        }
//
//        fun refresh() {
//            mc.scheduleStartTick(1) {
//                dropMenu.init()
//                dropMenu.screen()?.remeasure()
//            }
//        }
//
//        fun separatorColor(separatorColor: ARGBColor) {
//            owner().separatorColor = separatorColor
//        }
//
//    }
//
//
//}
//
//typealias DropMenuScope = DropMenuWidget.DropMenuScope
//
//
//fun GuiScope<out WidgetContainer>.DropMenu(
//    modifier: Modifier = Modifier,
//    scope: DropMenuScope.() -> Unit,
//) = addWidgetChild(DropMenuWidget()) {
//    this.scope.scope()
//    init()
//    Modifier.padding(4).then(modifier).foldInApply()
//}
