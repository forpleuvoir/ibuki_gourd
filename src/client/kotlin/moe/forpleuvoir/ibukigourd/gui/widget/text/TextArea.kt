package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowWidget
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * 多行文本输入框
 */
class TextAreaWidget(
    maxLength: Int = Int.MAX_VALUE,
    val scrollState: ScrollState = ScrollState(),
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

    val fontHeight by textRenderer::fontHeight

    var amount: Float by scrollState::amount

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
        lines.forEachIndexed { index, s ->
            if (cursor in s) return index
        }
        return -1
    }

    var suggestion: ((text: String, preWord: String, cursor: Int) -> Iterable<Text>)? = null

    private val currentLine: Substring
        get() = this.getOffsetLine(0)

    private fun currentLine(cursor: Int): Substring {
        return lines[(currentLineIndex(cursor)).coerceIn(0, lines.size - 1)]
    }

    private fun getOffsetLine(offsetFromCurrent: Int): Substring {
        val i: Int = this.currentLineIndex
//		check(i < 0) { "Cursor is not within text (cursor = " + cursor + ", length = " + text.length + ")" }
        return lines[(i + offsetFromCurrent).coerceIn(0, lines.size - 1)]
    }


    fun getLine(index: Int): Substring {
        return lines[index.coerceIn(0, lines.size - 1)]
    }

    var text: String = ""
        set(value) {
            field = truncateForReplacement(value)
            this.onTextChanged(field)
            onChange()
        }

    var cursor: Int = 0

    val cursorChar: Char get() = runCatching { text[cursor] }.getOrElse { ' ' }

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

    var selection: Substring
        set(value) {
            selectionEnd = max(value.endIndex, value.beginIndex)
            cursor = min(value.endIndex, value.beginIndex)
        }
        get() = Substring(
            min(selectionEnd, cursor),
            max(selectionEnd, cursor)
        )

    private val previousWordOffsetAtCursor: Int
        get() {
            if (text.isEmpty()) {
                return 0
            }
            var target: Int = (cursor - 1).coerceAtLeast(0)
            //如果上一个字符为标点符号,直到找到下一个非标点符号的字符
            if (text[target].isPunct) {
                while (target > 0 && text[target - 1].isPunct) {
                    target--
                }
                return target - cursor
            }
            //如果上一个字符为空白符号,直到找到下一个非空白符号
            if (Character.isWhitespace(text[target])) {
                while (target > 0 && Character.isWhitespace(text[target - 1])) {
                    target--
                }
                return target - cursor
            }
            //如果上一个字符为其他字符,则直到找到下一个标点符号或空白符号的字符
            while (target > 0 && !Character.isWhitespace(text[target - 1]) && !text[target - 1].isPunct) {
                target--
            }
            return target - cursor
        }

    private val nextWordOffsetAtCursor: Int
        get() {
            if (text.isEmpty()) {
                return 0
            }
            var target: Int = cursor.coerceAtMost(text.lastIndex)
            //如果上一个字符为标点符号,直到找到下一个非标点符号的字符
            if (text[target].isPunct) {
                while (target < text.lastIndex && text[target + 1].isPunct) {
                    target++
                }
                return target - (cursor - 1)
            }
            //如果上一个字符为空白符号,直到找到下一个非空白符号
            if (Character.isWhitespace(text[target])) {
                while (target < text.lastIndex && Character.isWhitespace(text[target + 1])) {
                    target++
                }
                return target - (cursor - 1)
            }
            //如果上一个字符为其他字符,则直到找到下一个标点符号或空白符号的字符
            while (target < text.lastIndex && !Character.isWhitespace(text[target + 1]) && !text[target + 1].isPunct) {
                target++
            }
            return target - (cursor - 1)
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
            selectionEnd = (cursor + offset).coerceIn(0, text.length)
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
        cursor = cursor.coerceIn(0, text.length)
        this.onCursorChanged()
        if (!selecting) {
            selectionEnd = cursor
        }
    }

    private fun moveCursorLine(offset: Int) {
        if (offset == 0) {
            return
        }
        val i = text.substring(this.currentLine.beginIndex, cursor).width.toInt() + 2
        val substring: Substring = this.getOffsetLine(offset)
        val amount = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), i).length
        moveCursor(ABSOLUTE, substring.beginIndex + amount)
    }

    private fun moveCursor(mouseX: Float, mouseY: Float) {
        val x = floor(mouseX - this.transform.worldX - padding.left + 3f).toInt()
        val y = floor((mouseY - this.transform.worldY - padding.top + amount) / (fontHeight + spacing)).toInt()
        val substring: Substring = lines[y.coerceIn(0, lines.lastIndex)]
        val amount = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), x).length
        this.moveCursor(ABSOLUTE, substring.beginIndex + amount)
    }

    private fun onChange() {
        if (!constraints.fixed()) {
            remeasure()
        }
        this.reWrap()
        this.onCursorChanged()
    }

    private fun reWrap() {
        lines.clear()
        if (text.isEmpty()) {
            lines.add(Substring.EMPTY)
            return
        }
        text.wrapToLines(contentWidth) { start, end ->
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
        val c = this.constraints.constraintAs(constraints)
        val height = text.totalHeight(spacing, c.maxWidth) + padding.height
        val width = text.wrapToLines().maxWidth + padding.width
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        reWrap()
        return this
    }


    //------------ IGElement ------------\\

    override fun onTick() {
        history.onTick()
        if (isFocused) {
            ++focusedTicks
        } else {
            focusedTicks = 0
        }
    }

    private var lastPressTime = TimeSource.Monotonic.markNow()

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        event.tryUse {
            wasMouseOver && event.button == Mouse.LEFT
        }.onSuccess {
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            val oldCursor = cursor
            selecting = InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)
            moveCursor(event.x, event.y)
            if (oldCursor == cursor && lastPressTime.elapsedNow() < 500.milliseconds) {
                selectWord()
            }
            lastPressTime = TimeSource.Monotonic.markNow()
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        event.tryUse(wasDragging).onSuccess {
            selecting = true
            moveCursor(event.x, event.y)
            selecting = InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        event.tryUse(wasDragging).onSuccess {
            scrollState.scroll(event.verticalAmount)
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        if (!isFocused) return
        event.tryUse {
            selecting = InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)
            //全选
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
                cursor = text.length
                selectionEnd = 0
                return@tryUse true
            }
            //复制选中
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
                mc.keyboard.clipboard = this.selectedText
                return@tryUse true
            }
            //粘贴
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
                replaceSelection(mc.keyboard.clipboard)
                return@tryUse true
            }
            //剪切选中
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
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
            //选中当前单词
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.W)) {
                selectWord()
                return@tryUse true
            }
            //另起一行
            if (InputHandler.wasKeyPressed(Keyboard.RIGHT_SHIFT, Keyboard.ENTER)) {
                this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
                replaceSelection("\n")
                return@tryUse true
            }
            //撤回
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
                cursor = text.length
                selectionEnd = 0
                history.undo(text, cursor).let {
                    replaceSelection(it.text, true)
                    cursor = it.cursor
                }
                return@tryUse true
            }
            //重做
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
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
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)) {
                        var lineText = currentLine.getText(text)
                        repeat(4) {
                            lineText = lineText.removePrefix(" ")
                        }
                        selectionEnd = currentLine.beginIndex
                        cursor = currentLine.endIndex
                        replaceSelection(lineText, true)
                    } else {
                        replaceSelection("    ")
                    }
                    true
                }
                //光标左移
                Keyboard.LEFT                     -> {
                    val offset = if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.previousWordOffsetAtCursor
                    } else {
                        -1
                    }
                    this.moveCursor(RELATIVE, offset)
                    true
                }
                //光标右移
                Keyboard.RIGHT                    -> {
                    val offset = if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.nextWordOffsetAtCursor
                    } else {
                        1
                    }
                    this.moveCursor(RELATIVE, offset)
                    true
                }
                //光标上移
                Keyboard.UP                       -> {
                    if (!InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(-1)
                    } else {
                        amount -= fontHeight + spacing
                    }
                    true
                }
                //光标下移
                Keyboard.DOWN                     -> {
                    if (!InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(1)
                    } else {
                        amount += fontHeight + spacing
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
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(ABSOLUTE, 0)
                    } else {
                        this.moveCursor(ABSOLUTE, this.currentLine.beginIndex)
                    }
                    true
                }
                //光标移动至行尾,如果按下了ctrl则移动到文本结尾
                Keyboard.END                      -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(END, 0)
                    } else {
                        this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
                    }
                    true
                }
                //删除选中,如果没有选中则删除光标前的一个字符,如果按下了ctrl则删除光标前的一个单词
                Keyboard.BACKSPACE                -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        delete(this.previousWordOffsetAtCursor)
                    } else {
                        delete(-1)
                    }
                    true
                }
                //删除选中,如果没有选中则删除光标后的一个字符,如果按下了ctrl则删除光标后的一个单词
                Keyboard.DELETE                   -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        delete(this.nextWordOffsetAtCursor)
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

    private fun selectWord() {
        this.moveCursor(RELATIVE, previousWordOffsetAtCursor)
        selecting = true
        this.moveCursor(RELATIVE, nextWordOffsetAtCursor)
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        selecting = if (event.keyCode == Keyboard.LEFT_SHIFT) false else selecting
    }

    override fun onCharTyped(event: CharTypedEvent) {
        event.tryUse { this.isFocused && StringHelper.isValidChar(event.char) }.onSuccess {
            replaceSelection(event.char.toString())
        }
    }


    //------------ IGDrawable ------------\\

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
                    pushTextLines(
                        hintText!!,
                        contentBox,
                        horizontalAlignment = Alignment.Left,
                        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.Top),
                        defaultColor = hintColor,
                        layerType = TextRenderer.TextLayerType.SEE_THROUGH
                    )
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
                        pushText(
                            text.substring(it.beginIndex, it.endIndex),
                            contentBox.left,
                            y,
                            color = textColor,
                            layerType = TextRenderer.TextLayerType.SEE_THROUGH
                        )
                    y += fontHeight + spacing
                }
            }
        //渲染选中文本高亮
        //应该最多渲染三个矩形
        if (selectedText.isNotEmpty() && isFocused) {
            val (start, end) = selection
            val startXOffset = text.substring(currentLine(start).beginIndex, start).width
            val endXOffset = text.substring(currentLine(end).beginIndex, end).width
            val startY = contentBox.top + currentLineIndex(start) * (fontHeight + spacing) - amount
            val endY = contentBox.top + currentLineIndex(end) * (fontHeight + spacing) - amount
            val mindY = (startY + (fontHeight + spacing)).let { if (it == endY) 0f else it }
            if (startY == endY) {
                context.renderBox(
                    Box(contentBox.left + startXOffset, startY, selection.getText(this.text).size),
                    selectedColor, RenderLayer.getGuiTextHighlight()
                )
            } else if (mindY == 0f) {
                context.batchRenderBox(RenderLayer.getGuiTextHighlight()) {
                    pushBox(Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, fontHeight + spacing)), selectedColor)
                    pushBox(Box(contentBox.left, endY, Size(endXOffset, fontHeight.toFloat())), selectedColor)
                }
            } else {
                context.batchRenderBox(RenderLayer.getGuiTextHighlight()) {
                    pushBox(Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, fontHeight + spacing)), selectedColor)
                    pushBox(Box(contentBox.left, mindY, Size(contentBox.width, endY - startY - (fontHeight + spacing))), selectedColor)
                    pushBox(Box(contentBox.left, endY, Size(endXOffset, fontHeight.toFloat())), selectedColor)
                }
            }
        }
    }

    private fun renderCursor(context: IGDrawContext) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val contentBox = contentBox(true)
            val thickness = 0.75f
            val xOffset =
                text.substring(currentLine.beginIndex, cursor).width.let { if (cursor == text.length) it else it - thickness }
            val y = contentBox.top + currentLineIndex * (fontHeight + spacing) - amount - spacing
            if (y !in contentBox.top - fontHeight..contentBox.bottom) return
            context.renderBox(Box(contentBox.left + xOffset, y + spacing, Size(thickness, textRenderer.fontHeight.toFloat())), cursorColor)
        }
    }


    companion object {

        const val UNLIMITED_LENGTH = Int.MAX_VALUE

    }

    fun interface Scope : GuiScope<TextAreaWidget> {

        var text: String
            get() = owner().text
            set(value) {
                owner().apply {
                    cursor = text.length
                    selectionEnd = 0
                    replaceSelection(value, true)
                }
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

        fun suggestion(suggestion: ((text: String, preWord: String, cursor: Int) -> Iterable<Text>)) {
            owner().suggestion = suggestion
        }

        fun textConsumer(consumer: (String) -> Unit) {
            owner().onTextChanged = consumer
        }

        fun amountConsumer(consumer: (Float) -> Unit) {
            owner().scrollState.subscribe(consumer)
        }
    }

}

