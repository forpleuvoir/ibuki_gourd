package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.dropmenu.dropMenu
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = rowScreen {
    dropMenu {
        content {
            text("测试1")
        }
        items {
            repeat(35) {
                text("测试内容$it")
            }
        }
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
                            ctx.boxOutline(contentBox(true), Colors.ROSE)
                            ctx.boxOutline(transform.asWorldBox, Colors.MEDIUM_TEAL)
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
                        ctx.boxOutline(transform.asWorldBox, Colors.AQUA)
                    }
                    ctx.box(transform.asWorldBox, Colors.AQUA.alpha(.25f))
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
                        ctx.boxOutline(transform.asWorldBox, Colors.AQUA)
                    }
                    ctx.box(transform.asWorldBox, Colors.AQUA.alpha(.25f))
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
                        ctx.boxOutline(transform.asWorldBox, Colors.AQUA)
                    }
                    ctx.box(transform.asWorldBox, Colors.AQUA.alpha(.25f))
                }
            }
    ) {
        button { text("按钮1") }
        button { text("按钮2") }
        button { text("按钮3") }
    }
}