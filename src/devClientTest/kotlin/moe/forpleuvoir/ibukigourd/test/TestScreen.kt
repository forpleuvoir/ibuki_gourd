package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.screen.getScreenDataOr
import moe.forpleuvoir.ibukigourd.gui.base.screen.pushScreenData
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.box
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.gui.widget.text.text
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig.Screen.BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.overlayMessage
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class TestScreen : IGScreenImpl(), BoxLayout {

    override fun ScreenScope<out IGScreen>.content() {
        padding = Padding(8f)
        var deltaCount = 0
        var fps = 0
        var renderTime = 0.seconds
        tick = {
            onTick()
            deltaCount++
            if (deltaCount % 10 == 0) {
                fps = (1.seconds / latestRenderTime).toInt()
                deltaCount = 0
            }
            renderTime = latestRenderTime

        }
        renderOverlay = { context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float ->
            val contentBox = contentBox(true)
            onRenderOverlay(context, mouseX, mouseY, delta)
            context.batchRenderBox {
                context.boxOutline(contentBox, Colors.ROSE)
            }
            context.batchRenderText {
                val texts = listOf(
                    Literal("Screen renderTime:$renderTime").style { color(Colors.AQUA) },
                    Literal("Screen FPS:$fps").style { color(0x00FF00) }
                )
                context.textLines(texts, contentBox, align = BoxAlignment::TopLeft)
                val mouse = listOf(
                    Literal("MouseX:$mouseX").style { color(Colors.RED) },
                    Literal("MouseY:$mouseY").style { color(0x00FF00) },
                )
                context.textLines(
                    mouse,
                    contentBox.copy(height = contentBox.height + 2f),
                    defaultColor = Colors.ALIEN_GREEN.opacity(.3f),
                    align = BoxAlignment::BottomLeft,
                    backgroundColor = Colors.BLACK.opacity(.3f)
                )
            }
        }
        row(modifier = Modifier.padding(20f).renderOverlay { context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float ->
            val row = this as RowWidget
            context.batchRenderBox {
                context.boxOutline(contentBox(true), Colors.ROSE)
            }
        }) {
            spacing(5f)
            listWithScroller(
                Orientation.Horizontal,
                2f,
                amountConsumer = { mc.pushScreenData("list", it) },
                initialAmount = mc.getScreenDataOr("list", 0f),
                modifier = Modifier
            ) {
                repeat(50) {
                    if (it == 29) {
                        button(modifier = Modifier.width(50f)) {
                            icon(IconTextures.CLOSE)
                        }
                    } else {
                        button {
                            icon(IconTextures.CLOSE)
                        }
                    }
                }
            }
            testColumn()
            val s1 = scroller(
                { 1f },
                { 10f },
                { 0.1f },
                BG_BLUR_RADIUS,
                { BG_BLUR_RADIUS = it },
                orientation = Orientation.Horizontal,
                modifier = Modifier.maxWidth(180f)
            )
        }
    }

    fun RowScope.testColumn() =
        column(
            modifier = Modifier.renderOverlay { context, mouseX, mouseY, delta ->
                this as IGWidget
                context.batchRenderBox {
                    context.boxOutline(transform.asWorldBox, Colors.AQUA)
                }
            }.weight(8)
        ) {
            spacing(5f)
            listWithScroller(
                Orientation.Vertical,
                2f,
                amountConsumer = { mc.pushScreenData("list1", it) },
                initialAmount = mc.getScreenDataOr("list1", 0f),
                listModifier = { Modifier.width(120f) }
            ) {
                var c = 0
                var f = true
                repeat(50) {
                    val m = when (c) {
                        0    -> Modifier.gravityStart()
                        1    -> Modifier.gravityCenter()
                        2    -> Modifier.gravityEnd()
                        else -> Modifier.gravityCenter()
                    }

                    if (it == 29) {
                        button(modifier = m.width(50f)) {
                            icon(IconTextures.CLOSE)
                        }
                    } else if (it % 5 == 0) {
                        button(modifier = m) {
                            var text = ""
                            press {
                                text += "\n"
                                text += "测试宽度测试宽度测试宽度测试宽度"
                            }
                            text({ Literal("测试文本$it:$text") }) {
                                setting {
                                    if (it == 15) spacing = 8f
                                }
                            }
                        }
                    } else {
                        button(modifier = m) {
                            icon(IconTextures.CLOSE)
                        }
                    }
                    if (c == 2) {
                        f = false
                    } else if (c == 0) {
                        f = true
                    }
                    if (f) c++
                    else c--
                }
            }
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
                icon(IconTextures.FILTER, modifier = Modifier.alignment(BoxAlignment.CenterCenter()))
            }
            scroller({ 5f }, { 500f }, { 0.1f }, modifier = Modifier.maxHeight(180f))
            button(
                modifier = Modifier.height(40f).renderOverlay { context, mouseX, mouseY, delta ->
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
                    spacing(8f)
                    icon(IconTextures.CLOSE, modifier = Modifier.gravityStart().maxWidth(16f))
                    icon(IconTextures.SEARCH, modifier = Modifier.gravityCenter().maxWidth(16f))
                    icon(IconTextures.MINUS, modifier = Modifier.gravityEnd().maxWidth(16f))
                    icon(IconTextures.LOCK, modifier = Modifier.gravityCenter().maxWidth(16f))
                    icon(IconTextures.FILTER, modifier = Modifier.gravityStart().maxWidth(16f))
                }
            }

        }


}