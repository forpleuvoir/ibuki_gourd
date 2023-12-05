package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.base.mouseHoverContent
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TEXT_INPUT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TEXT_SELECTED_INPUT
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.*
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.wrapToLines
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.ternary
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.SharedConstants
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.input.CursorMovement
import net.minecraft.client.input.CursorMovement.*
import net.minecraft.util.StringHelper
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 多行文本输入框
 */
class TextBox(
    width: Float,
    height: Float,
    padding: Margin = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    maxLength: Int = Int.MAX_VALUE,
    scrollerThickness: Float = 10f,
    var textColor: ARGBColor = Theme.TEXT_INPUT.TEXT_COLOR,
    var hintColor: ARGBColor = Theme.TEXT_INPUT.HINT_COLOR,
    var bgShaderColor: ARGBColor = Theme.TEXT_INPUT.BACKGROUND_SHADER_COLOR,
    var selectedColor: ARGBColor = Theme.TEXT_INPUT.SELECTED_COLOR,
    var cursorColor: ARGBColor = Theme.TEXT_INPUT.CURSOR_COLOR,
    override var spacing: Float = 1f,
    private val showScroller: Boolean = true,
    private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : ClickableElement() {
    companion object {

        const val UNLIMITED_LENGTH = Int.MAX_VALUE

    }

    data class Substring(val beginIndex: Int, val endIndex: Int) {
        companion object {
            val EMPTY = Substring(0, 0)
        }

        fun getText(text: String): String {
            return text.substring(beginIndex, endIndex)
        }

        operator fun contains(index: Int): Boolean {
            return index in beginIndex..endIndex
        }
    }

    override val focusable: Boolean = true

    init {
        transform.width = width.also { transform.fixedWidth = true }
        transform.height = height.also { transform.fixedHeight = true }
        padding(padding)
        margin?.let(::margin)

    }

    /**
     * 单页高度
     */
    private val contentHeight: Float get() = height - padding.height

    /**
     * 文本总高度
     */
    val textContentHeight: Float
        get() = (lineCount * (fontHeight + spacing)) - spacing

    override fun init() {
        text = ""
        super.init()
        if (!this::scrollerBar.isInitialized) {
            scrollerBar = scroller(
                transform.height - padding.height,
                scrollerThickness,
                { fontHeight / 2f },
                { (textContentHeight - contentHeight).coerceAtLeast(0f) },
                { (contentHeight / textContentHeight).clamp(0f..1f) },
                Arrangement.Vertical
            ) {
                fixed = true
                visible = showScroller
            }
            scrollerBar.transform.worldX = this.transform.worldRight - scrollerThickness - (this.padding.right / 2).coerceAtLeast(4f)
            scrollerBar.transform.y = this.padding.top
        }
    }

    val overflows: Boolean get() = lineCount > maxLinesWithoutOverflow

    val maxLinesWithoutOverflow: Float get() = (this.height - this.padding.height) / (fontHeight + spacing)

    lateinit var scrollerBar: Scroller
        private set

    val fontHeight by textRenderer::fontHeight

    var amount: Float
        get() {
            return if (this::scrollerBar.isInitialized) {
                scrollerBar.amount
            } else 0f
        }
        set(value) {
            if (this::scrollerBar.isInitialized) {
                scrollerBar.amount = value
            }
        }

    private val scrollerThickness: Float = if (!showScroller) 0f else scrollerThickness

    val width by transform::width

    val height by transform::height

    var hintText: Text? = null

    private val lines: MutableList<Substring> = ArrayList()

    val lineCount get() = lines.size

    fun getLines(): Iterable<Substring> {
        return lines
    }

    private val currentLineIndex: Int
        get() {
            return currentLineIndex(this.cursor)
        }

    private fun currentLineIndex(cursor: Int): Int {
        for (i in lines.indices) {
            val substring: Substring = lines[i]
            if (cursor !in substring) continue
            return i
        }
        return -1
    }

    var suggestion: ((text: String, preWord: String) -> Iterable<Text>)? = null

    private val currentLine: Substring
        get() = this.getOffsetLine(0)

    private fun currentLine(cursor: Int): Substring {
        return lines[(currentLineIndex(cursor)).clamp(0, lines.size - 1)]
    }

    private fun getOffsetLine(offsetFromCurrent: Int): Substring {
        val i: Int = this.currentLineIndex
//		check(i < 0) { "Cursor is not within text (cursor = " + cursor + ", length = " + text.length + ")" }
        return lines[(i + offsetFromCurrent).clamp(0, lines.size - 1)]
    }


    fun getLine(index: Int): Substring {
        return lines[index.clamp(0, lines.size - 1)]
    }

    var text: String = ""
        set(value) {
            field = truncateForReplacement(value)
            this.selectionEnd = value.length
            this.cursor = value.length
            this.onTextChanged(field)
            onChange()
        }

    var cursor: Int = 0

    val history: HistoryRecord = HistoryRecord(currentRecord = HistoryRecord.Record(text, cursor))

    val selectedText: String
        get() = this.selection.getText(this.text)

    var selectionEnd: Int = 0

    private var selecting: Boolean = false

    var maxLength: Int = maxLength
        set(value) {
            field = value.coerceAtLeast(0)
        }

    val hasMaxLength: Boolean get() = maxLength != UNLIMITED_LENGTH

    val hasSelection: Boolean get() = selectionEnd != cursor

    var onTextChanged: (text: String) -> Unit = {}

    var onCursorChanged: () -> Unit = {
        var amount = this.amount

        val firstLine: Substring = getLine((ceil(amount / (fontHeight + spacing))).toInt())
        if (this.cursor <= firstLine.beginIndex) {
            amount = this.currentLineIndex * (spacing + fontHeight) - spacing
        } else {
            val endLine: Substring = getLine((floor(amount + this.height) / (fontHeight + spacing)).toInt() - 1)
            if (this.cursor > endLine.endIndex) {
                amount = this.currentLineIndex * (spacing + fontHeight) - spacing - this.height + fontHeight + this.padding.height
            }
        }
        this.amount = amount
    }

    val selection: Substring
        get() = Substring(
            min(selectionEnd, cursor),
            max(selectionEnd, cursor)
        )

    private val previousWordAtCursor: Substring
        get() {
            if (text.isEmpty()) {
                return Substring.EMPTY
            }
            var result: Int = cursor.clamp(0, text.length - 1)

            if (result > 0 && text[result - 1] == ' ') {
                --result
                return Substring(result, this.getWordEndIndex(result))
            }
            while (result > 0 && Character.isWhitespace(text[result - 1])) {
                --result
            }
            while (result > 0 && !Character.isWhitespace(text[result - 1])) {
                --result
            }
            return Substring(result, this.getWordEndIndex(result))
        }

    private val nextWordAtCursor: Substring
        get() {
            if (text.isEmpty()) {
                return Substring.EMPTY
            }
            var result: Int = cursor.clamp(0, text.length - 1)
            if (result < text.length && text[result] == ' ') {
                ++result
                return Substring(result, getWordEndIndex(result))
            }
            while (result < text.length && !Character.isWhitespace(text[result])) {
                ++result
            }
            while (result < text.length && Character.isWhitespace(text[result])) {
                ++result
            }
            return Substring(result, getWordEndIndex(result))
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


    @Suppress("DuplicatedCode")
    private fun replaceSelection(string: String, historyOpt: Boolean = false) {
        if (string.isEmpty() && !this.hasSelection) {
            return
        }
        val string2 = truncate(SharedConstants.stripInvalidChars(string, true))
        val substring: Substring = this.selection
        text = StringBuilder(text).replace(substring.beginIndex, substring.endIndex, string2).toString()
        cursor = substring.beginIndex + string2.length
        selectionEnd = cursor
        if (!historyOpt)
            history.textChange(this.text, cursor)
        onChange()
    }

    private fun delete(offset: Int) {
        if (!this.hasSelection) {
            selectionEnd = (cursor + offset).clamp(0, text.length)
        }
        replaceSelection("")
    }

    private fun moveCursor(movement: CursorMovement, amount: Int) {
        when (movement) {
            ABSOLUTE -> {
                cursor = amount
            }

            RELATIVE -> {
                cursor += amount
            }

            END      -> {
                cursor = text.length + amount
            }
        }
        cursor = cursor.clamp(0, text.length)
        this.onCursorChanged()
        if (!selecting) {
            selectionEnd = cursor
        }
    }

    private fun moveCursorLine(offset: Int) {
        if (offset == 0) {
            return
        }
        val i = textRenderer.getWidth(text.substring(this.currentLine.beginIndex, cursor)) + 2
        val substring: Substring = this.getOffsetLine(offset)
        val amount = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), i).length
        moveCursor(ABSOLUTE, substring.beginIndex + amount)
    }

    private fun getWordEndIndex(startIndex: Int): Int {
        var result: Int = startIndex
        while (result < text.length && !Character.isWhitespace(text[result])) {
            ++result
        }
        return result
    }

    private fun moveCursor(mouseX: Float, mouseY: Float) {
        val x = floor(mouseX - this.transform.worldX - padding.left + 3f).toInt()
        val y = floor((mouseY - this.transform.worldY - padding.top + amount) / (fontHeight + spacing)).toInt()
        val substring: Substring = lines[y.clamp(0, lines.size - 1)]
        val amount = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), x).length
        this.moveCursor(ABSOLUTE, substring.beginIndex + amount)
    }

    private fun onChange() {
        this.reWrap()
        this.onCursorChanged()
    }

    private fun reWrap() {
        lines.clear()
        if (text.isEmpty()) {
            lines.add(Substring.EMPTY)
            return
        }
        text.wrapToLines(textRenderer, contentRect(true).width.toInt()) { start, end ->
            lines.add(Substring(start, end))
        }
        if (text[text.length - 1] == '\n') {
            lines.add(Substring(text.length, text.length))
        }
    }

    private fun truncateForReplacement(value: String): String {
        return if (this.hasMaxLength) {
            StringHelper.truncate(value, maxLength, false)
        } else value
    }

    private fun truncate(value: String): String {
        if (this.hasMaxLength) {
            val i = maxLength - text.length
            return StringHelper.truncate(value, i, false)
        }
        return value
    }

    @Suppress("DuplicatedCode")
    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        val top = if (isWorld) transform.worldTop + padding.top else padding.top

        val bottom = if (isWorld) transform.worldBottom - padding.bottom
        else transform.height - padding.bottom

        val left = if (isWorld) transform.worldLeft + padding.left else padding.left

        val right = if (isWorld) transform.worldRight - padding.right - scrollerThickness
        else transform.width - padding.right - scrollerThickness

        return rect(vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top)
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
        super.onMouseClick(mouseX, mouseY, button)
        if (!scrollerBar.mouseHover() && mouseHoverContent() && button == Mouse.LEFT) {
            selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
            moveCursor(mouseX, mouseY)
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        mouseHover {
            if (!scrollerBar.mouseHover()) {
                scrollerBar.amount -= scrollerBar.amountStep() * amount
            }
        }
        return super.onMouseScrolling(mouseX, mouseY, amount)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        if (!active || !dragging) return NextAction.Continue
        if (super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
        if (scrollerBar.dragging) return NextAction.Continue
        selecting = true
        moveCursor(mouseX, mouseY)
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        return NextAction.Cancel
    }

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (!focused) return NextAction.Continue
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        //全选
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
            cursor = text.length
            selectionEnd = 0
            return NextAction.Cancel
        }
        //复制选中
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
            mc.keyboard.clipboard = this.selectedText
            return NextAction.Cancel
        }
        //粘贴
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
            replaceSelection(mc.keyboard.clipboard)
            return NextAction.Cancel
        }
        //剪切选中
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
            mc.keyboard.clipboard = this.selectedText
            replaceSelection("")
            return NextAction.Cancel
        }
        //另起一行
        if (InputHandler.hasKeyPressed(Keyboard.RIGHT_SHIFT, Keyboard.ENTER)) {
            this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
            replaceSelection("\n")
            return NextAction.Cancel
        }
        //撤回
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
            cursor = text.length
            selectionEnd = 0
            history.undo(text, cursor).let {
                replaceSelection(it.text, true)
                cursor = it.cursor
            }
            return NextAction.Cancel
        }
        //重做
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
            cursor = text.length
            selectionEnd = 0
            history.redo(text, cursor).let {
                replaceSelection(it.text, true)
                cursor = it.cursor
            }
            return NextAction.Cancel
        }
        when (keyCode) {
            //光标左移
            Keyboard.LEFT                     -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    val substring: Substring = this.previousWordAtCursor
                    this.moveCursor(ABSOLUTE, substring.beginIndex)
                } else {
                    this.moveCursor(RELATIVE, -1)
                }
                return NextAction.Cancel
            }
            //光标右移
            Keyboard.RIGHT                    -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    val substring: Substring = this.nextWordAtCursor
                    this.moveCursor(ABSOLUTE, substring.beginIndex)
                } else {
                    this.moveCursor(RELATIVE, 1)
                }
                return NextAction.Cancel
            }
            //光标上移
            Keyboard.UP                       -> {
                if (!InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    moveCursorLine(-1)
                }
                return NextAction.Cancel
            }
            //光标下移
            Keyboard.DOWN                     -> {
                if (!InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    moveCursorLine(1)
                }
                return NextAction.Cancel
            }
            //上一页
            Keyboard.PAGE_UP                  -> {
                this.moveCursor(ABSOLUTE, 0)
                return NextAction.Cancel
            }
            //下一页
            Keyboard.PAGE_DOWN                -> {
                this.moveCursor(END, 0)
                return NextAction.Cancel
            }
            //光标移动到行首,如果按下了ctrl则移动到文本开头
            Keyboard.HOME                     -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    this.moveCursor(ABSOLUTE, 0)
                } else NextAction.Cancel
                return NextAction.Cancel
            }
            //光标移动到行尾,如果按下了ctrl则移动到文本结尾
            Keyboard.END                      -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    this.moveCursor(END, 0)
                } else {
                    this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
                }
                return NextAction.Cancel
            }
            //删除选中,如果没有选中则删除光标前的一个字符,如果按下了ctrl则删除光标前的一个单词
            Keyboard.BACKSPACE                -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    val substring: Substring = this.previousWordAtCursor
                    delete(substring.beginIndex - cursor)
                } else {
                    delete(-1)
                }
                return NextAction.Cancel
            }
            //删除选中,如果没有选中则删除光标后的一个字符,如果按下了ctrl则删除光标后的一个单词
            Keyboard.DELETE                   -> {
                if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                    val substring: Substring = this.nextWordAtCursor
                    delete(substring.beginIndex - cursor)
                } else {
                    delete(1)
                }
                return NextAction.Cancel
            }
            //删除选中,如果没有选中则删除光标前的一个单词
            Keyboard.ENTER, Keyboard.KP_ENTER -> {
                replaceSelection("\n")
                return NextAction.Cancel
            }
        }
        return NextAction.Continue
    }

    override fun onKeyRelease(keyCode: KeyCode): NextAction {
        selecting = if (keyCode == Keyboard.LEFT_SHIFT) false else selecting
        return super.onKeyRelease(keyCode)
    }

    override fun onCharTyped(chr: Char): NextAction {
        if (!(this.focused && SharedConstants.isValidChar(chr))) {
            return NextAction.Continue
        }
        replaceSelection(chr.toString())
        return NextAction.Cancel
    }

    override fun onRender(renderContext: RenderContext) {
        renderBackground.invoke(renderContext)
        renderContext.scissor(contentRect(true)) {
            renderText(renderContext)
        }
        fixedElements.forEach { it.render(renderContext) }
        scrollerBar.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, this.transform, focused.ternary(TEXT_SELECTED_INPUT, TEXT_INPUT), bgShaderColor)
    }

    //绘制选择高亮
    override fun onRenderOverlay(renderContext: RenderContext) {
        renderContext.scissor(contentRect(true), ::renderCursor)
    }


    private fun renderText(renderContext: RenderContext) {
        val contentRect = contentRect(true)
        //渲染提示文本
        if (text.isEmpty() && !focused) {
            if (hintText != null) {
                textRenderer.renderTextLines(renderContext.matrixStack, hintText!!, contentRect, spacing, PlanarAlignment::TopLeft, color = hintColor)
            }
            return
        }
        //渲染文本本体
        if (text.isNotEmpty())
            textRenderer.batchRender {
                var y = contentRect.top - amount
                lines.forEach {
                    if (y in contentRect.top - fontHeight..contentRect.bottom)
                        renderText(renderContext.matrixStack, text.substring(it.beginIndex, it.endIndex), contentRect.left, y, contentRect.z, color = textColor)
                    y += fontHeight + spacing
                }
            }
        //渲染选中文本高亮
        //应该最多渲染三个矩形
        if (selectedText.isNotEmpty()) {
            val (start, end) = selection
            val startXOffset = textRenderer.getWidth(text.substring(currentLine(start).beginIndex, start))
            val endXOffset = textRenderer.getWidth(text.substring(currentLine(end).beginIndex, end))
            val startY = contentRect.top + currentLineIndex(start) * (fontHeight + spacing) - amount
            val endY = contentRect.top + currentLineIndex(end) * (fontHeight + spacing) - amount
            val mindY = (startY + (fontHeight + spacing)).let { if (it == endY) 0f else it }
            if (startY == endY) {
                renderRect(
                    renderContext.matrixStack,
                    rect(contentRect.left + startXOffset, startY, contentRect.z, textRenderer.getWidth(selection.getText(this.text)), fontHeight + spacing),
                    selectedColor
                )
            } else if (mindY == 0f) {
                rectBatchRender {
                    renderRect(
                        renderContext.matrixStack,
                        rect(contentRect.left + startXOffset, startY, contentRect.z, contentRect.width - startXOffset, fontHeight + spacing),
                        selectedColor
                    )
                    renderRect(
                        renderContext.matrixStack,
                        rect(contentRect.left, endY, contentRect.z, endXOffset, fontHeight + spacing), selectedColor
                    )
                }
            } else {
                rectBatchRender {
                    renderRect(
                        renderContext.matrixStack,
                        rect(contentRect.left + startXOffset, startY, contentRect.z, contentRect.width - startXOffset, fontHeight + spacing),
                        selectedColor
                    )
                    renderRect(
                        renderContext.matrixStack,
                        rect(contentRect.left, mindY, contentRect.z, contentRect.width, endY - startY - (fontHeight + spacing)), selectedColor
                    )
                    renderRect(
                        renderContext.matrixStack,
                        rect(contentRect.left, endY, contentRect.z, endXOffset, fontHeight + spacing), selectedColor
                    )
                }
            }

        }
    }

    private fun renderCursor(renderContext: RenderContext) {
        if (focusedTicks % 15 >= 5 && focused) {
            val contentRect = contentRect(true)
            val xOffset = textRenderer.getWidth(text.substring(currentLine.beginIndex, cursor)).let { it + if (it == 0) 0f else -0.95f }
            val y = contentRect.top + currentLineIndex * (fontHeight + spacing) - amount
            if (y !in contentRect.top - fontHeight..contentRect.bottom) return
            if (cursor == text.length)
                renderRect(renderContext.matrixStack, rect(contentRect.position.xyz(contentRect.left + xOffset, y + fontHeight - 1.25f), 5f, 1f), cursorColor)
            else
                renderRect(renderContext.matrixStack, rect(contentRect.position.xyz(contentRect.left + xOffset, y), 1f, fontHeight), cursorColor)
        }
    }

}

fun ElementContainer.textBox(
    width: Float,
    height: Float,
    padding: Margin = Theme.TEXT_INPUT.PADDING,
    margin: Margin? = null,
    maxLength: Int = Int.MAX_VALUE,
    scrollerThickness: Float = 10f,
    editableColor: ARGBColor = Theme.TEXT_INPUT.TEXT_COLOR,
    uneditableColor: ARGBColor = Theme.TEXT_INPUT.HINT_COLOR,
    backgroundColor: ARGBColor = Theme.TEXT_INPUT.BACKGROUND_SHADER_COLOR,
    selectedColor: ARGBColor = Theme.TEXT_INPUT.SELECTED_COLOR,
    cursorColor: ARGBColor = Theme.TEXT_INPUT.CURSOR_COLOR,
    spacing: Float = 1f,
    showScroller: Boolean = true,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    scope: TextBox.() -> Unit = {}
): TextBox = addElement(
    TextBox(
        width,
        height,
        padding,
        margin,
        maxLength,
        scrollerThickness,
        editableColor,
        uneditableColor,
        backgroundColor,
        selectedColor,
        cursorColor,
        spacing,
        showScroller,
        textRenderer
    ).apply(scope)
)