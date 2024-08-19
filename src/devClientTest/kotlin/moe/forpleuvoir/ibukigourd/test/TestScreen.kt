package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.boxScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.*
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig.Screen.BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.overlayMessage
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

fun testScreen() = boxScreen(
    {
        val screen = owner() as IGScreenImpl<*>
        var deltaCount = 0
        var fps = 0
        var renderTime = 0.seconds
        Modifier.padding(8f)
            .tick {
                onTick()
                deltaCount++
                if (deltaCount % 10 == 0) {
                    fps = (1.seconds / screen.latestRenderTime).toInt()
                    deltaCount = 0
                }
                renderTime = screen.latestRenderTime

            }
            .renderOverlay { context, mouseX, mouseY, delta ->
                val contentBox = screen.contentBox(true)
                onRenderOverlay(context, mouseX, mouseY, delta)
                context.batchRenderBox {
                    pushBoxOutline(contentBox, Colors.ROSE)
                }
                context.batchRenderText {
                    val texts = listOf(
                        Literal("Screen renderTime:$renderTime").style { color(Colors.AQUA) },
                        Literal("Screen FPS:$fps").style { color(0x00FF00) },
                        Literal("MouseCursor:${MouseCursor.current.name}")
                    )
                    pushTextLines(texts, contentBox, align = BoxAlignment::TopLeft)
                    val mouse = listOf(
                        Literal("MouseX:$mouseX").style { color(Colors.RED) },
                        Literal("MouseY:$mouseY").style { color(0x00FF00) },
                    )
                    pushTextLines(
                        mouse,
                        contentBox.copy(height = contentBox.height + 2f),
                        defaultColor = Colors.ALIEN_GREEN.opacity(.3f),
                        align = BoxAlignment::BottomLeft,
                        backgroundColor = Colors.BLACK.opacity(.3f)
                    )
                }
            }
    }
) {
    Row(modifier = Modifier.padding(20f).renderOverlay { context, _, _, _ ->
        this as RowWidget
        context.batchRenderBox {
            pushBoxOutline(contentBox(true), Colors.ROSE)
        }
    }) {
        ColumnListWrapped(
            spacing = 3f,
        ) {
            repeat(50) {
                Button { Text("$it") }
            }
        }
        TestColumn()
        Scroller(
            { 1f },
            { 10f },
            { 0.1f },
            { BG_BLUR_RADIUS },
            { BG_BLUR_RADIUS = it },
            orientation = Orientation.Horizontal,
            modifier = Modifier.maxWidth(180f)
        )
    }


}

fun RowScope.TestColumn() = Column(
    modifier = Modifier.renderOverlay { context, _, _, _ ->
        this as IGWidget
        context.batchRenderBox {
            pushBoxOutline(transform, Colors.AQUA)
        }
    }.weight(8)
) {
    RowListWrapped(
        modifier = Modifier.width(120f),
        spacing = 2f,
        listModifier = { Modifier.weight(1) },
    ) {
        var c = 0
        var f = true
        TextField {
            text = "我去还这样嵌套?"
        }
        repeat(50) {
            val m = when (c) {
                0    -> Modifier.align(Alignment.Left)
                1    -> Modifier.align(Alignment.CenterHorizontally)
                2    -> Modifier.align(Alignment.Right)
                else -> Modifier.align(Alignment.CenterHorizontally)
            }

            if (it == 29) {
                Button(modifier = m.width(50f)) {
                    Icon(IconTextures.CLOSE)
                }
            } else if (it % 5 == 0) {
                Button(modifier = m) {
                    var text = ""
                    press {
                        text += "\n"
                        text += "测试宽度测试宽度测试宽度测试宽度"
                    }
                    Text(
                        text = {
                            Literal("测试文本$it:$text").style { color(Colors.BRIGHT_NEON_PINK) }
                        },
                        modifier = Modifier.renderOverlay { ctx, _, _, _ ->
                            this as IGWidget
                            if (wasMouseOver)
                                ctx.batchRenderBox {
                                    pushBoxOutline(contentBox(true), Colors.ROSE)
                                    pushBoxOutline(transform, Colors.MEDIUM_TEAL)
                                }
                        }
                    ) {
                        setting {
                            if (it == 15) spacing = 8f
                        }
                    }
                }
            } else {
                Button(modifier = m) {
                    Icon(
                        IconTextures.CLOSE, modifier = Modifier
                        .padding(2)
                        .renderOverlay { ctx, _, _, _ ->
                            this as IGWidget
                            if (wasMouseOver)
                                ctx.batchRenderBox {
                                    pushBoxOutline(transform, Colors.ROSE)
                                }
                        })
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

    Box(
        modifier = Modifier.renderOverlay { context, _, _, _ ->
            this as IGWidget
            context.batchRenderBox {
                pushBoxOutline(transform, Colors.AQUA)
            }
        }
    ) {
        Column {
            icons.forEach {
                Icon(it)
                Box(Modifier.width(5f))
            }
        }
    }
    Box(
        Modifier.size(80f, 80f)
            .renderOverlay { context, _, _, _ ->
                this as IGWidget
                context.batchRenderBox {
                    if (wasMouseOver) pushBoxOutline(transform, Colors.AQUA)
                }
            }
    ) {
        Icon(IconTextures.CLOSE, modifier = Modifier.alignment(BoxAlignment.TopLeft()))
        Icon(IconTextures.SEARCH, modifier = Modifier.alignment(BoxAlignment.TopRight()))
        Icon(IconTextures.MINUS, modifier = Modifier.alignment(BoxAlignment.BottomLeft()))
        Icon(IconTextures.LOCK, modifier = Modifier.alignment(BoxAlignment.BottomRight()))
        Icon(IconTextures.FILTER, modifier = Modifier.alignment(BoxAlignment.CenterCenter()))
    }
    Scroller({ 5f }, { 500f }, { 0.1f }, modifier = Modifier.maxHeight(180f))
    Button(
        modifier = Modifier.height(40f).renderOverlay { context, _, _, _ ->
            this as IGWidget
            measureTime {
                context.batchRenderBox {
                    if (wasMouseOver) pushBoxOutline(transform, Colors.AQUA)
                }
            }.let {
//                        println(it)
            }
        }
    ) {
        press {
            println("按下了测试按钮")
            mc.overlayMessage("按下了测试按钮")
        }
        longPress(10) {
            println("长按了按钮")
        }
        spacing(8f)
        Icon(IconTextures.CLOSE, modifier = Modifier.gravityStart().maxWidth(16f))
        Icon(IconTextures.SEARCH, modifier = Modifier.gravityCenter().maxWidth(16f))
        Icon(IconTextures.MINUS, modifier = Modifier.gravityEnd().maxWidth(16f))
        Icon(IconTextures.LOCK, modifier = Modifier.gravityCenter().maxWidth(16f))
        Icon(IconTextures.FILTER, modifier = Modifier.gravityStart().maxWidth(16f))
    }

}