typealias TextAreaScope = TextAreaWidget.Scope

fun WidgetContainerScope.TextArea(
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    scrollState: ScrollState = ScrollState(),
    textColor: ARGBColor = Color(0x303030),
    hintColor: ARGBColor = Color(0x707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0x008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    spacing: Float = 1f,
    textRenderer: TextRenderer = mc.textRenderer,
    scope: TextAreaScope.() -> Unit = {}
) = addWidgetChild(
    TextAreaWidget(
        maxLength,
        scrollState,
        textColor,
        hintColor,
        bgShaderColor,
        selectedColor,
        suggestionColor,
        cursorColor,
        spacing,
        textRenderer
    )
) {
    Modifier
        .name("TextArea")
        .mouseOverCursor(MouseCursor.IBEAM_CURSOR)
        .padding(5.5f)
        .measureCompletion {
            this as TextAreaWidget
            scrollState {
                amountStep = textRenderer.fontHeight / 2f
                maxAmount = textContentHeight - contentHeight
                barProportion = contentHeight / textContentHeight
            }
        }
        .renderBackground { context, _, _, _ ->
            this as TextAreaWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(WidgetTheme.TextInput), this@addWidgetChild.bgShaderColor)
            }
        }.then(modifier).foldInApply()
    TextAreaScope { this }.scope()
}

