package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useScissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.lateInitValueOf
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.disableNotification
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.Whence
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.StringUtil
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
    private val font: Font = mc.font
) : GuiWidgetImpl() {

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
        get() = (lineCount * (lineHeight + spacing)) - spacing

    val lineHeight by font::lineHeight

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
            if (enableTextNotification) this.onTextChanged(field)
            onChange()
        }

    var enableTextNotification = true

    fun disableTextNotification(block: () -> Unit) {
        enableTextNotification = false
        block()
        enableTextNotification = true
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

        val firstLine: Substring = getLine((ceil(amount / (lineHeight + spacing))).toInt())
        if (this.cursor <= firstLine.beginIndex) {
            amount = this.currentLineIndex * (spacing + lineHeight) - spacing
        } else {
            val endLine: Substring = getLine((floor(amount + this.height) / (lineHeight + spacing)).toInt() - 1)
            if (this.cursor > endLine.endIndex) {
                amount = this.currentLineIndex * (spacing + lineHeight) - spacing - this.height + lineHeight + this.padding.height
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
        val string2 = truncate(StringUtil.filterText(string, true))
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

    private fun moveCursor(movement: Whence, amount: Int) {
        when (movement) {
            Whence.ABSOLUTE -> {
                cursor = amount
            }

            Whence.RELATIVE -> {
                cursor += amount
            }

            Whence.END      -> {
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
        val amount = font.plainSubstrByWidth(text.substring(substring.beginIndex, substring.endIndex), i).length
        moveCursor(Whence.ABSOLUTE, substring.beginIndex + amount)
    }

    private fun moveCursor(mouseX: Float, mouseY: Float) {
        val x = floor(mouseX - this.transform.worldX - padding.left + 3f).toInt()
        val y = floor((mouseY - this.transform.worldY - padding.top + amount) / (lineHeight + spacing)).toInt()
        val substring: Substring = lines[y.coerceIn(0, lines.lastIndex)]
        val amount = font.plainSubstrByWidth(text.substring(substring.beginIndex, substring.endIndex), x).length
        this.moveCursor(Whence.ABSOLUTE, substring.beginIndex + amount)
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
            StringUtil.truncateStringIfNecessary(value, maxLength, false)
        } else value
    }

    private fun truncate(value: String): String {
        if (this.hasMaxLength) {
            val i = maxLength - text.length
            return StringUtil.truncateStringIfNecessary(value, i, false)
        }
        return value
    }

    //------------ Measure ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.merge(constraints)
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
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
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
                mc.keyboardHandler.clipboard = this.selectedText
                return@tryUse true
            }
            //粘贴
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
                replaceSelection(mc.keyboardHandler.clipboard)
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
                mc.keyboardHandler.clipboard = this.selectedText
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
                this.moveCursor(Whence.ABSOLUTE, this.currentLine.endIndex)
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
                    selectionEnd = it.cursor
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
                    selectionEnd = it.cursor
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
                    this.moveCursor(Whence.RELATIVE, offset)
                    true
                }
                //光标右移
                Keyboard.RIGHT                    -> {
                    val offset = if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.nextWordOffsetAtCursor
                    } else {
                        1
                    }
                    this.moveCursor(Whence.RELATIVE, offset)
                    true
                }
                //光标上移
                Keyboard.UP                       -> {
                    if (!InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(-1)
                    } else {
                        amount -= lineHeight + spacing
                    }
                    true
                }
                //光标下移
                Keyboard.DOWN                     -> {
                    if (!InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        moveCursorLine(1)
                    } else {
                        amount += lineHeight + spacing
                    }
                    true
                }
                //上一页
                Keyboard.PAGE_UP                  -> {
                    this.moveCursor(Whence.ABSOLUTE, 0)
                    true
                }
                //下一页
                Keyboard.PAGE_DOWN                -> {
                    this.moveCursor(Whence.END, 0)
                    true
                }
                //光标移动至行首,如果按下了ctrl则移动到文本开头
                Keyboard.HOME                     -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(Whence.ABSOLUTE, 0)
                    } else {
                        this.moveCursor(Whence.ABSOLUTE, this.currentLine.beginIndex)
                    }
                    true
                }
                //光标移动至行尾,如果按下了ctrl则移动到文本结尾
                Keyboard.END                      -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        this.moveCursor(Whence.END, 0)
                    } else {
                        this.moveCursor(Whence.ABSOLUTE, this.currentLine.endIndex)
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
        this.moveCursor(Whence.RELATIVE, previousWordOffsetAtCursor)
        selecting = true
        this.moveCursor(Whence.RELATIVE, nextWordOffsetAtCursor)
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        selecting = if (event.keyCode == Keyboard.LEFT_SHIFT) false else selecting
    }

    override fun onCharTyped(event: CharTypedEvent) {
        event.tryUse { this.isFocused && StringUtil.isAllowedChatCharacter(event.codepoint) }.onSuccess {
            replaceSelection(event.codepoint.toString())
        }
    }


    //------------ IGDrawable ------------\\

    override fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) {
        guiGraphics.useScissor(contentBox(true)) {
            renderText(guiGraphics)
        }
        guiGraphics.useScissor(contentBox(true).expandEdges(1f)) {
            renderCursor(guiGraphics)
        }
    }

    private fun renderText(guiGraphics: IGGuiGraphics) {
        val contentBox = contentBox(true)
        //渲染提示文本
        if (text.isEmpty() && !isFocused) {
            if (hintText != null) {
                guiGraphics.pushTextLines(hintText!!, contentBox, Alignment.Left, Arrangement.spacedBy(spacing, Alignment.Top), hintColor, font = font)
            }
            return
        }
        //渲染文本本体
        if (text.isNotEmpty()) guiGraphics {
            var y = contentBox.top - amount
            lines.forEach {
                if (y in contentBox.top - lineHeight..contentBox.bottom)
                    pushText(
                        text.substring(it.beginIndex, it.endIndex),
                        contentBox.left,
                        y,
                        color = textColor,
                        font = font
                    )
                y += lineHeight + spacing
            }
        }
        //渲染选中文本高亮
        //应该最多渲染三个矩形
        if (selectedText.isNotEmpty() && isFocused) {
            val (start, end) = selection
            val startXOffset = text.substring(currentLine(start).beginIndex, start).width
            val endXOffset = text.substring(currentLine(end).beginIndex, end).width
            val startY = contentBox.top + currentLineIndex(start) * (lineHeight + spacing) - amount
            val endY = contentBox.top + currentLineIndex(end) * (lineHeight + spacing) - amount
            val mindY = (startY + (lineHeight + spacing)).let { if (it == endY) 0f else it }
            if (startY == endY) {
                guiGraphics.pushTextHighLight(Box(contentBox.left + startXOffset, startY, selection.getText(this.text).size), highLightColor = selectedColor)
            } else if (mindY == 0f) {
                guiGraphics {
                    pushTextHighLight(
                        Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, lineHeight + spacing)),
                        highLightColor = selectedColor
                    )
                    pushTextHighLight(Box(contentBox.left, endY, Size(endXOffset, lineHeight.toFloat())), highLightColor = selectedColor)
                }
            } else {
                guiGraphics {
                    pushTextHighLight(
                        Box(contentBox.left + startXOffset, startY, Size(contentBox.width - startXOffset, lineHeight + spacing)),
                        highLightColor = selectedColor
                    )
                    pushTextHighLight(
                        Box(contentBox.left, mindY, Size(contentBox.width, endY - startY - (lineHeight + spacing))),
                        highLightColor = selectedColor
                    )
                    pushTextHighLight(Box(contentBox.left, endY, Size(endXOffset, lineHeight.toFloat())), highLightColor = selectedColor)
                }
            }
        }
    }

    private fun renderCursor(guiGraphics: IGGuiGraphics) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val contentBox = contentBox(true)
            val thickness = 0.75f
            val xOffset =
                text.substring(currentLine.beginIndex, cursor).width.let { if (cursor == text.length) it else it - thickness }
            val y = contentBox.top + currentLineIndex * (lineHeight + spacing) - amount - spacing
            if (y !in contentBox.top - lineHeight..contentBox.bottom) return
            guiGraphics.pushBox(Box(contentBox.left + xOffset, y + spacing, Size(thickness, lineHeight.toFloat())), cursorColor)
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

        fun bindState(text: MutableState<String>) {
            this.text = text.getValue()
            text.subscribe {
                text.disableNotification {
                    this.text = it
                }
            }
            textConsumer {
                disableTextNotification {
                    text.setValue(it)
                }
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

        fun disableTextNotification(block: () -> Unit) {
            owner().disableTextNotification(block)
        }
    }

}

