package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderPriority
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick


data class TabScope(
    val owner: ColumnWidget
) : ColumnScope {

    internal lateinit var content: MutableState<BoxScope.() -> IGWidget>

    override fun owner(): ColumnWidget = owner

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
}


fun WidgetContainerScope.Tabs(
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
        tabScope = TabScope(this.owner()).apply {
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
        Proxy(tabScope!!.content, 0)
    }
}

fun TabScope.Tab(
    activeColor: State<ARGBColor> = stateOf(tabColor),
    inactiveColor: State<ARGBColor> = stateOf(this.inactiveColor),
    modifier: Modifier = Modifier,
    scope: ButtonScope.() -> Unit = {},
    content: BoxScope.() -> IGWidget
) = Button(
    modifier = Modifier
        .padding(top = 5, right = 5, left = 5, bottom = 0)
        .render { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, tabButtonTexture(Top, active), active.pick(inactiveColor.getValue(), activeColor.getValue()))
            }
        }.then(modifier)
) {
    this@Tab.addTab(this.owner(), content)
    press {
        this@Tab.apply {
            setCurrent(this@Button.owner())
            this.content.setValue(content)
        }
    }
    scope()
}

fun tabButtonTexture(direction: Direction, active: Boolean): WidgetTexture = when (direction) {
    Top    -> active.pick(WidgetTextures.TAB_INACTIVE_TOP, WidgetTextures.TAB_ACTIVE_TOP)
    Right  -> active.pick(WidgetTextures.TAB_INACTIVE_RIGHT, WidgetTextures.TAB_ACTIVE_RIGHT)
    Bottom -> active.pick(WidgetTextures.TAB_INACTIVE_BOTTOM, WidgetTextures.TAB_ACTIVE_BOTTOM)
    Left   -> active.pick(WidgetTextures.TAB_INACTIVE_LEFT, WidgetTextures.TAB_ACTIVE_LEFT)
}
