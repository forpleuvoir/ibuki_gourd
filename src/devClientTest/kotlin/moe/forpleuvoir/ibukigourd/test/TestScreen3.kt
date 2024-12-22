package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.DropDownMenu
import moe.forpleuvoir.ibukigourd.gui.widget.Selector
import moe.forpleuvoir.ibukigourd.gui.widget.button.*
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.plus
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = RowScreen(
    modifier = Modifier.renderOverlay { ctx, _, _, _ ->
        val lines = listOf(
            Literal(screen()?.focusedWidget.toString()),
            Literal(screen()?.hoveredWidget.toString())
        )
        ctx.batchRenderText {
            pushTextLines(
                lines, transform.asWorldCoordinateBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top
            )
        }
    }
) {
    val selectText = mutableStateOf("本居小铃")
    val listString = listOf("东风谷早苗", "博丽灵梦", "雾雨魔理沙", "伊吹萃香")
    Column {
        DropDownMenu {
            TextLabel(selectText)
            DropDownContent {
                RowListWrapped(
                    modifier = Modifier.disableRenderBackground().padding(0f),
                    horizontalAlignment = Alignment.Left,
                ) {
                    listString.forEach { str ->
                        FlatButton(
                            modifier = Modifier.width(listString.maxWidth(textRenderer) + 2f),
                            hoveredColor = Colors.CYAN.opacity(.35f),
                            horizontalArrangement = Arrangement.Left
                        ) {
                            click {
                                this@DropDownMenu.toggle()
                                selectText.setValue(str)
                            }
                            TextLabel(str)
                        }
                    }
                    repeat(23) {
                        TextLabel("aa$it", modifier = Modifier.width(40f).renderHoveredOutlineBox(Colors.BANANA_YELLOW)) {
                            setting {
                                horizontalAlignment = Alignment.Left
                            }
                        }
                    }
                }
            }
        }
        FlatButton(hoveredColor = Colors.AQUA.opacity(.25f)) {
            TextLabel(selectText)
        }
        val status = mutableStateOf(true)
        SwitchButton(status)
        LockButton(status)
        val map = mapOf(
            "东风谷早苗" to IconTextures.CLOSE,
            "博丽灵梦" to IconTextures.DELETE,
            "雾雨魔理沙" to IconTextures.FILTER,
            "伊吹萃香" to IconTextures.SAVE
        )
        val wrapper: ButtonScope.(Pair<String, WidgetTexture>) -> IGWidget = { (str, icon) ->
            Column(
                modifier = Modifier.width(80f),
                horizontalArrangement = Arrangement.spacedBy(2f)
            ) {
                Icon(icon, color = Colors.BLACK, modifier = Modifier.size(8f, 8f))
                TextLabel(str)
            }
        }
        Selector(
            options = map.asIterable(),
            selectedWrapper = {
                wrapper(this, it.toPair())
            },
            optionWrapper = {
                wrapper(this, it.toPair())
            },
        )
        Selector(listString)
    }

    Button(
        modifier = Modifier.maxWidth(80f).maxHeight(80f)
    ) {
        val text = mutableStateOf("测试文本:")
        var count = 1
        click {
            text + "\n测试宽度$count"
            count++
        }
        TextLabel(
            str = text,
            style = style(color = Colors.COFFEE),
            modifier = Modifier
                .renderOverlay { ctx, _, _, _ ->
                    if (wasMouseOver)
                        ctx.batchRenderBox {
                            pushBoxOutline(contentBox(true), Colors.ROSE)
                            pushBoxOutline(transform.asWorldCoordinateBox, Colors.MEDIUM_TEAL)
                        }
                }
                .padding(4f)
        )
    }

    val list = listOf(
        Arrangement.SpaceBetween,
        Arrangement.SpaceAround,
        Arrangement.SpaceEvenly,
    )
    val lv = listOf(
        Arrangement.Top,
        Arrangement.Center,
        Arrangement.Bottom
    )
    val lh = listOf(
        Arrangement.Left,
        Arrangement.Center,
        Arrangement.Right
    )
    Column(Modifier.height(200f)) {
        Row(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lh).forEach { arrangement ->
                ColumnTest(arrangement)
            }
        }
        Column(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lv).forEach { arrangement ->
                RowTest(arrangement)
            }
        }
    }

}

private fun RowScope.ColumnTest(arrangement: Arrangement.Horizontal) = Column(
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { ctx, _, _, _ ->
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        },
    horizontalArrangement = arrangement
) {
    Button { TextLabel("按钮1") }
    Button { TextLabel("按钮2") }
    Button { TextLabel("按钮3") }
}

private fun ColumnScope.RowTest(arrangement: Arrangement.Vertical) = Row(
    verticalArrangement = arrangement,
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { ctx, _, _, _ ->
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        }
) {
    Button(
        modifier = Modifier.hoverText("我踏马要写一大段测试文本,\n而且我还要换行.我TM换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换\n换")
    ) {
        TextLabel("按钮1")
    }
    Button(
        Modifier.hoverText("超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本")
    ) { TextLabel("按钮2") }
    Button { TextLabel("按钮3") }
}