typealias TextAreaScope = TextAreaWidget.Scope

fun ContainerScope.TextArea(
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
    font: Font = mc.font,
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
        font
    )
) {
    Modifier
        .name("TextArea")
        .mouseOverCursor(MouseCursor.IBEAM_CURSOR)
        .padding(5.5f)
        .measureCompletion {
            this as TextAreaWidget
            scrollState {
                amountStep = font.lineHeight / 2f
                maxAmount = textContentHeight - contentHeight
                barProportion = contentHeight / textContentHeight
            }
        }
        .renderBackground { guiGraphics, _, _, _ ->
            this as TextAreaWidget
            guiGraphics.pushWidgetTexture(transform, theme(WidgetTheme.TextInput), this@addWidgetChild.bgShaderColor)

        }.then(modifier).foldInApply()
    TextAreaScope { this }.scope()
}

fun ContainerScope.TextAreaWrapped(
    maxLength: Int = Int.MAX_VALUE,
    font: Font = mc.font,
    barThickness: Float = 9f,
    scrollState: ScrollState = ScrollState(),
    modifier: Modifier = Modifier,
    textAreaModifier: RowScope .() -> Modifier = { Modifier },
    scrollerModifier: BoxScope.() -> Modifier = { Modifier },
    scope: TextAreaScope.() -> Unit = {}
): RowWidget {
    var textArea: TextAreaWidget? = null
    var recompose by lateInitValueOf {}
    var renderBar = false

    return Row(
        modifier = Modifier
            .padding(5.5f, 4f, 5.5f, 5.5f)
            .renderBackground { guiGraphics, _, _, _ ->
                textArea?.let {
                    guiGraphics.pushWidgetTexture(transform, it.theme(WidgetTheme.TextInput), it.bgShaderColor)
                }
            }.layoutCompleted {
                onLayoutCompletion()
                val oldState = renderBar
                renderBar = scrollState.barProportion != 1f && scrollState.barProportion != 0f
                if (oldState != renderBar) {
                    recompose()
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
                        amountStep = font.lineHeight / 2f
                        maxAmount = textContentHeight - contentHeight
                        barProportion = contentHeight / textContentHeight
                    }
                }
                .mouseScrolling {
                    if (this.wasMouseOver) scrollState.scroll(it.verticalAmount)
                } then textAreaModifier(),
            maxLength,
            scrollState = scrollState,
            font = font
        ) {
            scope()
        }
        Box(Modifier.matchSibling()) {
            if (renderBar) {
                Scroller(
                    scrollState = scrollState,
                    orientation = Orientation.Vertical,
                    modifier = Modifier
                        .fillHeight()
                        .width(barThickness)
                        .margin(top = 1f, left = 1f) then scrollerModifier()
                )
            }
        }.apply {
            recompose = { this.executeRecompose() }
        }
    }
}