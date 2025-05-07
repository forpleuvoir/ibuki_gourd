package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.tick
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds

fun Modifier.debugInfo(): Modifier {
    var deltaCount = 0
    var fps = 0
    var renderTime = 0.seconds
    return Modifier
        .tick {
            this as IGScreenImpl
            onTick()
            deltaCount++
            if (deltaCount % 10 == 0) {
                fps = (1.seconds / this.latestRenderTime).toInt()
                deltaCount = 0
            }
            renderTime = this.latestRenderTime

        }
        .renderOverlay { context, mouseX, mouseY, delta ->
            this as IGScreenImpl
            val contentBox = contentBox(true).trimEdges(8f)
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