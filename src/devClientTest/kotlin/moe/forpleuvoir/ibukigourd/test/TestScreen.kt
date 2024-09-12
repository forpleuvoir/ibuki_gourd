package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.screen.BoxScreen
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.FloatSlider
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextArea
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.overlayMessage
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.plus
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds

fun modifier(): Modifier {
    var deltaCount = 0
    var fps = 0
    var renderTime = 0.seconds
    return Modifier.padding(8f)
        .tick {
            this as IGScreenImpl<*>
            onTick()
            deltaCount++
            if (deltaCount % 10 == 0) {
                fps = (1.seconds / this.latestRenderTime).toInt()
                deltaCount = 0
            }
            renderTime = this.latestRenderTime

        }
        .renderOverlay { context, mouseX, mouseY, delta ->
            this as IGScreenImpl<*>
            val contentBox = contentBox(true)
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
                pushTextLines(texts, contentBox, Alignment.Left, Arrangement.Top)
                val mouse = listOf(
                    Literal("MouseX:$mouseX").style { color(Colors.RED) },
                    Literal("MouseY:$mouseY").style { color(0x00FF00) },
                )
                pushTextLines(
                    mouse,
                    contentBox.copy(height = contentBox.height + 2f),
                    Alignment.Left, Arrangement.Bottom,
                    defaultColor = Colors.ALIEN_GREEN.opacity(.3f),
                    backgroundColor = Colors.BLACK.opacity(.3f)
                )
            }
        }
}

fun TestScreen() = BoxScreen(modifier()) {
    Row(
        modifier = Modifier
            .padding(20f)
            .align(Alignment.Center)
            .renderOverlay { context, _, _, _ ->
                this as RowWidget
                context.batchRenderBox {
                    pushBoxOutline(contentBox(true), Colors.BLUE)
                }
            },
        verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
    ) {
        ColumnListWrapped(
            spacing = 3f,
            modifier = Modifier.weight(2),
            listModifier = { Modifier.weight(1).renderHoveredOutlineBox(Colors.PARCHMENT) }
        ) {
            var c = 0
            var f = true
            repeat(50) {
                val m = when (c) {
                    0    -> Modifier.align(Alignment.Top)
                    1    -> Modifier.align(Alignment.CenterVertically)
                    2    -> Modifier.align(Alignment.Bottom)
                    else -> Modifier.align(Alignment.CenterVertically)
                }
                Button(m) {
                    if (it == 12) {
                        HoverTip {
                            Button { TextLabel("悬浮测试") }
                        }
                    }
                    TextLabel("$it")
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
        TestColumn()
        FloatSlider(
            mutableStateOf(GuiConfig.Screen::BG_BLUR_RADIUS),
            0f..25f,
            textMapper = { Literal("背景模糊:%.2f".format(it)) },
            modifier = Modifier.minWidth(120f)
        )
    }


}

fun RowScope.TestColumn() = Column(
    modifier = Modifier.renderOverlay { context, _, _, _ ->
        context.batchRenderBox {
            pushBoxOutline(transform, Colors.AQUA)
        }
    }.weight(5),
    horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
) {
    RowListWrapped(
        modifier = Modifier.width(120f),
        spacing = 2f,
        listModifier = { Modifier.weight(1) }
    ) {
        var c = 0
        var f = true
        TextArea {
            text = "我去还这样嵌套?"
        }
        repeat(50) {
            val m = when (c) {
                0 -> Modifier.align(Alignment.Left)
                1 -> Modifier.align(Alignment.CenterHorizontally)
                2 -> Modifier.align(Alignment.Right)
                else -> Modifier.align(Alignment.CenterHorizontally)
            }

            if (it == 29) {
                Button(modifier = m.width(50f)) {
                    Icon(IconTextures.CLOSE)
                }
            } else if (it % 5 == 0) {
                Button(modifier = m) {
                    val text = mutableStateOf("测试文本:")
                    press {
                        text + "\n测试宽度测试宽度测试宽度测试宽度"
                    }
                    TextLabel(
                        str = text,
                        style = style(color = Colors.BRIGHT_NEON_PINK),
                        modifier = Modifier.renderOverlay { ctx, _, _, _ ->
                            if (wasMouseOver)
                                ctx.batchRenderBox {
                                    pushBoxOutline(contentBox(true), Colors.ROSE)
                                    pushBoxOutline(transform, Colors.MEDIUM_TEAL)
                                }
                        }
                    )
                }
            } else {
                Button(modifier = m) {
                    Icon(
                        IconTextures.CLOSE, modifier = Modifier
                            .padding(2)
                            .renderOverlay { ctx, _, _, _ ->
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
        modifier = Modifier
            .matchSibling()
            .renderOverlay { context, _, _, _ ->
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
                context.batchRenderBox {
                    if (wasMouseOver) pushBoxOutline(transform, Colors.AQUA)
                }
            }
    ) {
        Icon(IconTextures.CLOSE, modifier = Modifier.align(Alignment.TopLeft))
        Icon(IconTextures.SEARCH, modifier = Modifier.align(Alignment.TopRight))
        Icon(IconTextures.MINUS, modifier = Modifier.align(Alignment.BottomLeft))
        Icon(IconTextures.LOCK, modifier = Modifier.align(Alignment.BottomRight))
        Icon(IconTextures.FILTER, modifier = Modifier.align(Alignment.Center))
    }

    Scroller(
        scrollState = ScrollState().apply {
            amountStep = 1f
            maxAmount = 10f
            barProportion = 0.1f
            amount = 0f
        },
        modifier = Modifier.maxHeight(180f)
    )
    Button(
        modifier = Modifier.height(40f).renderOverlay { context, _, _, _ ->
            context.batchRenderBox {
                if (wasMouseOver) pushBoxOutline(transform, Colors.AQUA)
            }
        },
        horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
    ) {
        press {
            println("按下了测试按钮")
            mc.overlayMessage("按下了测试按钮")
        }
        longPress(10) {
            println("长按了按钮")
        }
        Icon(IconTextures.CLOSE, modifier = Modifier.align(Alignment.Top).maxWidth(16f))
        Icon(IconTextures.SEARCH, modifier = Modifier.align(Alignment.CenterVertically).maxWidth(16f))
        Icon(IconTextures.MINUS, modifier = Modifier.align(Alignment.Bottom).maxWidth(16f))
        Icon(IconTextures.LOCK, modifier = Modifier.align(Alignment.CenterVertically).maxWidth(16f))
        Icon(IconTextures.FILTER, modifier = Modifier.align(Alignment.Top).maxWidth(16f))
    }

}