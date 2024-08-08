package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.totalHeight
import moe.forpleuvoir.ibukigourd.text.wrapToLines
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.input.CursorMovement
import net.minecraft.client.input.CursorMovement.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.StringHelper
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 多行文本输入框
 */
class TextArea(
    maxLength: Int = Int.MAX_VALUE,
    var textColor: ARGBColor = Color(0x303030),
    var hintColor: ARGBColor = Color(0x707070),
    var bgShaderColor: ARGBColor = Colors.WHITE,
    var selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    var suggestionColor: ARGBColor = Color(0x008F72).alpha(0.45f),
    var cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    var spacing: Float = 1f,
    private val textRenderer: TextRenderer = mc.textRenderer
) : IGWidgetImpl() {

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

    /**
     * 文本总高度
     */
    val textContentHeight: Float
        get() = (lineCount * (fontHeight + spacing)) - spacing


    private val scrollableAmount: Float
        get() {
            val amount = textContentHeight - contentHeight
            return if (amount > 0f) amount else 0f
        }

    val fontHeight by textRenderer::fontHeight

    var amountConsumer: (Float) -> Unit = {}

    var amount: Float = 0f
        set(value) {
            field = value.coerceIn(0f, scrollableAmount)
        }

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
        amountConsumer(amount)
    }

    var selection: Substring
        set(value) {
            selectionEnd = max(value.endIndex, value.beginIndex)
            cursor = min(value.endIndex, value.beginIndex)
        }
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

    @Suppress("DuplicatedCode")
    private fun replaceSelection(string: String, historyOpt: Boolean = false) {
        if (string.isEmpty() && !this.hasSelection) {
            return
        }
        val string2 = truncate(StringHelper.stripInvalidChars(string, true))
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
        val substring: Substring = lines[y.coerceIn(0, lines.lastIndex)]
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
        text.wrapToLines(textRenderer, contentWidth.toInt()) { start, end ->
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

    //------------ Measure ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraint(constraints)
        val height = text.totalHeight(textRenderer, spacing, c.maxWidth.toInt())
        val width = text.wrapToLines(textRenderer).maxWidth(textRenderer).toFloat()
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        reWrap()
        return this
    }


    //------------ IGElement ------------\\

    override val mouseOverCursor: MouseCursor.Cursor
        get() = MouseCursor.Cursor.IBEAM_CURSOR

    override fun onTick() {
        history.onTick()
        if (isFocused) {
            ++focusedTicks
        } else {
            focusedTicks = 0
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        event.tryUse {
            wasMouseOver && event.button == Mouse.LEFT
        }.onSuccess {
            selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
            moveCursor(event.x, event.y)
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        event.tryUse { wasMouseOver && wasDragging }.onSuccess {
            selecting = true
            moveCursor(event.x, event.y)
            selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        event.tryUse { wasMouseOver }.onSuccess {
            amount -= event.verticalAmount * (fontHeight + spacing) / 2f
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        if (!isFocused) return
        event.tryUse {
            selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
            //全选
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
                cursor = text.length
                selectionEnd = 0
                return@tryUse true
            }
            //复制选中
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
                mc.keyboard.clipboard = this.selectedText
                return@tryUse true
            }
            //粘贴
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
                replaceSelection(mc.keyboard.clipboard)
                return@tryUse true
            }
            //剪切选中
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
                if (this.selectedText.isEmpty()) {
                    this.selection = currentLine
                    if (this.text[currentLine.beginIndex - 1] == '\n') {
                        this.cursor = currentLine.beginIndex - 1
                    }
                }
                mc.keyboard.clipboard = this.selectedText
                replaceSelection("")
                return@tryUse true
            }
            //另起一行
            if (InputHandler.hasKeyPressed(Keyboard.RIGHT_SHIFT, Keyboard.ENTER)) {
                this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
                replaceSelection("\n")
                return@tryUse true
            }
            //撤回
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
                cursor = text.length
                selectionEnd = 0
                history.undo(text, cursor).let {
                    replaceSelection(it.text, true)
                    cursor = it.cursor
                }
                return@tryUse true
            }
            //重做
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
                cursor = text.length
                selectionEnd = 0
                history.redo(text, cursor).let {
                    replaceSelection(it.text, true)
                    cursor = it.cursor
                }
                return@tryUse true
            }
            return@tryUse when (event.keyCode) {
                //输入制表符或者四个空格
                Keyboard.TAB                      -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL))
                        replaceSelection("\t")
                    else
                        replaceSelection("    ")
                    true
                }
                //光标左移
                Keyboard.LEFT                     -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        val substring: Substring = this.previousWordAtCursor
                        this.moveCursor(ABSOLUTE, substring.beginIndex)
                    } else {
                        this.moveCursor(RELATIVE, -1)
                    }
                    true
                }
                //光标右移
                Keyboard.RIGHT                    -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        val substring: Substring = this.nextWordAtCursor
                        this.moveCursor(ABSOLUTE, substring.beginIndex)
                    } else {
                        this.moveCursor(RELATIVE, 1)
                    }
                    true
                }
                //光标上移
                Keyboard.UP                       -> {
                    if (!InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(-1)
                    }
                    true
                }
                //光标下移
                Keyboard.DOWN                     -> {
                    if (!InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(1)
                    }
                    true
                }
                //上一页
                Keyboard.PAGE_UP                  -> {
                    this.moveCursor(ABSOLUTE, 0)
                    true
                }
                //下一页
                Keyboard.PAGE_DOWN                -> {
                    this.moveCursor(END, 0)
                    true
                }
                //光标移动至行首,如果按下了ctrl则移动到文本开头
                Keyboard.HOME                     -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(ABSOLUTE, 0)
                    } else {
                        this.moveCursor(ABSOLUTE, this.currentLine.beginIndex)
                    }
                    true
                }
                //光标移动至行尾,如果按下了ctrl则移动到文本结尾
                Keyboard.END                      -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(END, 0)
                    } else {
                        this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
                    }
                    true
                }
                //删除选中,如果没有选中则删除光标前的一个字符,如果按下了ctrl则删除光标前的一个单词
                Keyboard.BACKSPACE                -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        val substring: Substring = this.previousWordAtCursor
                        delete(substring.beginIndex - cursor)
                    } else {
                        delete(-1)
                    }
                    true
                }
                //删除选中,如果没有选中则删除光标后的一个字符,如果按下了ctrl则删除光标后的一个单词
                Keyboard.DELETE                   -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        val substring: Substring = this.nextWordAtCursor
                        delete(substring.beginIndex - cursor)
                    } else {
                        delete(1)
                    }
                    true
                }
                //删除选中,如果没有选中则删除光标前的一个单词
                Keyboard.ENTER, Keyboard.KP_ENTER -> {
                    replaceSelection("\n")
                    true
                }

                else                              -> false
            }

        }

    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        selecting = if (event.keyCode == Keyboard.LEFT_SHIFT) false else selecting
    }

    override fun onCharTyped(event: CharTypedEvent) {
        if (!(this.isFocused && StringHelper.isValidChar(event.char))) return
        event.tryUse().onSuccess {
            replaceSelection(event.char.toString())
        }
    }

    //------------ IGDrawable ------------\\

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(transform.asWorldBox, theme(WidgetTheme.TextInput), bgShaderColor)
        }
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.scissor(contentBox(true)) {
            renderText(context)
        }
    }

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.scissor(contentBox(true)) {
            renderCursor(context)
        }
    }

    private fun renderText(context: IGDrawContext) {
        val contentBox = contentBox(true)
        //渲染提示文本
        if (text.isEmpty() && !isFocused) {
            if (hintText != null) {
                context.batchRenderText(textRenderer) {
                    context.textLines(hintText!!, contentBox, spacing, BoxAlignment::TopLeft, defaultColor = hintColor)
                }
            }
            return
        }
        //渲染文本本体
        if (text.isNotEmpty())
            context.batchRenderText(textRenderer) {
                var y = contentBox.top - amount
                lines.forEach {
                    if (y in contentBox.top - fontHeight..contentBox.bottom)
                        context.text(text.substring(it.beginIndex, it.endIndex), contentBox.left, y, color = textColor)
                    y += fontHeight + spacing
                }
            }
        //渲染选中文本高亮
        //应该最多渲染三个矩形
        if (selectedText.isNotEmpty()) {
            val (start, end) = selection
            val startXOffset = textRenderer.getWidth(text.substring(currentLine(start).beginIndex, start))
            val endXOffset = textRenderer.getWidth(text.substring(currentLine(end).beginIndex, end))
            val startY = contentBox.top + currentLineIndex(start) * (fontHeight + spacing) - amount
            val endY = contentBox.top + currentLineIndex(end) * (fontHeight + spacing) - amount
            val mindY = (startY + (fontHeight + spacing)).let { if (it == endY) 0f else it }
            if (startY == endY) {
                context.renderBox(
                    Box(contentBox.left + startXOffset, startY, Size(textRenderer.getWidth(selection.getText(this.text)), fontHeight)),
                    selectedColor, RenderLayer.getGuiTextHighlight()
                )
            } else if (mindY == 0f) {
                context.batchRenderBox(RenderLayer.getGuiTextHighlight()) {
                    context.box(Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, fontHeight + spacing)), selectedColor)
                    context.box(Box(contentBox.left, endY, Size(endXOffset, fontHeight)), selectedColor)
                }
            } else {
                context.batchRenderBox(RenderLayer.getGuiTextHighlight()) {
                    context.box(Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, fontHeight + spacing)), selectedColor)
                    context.box(Box(contentBox.left, mindY, Size(contentBox.width, endY - startY - (fontHeight + spacing))), selectedColor)
                    context.box(Box(contentBox.left, endY, Size(endXOffset, fontHeight)), selectedColor)
                }
            }
        }
    }

    private fun renderCursor(context: IGDrawContext) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val contentBox = contentBox(true)
            val thickness = 0.75f
            val xOffset = textRenderer.getWidth(text.substring(currentLine.beginIndex, cursor)).let { if (cursor == text.length) it.toFloat() else it - .85f }
            val y = contentBox.top + currentLineIndex * (fontHeight + spacing) - amount - spacing
            if (y !in contentBox.top - fontHeight..contentBox.bottom) return
            if (cursor == text.length)
                context.renderBox(Box(contentBox.left + xOffset, y + fontHeight, Size(7f, thickness)), cursorColor)
            else
                context.renderBox(Box(contentBox.left + xOffset, y + spacing, Size(thickness, textRenderer.fontHeight.toFloat())), cursorColor)
        }
    }


    companion object {

        const val UNLIMITED_LENGTH = Int.MAX_VALUE

        fun interface TextAreaScope : GuiScope<TextArea> {

            var text: String
                get() = owner().text
                set(value) {
                    owner().text = value
                }

            var hintText: Text?
                get() = owner().hintText
                set(value) {
                    owner().hintText = value
                }

            var textColor: ARGBColor
                get() = owner().textColor
                set(value) {
                    owner().textColor = value
                }

            var hintColor: ARGBColor
                get() = owner().hintColor
                set(value) {
                    owner().hintColor = value
                }

            var bgShaderColor: ARGBColor
                get() = owner().bgShaderColor
                set(value) {
                    owner().bgShaderColor = value
                }

            var selectedColor: ARGBColor
                get() = owner().selectedColor
                set(value) {
                    owner().selectedColor = value
                }

            var suggestionColor: ARGBColor
                get() = owner().suggestionColor
                set(value) {
                    owner().suggestionColor = value
                }
            var cursorColor: ARGBColor
                get() = owner().cursorColor
                set(value) {
                    owner().cursorColor = value
                }

            var spacing: Float
                get() = owner().spacing
                set(value) {
                    owner().spacing = value
                }

            fun suggestion(suggestion: ((text: String, preWord: String) -> Iterable<Text>)) {
                owner().suggestion = suggestion
            }

            fun textConsumer(consumer: (String) -> Unit) {
                owner().onTextChanged = consumer
            }

            fun amountConsumer(consumer: (Float) -> Unit) {
                owner().amountConsumer = consumer
            }
        }

    }

}

