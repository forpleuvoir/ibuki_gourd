package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.element.isInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.width
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.copy
import moe.forpleuvoir.ibukigourd.util.math.plus
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.soundManager
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.disableNotification
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.input.CursorMovement
import net.minecraft.client.input.CursorMovement.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.StringHelper
import net.minecraft.util.Util
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Suppress("MemberVisibilityCanBePrivate", "Unused")
open class TextEditorWidget(
    var textColor: ARGBColor = Color(0xFF303030),
    var hintColor: ARGBColor = Color(0xFF707070),
    var bgShaderColor: ARGBColor = Colors.WHITE,
    var selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    var suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    var cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    private val textRenderer: TextRenderer = mc.textRenderer
) : IGWidgetImpl() {

    //------------ TextField ------------\\

    var text: String = ""
        set(value) {
            if (value != field && textPredicate(value)) {
                field = value
                if (enableTextNotification) onTextChanged(field)
                if (!constraints.widthFixed()) {
                    remeasure()
                }
            }
        }

    var enableTextNotification = true

    fun disableTextNotification(block: () -> Unit) {
        enableTextNotification = false
        block()
        enableTextNotification = true
    }

    private val history: HistoryRecord = HistoryRecord(currentRecord = HistoryRecord.Record(text, cursor))

    private var hintText: State<String?> = stateOf(null)

    var suggestion: ((text: String, cursor: Int) -> String)? = null

    var onTextChanged: (text: String) -> Unit = {}

    var textPredicate: (text: String) -> Boolean = { true }

    /**
     * The index of the leftmost character that is rendered on a screen.
     */
    private var firstCharacterIndex: Int = 0

    var selectionStart: Int = 0
        private set(value) {
            field = value.coerceIn(0, text.length)
        }

    var selectionEnd: Int = 0
        private set(value) {
            val textLength = text.length
            field = value.coerceIn(0, textLength)
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
            firstCharacterIndex = firstCharacterIndex.coerceIn(0, textLength)
        }

    var cursor: Int
        get() = selectionStart
        private set(value) {
            selectionStart = value
            if (!selecting) {
                selectionEnd = selectionStart
            }
        }

    val cursorChar: Char get() = runCatching { text[cursor] }.getOrElse { ' ' }

    private var maxLength = 255
        set(value) {
            field = value
            if (text.length > value) {
                text = text.substring(0, value)
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
        this.eraseCharacters(offset)
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
        if (!selecting) {
            selectionEnd = cursor
        }
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
        val c = this.constraints.merge(constraints)
        val width =
            text.isNotEmpty().pick(text.width, hintText.getValue()?.width ?: 0).toFloat() + padding.width + 5f
        val height = textRenderer.fontHeight + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    override fun onMeasureCompletion() {
        firstCharacterIndex = 0
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

    private fun selectWord() {
        this.moveCursor(RELATIVE, previousWordOffsetAtCursor)
        selecting = true
        this.moveCursor(RELATIVE, nextWordOffsetAtCursor)
        selecting = false
    }

    override fun onKeyPress(event: KeyPressEvent) {
        if (!this.isActive) return
        selecting = InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)
        event.tryUse {
            //全选文本
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
                setCursorToEnd()
                this.selectionEnd = 0
                return@tryUse true
            }
            //选中当前单词
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.W)) {
                selectWord()
                return@tryUse true
            }
            //复制选中文本
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
                mc.keyboard.clipboard = this.selectedText
                return@tryUse true
            }
            //粘贴文本
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
                write(mc.keyboard.clipboard)
                return@tryUse true
            }
            //剪切选中文本
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
                mc.keyboard.clipboard = this.selectedText
                write("")
                return@tryUse true
            }
            //撤销
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Z)) {
                setCursorToEnd()
                this.selectionEnd = 0
                history.undo(text, cursor).let {
                    write(it.text, true)
                    cursor = it.cursor
                    selectionEnd = cursor
                }
                return@tryUse true
            }
            //重做
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.Y)) {
                setCursorToEnd()
                this.selectionEnd = 0
                history.redo(text, cursor).let {
                    write(it.text, true)
                    cursor = it.cursor
                    selectionEnd = cursor
                }
                return@tryUse true
            }
            return@tryUse when (event.keyCode) {
                //制表符，如果有建议文本则补全建议文本，否则输入四个空格
                Keyboard.TAB       -> {
                    if (suggestion != null && cursor == text.length) {
                        suggestion!!(text, cursor).let {
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
                    val offset = if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        previousWordOffsetAtCursor
                    } else {
                        -1
                    }
                    moveCursor(RELATIVE, offset)
                    true
                }
                //光标右移,如果按下左控制键则跳过一个单词
                Keyboard.RIGHT     -> {
                    val offset = if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        nextWordOffsetAtCursor
                    } else {
                        1
                    }
                    moveCursor(RELATIVE, offset)
                    true
                }
                //删除一个字符
                Keyboard.BACKSPACE -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        erase(this.previousWordOffsetAtCursor)
                    } else {
                        erase(-1)
                    }
                    true
                }
                //删除一个字符
                Keyboard.DELETE    -> {
                    if (InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL)) {
                        selecting = true
                        this.moveCursor(RELATIVE, nextWordOffsetAtCursor)
                        selecting = false
                    }
                    erase(1)
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
        if (text.isEmpty()) {
            cursor = 0
            return
        }
        val str = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentWidth.toInt())
        val xOffset = (mouseX - contentLeft(true)).coerceAtLeast(0f)
        val count = textRenderer.trimToWidth(str, xOffset.toInt()).length
        val countWidth = text.substring(firstCharacterIndex, firstCharacterIndex + count).width
        val endCharWidth = text[(count + firstCharacterIndex + 1).coerceIn(0..text.lastIndex.coerceAtLeast(0))].toString().width
        val offset = if (xOffset - countWidth > endCharWidth / 2f) 1 else 0
        cursor = count + firstCharacterIndex + offset
    }

    private var lastPressTime = TimeSource.Monotonic.markNow()

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        event.tryUse {
            wasMouseOver && event.button == Mouse.LEFT
        }.onSuccess {
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            val oldCursor = cursor
            setCursorFromMouse(event.x)
            if (oldCursor == cursor && lastPressTime.elapsedNow() < 500.milliseconds) {
                selectWord()
            }
            lastPressTime = TimeSource.Monotonic.markNow()
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        event.tryUse { wasDragging }.onSuccess {
            selecting = true
            setCursorFromMouse(event.x)
            selecting = InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)
        }
    }


    override fun onMouseScrolling(event: MouseScrollEvent) {
        if (!isActive) return
        event.tryUse { wasMouseOver }
            .onSuccess {
                moveCursor(RELATIVE, (event.verticalAmount < 0f).pick(1, -1))
            }
    }

    //------------ Render ------------\\

    fun renderCursor(content: DrawContext) {
        if (focusedTicks % 15 >= 5 && isFocused) {
            val box = contentBox(true)
            val height = textRenderer.fontHeight.toFloat()
            val thickness = 0.75f
            val y = box.top + (box.height - height) / 2f
            val offset = text.substring(
                min(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0),
                max(firstCharacterIndex, cursor).coerceAtMost(text.length).coerceAtLeast(0)
            ).width

            content.renderBox(Box(box.left + offset - 0.85f, y, Size(thickness, height)), cursorColor)
        }
    }

    fun renderText(content: DrawContext) {
        val contentBox = contentBox(true)
        content.useMatrixStack { matrixStack ->
            matrixStack.translate(0f, textRenderOffset.y(), 0f)
            content.batchRenderText(textRenderer) {
                //"渲染提示文本"
                if (text.isEmpty() && hintText.getValue() != null && !isFocused) {
                    pushAlignmentText(hintText.getValue()!!, contentBox, color = hintColor, layerType = TextRenderer.TextLayerType.SEE_THROUGH)
                }
                //"渲染文本本体"
                val renderText = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentBox.width.toInt())
                renderText.takeIf { it.isNotEmpty() }?.let {
                    pushAlignmentText(it, contentBox, color = textColor, layerType = TextRenderer.TextLayerType.SEE_THROUGH)
                }
                //"渲染文本建议"
                suggestion?.invoke(text, cursor)?.let { suggestion ->
                    if (isFocused && cursor == text.length) {
                        val renderTextWidth = renderText.width
                        val box =
                            Box(contentBox.position + Vector2f(renderTextWidth), contentBox.width - renderTextWidth, contentBox.height)
                        pushAlignmentText(suggestion, box, color = suggestionColor, layerType = TextRenderer.TextLayerType.SEE_THROUGH)
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
                        text.substring(firstCharacterIndex, firstCharacterIndex + startIndex).width
                    else 0f
            val end = contentBox.left +
                    if (endIndex > 0)
                        text.substring(firstCharacterIndex, firstCharacterIndex + endIndex).width
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
        context.useScissor(contentBox(true)) {
            renderText(context)
        }
        context.useScissor(transform.asWorldCoordinateBox) {
            renderCursor(context)
        }
    }

    fun interface Scope : GuiScope<TextEditorWidget> {

        var text: String
            get() = owner().text
            set(value) {
                owner().apply {
                    setCursorToEnd()
                    this.selectionEnd = 0
                    write(value, true)
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

        var hintText: State<String?>
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

        fun suggestion(suggestion: (text: String, cursor: Int) -> String) {
            owner().suggestion = suggestion
        }

        fun textConsumer(consumer: (text: String) -> Unit) {
            owner().onTextChanged = consumer
        }

        fun textPredicate(predicate: (text: String) -> Boolean) {
            owner().textPredicate = predicate
        }

        fun disableTextNotification(block: () -> Unit) {
            owner().disableTextNotification(block)
        }
    }

}

typealias TextEditorScope = TextEditorWidget.Scope

fun ContainerScope.TextEditor(
    modifier: Modifier = Modifier,
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: TextEditorScope.() -> Unit = {}
) = addWidgetChild(TextEditorWidget(textColor, hintColor, bgShaderColor, selectedColor, suggestionColor, cursorColor, textRenderer)) {
    Modifier
        .name("TextEditor")
        .mouseOverCursor(MouseCursor.IBEAM_CURSOR)
        .padding(5, 5, 5, 4)
        .renderBackground { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(WidgetTheme.TextInput), bgShaderColor)
            }
        }
        .then(modifier).foldInApply()
    TextEditorScope { this }.scope()
}

data class ValueStep<T>(val click: T, val shift: T, val ctrl: T, val alt: T, val mouseScroller: T) where T : Comparable<T>, T : Number

fun <T> ContainerScope.NumberEditor(
    value: MutableState<T>,
    valueMapper: (T) -> String,
    valueRange: ClosedRange<T>,
    textMapper: (String) -> T,
    plus: (T, T) -> T,
    minus: (T, T) -> T,
    step: ValueStep<T>,
    textPredicate: (String) -> Boolean,
    modifier: Modifier = Modifier,
    editorModifier: RowScope.() -> Modifier = { Modifier },
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: RowScope.() -> Unit = {},
    editorScope: TextEditorScope .() -> Unit = {}
) where  T : Comparable<T>, T : Number = Row(
    Modifier
        .name("NumberEditor")
        .padding(2, 4, 2, 2)
        .renderBackground { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(
                    transform,
                    theme(
                        WidgetTheme.TextInput,
                        hovered = screen()?.hoveredWidget?.getValue()?.isInParentChain(this@renderBackground) == true
                                || screen()?.focusedWidget?.getValue()?.isInParentChain(this@renderBackground) == true
                    ),
                    bgShaderColor
                )
            }
        }.then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    value.onSetValue = { it.coerceIn(valueRange) }
    TextEditor(
        modifier = Modifier
            .padding(3, 3, 3, 2)
            .disableRenderBackground()
            .mouseScrolling { event ->
                event.tryUse(wasMouseOver && isFocused).onSuccess {
                    val s = if (event.verticalAmount > 0) plus(value.getValue(), step.mouseScroller)
                    else minus(value.getValue(), step.mouseScroller)
                    value.setValue(s)
                }
            }.then(editorModifier()),
        textColor, hintColor, bgShaderColor, selectedColor, suggestionColor, cursorColor, textRenderer
    ) {
        text = valueMapper(value.getValue())
        var notifiable = true
        textConsumer {
            notifiable = false
            value.setValue(textMapper(it))
            notifiable = true
        }
        value.subscribe {
            if (notifiable) text = valueMapper(value.getValue())
        }
        textPredicate(textPredicate)
        editorScope()
    }
    Column(
        Modifier.height(12f),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FlatButton(
            modifier = Modifier.padding(right = 2f).margin(top = 1f),
            round = 0,
            hoveredColor = Colors.GRAY.opacity(.15f)
        ) {
            Box(Modifier.size(5f, 5f)) {
                Icon(IconTextures.PLUS, Colors.BLACK)
            }
            click {
                val s = when {
                    InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)   -> step.shift
                    InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL) -> step.ctrl
                    InputHandler.wasKeyPressed(Keyboard.LEFT_ALT)     -> step.alt
                    else                                              -> step.click
                }
                value.setValue(plus(value.getValue(), s))
            }
        }
        FlatButton(
            modifier = Modifier.padding(right = 2f),
            round = 0,
            hoveredColor = Colors.GRAY.opacity(.15f)
        ) {
            Box(Modifier.size(5f, 5f)) {
                Icon(IconTextures.MINUS, Colors.BLACK)
            }
            click {
                val s = when {
                    InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)   -> step.shift
                    InputHandler.wasKeyPressed(Keyboard.LEFT_CONTROL) -> step.ctrl
                    InputHandler.wasKeyPressed(Keyboard.LEFT_ALT)     -> step.alt
                    else                                              -> step.click
                }
                value.setValue(minus(value.getValue(), s))
            }
        }
    }
    scope()
}


