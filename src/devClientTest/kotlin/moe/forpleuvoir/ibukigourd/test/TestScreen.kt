package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.box
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.overlayMessage
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class TestScreen : IGScreenImpl(), BoxLayout {

    override fun ScreenScope<out IGScreen>.content() {
        padding = Padding(8f)
        renderOverlay = { context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float ->
            onRenderOverlay(context, mouseX, mouseY, delta)
            context.batchRenderText {
                val texts = listOf(
                    Literal("Screen renderTime:${latestRenderTime}").style { color(Colors.AQUA) },
                    Literal("Screen FPS:${(1.seconds / latestRenderTime).toInt()}").style { color(Colors.GREEN) }
                )
                context.textLines(texts, contentBox(true), align = BoxAlignment::TopLeft)
            }
        }
        column(modifier = Modifier.renderOverlay { context, mouseX, mouseY, delta ->
            this as IGWidget
            context.batchRenderBox {
                context.boxOutline(transform.asWorldBox, Colors.AQUA)
            }
        }) {
            val icons = listOf(
                IconTextures.CLOSE,
                IconTextures.SEARCH,
                IconTextures.FILTER,
                IconTextures.MINUS,
            )

            box(
                modifier = Modifier.renderOverlay { context, mouseX, mouseY, delta ->
                    this as IGWidget
                    context.batchRenderBox {
                        context.boxOutline(transform.asWorldBox, Colors.AQUA)
                    }
                }
            ) {
                column {
                    icons.forEach {
                        icon(it)
                        box(Modifier.width(5f))
                    }
                }
            }
            box(
                Modifier.size(80f, 80f)
                    .margin(horizontal = 10f)
                    .renderOverlay { context, mouseX, mouseY, delta ->
                        this as IGWidget
                        context.batchRenderBox {
                            if (wasMouseOver) context.boxOutline(transform.asWorldBox, Colors.AQUA)
                        }
                    }
            ) {
                icon(IconTextures.CLOSE, modifier = Modifier.alignment(BoxAlignment.TopLeft()))
                icon(IconTextures.SEARCH, modifier = Modifier.alignment(BoxAlignment.TopRight()))
                icon(IconTextures.MINUS, modifier = Modifier.alignment(BoxAlignment.BottomLeft()))
                icon(IconTextures.LOCK, modifier = Modifier.alignment(BoxAlignment.BottomRight()))
                icon(IconTextures.FILTER, modifier = Modifier.alignment(BoxAlignment.Center()))
            }
            button(
                Modifier.height(40f).renderOverlay { context, mouseX, mouseY, delta ->
                    this as IGWidget
                    measureTime {
                        context.batchRenderBox {
                            if (wasMouseOver) context.boxOutline(transform.asWorldBox, Colors.AQUA)
                        }
                    }.let {
//                        println(it)
                    }
                }
            ) {
                press {
                    println("按下了测试按钮")
                    println(this@TestScreen.focusedWidget)
                    mc.overlayMessage("按下了测试按钮")
                }
                longPress(10) {
                    println("长按了按钮")
                }
                column(modifier = Modifier.fillHeight().renderOverlay { context, mouseX, mouseY, delta ->
                    this as IGWidget
                    context.batchRenderBox {
                        context.boxOutline(transform.asWorldBox, Colors.AQUA)
                    }
                }) {
                    icon(IconTextures.CLOSE, modifier = Modifier.gravityStart().maxWidth(16f))
                    icon(IconTextures.SEARCH, modifier = Modifier.gravityCenter().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.MINUS, modifier = Modifier.gravityEnd().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.LOCK, modifier = Modifier.gravityCenter().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.FILTER, modifier = Modifier.gravityStart().maxWidth(16f).margin(Margin(left = 8f)))
                }
            }

        }
    }


}