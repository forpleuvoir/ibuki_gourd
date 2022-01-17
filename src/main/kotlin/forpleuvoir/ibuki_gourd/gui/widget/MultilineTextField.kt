package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutline
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.SharedConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.util.InputUtil.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.StringVisitable
import net.minecraft.util.Language
import net.minecraft.util.math.MathHelper
import java.util.*
import java.util.function.Consumer


/**
 * 多行文本输入框

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 MultilineTextField

 * 创建时间 2021/12/29 17:35

 * @author forpleuvoir

 */
open class MultilineTextField(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	margin: Int = 2,
	multiline: Boolean = true
) : WidgetText(x, y, width, height), Selectable {

	companion object {
		private val mc = MinecraftClient.getInstance()
		private val FILTER_CHARS = charArrayOf('\r', '\u000c')
	}

	private val fontRenderer: TextRenderer get() = mc.textRenderer
	private val fontHeight: Int get() = fontRenderer.fontHeight
	private val margin: Int
	private val maxVisibleLines: Int
	private val wrapWidth: Int
	private val multiline: Boolean
	private var text: String
	private var textChangedListener: Consumer<String>? = null
	private var topVisibleLine = 0
	private var bottomVisibleLine = 0
	private var cursorCounter = 0
	private var isFocused = false
	private var cursorPos = 0
	private var selectionPos: Int

	var backgroundColor: IColor<out Number> = Color4f.BLACK
	var borderColor: IColor<out Number> = Color4f.WHITE
	var textColor: IColor<out Number> = Color4i(224, 224, 224)
	var cursorColor: IColor<out Number> = Color4i(208, 208, 208)

	val scrollbar: Scrollbar = Scrollbar(
		x = this.x + this.width - margin - 4,
		y = this.y + margin,
		width = 4,
		height = this.height - margin * 2,
		maxScroll = { toLines().size * fontHeight },
		percent = { visibleLineCount.toDouble() / toLines().size }
	).apply { this.holdVisible = true }

	fun setTextChangedListener(textChangedListener: Consumer<String>?) {
		this.textChangedListener = textChangedListener
	}

	override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawBackground(matrixStack, mouseX, mouseY, delta)
		renderVisibleText(matrixStack)
		renderCursor(matrixStack)
		scrollbar.render(matrixStack, mouseX, mouseY, delta)
		drawSelectionBox(matrixStack, mouseX, mouseY, delta)
	}

	protected open fun drawBackground(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawRect(matrixStack, x, y, width, height, backgroundColor)
	}

	protected open fun drawBorder(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawOutline(matrixStack, x, y, width, height, 1, borderColor)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
		val isWithinBounds = isWithinBounds(mouseX, mouseY)
		setFocused(isWithinBounds)
		return click(mouseX, mouseY, mouseButton, isWithinBounds)
	}


	override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
		val isWithinBounds = isWithinBounds(mouseX, mouseY)
		setFocused(isWithinBounds)
		return click(mouseX, mouseY, state, isWithinBounds)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		if (!active || !isFocused) return false
		scrollbar - amount * fontHeight
		return if (amount < 0.0) {
			incrementVisibleLines()
			true
		} else if (amount > 0.0) {
			decrementVisibleLines()
			true
		} else {
			false
		}
	}

	private fun click(mouseX: Double, mouseY: Double, mouseButton: Int, isWithinBounds: Boolean): Boolean {
		return if (isFocused && isWithinBounds && mouseButton == 0) {
			val relativeMouseX = mouseX.toInt() - x - margin
			val relativeMouseY = mouseY.toInt() - y - margin
			val y = MathHelper.clamp(relativeMouseY / 9 + topVisibleLine, 0, finalLineIndex)
			val x =
				fontRenderer.trimToWidth(StringVisitable.plain(getLine(y).string), relativeMouseX).string.length
			setCursorPos(countCharacters(y) + x)
			true
		} else {
			false
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (!this.isActive) {
			return false
		}
		if (Screen.isSelectAll(keyCode)) {
			setCursorToEnd()
			setSelectionEnd(0)
			return true
		}
		if (Screen.isCopy(keyCode)) {
			mc.keyboard.clipboard = this.selectedText
		} else if (Screen.isCut(keyCode)) {
			if (selectionDifference != 0) {
				mc.keyboard.clipboard = this.selectedText
				deleteSelectedText()
			}
		} else if (Screen.isPaste(keyCode)) {
			insert(mc.keyboard.clipboard)
		} else if (isKeyComboCtrlBack(keyCode)) {
			deletePrevWord()
		} else {
			if (keyCode == GLFW_KEY_BACKSPACE) {
				if (selectionDifference != 0) {
					deleteSelectedText()
				} else {
					deletePrev()
				}
				return true
			}
			if (keyCode == GLFW_KEY_DELETE) {
				if (selectionDifference != 0) {
					deleteSelectedText()
				} else {
					deleteNext()
				}
				return true
			}
			if (keyCode == GLFW_KEY_TAB) {
				insert("    ")
				return true
			}
			if (keyCode == GLFW_KEY_KP_ENTER) {
				if (multiline) {
					insertNewLine()
				}
				return true
			}
			if (keyCode == GLFW_KEY_ENTER) {
				if (multiline) {
					insertNewLine()
				}
				return true
			}
			if (keyCode == GLFW_KEY_HOME) {
				updateSelectionPos()
				setCursorPos(0)
				return true
			}
			if (keyCode == GLFW_KEY_END) {
				updateSelectionPos()
				setCursorPos(this.text.length)
				return true
			}
			if (keyCode == GLFW_KEY_UP) {
				updateSelectionPos()
				moveUp()
				return true
			}
			if (keyCode == GLFW_KEY_DOWN) {
				updateSelectionPos()
				moveDown()
				return true
			}
			var moveRight: Boolean
			if (keyCode == GLFW_KEY_LEFT) {
				moveRight = true
				if (Screen.hasShiftDown()) {
					if (selectionPos < 0) {
						selectionPos = cursorPos
					}
				} else {
					if (selectionPos > -1) {
						setCursorPos(this.getSelectionStart())
						moveRight = false
					}
					selectionPos = -1
				}
				if (moveRight) {
					moveLeft()
				}
				return true
			}
			if (keyCode == GLFW_KEY_RIGHT) {
				moveRight = true
				if (Screen.hasShiftDown()) {
					if (selectionPos < 0) {
						selectionPos = cursorPos
					}
				} else {
					if (selectionPos > -1) {
						setCursorPos(this.getSelectionEnd())
						moveRight = false
					}
					selectionPos = -1
				}
				if (moveRight) {
					moveRight()
				}
				return true
			}
		}
		return false
	}

	override fun charTyped(typedChar: Char, p_charTyped_2_: Int): Boolean {
		return if (isFocused() && SharedConstants.isValidChar(typedChar)) {
			insert(typedChar.toString())
			updateVisibleLines()
			true
		} else {
			false
		}
	}

	override fun tick() {
		++cursorCounter
	}

	private fun toLines(): List<StringVisitable> {
		return wrapToWidth(this.text, wrapWidth)
	}

	private fun toLinesWithIndication(): List<WrappedString> {
		return wrapToWidthWithIndication(this.text, wrapWidth)
	}

	private fun getLine(line: Int): StringVisitable {
		return if (line >= 0 && line < toLines().size) toLines()[line] else finalLine
	}

	val finalLine: StringVisitable
		get() = getLine(finalLineIndex)

	val currentLine: StringVisitable
		get() = getLine(cursorY)

	val visibleLines: List<StringVisitable>
		get() {
			val lines = toLines()
			val visibleLines: MutableList<StringVisitable> = ArrayList()
			for (i in topVisibleLine..bottomVisibleLine) {
				if (i < lines.size) {
					visibleLines.add(lines[i])
				}
			}
			return visibleLines
		}

	override fun getText(): String {
		return this.text
	}

	override fun setText(newText: String) {
		if (multiline) {
			this.text = newText
		} else {
			this.text = newText.replace("\n".toRegex(), "")
		}
		this.onChanged()
		updateVisibleLines()
	}

	private fun onChanged() {
		if (textChangedListener != null) {
			textChangedListener!!.accept(this.text)
		}
	}

	private val finalLineIndex: Int
		get() = toLines().size - 1

	private fun cursorIsValid(): Boolean {
		val y = cursorY
		return y in (topVisibleLine..bottomVisibleLine)
	}

	private val renderSafeCursorY: Int
		get() = cursorY - topVisibleLine

	val absoluteBottomVisibleLine: Int
		get() = topVisibleLine + (maxVisibleLines - 1)

	private fun getCursorWidth(): Int {
		val line = currentLine
		return fontRenderer.getWidth(line.string.substring(0, MathHelper.clamp(cursorX, 0, line.string.length)))
	}


	private fun isWithinBounds(mouseX: Double, mouseY: Double): Boolean {
		return isMouseOver(mouseX, mouseY)
	}

	fun atBeginningOfLine(): Boolean {
		return cursorX == 0
	}

	fun atEndOfLine(): Boolean {
		return cursorX == currentLine.string.length
	}

	private fun atBeginningOfNote(): Boolean {
		return cursorPos == 0
	}

	private fun atEndOfNote(): Boolean {
		return cursorPos >= this.text.length
	}

	private val visibleLineCount: Int
		get() = bottomVisibleLine - topVisibleLine + 1

	private fun updateVisibleLines() {
		while (visibleLineCount <= maxVisibleLines && bottomVisibleLine < finalLineIndex) {
			++bottomVisibleLine
		}
	}

	private fun needsScrollBar(): Boolean {
		return toLines().size > visibleLineCount
	}

	override fun getWidth(): Int {
		return this.width - margin * 2
	}

	override fun isFocused(): Boolean {
		return isFocused
	}

	override fun setFocused(focused: Boolean) {
		if (focused && !isFocused) {
			cursorCounter = 0
		}
		isFocused = focused
	}

	private fun isKeyComboCtrlBack(keyCode: Int): Boolean {
		return keyCode == GLFW_KEY_BACKSPACE && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown()
	}

	private fun insert(newText: String) {
		deleteSelectedText()
		val finalText = insertStringAt(filter(newText), this.text, cursorPos)
		setText(finalText)
		moveCursorPosBy(newText.length)
	}

	private fun insertNewLine() {
		insert('\n'.toString())
	}

	private fun deleteNext() {
		val currentText = this.text
		if (!atEndOfNote() && currentText.isNotEmpty()) {
			val sb = StringBuilder(currentText)
			sb.deleteCharAt(cursorPos)
			setText(sb.toString())
			--selectionPos
		}
	}

	private fun deletePrev() {
		val currentText = this.text
		if (!atBeginningOfNote() && currentText.isNotEmpty()) {
			val sb = StringBuilder(currentText)
			sb.deleteCharAt(cursorPos - 1)
			setText(sb.toString())
			moveLeft()
		}
	}

	private fun deletePrevWord() {
		if (!atBeginningOfNote()) {
			var prev = this.text[cursorPos - 1]
			if (prev == ' ') {
				while (prev == ' ') {
					deletePrev()
					if (atBeginningOfNote()) {
						return
					}
					prev = this.text[cursorPos - 1]
				}
			} else {
				while (prev != ' ') {
					deletePrev()
					if (atBeginningOfNote()) {
						return
					}
					prev = this.text[cursorPos - 1]
				}
			}
		}
	}

	private fun deleteSelectedText() {
		while (selectionDifference > 0) {
			deletePrev()
		}
		while (selectionDifference < 0) {
			deleteNext()
		}
		selectionPos = -1
	}

	private fun incrementVisibleLines() {
		if (bottomVisibleLine < finalLineIndex) {
			++topVisibleLine
			++bottomVisibleLine
		}
	}

	private fun decrementVisibleLines() {
		if (topVisibleLine > 0) {
			--topVisibleLine
			--bottomVisibleLine
		}
	}

	private fun countCharacters(maxLineIndex: Int): Int {
		val wrappedLines = toLinesWithIndication()
		var count = 0
		for (i in 0 until maxLineIndex) {
			val wrappedLine = wrappedLines[i]
			count += wrappedLine.text.length
			if (!wrappedLine.isWrapped) {
				++count
			}
		}
		return count
	}

	private fun getCursorX(pos: Int): Int {
		val wrappedLines = toLinesWithIndication()
		val y = cursorY
		var currentLineIsWrapped = false
		var count = 0
		for (i in 0..y) {
			if (i < wrappedLines.size) {
				val wrappedLine = wrappedLines[i]
				if (i < y) {
					count += wrappedLine.text.length
					if (!wrappedLine.isWrapped) {
						++count
					}
				}
				if (wrappedLine.isWrapped && i == y && i > 0) {
					currentLineIsWrapped = true
				}
			}
		}
		if (currentLineIsWrapped) {
			--count
		}
		return pos - count
	}

	private val cursorX: Int
		get() = getCursorX(cursorPos)

	private fun getCursorY(pos: Int): Int {
		val wrappedLines = toLinesWithIndication()
		var count = 0
		for (i in wrappedLines.indices) {
			val wrappedLine = wrappedLines[i]
			count += wrappedLine.text.length
			if (!wrappedLine.isWrapped) {
				++count
			}
			if (count > pos) {
				return i
			}
		}
		return finalLineIndex
	}

	private val cursorY: Int
		get() = getCursorY(cursorPos)
	private val selectionDifference: Int
		get() = if (selectionPos > -1) cursorPos - selectionPos else 0

	private fun hasSelectionOnLine(line: Int): Boolean {
		if (selectionPos > -1) {
			val wrappedLines = toLinesWithIndication()
			var count = 0
			for (i in 0..line) {
				val wrappedLine = wrappedLines[i]
				for (j in 0 until wrappedLine.text.length) {
					++count
					if (line == i && isInSelection(count)) {
						return true
					}
				}
				if (!wrappedLine.isWrapped) {
					++count
				}
			}
		}
		return false
	}

	private fun setCursorPos(pos: Int) {
		cursorPos = MathHelper.clamp(pos, 0, this.text.length)
		if (cursorY > bottomVisibleLine) {
			incrementVisibleLines()
		} else if (cursorY < topVisibleLine) {
			decrementVisibleLines()
		}
	}

	private fun moveCursorPosBy(amount: Int) {
		setCursorPos(cursorPos + amount)
	}

	private fun moveRight() {
		if (!atEndOfNote()) {
			moveCursorPosBy(1)
		}
	}

	private fun moveLeft() {
		if (!atBeginningOfNote()) {
			moveCursorPosBy(-1)
		}
	}

	private fun moveUp() {
		val width = getCursorWidth()
		val y = cursorY
		while (cursorPos > 0 && (cursorY == y || getCursorWidth() > width)) {
			moveLeft()
		}
	}

	private fun moveDown() {
		val width = getCursorWidth()
		val y = cursorY
		while (cursorPos < this.text.length && (cursorY == y || getCursorWidth() < width)) {
			moveRight()
		}
	}

	private fun updateSelectionPos() {
		if (Screen.hasShiftDown()) {
			if (selectionPos < 0) {
				selectionPos = cursorPos
			}
		} else {
			selectionPos = -1
		}
	}

	private fun isInSelection(pos: Int): Boolean {
		return if (selectionPos <= -1) {
			false
		} else {
			pos >= this.getSelectionStart() && pos <= this.getSelectionEnd()
		}
	}

	private fun getSelectionStart(): Int {
		if (selectionPos > -1) {
			if (selectionPos > cursorPos) {
				return cursorPos
			}
			if (cursorPos > selectionPos) {
				return selectionPos
			}
		}
		return -1
	}


	private fun getSelectionEnd(): Int {
		if (selectionPos > -1) {
			if (selectionPos > cursorPos) {
				return selectionPos
			}
			if (cursorPos > selectionPos) {
				return cursorPos
			}
		}
		return -1
	}


	override fun getSelectedText(): String {
		return if (this.getSelectionStart() >= 0 && this.getSelectionEnd() >= 0) this.text.substring(
			this.getSelectionStart(),
			this.getSelectionEnd()
		) else ""
	}

	private fun drawSelectionBox(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (isFocused()) drawBorder(matrixStack, mouseX, mouseY, delta)
	}

	private fun renderVisibleText(matrixStack: MatrixStack) {
		var renderY = y + margin
		for (line in visibleLines) {
			val language = Language.getInstance()
			fontRenderer.drawWithShadow(
				matrixStack,
				language.reorder(line),
				(x + margin).toFloat(),
				renderY.toFloat(),
				textColor.rgba
			)
			renderY += 9
		}
	}

	private fun renderCursor(matrixStack: MatrixStack) {
		val shouldDisplayCursor = isFocused && cursorCounter / 6 % 2 == 0 && cursorIsValid()
		if (shouldDisplayCursor) {
			val line = currentLine
			val renderCursorX = x + margin + fontRenderer.getWidth(
				line.string.substring(
					0, MathHelper.clamp(
						cursorX, 0, line.string.length
					)
				)
			)
			val y1 = y + margin
			val y2 = renderSafeCursorY
			val renderCursorY = y1 + y2 * 9
			drawRect(matrixStack, renderCursorX, renderCursorY - 0.5, 1, fontRenderer.fontHeight, cursorColor)
		}
	}


	override fun appendNarrations(builder: NarrationMessageBuilder) {}
	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.FOCUSED
	}

	class WrappedString(val text: String, private val wrapped: Boolean) {
		val isWrapped: Boolean
			get() = this.wrapped
	}


	private fun insertStringAt(insert: String, insertTo: String, pos: Int): String {
		return insertTo.substring(0, pos) + insert + insertTo.substring(pos)
	}

	private fun filter(s: String): String {
		var filtered = s.replace('\t'.toString(), "    ")
		for (c in FILTER_CHARS) {
			filtered = filtered.replace(c.toString(), "")
		}
		return filtered
	}

	private fun wrapToWidthWithIndication(str: String, wrapWidth: Int): List<WrappedString> {
		val strings: MutableList<WrappedString> = ArrayList()
		var temp = StringBuilder()
		var wrapped = false
		for (element in str) {
			if (element == '\n') {
				strings.add(WrappedString(temp.toString(), wrapped))
				temp = StringBuilder()
				wrapped = false
			} else {
				val text = temp.toString()
				if (fontRenderer.getWidth(text + element) >= wrapWidth) {
					strings.add(WrappedString(temp.toString(), wrapped))
					temp = StringBuilder()
					wrapped = true
				}
			}
			if (element != '\n') {
				temp.append(element)
			}
		}
		strings.add(WrappedString(temp.toString(), wrapped))
		return strings
	}

	private fun wrapToWidth(str: String, wrapWidth: Int): MutableList<StringVisitable> {
		val strings: MutableList<StringVisitable> = ArrayList()
		var temp = StringBuilder()
		for (element in str) {
			run {
				if (element != '\n') {
					val text = temp.toString()
					if (fontRenderer.getWidth(text + element) < wrapWidth) {
						return@run
					}
				}
				strings.add(LiteralText(temp.toString()))
				temp = StringBuilder()
			}
			if (element != '\n') {
				temp.append(element)
			}
		}
		strings.add(LiteralText(temp.toString()))
		return strings
	}

	init {
		this.width = width
		this.height = height
		this.margin = margin
		this.multiline = multiline
		this.text = ""
		val var10001 = height.toFloat() - margin.toFloat() * 2.0f
		Objects.requireNonNull(fontRenderer)
		maxVisibleLines = MathHelper.floor(var10001 / 9.0f) - 1
		wrapWidth = width - margin * 2
		selectionPos = -1
	}
}