fun ContainerScope.IntEditor(
    value: MutableState<Int>,
    range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
    step: ValueStep<Int> = ValueStep(1, 5, 10, 15, 1),
    valueMapper: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    editorModifier: RowScope.() -> Modifier = { Modifier },
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: RowScope.() -> Unit = {},
    editorScope: TextEditorScope.() -> Unit = {}
) = NumberEditor(
    value = value,
    valueRange = range,
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
    valueMapper = valueMapper,
    textMapper = { runCatching { it.toInt() }.getOrElse { 0 } },
    step = step,
    textPredicate = { (Regex("-?\\d+").matches(it) && runCatching { it.toInt() in range }.getOrElse { false }) || it.isEmpty() },
    modifier = modifier.attachLeft { name("IntEditor") },
    editorModifier = editorModifier,
    textColor = textColor,
    hintColor = hintColor,
    bgShaderColor = bgShaderColor,
    selectedColor = selectedColor,
    suggestionColor = suggestionColor,
    cursorColor = cursorColor,
    textRenderer = textRenderer,
    scope = scope,
    editorScope = editorScope
)

fun ContainerScope.LongEditor(
    value: MutableState<Long>,
    range: LongRange = Long.MIN_VALUE..Int.MAX_VALUE,
    step: ValueStep<Long> = ValueStep(1, 5, 10, 15, 1),
    valueMapper: (Long) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    editorModifier: RowScope.() -> Modifier = { Modifier },
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: RowScope.() -> Unit = {},
    editorScope: TextEditorScope.() -> Unit = {}
) = NumberEditor(
    value = value,
    valueRange = range,
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
    valueMapper = valueMapper,
    textMapper = { runCatching { it.toLong() }.getOrElse { 0L } },
    step = step,
    textPredicate = { (Regex("-?\\d+").matches(it) && runCatching { it.toLong() in range }.getOrElse { false }) || it.isEmpty() },
    modifier = modifier.attachLeft { name("LongEditor") },
    editorModifier = editorModifier,
    textColor = textColor,
    hintColor = hintColor,
    bgShaderColor = bgShaderColor,
    selectedColor = selectedColor,
    suggestionColor = suggestionColor,
    cursorColor = cursorColor,
    textRenderer = textRenderer,
    scope = scope,
    editorScope = editorScope
)

