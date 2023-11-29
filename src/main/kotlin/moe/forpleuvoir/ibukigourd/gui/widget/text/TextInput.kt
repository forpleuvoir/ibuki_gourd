package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.base.mouseHoverContent
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TEXT_INPUT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TEXT_SELECTED_INPUT
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.BACKGROUND_SHADER_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.CURSOR_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.HINT_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.SELECTED_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.TEXT_COLOR
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.batchRender
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.ifc
import moe.forpleuvoir.nebula.common.ternary
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.SharedConstants
import net.minecraft.client.font.TextRenderer
import net.minecraft.util.Util
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate", "Unused")
open class TextInput(
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    var textColor: ARGBColor = TEXT_COLOR,
    var hintColor: ARGBColor = HINT_COLOR,
    var bgShaderColor: ARGBColor = BACKGROUND_SHADER_COLOR,
    var selectedColor: ARGBColor = SELECTED_COLOR,
    var cursorColor: ARGBColor = CURSOR_COLOR,
    private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : ClickableElement() {

    override val focusable: Boolean = true

    init {
        transform.width = width.also { transform.fixedWidth = true }
        transform.height = height.also { transform.fixedHeight = true }
        padding?.let(::padding)
        margin?.let(::margin)
    }

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
            val width: Int = this.contentRect(true).width.toInt()
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

    override var onFocusedChanged: ((Boolean) -> Unit)? = {
        if (!it) {
            selectionStart = 0
            selectionEnd = 0
        }
    }

    private var focusedTicks = 0

    override fun tick() {
        super.tick()
        history.tick()
        if (focused) {
            ++focusedTicks
        } else {
            focusedTicks = 0
        }
    }

    val selectedText: String
        get() {
            return text.substring(
                min(selectionStart, selectionEnd).coerceAtMost(text.lastIndex).coerceAtLeast(0),
                max(selectionStart, selectionEnd).coerceAtMost(text.lastIndex).coerceAtLeast(0)
            )
        }

    fun write(text: String, historyOpt: Boolean = false) {
        var string2: String
        var string: String
        var l: Int
        val i = selectionStart.coerceAtMost(selectionEnd)
        val j = selectionStart.coerceAtLeast(selectionEnd)
        val k = maxLength - this.text.length - (i - j)
        if (k < SharedConstants.stripInvalidChars(text).also { string = it }.length.also { l = it }) {
            string = string.substring(0, k)
            l = k
        }
        if (!textPredicate(StringBuilder(this.text).replace(i, j, string).toString().also { string2 = it })) {
            return
        }
        this.text = string2
        this.selectionStart = i + l
        this.selectionEnd = selectionStart
        this.onTextChanged(this.text)
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

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (!this.isActive) return NextAction.Continue
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        //全选文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
            setCursorToEnd()
            this.selectionEnd = 0
            return NextAction.Cancel
        }
        //复制选中文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
            mc.keyboard.clipboard = this.selectedText
            return NextAction.Cancel
        }
        //粘贴文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
            write(mc.keyboard.clipboard)
            return NextAction.Cancel
        }
        //剪切选中文本
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
            mc.keyboard.clipboard = this.selectedText
            write("")
            return NextAction.Cancel
        }
        //撤销
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
            setCursorToEnd()
            this.selectionEnd = 0
            history.undo(text, cursor).let {
                write(it.text, true)
                cursor = it.cursor
            }
            return NextAction.Cancel
        }
        //重做
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
            setCursorToEnd()
            this.selectionEnd = 0
            history.redo(text, cursor).let {
                write(it.text, true)
                cursor = it.cursor
            }
            return NextAction.Cancel
        }
        when (keyCode) {
            //制表符，如果有建议文本则补全建议文本，否则输入四个空格
            Keyboard.TAB       -> {
                if (suggestion != null) {
                    suggestion!!(text).let {
                        if (it.isNotEmpty()) {
                            write(it)
                            return NextAction.Cancel
                        }
                    }
                    write("    ")
                }
                return NextAction.Cancel
            }
            //光标左移,如果按下左控制键则跳过一个单词
            Keyboard.LEFT      -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    cursor = this.getWordSkipPosition(-1)
                } else {
                    moveCursor(-1)
                }
                return NextAction.Cancel
            }
            //光标右移,如果按下左控制键则跳过一个单词
            Keyboard.RIGHT     -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    cursor = this.getWordSkipPosition(1)
                } else {
                    moveCursor(1)
                }
                return NextAction.Cancel
            }
            //删除一个字符
            Keyboard.BACKSPACE -> {
                selecting = false
                erase(-1)
                selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                return NextAction.Cancel
            }
            //删除一个字符
            Keyboard.DELETE    -> {
                selecting = false
                erase(1)
                selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                return NextAction.Cancel
            }
            //将光标移动到文本开头
            Keyboard.HOME      -> {
                setCursorToStart()
                return NextAction.Cancel
            }
            //将光标移动到文本结尾
            Keyboard.END       -> {
                setCursorToEnd()
                return NextAction.Cancel
            }
        }
        return NextAction.Continue
    }

    override fun onKeyRelease(keyCode: KeyCode): NextAction {
        selecting = if (keyCode == Keyboard.LEFT_SHIFT) false else selecting
        return super.onKeyRelease(keyCode)
    }


    val isActive: Boolean
        get() {
            return visible && focused
        }

    override fun onCharTyped(chr: Char): NextAction {
        if (!isActive) return NextAction.Continue
        if (SharedConstants.isValidChar(chr)) {
            write(chr.toString())
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (!active) return NextAction.Continue
        for (element in handleElements) {
            if (element.mouseMove(mouseX, mouseY) == NextAction.Cancel) return NextAction.Cancel
        }
        if (!visible) return NextAction.Continue
        screen().let {
            if (!mouseHoverContent(it.preMousePosition) && mouseHoverContent(it.mousePosition)) {
                mouseMoveIn(mouseX, mouseY)
            } else if (mouseHoverContent(it.preMousePosition) && !mouseHoverContent(it.mousePosition)) {
                mouseMoveOut(mouseX, mouseY)
            }
        }
        return NextAction.Continue
    }

    override fun onMouseMoveIn(mouseX: Float, mouseY: Float) {
        MouseCursor.current = MouseCursor.Cursor.IBEAM_CURSOR
    }

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) {
        MouseCursor.current = MouseCursor.Cursor.ARROW_CURSOR
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (mouseHover() && button == Mouse.LEFT) {
            val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentRect(true).width.toInt())
            cursor = textRenderer.trimToWidth(string, (mouseX - this.transform.worldX - padding.left + 3f).toInt()).length + firstCharacterIndex
        }
        return super.onMouseClick(mouseX, mouseY, button)
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        if (!isActive) return super.onMouseScrolling(mouseX, mouseY, amount)
        mouseHover {
            moveCursor((amount < 0f).ternary(1, -1))
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        if (!active || !dragging) return NextAction.Continue
        if (super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
        selecting = true
        val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentRect(true).width.toInt())
        cursor = textRenderer.trimToWidth(string, (mouseX - this.transform.worldX - padding.left + 3f).toInt()).length + firstCharacterIndex
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        return NextAction.Cancel
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, this.transform, focused.ternary(TEXT_SELECTED_INPUT, TEXT_INPUT), bgShaderColor)
    }

    fun renderCursor(renderContext: RenderContext) {
        if (focusedTicks % 15 >= 5 && focused) {
            val rect = contentRect(true)
            val height = textRenderer.fontHeight.toFloat()
            val y = rect.top + (rect.height - height) / 2f - 0.75f
            val offset = textRenderer.getWidth(
                text.substring(
                    min(firstCharacterIndex, cursor).coerceAtMost(text.lastIndex).coerceAtLeast(0),
                    max(firstCharacterIndex, cursor).coerceAtMost(text.lastIndex).coerceAtLeast(0)
                )
            )
            if (cursor == text.length) {
                renderRect(renderContext.matrixStack, rect(rect.position.xyz(rect.left + offset, y + height - 1.25f), 5f, 1f), cursorColor)
                return
            }
            if (cursor - firstCharacterIndex > 0) {
                renderRect(renderContext.matrixStack, rect(rect.position.xyz(rect.left + offset - 0.85f, y), 1f, height), cursorColor)
            } else {
                renderRect(renderContext.matrixStack, rect(rect.position.y(y), Size.create(1f, height)), cursorColor)
            }
        }
    }

    fun renderText(renderContext: RenderContext) {
        val contentRect = contentRect(true)
        renderContext.matrixStack {
            matrixStack.translate(0.0f, 0.4f, 0f)
            textRenderer.batchRender {
                //"渲染提示文本"
                if (text.isEmpty() && hintText != null && !focused) {
                    renderAlignmentText(renderContext.matrixStack, hintText!!, contentRect, color = hintColor)
                }
                //"渲染文本本体"
                val renderText = textRenderer!!.trimToWidth(text.substring(firstCharacterIndex), contentRect.width.toInt())
                renderText.isNotEmpty().ifc {
                    renderAlignmentText(renderContext.matrixStack, renderText, contentRect, color = textColor)
                }
                //"渲染文本建议"
                suggestion?.invoke(text)?.let {
                    if (focused && cursor == text.length) {
                        val renderTextWidth = textRenderer!!.getWidth(renderText).toFloat()
                        val rect = rect(contentRect.position + Vector3f(renderTextWidth, 0f, 0f), contentRect.width - renderTextWidth, contentRect.height)
                        renderAlignmentText(renderContext.matrixStack, it, rect)
                    }
                }
            }
        }

        //"渲染选中的文本高亮"
        if (selectedText.isNotEmpty() && focused) {
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
                rect(contentRect.position.x(start), Size.create(width, contentRect.height))
            } else {
                rect(contentRect.position.x(end), Size.create(width, contentRect.height))
            }
            renderRect(renderContext.matrixStack, rect, selectedColor)
        }
    }

    override fun onRender(renderContext: RenderContext) {
        if (!isActive && !visible) return
        renderBackground(renderContext)
        renderContext.scissor(contentRect(true)) {
            renderText(renderContext)
            renderCursor(renderContext)
        }
    }

}

/**
 * 在当前容器中添加一个[TextInput]
 * @receiver ElementContainer
 * @param width Float 宽度
 * @param height Float 高度
 * @param padding Margin 内部边距
 * @param margin Margin? 外部边距 null -> 无外部边距
 * @param scope TextInput.() -> Unit
 * @return TextInput
 */
@OptIn(ExperimentalContracts::class)
fun ElementContainer.textInput(
    width: Float = 60f,
    height: Float = 20f,
    padding: Padding? = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    scope: TextInput.() -> Unit = {}
): TextInput {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(TextInput(width, height, padding, margin).apply(scope))
}

