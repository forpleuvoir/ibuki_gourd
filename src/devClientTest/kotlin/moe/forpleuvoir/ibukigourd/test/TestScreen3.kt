package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.maxHeight
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.maxWidth
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.dropmenu.dropMenu
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = rowScreen {
    dropMenu {
        content {
            text("测试1")
        }
        items {
            repeat(13) {
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


}