package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.hoverable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderPriority
import moe.forpleuvoir.ibukigourd.gui.base.modifier.then
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.userData
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.ibukigourd.util.lateInitValueOf
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.either

data class TabScope(
    val owner: GuiWidgetContainer,
    val direction: Direction
) : LinearLayoutScope<Alignment.Linear>, GuiScope<GuiWidgetContainer> {

    internal lateinit var content: MutableState<BoxScope.() -> GuiWidget>

    override fun owner(): GuiWidgetContainer = owner

    private val tabs = mutableMapOf<GuiWidget, BoxScope.() -> GuiWidget>()

    private val tabsChangedListener = mutableMapOf<GuiWidget, TabScope.(Boolean) -> Unit>()

    private var defaultTab: Pair<GuiWidget, (BoxScope.() -> GuiWidget)>? = null

    private fun onChanged(current: GuiWidget) {
        tabsChangedListener.forEach {
            it.value.invoke(this, it.key == current)
        }
    }

    private fun addTab(tab: GuiWidget, tabsChangedListener: TabScope.(Boolean) -> Unit, content: BoxScope.() -> GuiWidget) {
        tabs[tab] = content
        this.tabsChangedListener[tab] = tabsChangedListener
        if (!tab.active && !this::content.isInitialized) {
            this.content = mutableStateOf(content)
        }
    }

    private fun setCurrent(tab: GuiWidget) {
        if (!tab.active) return
        tab.active = false
        onChanged(tab)
        tabs.filter { it.key != tab }
            .forEach {
                it.key.active = true
            }
    }

    internal fun initialized() {
        if (!this::content.isInitialized) {
            check(tabs.isNotEmpty()) { "Tabs cannot be empty when initializing." }
            val (tab, content) = defaultTab ?: tabs.entries.first().toPair()
            tab.active = false
            onChanged(tab)
            this.content = mutableStateOf(content)
        }
    }

    val tabColor: MutableState<ARGBColor> = mutableStateOf(Colors.WHITE)

    fun tabColor(color: ARGBColor) = tabColor.setValue(color)

    val inactiveColor: MutableState<ARGBColor> = mutableStateOf(Colors.GRAY)

    fun inactiveColor(color: ARGBColor) = inactiveColor.setValue(color)

    fun TabScope.Tab(
        initial: Boolean = false,
        activeColor: State<ARGBColor> = tabColor,
        inactiveColor: State<ARGBColor> = this.inactiveColor,
        modifier: Modifier = Modifier,
        onTabChanged: TabScope.(Boolean) -> Unit = {},
        scope: ButtonScope.() -> Unit = {},
        content: BoxScope.() -> GuiWidget
    ) = Button(
        modifier = Modifier
            .then {
                when (direction) {
                    Top    -> padding(top = 5, right = 5, left = 5, bottom = 1)
                    Bottom -> padding(top = 1, right = 5, left = 5, bottom = 5)
                    Right  -> padding(top = 5, right = 5, left = 3, bottom = 5)
                    Left   -> padding(top = 5, right = 3, left = 5, bottom = 5)
                }
            }
            .align(
                when (direction) {
                    Top    -> Alignment.Bottom
                    Bottom -> Alignment.Top
                    Right  -> Alignment.Left
                    Left   -> Alignment.Right
                }
            )
            .render { guiGraphics, _, _, _ ->
                guiGraphics.pushWidgetTexture(transform, tabButtonTexture(direction, active), active.either({ inactiveColor.getValue() },
                    { activeColor.getValue() }))
            }.then(modifier)
    ) {
        this@TabScope.addTab(this.owner(), onTabChanged, content)
        if (initial) this@TabScope.defaultTab = this.owner() to content
        click {
            this@Tab.apply {
                setCurrent(this@Button.owner())
                this.content.setValue(content)
            }
        }
        scope()
    }

    fun TabScope.Tab(
        text: String,
        initial: Boolean = false,
        onTabChanged: TabScope.(Boolean) -> Unit = {},
        activeTextColor: State<ARGBColor> = stateOf(Colors.WHITE),
        inactiveTextColor: State<ARGBColor> = stateOf(Colors.BLACK),
        modifier: Modifier = Modifier,
        content: BoxScope.() -> GuiWidget
    ): IGButtonWidget {
        val t = mutableStateOf(Literal(text))
        var yOffset = -0.5f
        return Tab(
            initial = initial,
            modifier = modifier,
            onTabChanged = {
                t.setValue(Literal(text).withColor(if (it) activeTextColor.getValue() else inactiveTextColor.getValue()))
                yOffset = if (it) -0.5f else 0.5f
                onTabChanged(this, it)
            },
            content = content,
            scope = {
                Text(
                    t,
                    modifier = Modifier.render { context, x, y, d ->
                        context.useMatrixStack {
                            it.translate(0f, yOffset)
                            onRender(context, x, y, d)
                        }
                    }
                )
            }
        )
    }

    override fun Modifier.weight(weight: Int): Modifier {
        return when (owner) {
            is ColumnWidget ->
                ColumnScope { owner }.run { weight(weight) }

            is RowWidget    ->
                RowScope { owner }.run { weight(weight) }

            else            -> throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

    override fun Modifier.fillMode(fillMode: FillMode): Modifier {
        return when (owner) {
            is ColumnWidget ->
                ColumnScope { owner }.run { fillMode(fillMode) }

            is RowWidget    ->
                RowScope { owner }.run { fillMode(fillMode) }

            else            ->
                throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

    override fun Modifier.priority(priority: Int): Modifier {
        return when (owner) {
            is ColumnWidget ->
                ColumnScope { owner }.run { priority(priority) }

            is RowWidget    ->
                RowScope { owner }.run { priority(priority) }

            else            ->
                throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

    override fun Modifier.align(alignment: Alignment.Linear): Modifier {
        return when (owner) {
            is ColumnWidget ->
                ColumnScope { owner }.run { align(alignment as Alignment.Horizontal) }

            is RowWidget    ->
                RowScope { owner }.run { align(alignment as Alignment.Vertical) }

            else            ->
                throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

}

fun ContainerScope.Tabs(
    direction: Direction = Top,
    modifier: Modifier = Modifier,
    tabsModifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
): GuiWidgetContainerImpl = when (direction) {
    Top, Bottom -> ColumnTabs(direction, modifier, tabsModifier, contentModifier, scope)
    Right, Left -> RowTabs(direction, modifier, tabsModifier, contentModifier, scope)
}


private fun ContainerScope.ColumnTabs(
    direction: Direction,
    modifier: Modifier = Modifier,
    tabsModifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
) = Column(modifier) {
    var tabScope by lateInitValueOf<TabScope>()
    Row(
        Modifier
            .renderPriority(1)
            .matchSibling()
            .padding(horizontal = 4)
            .then(tabsModifier),
        horizontalArrangement = Arrangement.Left
    ) {
        tabScope = TabScope(this.owner(), direction).apply {
            scope()
            initialized()
        }
    }
    Box(
        modifier = Modifier
            .padding(5)
            .render { guiGraphics, _, _, _ ->
                guiGraphics.pushWidgetTexture(transform, WidgetTextures.TABS_BACKGROUND, tabScope.tabColor.getValue())
            }.then(contentModifier)
    ) {
        userData["#tab_scope"] = tabScope
        Proxy(tabScope.content)
    }
    if (direction == Bottom) owner().swapWidgetChildren(0, 1)
}

private fun ContainerScope.RowTabs(
    direction: Direction,
    modifier: Modifier = Modifier,
    tabsModifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
) = Row(modifier) {
    var tabScope by lateInitValueOf<TabScope>()
    Column(
        Modifier
            .renderPriority(1)
            .matchSibling()
            .padding(vertical = 4)
            .then(tabsModifier),
        verticalArrangement = Arrangement.Top
    ) {
        tabScope = TabScope(this.owner(), direction).apply {
            scope()
            initialized()
        }
    }
    Box(
        modifier = Modifier
            .padding(5)
            .render { guiGraphics, _, _, _ ->
                guiGraphics.pushWidgetTexture(transform, WidgetTextures.TABS_BACKGROUND, tabScope.tabColor.getValue())
            }.then(contentModifier)
    ) {
        userData["#tab_scope"] = tabScope
        Proxy(tabScope.content)
    }
    if (direction == Right) owner().swapWidgetChildren(0, 1)
}


private fun tabButtonTexture(direction: Direction, active: Boolean): WidgetTexture = when (direction) {
    Top    -> active.either(WidgetTextures.TAB_INACTIVE_TOP, WidgetTextures.TAB_ACTIVE_TOP)
    Right  -> active.either(WidgetTextures.TAB_INACTIVE_RIGHT, WidgetTextures.TAB_ACTIVE_RIGHT)
    Bottom -> active.either(WidgetTextures.TAB_INACTIVE_BOTTOM, WidgetTextures.TAB_ACTIVE_BOTTOM)
    Left   -> active.either(WidgetTextures.TAB_INACTIVE_LEFT, WidgetTextures.TAB_ACTIVE_LEFT)
}
