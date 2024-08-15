package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.booleanButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.lockButton
import moe.forpleuvoir.ibukigourd.gui.widget.dropmenu.dropMenu
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = rowScreen {
    column {
        var selectText = "本居小铃"
        val list = listOf("东风谷早苗", "博丽灵梦", "雾雨魔理沙", "伊吹萃香")
        dropMenu {
            separatorColor(Color(0xFFCCCCCC))
            content {
                text({ Literal(selectText) })
            }
            items {
                list.forEach { str ->
                    flatButton(modifier = Modifier.width(list.maxWidth(textRenderer) + 2f), hoveredColor = { Colors.CYAN.opacity(.35f) }) {
                        arrangement(Arrangement.Start)
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
                    text = "我去还这样嵌套?"
                }
                repeat(50) {
                    text("aa$it")
                }
            }
        }
        flatButton(hoveredColor = { Colors.AQUA.opacity(.25f) }) {
            text({ Literal(selectText) })
        }
        val status = delegate(true)
        booleanButton(status)
        lockButton(status)
    }

    button(
        modifier = Modifier.maxWidth(80f).maxHeight(80f)
    ) {
        var text = ""
        var count = 1
        press {
            text += "\n"
            text += "测试宽度$count"
            count++
        }
        text(
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

    column(
        arrangement = Arrangement.SpaceBetween,
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
        button { text("按钮1") }
        button { text("按钮2") }
        button { text("按钮3") }
    }

    column(
        arrangement = Arrangement.SpaceAround,
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
        button { text("按钮1") }
        button { text("按钮2") }
        button { text("按钮3") }
    }

    column(
        arrangement = Arrangement.SpaceEvenly,
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
        button { text("按钮1") }
        button { text("按钮2") }
        button { text("按钮3") }
    }
}