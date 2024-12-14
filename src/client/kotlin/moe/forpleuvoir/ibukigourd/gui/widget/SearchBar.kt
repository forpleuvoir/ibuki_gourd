package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

fun WidgetContainerScope.SearchBar(
    textConsumer: (String) -> Unit,
    hintText: State<String?> = stateOf(null),
    bgShaderColor: ARGBColor = Colors.WHITE,
    modifier: Modifier = Modifier,
    textEditorModifier: ColumnScope.() -> Modifier = { Modifier },
) = Column(
    modifier.attachLeft {
        padding(5, 3, 3, 3)
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(
                        transform,
                        theme(
                            WidgetTheme.TextInput,
                            hovered = screen()?.hoveredWidget?.getValue()?.isInParentChain(this@render) == true
                                    || screen()?.focusedWidget?.getValue()?.isInParentChain(this@render) == true
                        ),
                        bgShaderColor
                    )
                }
            }
    }
) {
    Icon(IconTextures.SEARCH)
    TextEditor(
        modifier = Modifier
            .disableRenderBackground()
            .padding(3)
            .then(textEditorModifier())
    ) {
        this.hintText = hintText
        this.textConsumer(textConsumer)
    }
}