package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.totalHeight
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun interface DebugInfoEntry {
    fun text(screen: IGScreen, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float): Text
}

data class DebugInfoScope(internal val entries: MutableList<DebugInfoEntry> = mutableListOf()) {

    fun info(entry: DebugInfoEntry) {
        entries.add(entry)
    }

}

private var IGScreen.renderInfoUpdateTime: Float
    get() = (this as? IGScreenImpl)?.userData["#debug_render_info_update_time"] as? Float ?: 0f
    set(value) {
        this.userData["#debug_render_info_update_time"] = value
    }

private inline fun IGScreen.updateRenderInfo(delta: Float, action: () -> Unit) {
    this.renderInfoUpdateTime += delta
    if (this.renderInfoUpdateTime > 10f) {
        this.renderInfoUpdateTime = 0f
        action()
    }
}

@Suppress("UNCHECKED_CAST")
private val IGScreen.debugRenderTimes: MutableList<Duration>
    get() = (userData["#debug_render_times"] as? MutableList<Duration>) ?: run {
        mutableListOf<Duration>().apply {
            userData["#debug_render_times"] = this
        }
    }

private infix fun IGScreen.addDebugRenderTime(time: Duration) {
    debugRenderTimes.add(time)
    if (this.debugRenderTimes.size > 500) {
        debugRenderTimes.clear()
    }
}

private val IGScreen.debugAverageRenderTimes: Duration
    get() = debugRenderTimes.map { it.inWholeNanoseconds }.average().let {
        if (it.isNaN()) 0.seconds
        else it.toDuration(DurationUnit.NANOSECONDS)
    }

fun DebugInfoScope.ScreenRenderTime() = info { screen, context, mouseX, mouseY, delta ->
    (screen as? IGScreenImpl)?.let {
        it addDebugRenderTime screen.latestRenderTime
        it.updateRenderInfo(delta) { it.userData["#debug_render_time"] = screen.debugAverageRenderTimes }
    }
    Literal("RenderTime:${screen.userData["#debug_render_time"]}").style { color(Colors.AQUA) }
}

fun DebugInfoScope.ScreenFPS() = info { screen, context, mouseX, mouseY, delta ->
    (screen as? IGScreenImpl)?.let {
        it addDebugRenderTime screen.latestRenderTime
        it.updateRenderInfo(delta) { it.userData["#debug_render_fps"] = (1.seconds / screen.debugAverageRenderTimes).toInt() }
    }
    Literal("FPS:${screen.userData["#debug_render_fps"]}").style { color(0x00FF00) }
}

fun DebugInfoScope.MouseCursor() = info { screen, context, mouseX, mouseY, delta ->
    Literal("MouseCursor:${MouseCursor.current.name}").style { color(Colors.AQUA) }
}

fun DebugInfoScope.MousePosition() = info { screen, context, mouseX, mouseY, delta ->
    Literal("MouseX:$mouseX").style { color(Colors.RED) }.appendLiteral(", ").append(Literal("MouseY:$mouseY").style { color(0x00FF00) })
}

fun Modifier.debugInfo(
    bgColor: ARGBColor = Colors.BLACK.opacity(.2f),
    bgRound: Int = 2,
    textBgColor: ARGBColor = Colors.BLACK.opacity(.0f),
    padding: Padding = Padding(8f),
    horizontalAlignment: Alignment.Horizontal = Alignment.Left,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(1f, Alignment.Top),
    scope: DebugInfoScope.() -> Unit
): Modifier {
    val s = DebugInfoScope().apply(scope)
    return this.renderOverlay { context, mouseX, mouseY, delta ->
        this as IGScreen
        val texts = s.entries.map { it.text(this, context, mouseX, mouseY, delta) }
        val textSize = Size(texts.maxWidth, texts.totalHeight(verticalArrangement.spacing))
        val box = this.transform.asWorldCoordinateBox.trimEdges(padding.top, padding.bottom, padding.left, padding.right)
        val x = box.x + horizontalAlignment.align(box.width, textSize.width)
        val y = box.y + verticalArrangement.arrange(box.height, listOf(textSize.height))[0]
        context.batchRenderBox {
            pushRoundBox(Box(x, y, textSize), bgColor, bgRound)
        }
        context.batchRenderText {
            pushTextLines(texts, box, horizontalAlignment, verticalArrangement, backgroundColor = textBgColor)
        }
    }
}
