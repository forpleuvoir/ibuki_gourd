package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.toDuration

fun interface DebugInfoEntry {
    fun text(screen: IGScreen, guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float): Text
}

data class DebugInfoScope(internal val entries: MutableList<DebugInfoEntry> = mutableListOf()) {

    fun info(entry: DebugInfoEntry) {
        entries.add(entry)
    }

}

private fun IGScreen.renderInfoUpdateTime(key: String): ValueTimeMark =
    (this as? IGScreenImpl)?.userData["#debug_render_info_update_time_${key}"] as? ValueTimeMark ?: run {
        val mark = TimeSource.Monotonic.markNow()
        (this as? IGScreenImpl)?.userData["#debug_render_info_update_time_${key}"] = mark
        mark
    }

private fun IGScreen.renderInfoUpdateTimeMark(key: String) {
    this.userData["#debug_render_info_update_time_${key}"] = TimeSource.Monotonic.markNow()
}

private inline fun IGScreen.updateRenderInfo(key: String, action: () -> Unit) {
    if (renderInfoUpdateTime(key).elapsedNow() > 0.5.seconds) {
        renderInfoUpdateTimeMark(key)
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

fun DebugInfoScope.ScreenRenderTime(format: String = "RenderTime:%s", color: ARGBColor = Colors.AQUA) = info { screen, _, _, _, _ ->
    (screen as? IGScreenImpl)?.let {
        it addDebugRenderTime screen.latestFrameRenderTime
        it.updateRenderInfo("renderTime") { it.userData["#debug_render_time"] = screen.debugAverageRenderTimes }
    }
    Literal(format.format(screen.userData["#debug_render_time"])).style { color(color) }
}

fun DebugInfoScope.ScreenFPS(format: String = "FPS:%d", color: ARGBColor = Color.ofRGB(0x00FF00)) = info { screen, _, _, _, _ ->
    (screen as? IGScreenImpl)?.let {
        it addDebugRenderTime screen.latestFrameRenderTime
        it.updateRenderInfo("fps") { it.userData["#debug_render_fps"] = (1.seconds / screen.debugAverageRenderTimes).toInt() }
    }
    Literal(format.format(screen.userData["#debug_render_fps"])).style { color(color) }
}

fun DebugInfoScope.MouseCursor(format: String = "MouseCursor:%s", color: ARGBColor = Colors.AQUA) = info { _, _, _, _, _ ->
    Literal(format.format(MouseCursor.current.name)).style { color(color) }
}

fun DebugInfoScope.MousePosition(
    xFormat: String = "MouseX:%.2f",
    xColor: ARGBColor = Colors.RED,
    yFormat: String = "MouseY:%.2f",
    yColor: ARGBColor = Color.ofRGB(0x00FF00),
    connector: String = "\n",
    connectorColor: ARGBColor = Colors.BLACK
) = info { _, _, mouseX, mouseY, delta ->
    Literal(xFormat.format(mouseX)).style { color(xColor) }
        .append(Literal(connector).withColor(connectorColor))
        .append(Literal(yFormat.format(mouseY)).style { color(yColor) })
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
    return this.renderOverlay { guiGraphics, mouseX, mouseY, delta ->
        onRenderOverlay(guiGraphics, mouseX, mouseY, delta)
        this as IGScreen
        val texts = s.entries.map { it.text(this, guiGraphics, mouseX, mouseY, delta) }.wrapToTextLines()
        val textSize = Size(texts.maxWidth, texts.totalHeight(verticalArrangement.spacing))
        val box = this.transform.asWorldCoordinateBox.trimEdges(padding.top, padding.bottom, padding.left, padding.right)
        val x = box.x + horizontalAlignment.align(box.width, textSize.width)
        val y = box.y + verticalArrangement.arrange(box.height, listOf(textSize.height))[0]

        guiGraphics {
            pushRoundBox(Box(x, y, textSize).expandEdges(bgRound.toFloat()), bgColor, bgRound)
            pushTextLines(
                texts,
                box,
                horizontalAlignment,
                verticalArrangement,
                Colors.BLACK,
                textBgColor,
                false
            )
        }
    }
}
