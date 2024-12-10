package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderPriority
import moe.forpleuvoir.ibukigourd.gui.base.modifier.then
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick


data class TabScope(
    val owner: WidgetContainer,
    val direction: Direction
) : LinearLayoutScope<Alignment.Linear>, GuiScope<WidgetContainer> {

    internal lateinit var content: MutableState<BoxScope.() -> IGWidget>

    override fun owner(): WidgetContainer = owner

    private val tabs = mutableMapOf<IGWidget, BoxScope.() -> IGWidget>()

    fun addTab(tab: IGWidget, content: BoxScope.() -> IGWidget) {
        tabs[tab] = content
        if (!tab.active && !this::content.isInitialized) {
            this.content = mutableStateOf(content)
        }
    }

    fun setCurrent(tab: IGWidget) {
        if (!tab.active) return
        tab.active = false
        tabs.filter { it.key != tab }
            .forEach {
                it.key.active = true
            }
    }

    internal fun initialized() {
        if (!this::content.isInitialized) {
            check(tabs.isNotEmpty()) { "Tabs cannot be empty when initializing." }
            val (tab, content) = tabs.entries.first()
            tab.active = false
            this.content = mutableStateOf(content)
        }
    }

    val tabColor: MutableState<ARGBColor> = mutableStateOf(Colors.WHITE)

    val inactiveColor: MutableState<ARGBColor> = mutableStateOf(Colors.GRAY)

    override fun Modifier.weight(weight: Int): Modifier {
        return if (owner is RowWidget) {
            RowScope { owner }.run { weight(weight) }
        } else if (owner is ColumnWidget) {
            ColumnScope { owner }.run { weight(weight) }
        } else {
            throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

    override fun Modifier.fillMode(fillMode: FillMode): Modifier {
        return if (owner is RowWidget) {
            RowScope { owner }.run { fillMode(fillMode) }
        } else if (owner is ColumnWidget) {
            ColumnScope { owner }.run { fillMode(fillMode) }
        } else {
            throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

    override fun Modifier.align(alignment: Alignment.Linear): Modifier {
        return if (owner is RowWidget) {
            RowScope { owner }.run { align(alignment as Alignment.Horizontal) }
        } else if (owner is ColumnWidget) {
            ColumnScope { owner }.run { align(alignment as Alignment.Vertical) }
        } else {
            throw IllegalStateException("Invalid owner type: Expected types are RowWidget or ColumnWidget, but a different type was found.")
        }
    }

}

fun WidgetContainerScope.Tabs(
    direction: Direction = Top,
    modifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
): WidgetContainer = when (direction) {
    Top, Bottom -> RowTabs(direction, modifier, scope)
    Right, Left -> ColumnTabs(direction, modifier, scope)
}


private fun WidgetContainerScope.RowTabs(
    direction: Direction,
    modifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
) = Row(modifier) {
    var tabScope: TabScope? = null

    Column(
        Modifier
            .renderPriority(1)
            .matchSibling()
            .padding(horizontal = 4),
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
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.TABS_BACKGROUND, tabScope!!.tabColor.getValue())
                }
            }
    ) {
        Proxy(tabScope!!.content)
    }
    if (direction == Bottom) owner().swapWidgetChildren(0, 1)
}

private fun WidgetContainerScope.ColumnTabs(
    direction: Direction,
    modifier: Modifier = Modifier,
    scope: TabScope.() -> Unit
) = Column(modifier) {
    var tabScope: TabScope? = null
    Row(
        Modifier
            .renderPriority(1)
            .matchSibling()
            .padding(vertical = 4),
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
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.TABS_BACKGROUND, tabScope!!.tabColor.getValue())
                }
            }
    ) {
        Proxy(tabScope!!.content)
    }
    if (direction == Right) owner().swapWidgetChildren(0, 1)
}

fun TabScope.Tab(
    activeColor: State<ARGBColor> = tabColor,
    inactiveColor: State<ARGBColor> = this.inactiveColor,
    modifier: Modifier = Modifier,
    scope: ButtonScope.() -> Unit = {},
    content: BoxScope.() -> IGWidget
) = Button(
    modifier = Modifier
        .then {
            when (direction) {
                Top    -> padding(top = 5, right = 5, left = 5, bottom = 0)
                Bottom -> padding(top = 0, right = 5, left = 5, bottom = 5)
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
        .render { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, tabButtonTexture(direction, active), active.pick(inactiveColor.getValue(), activeColor.getValue()))
            }
        }.then(modifier)
) {
    this@Tab.addTab(this.owner(), content)
    click {
        this@Tab.apply {
            setCurrent(this@Button.owner())
            this.content.setValue(content)
        }
    }
    scope()
}

private fun tabButtonTexture(direction: Direction, active: Boolean): WidgetTexture = when (direction) {
    Top    -> active.pick(WidgetTextures.TAB_INACTIVE_TOP, WidgetTextures.TAB_ACTIVE_TOP)
    Right  -> active.pick(WidgetTextures.TAB_INACTIVE_RIGHT, WidgetTextures.TAB_ACTIVE_RIGHT)
    Bottom -> active.pick(WidgetTextures.TAB_INACTIVE_BOTTOM, WidgetTextures.TAB_ACTIVE_BOTTOM)
    Left   -> active.pick(WidgetTextures.TAB_INACTIVE_LEFT, WidgetTextures.TAB_ACTIVE_LEFT)
}
