package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.LockButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.dropmenu.dropMenu
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = rowScreen(
    modifier = Modifier.renderOverlay { ctx, _, _, _ ->
        this as IGScreenImpl<*>
        ctx.batchRenderText {
            pushTextLines(
                Literal(focusedWidget.toString()), transform.asWorldBox, align = BoxAlignment::TopLeft
            )
        }
    }
) {
    column {
        var selectText = "本居小铃"
        val list = listOf("东风谷早苗", "博丽灵梦", "雾雨魔理沙", "伊吹萃香")
        dropMenu {
            content {
                text({ Literal(selectText) })
            }
            items {
                list.forEach { str ->
                    flatButton(modifier = Modifier.width(list.maxWidth(textRenderer) + 2f), hoveredColor = { Colors.CYAN.opacity(.35f) }) {
                        var curText = str
                        press {
                            this@dropMenu.toggle()
                            curText = selectText
                            selectText = str
                        }
                        text({ Literal(curText) })
                    }
                }
                textField {
                    text = "短一点"
                }
                repeat(50) {
                    text("aa$it")
                }
            }
        }
        FlatButton(hoveredColor = { Colors.AQUA.opacity(.25f) }) {
            Text({ Literal(selectText) })
        }
        val status = delegate(true)
        SwitchButton(status)
        LockButton(status)
    }

    Button(
        modifier = Modifier.maxWidth(80f).maxHeight(80f)
    ) {
        var text = ""
        var count = 1
        press {
            text += "\n"
            text += "测试宽度$count"
            count++
        }
        Text(
            text = { Literal("测试文本:$text").style { color(Colors.BRIGHT_NEON_PINK) } },
            modifier = Modifier
                .renderOverlay { ctx, _, _, _ ->
                    this as IGWidget
                    if (wasMouseOver)
                        ctx.batchRenderBox {
                            pushBoxOutline(contentBox(true), Colors.ROSE)
                            pushBoxOutline(transform.asWorldBox, Colors.MEDIUM_TEAL)
                        }
                }
                .padding(4f)
        )
    }

    column {
        Row {
            Arrangement.values.forEach { arrangement ->
                columnTest(arrangement)
            }
        }
        column {
            Arrangement.values.forEach { arrangement ->
                rowTest(arrangement)
            }
        }
    }
}

private fun GuiScope<out WidgetContainer>.columnTest(arrangement: Arrangement) = column(
    arrangement = arrangement,
    modifier = Modifier
        .padding(3f)
        .width(240f)
        .renderBackground { ctx, _, _, _ ->
            this as IGWidget
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        }
) {
    Button { Text("按钮1") }
    Button { Text("按钮2") }
    Button { Text("按钮3") }
}

private fun GuiScope<out WidgetContainer>.rowTest(arrangement: Arrangement) = Row(
    arrangement = arrangement,
    modifier = Modifier
        .padding(3f)
        .height(240f)
        .renderBackground { ctx, _, _, _ ->
            this as IGWidget
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        }
) {
    Button { Text("按钮1") }
    Button { Text("按钮2") }
    Button { Text("按钮3") }
}