fun WidgetContainerScope.TextAreaWrapped(
    maxLength: Int = Int.MAX_VALUE,
    textRenderer: TextRenderer = mc.textRenderer,
    barThickness: Float = 9f,
    scrollState: ScrollState = ScrollState(),
    modifier: Modifier = Modifier,
    textAreaModifier: RowScope .() -> Modifier = { Modifier },
    scrollerModifier: RowScope.() -> Modifier = { Modifier },
    scope: TextAreaScope.() -> Unit = {}
): RowWidget {
    var textArea: TextAreaWidget? = null
    return Row(
        modifier = Modifier
            .padding(5.5f, 4f, 5.5f, 5.5f)
            .renderBackground { ctx, _, _, _ ->
                ctx.batchRenderTextureColored {
                    textArea?.let {
                        pushWidgetTexture(transform, it.theme(WidgetTheme.TextInput), it.bgShaderColor)
                    }
                }
            } then modifier
    ) {
        textArea = TextArea(
            modifier = Modifier
                .padding(0)
                .fill()
                .weight(1)
                .renderBackground { _, _, _, _ -> }
                .measureCompletion {
                    this as TextAreaWidget
                    scrollState {
                        amountStep = textRenderer.fontHeight / 2f
                        maxAmount = textContentHeight - contentHeight
                        barProportion = contentHeight / textContentHeight
                    }
                }
                .mouseScrolling {
                    if (this.wasMouseOver) scrollState.scroll(it.verticalAmount)
                } then textAreaModifier(),
            maxLength,
            scrollState = scrollState,
            textRenderer = textRenderer
        ) {
            scope()
        }
        Scroller(
            scrollState = scrollState,
            orientation = Orientation.Vertical,
            modifier = Modifier
                .matchSibling()
                .width(barThickness)
                .margin(left = 1f) then scrollerModifier()
        )
    }

}