fun GuiScope<out WidgetContainer>.textArea(
    maxLength: Int = Int.MAX_VALUE,
    textColor: ARGBColor = Color(0x303030),
    hintColor: ARGBColor = Color(0x707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0x008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    spacing: Float = 1f,
    textRenderer: TextRenderer = mc.textRenderer,
    modifier: Modifier? = null,
    scope: TextArea.Companion.TextAreaScope.() -> Unit = {}
) = owner().addWidgetChild(TextArea(maxLength, textColor, hintColor, bgShaderColor, selectedColor, suggestionColor, cursorColor, spacing, textRenderer)) {
    val m = Modifier.padding(5.5f) thenNullable modifier
    m.foldIn(Unit) { _, modifier ->
        modifier.tryApplyModify(this)
    }
    TextArea.Companion.TextAreaScope { this }.scope()
}

fun GuiScope<out WidgetContainer>.textAreaWidthScroller(
    maxLength: Int = Int.MAX_VALUE,
    textRenderer: TextRenderer = mc.textRenderer,
    barThickness: Float = 9f,
    amountConsumer: (Float) -> Unit = { },
    initialAmount: () -> Float = { 0f },
    modifier: Modifier? = null,
    textAreaModifier: (ColumnScope.() -> Modifier)? = null,
    scrollerModifier: (ColumnScope.() -> Modifier)? = null,
    scope: TextArea.Companion.TextAreaScope.() -> Unit = {}
): ColumnWidget {
    var textSupplier: () -> TextArea? = { null }
    return column(modifier = Modifier
        .padding(5.5f, 4f, 5.5f, 5.5f)
        .renderBackground { ctx, _, _, _ ->
            val widget = this as IGWidget
            ctx.batchRenderTextureColored {
                textSupplier()?.let {
                    ctx.drawWidgetTexture(widget.transform.asWorldBox, it.theme(WidgetTheme.TextInput))
                }
            }
        } thenNullable modifier
    ) {
        var scrollerSupplier: () -> ScrollerWidget? = { null }
        val tModifier = Modifier
            .padding(0)
            .fill()
            .weight(1)
            .renderBackground { _, _, _, _ -> }
            .mouseScrolling {
                this as IGWidget
                if (this.wasMouseOver) scrollerSupplier.invoke()?.scroller(it.verticalAmount)
            } thenNullable textAreaModifier?.invoke(this)
        val text = textArea(maxLength, textRenderer = textRenderer, modifier = tModifier) {
            scope()
            amountConsumer {
                scrollerSupplier.invoke()?.amount = it
            }
        }
        textSupplier = { text }
        val scroller = scroller(
            amountStep = { textRenderer.fontHeight / 2f },
            totalAmount = { (text.textContentHeight - text.contentHeight).coerceAtLeast(0f) },
            barProportion = { (text.contentHeight / text.textContentHeight).coerceIn(0f..1f) },
            amountConsumer = { text.amount = it;amountConsumer.invoke(it) },
            initialAmount = initialAmount,
            orientation = Orientation.Vertical,
            modifier = Modifier
                .fill()
                .width(barThickness)
                .margin(left = 1f, top = -3f) thenNullable scrollerModifier?.invoke(this)
        )
        scrollerSupplier = { scroller }
    }

}