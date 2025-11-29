package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.modifier.*
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.DropDownMenu
import moe.forpleuvoir.ibukigourd.gui.widget.Selector
import moe.forpleuvoir.ibukigourd.gui.widget.SelectorWithSearcher
import moe.forpleuvoir.ibukigourd.gui.widget.button.*
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.plus
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = ColumnScreen(
    modifier = Modifier.renderOverlay { guiGraphics, _, _, _ ->
        val lines = listOf(
            Literal(screen()?.focusedWidget.toString()),
            Literal(screen()?.hoveredWidget.toString())
        )
        guiGraphics.pushTextLines(
            lines, transform.asWorldCoordinateBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top
        )
    }.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MouseCursor()
        MousePosition()
    }
) {
    val selectText = mutableStateOf("本居小铃")
    val listString = listOf("东风谷早苗", "博丽灵梦", "雾雨魔理沙", "伊吹萃香", "本居小铃")
    Row {
        SelectorWithSearcher(
            listString,
            { str, s ->
                str.contains(s)
            },
            selectText,
            selectedWrapper = {
                Text(it)
            },
            optionWrapper = {
                Text(it, modifier = Modifier.width(80f))
            },
            searchBarModifier = { Modifier.width(80f) },
            listModifier = { Modifier.width(80f) },
        )
        DropDownMenu {
            Text(selectText)
            DropDownContent {
                ColumnListWrapped(
                    modifier = Modifier.disableRenderBackground().padding(0f),
                    horizontalAlignment = Alignment.Left,
                ) {
                    listString.forEach { str ->
                        FlatButton(
                            modifier = Modifier.width(listString.maxWidth + 2f),
                            hoveredColor = Colors.CYAN.opacity(.35f),
                            horizontalArrangement = Arrangement.Left
                        ) {
                            click {
                                this@DropDownMenu.toggle()
                                selectText.setValue(str)
                            }
                            Text(str)
                        }
                    }
                    repeat(23) {
                        Text("aa$it", modifier = Modifier.width(40f).renderHoveredOutlineBox(Colors.BANANA_YELLOW)) {
                            setting {
                                horizontalAlignment = Alignment.Left
                            }
                        }
                    }
                }
            }
        }
        FlatButton(hoveredColor = Colors.AQUA.opacity(.25f)) {
            Text(selectText)
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
        val wrapper: ButtonScope.(Pair<String, WidgetTexture>) -> GuiWidget = { (str, icon) ->
            Row(
                modifier = Modifier.width(80f),
                horizontalArrangement = Arrangement.spacedBy(2f)
            ) {
                Icon(icon, color = Colors.BLACK, modifier = Modifier.size(8f, 8f))
                Text(str)
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
        Text(
            str = text,
            style = style(color = Colors.COFFEE),
            modifier = Modifier
                .renderOverlay { guiGraphics, _, _, _ ->
                    if (wasMouseOver)
                        guiGraphics {
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
    Row(Modifier.height(200f)) {
        Column(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lh).forEach { arrangement ->
                ColumnTest(arrangement)
            }
        }
        Row(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lv).forEach { arrangement ->
                RowTest(arrangement)
            }
        }
    }

}

private fun ColumnScope.ColumnTest(arrangement: Arrangement.Horizontal) = Row(
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { guiGraphics, _, _, _ ->
            guiGraphics {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        },
    horizontalArrangement = arrangement
) {
    Button { Text("按钮1") }
    Button { Text("按钮2") }
    Button { Text("按钮3") }
}

private fun RowScope.RowTest(arrangement: Arrangement.Vertical) = Column(
    verticalArrangement = arrangement,
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { guiGraphics, _, _, _ ->
            guiGraphics {
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
        Text("按钮1")
    }
    Button(
        Modifier.hoverText("超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本超宽文本")
    ) { Text("按钮2") }
    Button { Text("按钮3") }
}