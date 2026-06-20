@file:Suppress("unused")

package moe.forpleuvoir.ibukigourd.render.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.util.fastForEachIndexed
import moe.forpleuvoir.ibukigourd.render.extension.state.setXF
import moe.forpleuvoir.ibukigourd.render.extension.state.setYF
import moe.forpleuvoir.ibukigourd.render.peekScissorRect
import moe.forpleuvoir.ibukigourd.render.renderState
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.state.gui.GuiTextRenderState
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix3x2f

fun GuiGraphicsExtractor.pushText(
    text: FormattedCharSequence,
    x: Float,
    y: Float,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    renderState.addText(
        GuiTextRenderState(
            font,
            text,
            pose,
            x.toInt(),
            y.toInt(),
            color.argb,
            backgroundColor.argb,
            shadow,
            false,
            scissorArea
        ).apply {
            setXF(x)
            setYF(y)
        }
    )
}

fun GuiGraphicsExtractor.pushText(
    text: Component,
    x: Float,
    y: Float,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = pushText(text.visualOrderText, x, y, color, backgroundColor, shadow, pose, font, scissorArea)

fun GuiGraphicsExtractor.pushText(
    text: String,
    x: Float,
    y: Float,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = pushText(Language.getInstance().getVisualOrder(FormattedText.of(text)), x, y, color, backgroundColor, shadow, pose, font, scissorArea)

//region AlignmentText
fun GuiGraphicsExtractor.pushAlignmentText(
    text: FormattedCharSequence,
    area: IntRect,
    alignment: Alignment = Alignment.Center,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = alignment.align(text.size.toIntSize(), area.size, LayoutDirection.Ltr).run {
    pushText(text, (area.left + x).toFloat(), (area.top + y).toFloat(), color, backgroundColor, shadow, pose, font, scissorArea)
}

fun GuiGraphicsExtractor.pushAlignmentText(
    text: String,
    area: IntRect,
    alignment: Alignment = Alignment.Center,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = alignment.align(text.size.toIntSize(), area.size, LayoutDirection.Ltr).run {
    pushText(text, (area.left + x).toFloat(), (area.top + y).toFloat(), color, backgroundColor, shadow, pose, font, scissorArea)
}

fun GuiGraphicsExtractor.pushAlignmentText(
    text: Component,
    area: IntRect,
    alignment: Alignment = Alignment.Center,
    color: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0f),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = alignment.align(text.size.toIntSize(), area.size, LayoutDirection.Ltr).run {
    pushText(text, (area.left + x).toFloat(), (area.top + y).toFloat(), color, backgroundColor, shadow, pose, font, scissorArea)
}
//endregion

//region TextLines
private val density = Density(1f)

fun GuiGraphicsExtractor.pushStringLines(
    string: String,
    area: IntRect,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    defaultColor: Color = Colors.BLACK,
    backgroundColor: Color = Color.fromARGB(0),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val texts = string.wrapToLines(area.width.toFloat())
    val verticalOffsets = IntArray(texts.size)
    verticalArrangement.run {
        density.arrange(area.height, IntArray(texts.size) { font.lineHeight }, verticalOffsets)
    }
    val horizontalOffsets = texts.map { horizontalAlignment.align(it.width.toInt(), area.width, LayoutDirection.Ltr) }

    verticalOffsets.zip(horizontalOffsets).fastForEachIndexed { idx, (y, x) ->
        pushText(texts[idx], area.left + x.toFloat(), area.top + y.toFloat(), defaultColor, backgroundColor, shadow, pose, font, scissorArea)
    }
}

fun GuiGraphicsExtractor.pushStringLines(
    string: List<String>,
    area: IntRect,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    defaultColor: Color = Colors.BLACK,
    backgroundColor: Color = Color.fromARGB(0),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val texts = string.wrapToLines(area.width.toFloat())
    val verticalOffsets = IntArray(texts.size)
    verticalArrangement.run {
        density.arrange(area.height, IntArray(texts.size) { font.lineHeight }, verticalOffsets)
    }
    val horizontalOffsets = texts.map { horizontalAlignment.align(it.width.toInt(), area.width, LayoutDirection.Ltr) }

    verticalOffsets.zip(horizontalOffsets).fastForEachIndexed { idx, (y, x) ->
        pushText(texts[idx], area.left + x.toFloat(), area.top + y.toFloat(), defaultColor, backgroundColor, shadow, pose, font, scissorArea)
    }
}

fun GuiGraphicsExtractor.pushTextLines(
    text: Text,
    area: IntRect,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    defaultColor: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val texts = text.wrapToTextLines(area.width.toFloat())
    val verticalOffsets = IntArray(texts.size)
    verticalArrangement.run {
        density.arrange(area.height, IntArray(texts.size) { font.lineHeight }, verticalOffsets)
    }
    val horizontalOffsets = texts.map { horizontalAlignment.align(it.width.toInt(), area.width, LayoutDirection.Ltr) }

    verticalOffsets.zip(horizontalOffsets).fastForEachIndexed { idx, (y, x) ->
        pushText(texts[idx], area.left + x.toFloat(), area.top + y.toFloat(), defaultColor, backgroundColor, shadow, pose, font, scissorArea)
    }
}

fun GuiGraphicsExtractor.pushTextLines(
    text: List<Component>,
    area: IntRect,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    defaultColor: Color = Colors.BLACK,
    backgroundColor: Color = Colors.BLACK.alpha(0),
    shadow: Boolean = false,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    font: Font = textRenderer,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val texts = text.wrapToTextLines(area.width.toFloat())
    val verticalOffsets = IntArray(texts.size)
    verticalArrangement.run {
        density.arrange(area.height, IntArray(texts.size) { font.lineHeight }, verticalOffsets)
    }
    val horizontalOffsets = texts.map { horizontalAlignment.align(it.width.toInt(), area.width, LayoutDirection.Ltr) }

    verticalOffsets.zip(horizontalOffsets).fastForEachIndexed { idx, (y, x) ->
        pushText(texts[idx], area.left + x.toFloat(), area.top + y.toFloat(), defaultColor, backgroundColor, shadow, pose, font, scissorArea)
    }
}
//endregion



