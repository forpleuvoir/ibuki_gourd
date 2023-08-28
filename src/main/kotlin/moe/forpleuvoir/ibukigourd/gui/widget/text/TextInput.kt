package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.SharedConstants
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.util.Util
import kotlin.math.abs

class TextInput(private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer) : ClickableElement() {

	override val focusable: Boolean = true
	var text: String = ""
		set(value) {
			if (value != field) {
				field = value
				onTextChange(field)
			}
		}

	val hintText: Text? = null

	var suggestion: ((text: String) -> Text)? = null

	var onTextChange: (text: String) -> Unit = {}

	var textPredicate: (text: String) -> Boolean = { true }

	var renderTextProvider: (text: String, firstCharacterIndex: Int) -> OrderedText = { text, _ -> OrderedText.styledForwardsVisitedString(text, Style.EMPTY) }

	var editableColor: ARGBColor = Color(0xE0E0E0)

	var uneditableColor: ARGBColor = Color(0x707070)

	var firstCharacterIndex: Int = 0

	var selectionStart: Int = 0
		set(value) {
			field = value.clamp(0, text.length)
		}

	var selectionEnd: Int = 0
		set(value) {
			val i = text.length
			field = value.clamp(0, i)
			if (firstCharacterIndex > i) {
				firstCharacterIndex = i
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
			firstCharacterIndex = firstCharacterIndex.clamp(0, i)
		}

	var cursor: Int
		get() = selectionStart
		set(value) {
			selectionStart = value
			if (!selecting) {
				selectionEnd = selectionStart
			}
			onTextChange(text)
		}

	var maxLength = 32
		set(value) {
			field = value
			if (text.length > value) {
				text = text.substring(0, value)
				this.onTextChange(text)
			}
		}

	var editable = true

	private var selecting = false

	private var focusedTicks = 0

	override fun tick() {
		super.tick()
		if (focused) {
			++focusedTicks
		} else {
			focusedTicks = 0
		}
	}

	val selectedText: String
		get() {
			val start = selectionStart.coerceAtMost(selectionEnd)
			val end = selectionStart.coerceAtLeast(selectionEnd)
			return text.substring(start, end)
		}

	fun write(text: String) {
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
		this.onTextChange(this.text)
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
		cursor = j
	}

	private fun getWordSkipPosition(wordOffset: Int): Int {
		return getWordSkipPosition(wordOffset, cursor)
	}

	private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int): Int {
		return this.getWordSkipPosition(wordOffset, cursorPosition, true)
	}

	private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int, skipOverSpaces: Boolean): Int {
		var i = cursorPosition
		val bl = wordOffset < 0
		val j = abs(wordOffset)
		for (k in 0 until j) {
			if (bl) {
				while (skipOverSpaces && i > 0 && text[i - 1] == ' ') {
					--i
				}
				while (i > 0 && text[i - 1] != ' ') {
					--i
				}
				continue
			}
			val l = text.length
			if (text.indexOf(32.toChar(), i).also { i = it } == -1) {
				i = l
				continue
			}
			while (skipOverSpaces && i < l && text[i] == ' ') {
				++i
			}
		}
		return i
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
		if (!this.isActive) {
			return NextAction.Continue
		}
		selecting = Screen.hasShiftDown()
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
			setCursorToEnd()
			this.selectionEnd = 0
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
			mc.keyboard.clipboard = this.selectedText
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
			if (editable) {
				write(mc.keyboard.clipboard)
			}
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
			mc.keyboard.clipboard = this.selectedText
			if (editable) {
				write("")
			}
			return NextAction.Cancel
		}
		when (keyCode) {
			Keyboard.LEFT -> {
				if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
					cursor = this.getWordSkipPosition(-1)
				} else {
					moveCursor(-1)
				}
				return NextAction.Cancel
			}

			Keyboard.RIGHT -> {
				if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL)) {
					cursor = this.getWordSkipPosition(1)
				} else {
					moveCursor(1)
				}
				return NextAction.Cancel
			}

			Keyboard.BACKSPACE -> {
				if (editable) {
					selecting = false
					erase(-1)
					selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
				}
				return NextAction.Cancel
			}

			Keyboard.DELETE -> {
				if (editable) {
					selecting = false
					erase(1)
					selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
				}
				return NextAction.Cancel
			}

			Keyboard.HOME -> {
				setCursorToStart()
				return NextAction.Cancel
			}

			Keyboard.END -> {
				setCursorToEnd()
				return NextAction.Cancel
			}
		}
		return NextAction.Continue
	}

	val isActive: Boolean
		get() {
			return visible && focused && editable
		}
}