package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField.Companion
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.render.math.plus
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.pick
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.StringHelper
import net.minecraft.util.Util
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate", "Unused")
open class TextField(
    var textColor: ARGBColor = Color(0x303030),
    var hintColor: ARGBColor = Color(0x707070),
    var bgShaderColor: ARGBColor = Colors.WHITE,
    var selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    var suggestionColor: ARGBColor = Color(0x008F72).alpha(0.45f),
    var cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    private val textRenderer: TextRenderer = mc.textRenderer
) : IGWidgetImpl() {

    //------------ TextField ------------\\

    var text: String = ""
        set(value) {
            if (value != field && textPredicate(value)) {
                field = value
                onTextChanged(field)
            }
        }

    private val history: HistoryRecord = HistoryRecord(currentRecord = HistoryRecord.Record(text, cursor))

    private var hintText: Text? = null

    var suggestion: ((text: String) -> String)? = null

    var onTextChanged: (text: String) -> Unit = {}

    var textPredicate: (text: String) -> Boolean = { true }

    /**
     * The index of the leftmost character that is rendered on a screen.
     */
    private var firstCharacterIndex: Int = 0

    private var selectionStart: Int = 0
        set(value) {
            field = value.clamp(0, text.length)
        }

    private var selectionEnd: Int = 0
        set(value) {
            val textLength = text.length
            field = value.clamp(0, textLength)
            if (firstCharacterIndex > textLength) {
                firstCharacterIndex = textLength
            }
            val width: Int = contentWidth.toInt()
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
        private set(value) {
            selectionStart = value
            if (!selecting) {
                selectionEnd = selectionStart
            }
            onTextChanged(text)
        }

    private var maxLength = 255
        set(value) {
            field = value
            if (text.length > value) {
                text = text.substring(0, value)
                this.onTextChanged(text)
            }
        }

    private var selecting = false

    private var onFocusedChanged: ((Boolean) -> Unit)? = {
        if (!it) {
            selectionStart = 0
            selectionEnd = 0
        }
    }

    private var focusedTicks = 0

    val selectedText: String
        get() {
            return text.substring(
                min(selectionStart, selectionEnd).coerceAtMost(text.length).coerceAtLeast(0),
                max(selectionStart, selectionEnd).coerceAtMost(text.length).coerceAtLeast(0)
            )
        }

    private fun write(text: String, historyOpt: Boolean = false) {
        var string2: String
        var string: String
        var l: Int
        val start = selectionStart.coerceAtMost(selectionEnd)
        val end = selectionStart.coerceAtLeast(selectionEnd)
        val length = maxLength - this.text.length - (start - end)
        if (length < StringHelper.stripInvalidChars(text).also { string = it }.length.also { l = it }) {
            string = string.substring(0, length)
            l = length
        }
        if (!textPredicate(StringBuilder(this.text).replace(start, end, string).toString().also { string2 = it })) {
            return
        }
        this.text = string2
        this.selectionStart = start + l
        this.selectionEnd = selectionStart
        if (!historyOpt)
            history.textChange(this.text, cursor)
    }


    private fun erase(offset: Int) {
        if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
            this.eraseWords(offset)
        } else {
            this.eraseCharacters(offset)
        }
    }

    private fun eraseWords(wordOffset: Int) {
        if (text.isEmpty()) {
            return
        }
        if (selectionEnd != selectionStart) {
            write("")
            return
        }
        this.eraseCharacters(this.getWordSkipPosition(wordOffset) - selectionStart)
    }

    private fun eraseCharacters(characterOffset: Int) {
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

    //------------ Measure ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraint(constraints)
        val width = text.isNotEmpty().pick(textRenderer.getWidth(text), hintText?.let { textRenderer.getWidth(it) } ?: 0).toFloat() + padding.width
        val height = textRenderer.fontHeight + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
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

    override fun onKeyPress(event: KeyPressEvent) {
        if (!this.isActive) return
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
        event.tryUse {
            //全选文本
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
                setCursorToEnd()
                this.selectionEnd = 0
                return@tryUse true
            }
            //复制选中文本
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
                mc.keyboard.clipboard = this.selectedText
                return@tryUse true
            }
            //粘贴文本
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
                write(mc.keyboard.clipboard)
                return@tryUse true
            }
            //剪切选中文本
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
                mc.keyboard.clipboard = this.selectedText
                write("")
                return@tryUse true
            }
            //撤销
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
                setCursorToEnd()
                this.selectionEnd = 0
                history.undo(text, cursor).let {
                    write(it.text, true)
                    cursor = it.cursor
                }
                return@tryUse true
            }
            //重做
            if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
                setCursorToEnd()
                this.selectionEnd = 0
                history.redo(text, cursor).let {
                    write(it.text, true)
                    cursor = it.cursor
                }
                return@tryUse true
            }
            return@tryUse when (event.keyCode) {
                //制表符，如果有建议文本则补全建议文本，否则输入四个空格
                Keyboard.TAB       -> {
                    if (suggestion != null && cursor == text.length) {
                        suggestion!!(text).let {
                            if (it.isNotEmpty()) {
                                write(it)
                            }
                        }
                    } else {
                        write("    ")
                    }
                    true
                }
                //光标左移,如果按下左控制键则跳过一个单词
                Keyboard.LEFT      -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        cursor = this.getWordSkipPosition(-1)
                    } else {
                        moveCursor(-1)
                    }
                    true
                }
                //光标右移,如果按下左控制键则跳过一个单词
                Keyboard.RIGHT     -> {
                    if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        cursor = this.getWordSkipPosition(1)
                    } else {
                        moveCursor(1)
                    }
                    true
                }
                //删除一个字符
                Keyboard.BACKSPACE -> {
                    selecting = false
                    erase(-1)
                    selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                    true
                }
                //删除一个字符
                Keyboard.DELETE    -> {
                    selecting = false
                    erase(1)
                    selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
                    true
                }
                //将光标移动到文本开头
                Keyboard.HOME      -> {
                    setCursorToStart()
                    true
                }
                //将光标移动到文本结尾
                Keyboard.END       -> {
                    setCursorToEnd()
                    true
                }

                else               -> false
            }
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        selecting = if (event.keyCode == Keyboard.LEFT_SHIFT) false else selecting
        super.onKeyRelease(event)
    }

    val isActive: Boolean
        get() = visible && isFocused && active


    override fun onCharTyped(event: CharTypedEvent) {
        if (!isActive) return
        event.tryUse { StringHelper.isValidChar(event.char) }
            .onSuccess {
                write(event.char.toString())
            }
    }

    private fun setCursorFromMouse(mouseX: Float) {
        val str = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentWidth.toInt())
        val xOffset = mouseX - contentLeft(true)
        val count = textRenderer.trimToWidth(str, xOffset.toInt()).length
        val countWidth = textRenderer.getWidth(text.substring(firstCharacterIndex, count)).toFloat()
        val endCharWidth = textRenderer.getWidth(text[(count + firstCharacterIndex + 1).coerceIn(0..text.lastIndex)].toString())
        val offset = if (xOffset - countWidth > endCharWidth / 2f) 1 else 0
        cursor = count + firstCharacterIndex + offset
    }

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        event.tryUse {
            wasMouseOver && event.button == Mouse.LEFT
        }.onSuccess {
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            setCursorFromMouse(event.x)
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        selecting = true
        setCursorFromMouse(event.x)
        selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
    }


    override fun onMouseScrolling(event: MouseScrollEvent) {
        if (!isActive) return
        event.tryUse { wasMouseOver }
            .onSuccess {
                moveCursor((event.verticalAmount < 0f).pick(1, -1))
            }
    }

    //------------ Render ------------\\


    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(transform.asWorldBox, theme(WidgetTheme.TextInput), bgShaderColor)
        }
    }

    fun renderCursor(content: DrawContext) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val box = contentBox(true)
            val height = textRenderer.fontHeight.toFloat()
            val thickness = 0.75f
            val y = box.top + (box.height - height) / 2f
            val offset = textRenderer.getWidth(
                text.substring(
                    min(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0),
                    max(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0)
                )
            )
            if (cursor == text.length) {
                content.renderBox(Box(box.position.copy(box.left + offset, y + height - 1.25f), 5f, thickness), cursorColor)
                return
            }
            if (cursor - firstCharacterIndex > 0) {
                content.renderBox(Box(box.position.copy(box.left + offset - 0.85f, y), thickness, height), cursorColor)
            } else {
                content.renderBox(Box(box.position.copy(y = y), thickness, height), cursorColor)
            }
        }
    }

    fun renderText(content: DrawContext) {
        val contentBox = contentBox(true)
        content.useMatrixStack { matrixStack ->
            matrixStack.translate(0.0f, 0.4f, 0f)
            content.batchRenderText(textRenderer) {
                //"渲染提示文本"
                if (text.isEmpty() && hintText != null && !isFocused) {
                    alignmentText(hintText!!, contentBox, color = hintColor)
                }
                //"渲染文本本体"
                val renderText = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentBox.width.toInt())
                renderText.takeIf { it.isNotEmpty() }?.let {
                    alignmentText(it, contentBox, color = textColor)
                }
                //"渲染文本建议"
                suggestion?.invoke(text)?.let { suggestion ->
                    if (isFocused && cursor == text.length) {
                        val renderTextWidth = textRenderer.getWidth(renderText).toFloat()
                        val box =
                            Box(contentBox.position + Vector2f(renderTextWidth), contentBox.width - renderTextWidth, contentBox.height)
                        alignmentText(suggestion, box, color = suggestionColor)
                    }
                }
            }
        }


        //"渲染选中的文本高亮"
        if (selectedText.isNotEmpty() && isFocused) {
            val (startIndex, endIndex) = (selectionStart - firstCharacterIndex).coerceAtLeast(0) to
                    (selectionEnd - firstCharacterIndex).coerceAtLeast(0)
            val start = contentBox.left +
                    if (startIndex > 0)
                        textRenderer.getWidth(text.substring(firstCharacterIndex, firstCharacterIndex + startIndex)).toFloat()
                    else 0f
            val end = contentBox.left +
                    if (endIndex > 0)
                        textRenderer.getWidth(text.substring(firstCharacterIndex, firstCharacterIndex + endIndex)).toFloat()
                    else 0f
            val width = (start - end).absoluteValue
            val rect = if (selectionEnd > selectionStart) {
                Box(contentBox.position.copy(start), width, contentBox.height)
            } else {
                Box(contentBox.position.copy(end), width, contentBox.height)
            }
            content.renderBox(rect, selectedColor, RenderLayer.getGuiTextHighlight())
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

    companion object {

        fun interface TextFieldScope : GuiScope<TextField> {

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

            fun suggestion(suggestion: (text: String) -> String) {
                owner().suggestion = suggestion
            }

            fun textConsumer(consumer: (text: String) -> Unit) {
                owner().onTextChanged = consumer
            }

            fun textPredicate(predicate: (text: String) -> Boolean) {
                owner().textPredicate = predicate
            }

        }

    }

}

fun GuiScope<out WidgetContainer>.textField(
    textColor: ARGBColor = Color(0x303030),
    hintColor: ARGBColor = Color(0x707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0x007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0x008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    modifier: Modifier? = null,
    scope: Companion.TextFieldScope.() -> Unit = {}
) = owner().addWidgetChild(TextField(textColor, hintColor, bgShaderColor, selectedColor, suggestionColor, cursorColor, textRenderer)) {
    val m = Modifier.padding(5) thenNullable modifier
    m.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
    Companion.TextFieldScope { this }.scope()
}