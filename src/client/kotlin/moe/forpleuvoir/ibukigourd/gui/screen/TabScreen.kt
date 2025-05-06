package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.Tabs
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

fun TabScreen(
    screenModifier: Modifier = Modifier,
    modifier: BoxScreenScope.() -> Modifier = { Modifier },
    header: ColumnScope.() -> Unit = { },
    tabColor: State<ARGBColor> = stateOf(Colors.WHITE),
    inactiveColor: State<ARGBColor> = stateOf(Colors.GRAY),
    tabScope: TabScope.() -> Unit
) = ColumnScreen(
    screenModifier = screenModifier,
    modifier = modifier
) {
    header()
    Tabs(
        modifier = Modifier.fill().weight(1),
        tabsModifier = Modifier.fill(),
        contentModifier = Modifier
            .weight(1)
            .fill()
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.TABS_SCREEN_BACKGROUND, (userData["#tab_scope"] as TabScope).tabColor.getValue())
                }
            }
    ) {
        this.tabColor.setValue(tabColor.getValue())
        this.tabColor.bind(tabColor)
        this.inactiveColor.setValue(inactiveColor.getValue())
        this.inactiveColor.bind(inactiveColor)
        tabScope()
    }
}