fun ContainerScope.FloatEditor(
    value: MutableState<Float>,
    range: ClosedFloatingPointRange<Float> = Float.NEGATIVE_INFINITY..Float.POSITIVE_INFINITY,
    step: ValueStep<Float> = ValueStep(1f, 5f, 10f, 15f, 1f),
    valueMapper: (Float) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    editorModifier: RowScope.() -> Modifier = { Modifier },
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: RowScope.() -> Unit = {},
    editorScope: TextEditorScope.() -> Unit = {}
) = NumberEditor(
    value = value,
    valueRange = range,
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
    valueMapper = valueMapper,
    textMapper = { runCatching { it.toFloat() }.getOrElse { 0f } },
    step = step,
    textPredicate = {
        val str = if (it.endsWith('.') || it.isEmpty()) "${it}0" else it
        Regex("-?\\d+(\\.\\d+)?").matches(str) && runCatching { str.toFloat() in range }.getOrElse { false }
    },
    modifier = modifier.attachLeft { name("FloatEditor") },
    editorModifier = editorModifier,
    textColor = textColor,
    hintColor = hintColor,
    bgShaderColor = bgShaderColor,
    selectedColor = selectedColor,
    suggestionColor = suggestionColor,
    cursorColor = cursorColor,
    textRenderer = textRenderer,
    scope = scope,
    editorScope = editorScope
)


