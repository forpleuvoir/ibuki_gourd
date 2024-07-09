package moe.forpleuvoir.ibukigourd.gui.widget.text

import com.mojang.blaze3d.platform.GlStateManager
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.extensions.asBox
import moe.forpleuvoir.ibukigourd.gui.extensions.contentBox
import moe.forpleuvoir.ibukigourd.gui.extensions.drawcontent.*
import moe.forpleuvoir.ibukigourd.gui.extensions.mouseHoveredContent
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.texture.WidgetTextures.TEXT_INPUT
import moe.forpleuvoir.ibukigourd.gui.render.texture.WidgetTextures.TEXT_SELECTED_INPUT
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.render.useColorLogicOp
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.ifc
import moe.forpleuvoir.nebula.common.pick
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.util.StringHelper
import net.minecraft.util.Util
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate", "Unused")
open class TextInput(
    x: Int,
    y: Int,
    width: Int = 60,
    height: Int = 20,
    var textColor: ARGBColor = Color(0x303030),
    var hintColor: ARGBColor = Color(0x707070),
    var bgShaderColor: ARGBColor = Colors.WHITE,
    var selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    var cursorColor: ARGBColor = Colors.BLACK,
    val padding: Padding = Padding(6, 6, 6, 6),
    message: Text = Literal("textInput"),
    private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : PressableWidget(x, y, width, height, message), Tickable {

    var text: String = ""
        set(value) {
            if (value != field && textPredicate(value)) {
                field = value
                onTextChanged(field)
            }
        }

    val history: HistoryRecord = HistoryRecord(currentRecord = HistoryRecord.Record(text, cursor))

    var hintText: Text? = null

    var suggestion: ((text: String) -> String)? = null

    var onTextChanged: (text: String) -> Unit = {}

    var textPredicate: (text: String) -> Boolean = { true }

    /**
     * The index of the leftmost character that is rendered on a screen.
     */
    var firstCharacterIndex: Int = 0

    var selectionStart: Int = 0
        set(value) {
            field = value.clamp(0, text.length)
        }

    var selectionEnd: Int = 0
        set(value) {
            val textLength = text.length
            field = value.clamp(0, textLength)
            if (firstCharacterIndex > textLength) {
                firstCharacterIndex = textLength
            }
            val width: Int = contentBox(padding).width.toInt()
            val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), width)
            val k = string.length + firstCharacterIndex
            if (field == firstCharacterIndex) {
                firstCharacterIndex -= textRenderer.trimToWidth(text, width, true).length
            }
            if (field > k) {
                firstCharacterIndex += field - k
            } else if (field <= firstCharacterIndex) {
                firstCharacterIndex -= firstCharacterIndex - field
            }
            firstCharacterIndex = firstCharacterIndex.clamp(0, textLength)
        }

    var cursor: Int
        get() = selectionStart
        set(value) {
            selectionStart = value
            if (!selecting) {
                selectionEnd = selectionStart
            }
            onTextChanged(text)
        }

    var maxLength = 255
        set(value) {
            field = value
            if (text.length > value) {
                text = text.substring(0, value)
                this.onTextChanged(text)
            }
        }

    private var selecting = false

    var onFocusedChanged: ((Boolean) -> Unit)? = {
        if (!it) {
            selectionStart = 0
            selectionEnd = 0
        }
    }

    private var focusedTicks = 0

    override fun tick() {
        history.tick()
        if (isFocused) {
            ++focusedTicks
        } else {
            focusedTicks = 0
        }
    }

    val selectedText: String
        get() {
            return text.substring(
                min(selectionStart, selectionEnd).coerceAtMost(text.length).coerceAtLeast(0),
                max(selectionStart, selectionEnd).coerceAtMost(text.length).coerceAtLeast(0)
            )
        }

    fun write(text: String, historyOpt: Boolean = false) {
        var string2: String
        var string: String
        var l: Int
        val i = selectionStart.coerceAtMost(selectionEnd)
        val j = selectionStart.coerceAtLeast(selectionEnd)
        val k = maxLength - this.text.length - (i - j)
        if (k < StringHelper.stripInvalidChars(text).also { string = it }.length.also { l = it }) {
            string = string.substring(0, k)
            l = k
        }
        if (!textPredicate(StringBuilder(this.text).replace(i, j, string).toString().also { string2 = it })) {
            return
        }
        this.text = string2
        this.selectionStart = i + l
        this.selectionEnd = selectionStart
        if (!historyOpt)
            history.textChange(this.text, cursor)
    }


    fun erase(offset: Int) {
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
            this.eraseWords(offset)
        } else {
            this.eraseCharacters(offset)
        }
    }

    fun eraseWords(wordOffset: Int) {
        if (text.isEmpty()) {
            return
        }
        if (selectionEnd != selectionStart) {
            write("")
            return
        }
        this.eraseCharacters(this.getWordSkipPosition(wordOffset) - selectionStart)
    }

    fun eraseCharacters(characterOffset: Int) {
        var k: Int
        if (text.isEmpty()) {
            return
        }
        if (selectionEnd != selectionStart) {
            write("")
            return
        }
        val i: Int = this.getCursorPosWithOffset(characterOffset)
        val j = i.coerceAtMost(selectionStart)
        if (j == i.coerceAtLeast(selectionStart).also { k = it }) {
            return
        }
        val string = StringBuilder(text).delete(j, k).toString()
        if (!textPredicate(string)) {
            return
        }
        text = string
        history.textChange(this.text, cursor)
        cursor = j
    }

    private fun getWordSkipPosition(wordOffset: Int): Int {
        return getWordSkipPosition(wordOffset, cursor)
    }

    private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int, skipOverSpaces: Boolean = false): Int {
        var resultCursor = cursorPosition
        val leftOffset = wordOffset < 0
        val offset = abs(wordOffset)
        repeat(offset) {
            if (leftOffset) {
                if (!skipOverSpaces && resultCursor > 0 && text[resultCursor - 1] == ' ') {
                    --resultCursor
                    return@repeat
                }
                while (skipOverSpaces && resultCursor > 0 && text[resultCursor - 1] == ' ') {
                    --resultCursor
                }
                while (resultCursor > 0 && text[resultCursor - 1] != ' ') {
                    --resultCursor
                }
                return@repeat
            }
            val length = text.length
            if (!skipOverSpaces && resultCursor < text.length && text[resultCursor] == ' ') {
                ++resultCursor
                return@repeat
            }
            if (text.indexOf(32.toChar(), resultCursor).also { resultCursor = it } == -1) {
                resultCursor = length
                return@repeat
            }
            while (skipOverSpaces && resultCursor < length && text[resultCursor] == ' ') {
                ++resultCursor
            }
        }
        return resultCursor
    }

    fun moveCursor(offset: Int) {
        cursor = getCursorPosWithOffset(offset)
    }

    private fun getCursorPosWithOffset(offset: Int): Int {
        return Util.moveCursor(text, selectionStart, offset)
    }

    fun setCursorToStart() {
        cursor = 0
    }

    fun setCursorToEnd() {
        cursor = text.length
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!this.isActive) return false
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        //全选文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
            setCursorToEnd()
            this.selectionEnd = 0
            return true
        }
        //复制选中文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
            mc.keyboard.clipboard = this.selectedText
            return true
        }
        //粘贴文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
            write(mc.keyboard.clipboard)
            return true
        }
        //剪切选中文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
            mc.keyboard.clipboard = this.selectedText
            write("")
            return true
        }
        //撤销
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
            setCursorToEnd()
            this.selectionEnd = 0
            history.undo(text, cursor).let {
                write(it.text, true)
                cursor = it.cursor
            }
            return true
        }
        //重做
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
            setCursorToEnd()
            this.selectionEnd = 0
            history.redo(text, cursor).let {
                write(it.text, true)
                cursor = it.cursor
            }
            return true
        }
        when (keyCode) {
            //制表符，如果有建议文本则补全建议文本，否则输入四个空格
            Keyboard.TAB.code       -> {
                if (suggestion != null) {
                    suggestion!!(text).let {
                        if (it.isNotEmpty()) {
                            write(it)
                            return true
                        }
                    }
                    write("    ")
                }
                return true
            }
            //光标左移,如果按下左控制键则跳过一个单词
            Keyboard.LEFT.code      -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    cursor = this.getWordSkipPosition(-1)
                } else {
                    moveCursor(-1)
                }
                return true
            }
            //光标右移,如果按下左控制键则跳过一个单词
            Keyboard.RIGHT.code     -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    cursor = this.getWordSkipPosition(1)
                } else {
                    moveCursor(1)
                }
                return true
            }
            //删除一个字符
            Keyboard.BACKSPACE.code -> {
                selecting = false
                erase(-1)
                selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                return true
            }
            //删除一个字符
            Keyboard.DELETE.code    -> {
                selecting = false
                erase(1)
                selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                return true
            }
            //将光标移动到文本开头
            Keyboard.HOME.code      -> {
                setCursorToStart()
                return true
            }
            //将光标移动到文本结尾
            Keyboard.END.code       -> {
                setCursorToEnd()
                return true
            }
        }
        return false
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        selecting = if (keyCode == Keyboard.LEFT_SHIFT.code) false else selecting
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    val isActive: Boolean
        get() {
            return visible && isFocused
        }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (!isActive) return false
        if (StringHelper.isValidChar(chr)) {
            write(chr.toString())
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseHoveredContent(mouseX, mouseY, padding) && button == Mouse.LEFT.code) {
            val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentBox(padding).width.toInt())
            cursor = textRenderer.trimToWidth(string, (mouseX - this.x - padding.left + 3).toInt()).length + firstCharacterIndex
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!isActive) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        mouseHoveredContent(mouseX, mouseY, padding) {
            moveCursor((verticalAmount < 0f).pick(1, -1))
            return true
        }
        return false
    }

    override fun onDrag(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double) {
        selecting = true
        val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentBox(padding).width.toInt())
        cursor = textRenderer.trimToWidth(string, (mouseX - x - padding.left + 3f).toInt()).length + firstCharacterIndex
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
    }

    fun onRenderBackground(context: DrawContext) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(asBox, isFocused.pick(TEXT_SELECTED_INPUT, TEXT_INPUT), bgShaderColor)
        }
    }

    fun renderCursor(content: DrawContext) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val box = contentBox(padding)
            val height = textRenderer.fontHeight.toFloat()
            val y = box.top + (box.height - height) / 2f - 0.75f
            val offset = textRenderer.getWidth(
                text.substring(
                    min(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0),
                    max(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0)
                )
            )
            if (cursor == text.length) {
                content.renderBox(Box(box.position.copy(box.left + offset, y + height - 1.25f), 5f, 1f), cursorColor)
                return
            }
            if (cursor - firstCharacterIndex > 0) {
                content.renderBox(Box(box.position.copy(box.left + offset - 0.85f, y), 1f, height), cursorColor)
            } else {
                content.renderBox(Box(box.position.copy(y = y), 1f, height), cursorColor)
            }
        }
    }

    fun renderText(content: DrawContext) {
        val contentRect = contentBox(padding)
        content.useMatrixStack {
            it.translate(0.0f, 0.4f, 0f)
            content.batchRenderText(textRenderer) {
                //"渲染提示文本"
                if (text.isEmpty() && hintText != null && !isFocused) {
                    alignmentText(hintText!!, contentRect, color = hintColor)
                }
                //"渲染文本本体"
                val renderText = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentRect.width.toInt())
                renderText.isNotEmpty().ifc {
                    alignmentText(renderText, contentRect, color = textColor)
                }
                //"渲染文本建议"
                suggestion?.invoke(text)?.let { suggestion ->
                    if (isFocused && cursor == text.length) {
                        val renderTextWidth = textRenderer.getWidth(renderText).toFloat()
                        val rect =
                            Box(contentRect.position.copy(contentRect.position.x() + renderTextWidth), contentRect.width - renderTextWidth, contentRect.height)
                        alignmentText(suggestion, rect)
                    }
                }
            }
        }


        //"渲染选中的文本高亮"
        useColorLogicOp(GlStateManager.LogicOp.OR_REVERSE) {
            if (selectedText.isNotEmpty() && isFocused) {
                val (startIndex, endIndex) = (selectionStart - firstCharacterIndex).coerceAtLeast(0) to
                        (selectionEnd - firstCharacterIndex).coerceAtLeast(0)
                val start = contentRect.left +
                        if (startIndex > 0)
                            textRenderer.getWidth(text.substring(firstCharacterIndex, firstCharacterIndex + startIndex)).toFloat()
                        else 0f
                val end = contentRect.left +
                        if (endIndex > 0)
                            textRenderer.getWidth(text.substring(firstCharacterIndex, firstCharacterIndex + endIndex)).toFloat()
                        else 0f
                val width = (start - end).absoluteValue
                val rect = if (selectionEnd > selectionStart) {
                    Box(contentRect.position.copy(start), width, contentRect.height)
                } else {
                    Box(contentRect.position.copy(end), width, contentRect.height)
                }
                content.renderBox(rect, selectedColor)
            }
        }
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!isActive && !visible) return
        onRenderBackground(context)
        context.batchRenderBox {
            context.boxOutline(contentBox(padding), Colors.RED)
        }
        context.scissor(contentBox(padding)) {
            renderText(context)
            renderCursor(context)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, this.narrationMessage)
    }

    override fun onPress() {}

}
