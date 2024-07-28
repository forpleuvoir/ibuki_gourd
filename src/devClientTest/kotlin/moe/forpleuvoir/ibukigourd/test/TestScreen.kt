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
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.screen.getScreenDataOr
import moe.forpleuvoir.ibukigourd.gui.base.screen.pushScreenData
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.box
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.listWithScroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.row
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
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
            onRenderOverlay(context, mouseX, mouseY, delta)
            context.batchRenderText {
                val texts = listOf(
                    Literal("Screen renderTime:$renderTime").style { color(Colors.AQUA) },
                    Literal("Screen FPS:$fps").style { color(0x00FF00) }
                )
                context.textLines(texts, contentBox(true), align = BoxAlignment::TopLeft)
                val mouse = listOf(
                    Literal("MouseX:$mouseX").style { color(Colors.RED) },
                    Literal("MouseY:$mouseY").style { color(0x00FF00) },
                )
                context.textLines(mouse, contentBox(true), defaultColor = Colors.ALIEN_GREEN.opacity(.3f), align = BoxAlignment::BottomLeft)
            }
        }
        row(modifier = Modifier.padding(20f)) {
            listWithScroller(
                Orientation.Horizontal,
                2f,
                amountConsumer = { mc.pushScreenData("list", it) },
                initialAmount = mc.getScreenDataOr("list", 0f)
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
            box(Modifier.height(5f))
            testColumn()
            box(Modifier.height(5f))
            val s1 = scroller(
                { 1f },
                { 10f },
                { 0.1f },
                BG_BLUR_RADIUS,
                { BG_BLUR_RADIUS = it },
                orientation = Orientation.Horizontal,
                modifier = Modifier.maxWidth(180f)
            )
            box(Modifier.height(5f))

        }
    }

    fun GuiScope<out WidgetContainer>.testColumn() =
        column(modifier = Modifier.renderOverlay { context, mouseX, mouseY, delta ->
            this as IGWidget
            context.batchRenderBox {
                context.boxOutline(transform.asWorldBox, Colors.AQUA)
            }
        }) {
            listWithScroller(
                Orientation.Vertical,
                2f,
                amountConsumer = { mc.pushScreenData("list1", it) },
                initialAmount = mc.getScreenDataOr("list1", 0f)
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
                    icon(IconTextures.CLOSE, modifier = Modifier.gravityStart().maxWidth(16f))
                    icon(IconTextures.SEARCH, modifier = Modifier.gravityCenter().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.MINUS, modifier = Modifier.gravityEnd().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.LOCK, modifier = Modifier.gravityCenter().maxWidth(16f).margin(Margin(left = 8f)))
                    icon(IconTextures.FILTER, modifier = Modifier.gravityStart().maxWidth(16f).margin(Margin(left = 8f)))
                }
            }

        }


}