fun ContainerScope.DoubleEditor(
    value: MutableState<Double>,
    range: ClosedFloatingPointRange<Double> = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY,
    step: ValueStep<Double> = ValueStep(1.0, 5.0, 10.0, 15.0, 1.0),
    valueMapper: (Double) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    editorModifier: RowScope.() -> Modifier = { Modifier },
    textColor: ARGBColor = Color(0xFF303030),
    hintColor: ARGBColor = Color(0xFF707070),
    bgShaderColor: ARGBColor = Colors.WHITE,
    selectedColor: ARGBColor = Color(0xFF007F8F).alpha(0.45f),
    suggestionColor: ARGBColor = Color(0xFF008F72).alpha(0.45f),
    cursorColor: ARGBColor = Colors.BLACK.alpha(.8f),
    textRenderer: TextRenderer = mc.textRenderer,
    scope: RowScope.() -> Unit = {},
    editorScope: TextEditorScope.() -> Unit = {}
) = NumberEditor(
    value = value,
    valueRange = range,
    plus = { a, b -> a + b },
    minus = { a, b -> a - b },
    valueMapper = valueMapper,
    textMapper = { runCatching { it.toDouble() }.getOrElse { 0.0 } },
    step = step,
    textPredicate = {
        val str = if (it.endsWith('.') || it.isEmpty()) "${it}0" else it
        Regex("-?\\d+(\\.\\d+)?").matches(str) && runCatching { str.toDouble() in range }.getOrElse { false }
    },
    modifier = modifier.attachLeft { name("DoubleEditor") },
    editorModifier = editorModifier,
    textColor = textColor,
    hintColor = hintColor,
    bgShaderColor = bgShaderColor,
    selectedColor = selectedColor,
    suggestionColor = suggestionColor,
    cursorColor = cursorColor,
    textRenderer = textRenderer,
    scope = scope,
    editorScope = editorScope
)