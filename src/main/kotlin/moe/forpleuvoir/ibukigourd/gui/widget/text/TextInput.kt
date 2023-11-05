package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TEXT_INPUT
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TEXT_SELECTED_INPUT
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.BACKGROUND_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.CURSOR_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.SELECTED_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.TEXT_EDITABLE_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT_INPUT.TEXT_UNEDITABLE_COLOR
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.batchRenderText
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
import kotlin.math.abs
import kotlin.math.absoluteValue

@Suppress("MemberVisibilityCanBePrivate", "Unused")
class TextInput(
	width: Float,
	height: Float,
	padding: Margin = Theme.TEXT_INPUT.PADDING,
	margin: Margin? = null,
	var editableColor: ARGBColor = TEXT_EDITABLE_COLOR,
	var uneditableColor: ARGBColor = TEXT_UNEDITABLE_COLOR,
	var backgroundColor: ARGBColor = BACKGROUND_COLOR,
	var selectedColor: ARGBColor = SELECTED_COLOR,
	var cursorColor: ARGBColor = CURSOR_COLOR,
	private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : ClickableElement() {

	override val focusable: Boolean = true

	init {
		transform.width = width.also { transform.fixedWidth = true }
		transform.height = height.also { transform.fixedHeight = true }
		padding(padding)
		margin?.let(::margin)
	}

	var text: String = ""
		set(value) {
			if (value != field) {
				field = value
				onTextChanged(field)
			}
		}

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

	var maxLength = 32
		set(value) {
			field = value
			if (text.length > value) {
				text = text.substring(0, value)
				this.onTextChanged(text)
			}
		}

	var editable = true

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
		this.onTextChanged(this.text)
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

	private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int, skipOverSpaces: Boolean = true): Int {
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
		if (!this.isActive) return NextAction.Continue
		selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
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
			Keyboard.TAB -> {
				if (suggestion?.invoke(text)?.isNotEmpty() == true) {
					write(suggestion!!.invoke(text))
				} else {
					write("    ")
				}
			}

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

	override fun onKeyRelease(keyCode: KeyCode): NextAction {
		selecting = if (keyCode == Keyboard.LEFT_SHIFT) false else selecting
		return super.onKeyRelease(keyCode)
	}


	val isActive: Boolean
		get() {
			return visible && focused && editable
		}

	override fun onCharTyped(chr: Char): NextAction {
		if (!isActive) return NextAction.Continue
		if (SharedConstants.isValidChar(chr)) {
			if (editable) {
				write(chr.toString())
			}
			return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		mouseHover {
			val string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), contentRect(true).width.toInt())
			cursor = textRenderer.trimToWidth(string, (mouseX - this.transform.x).toInt()).length + firstCharacterIndex
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

	override fun onRenderBackground(renderContext: RenderContext) {
		renderTexture(renderContext.matrixStack, this.transform, focused.ternary(TEXT_SELECTED_INPUT, TEXT_INPUT), backgroundColor)
	}

	fun renderCursor(renderContext: RenderContext) {
		if (focusedTicks % 15 >= 5 && focused) {
			val rect = contentRect(true)
			val height = textRenderer.fontHeight.toFloat()
			val y = rect.top + (rect.height - height) / 2f - 0.75f
			if (cursor - firstCharacterIndex > 0) {
				val width = textRenderer.getWidth(text.substring(firstCharacterIndex, cursor))
				renderRect(renderContext.matrixStack, rect(rect.position.xyz(rect.left + width - 0.85f, y), Size.create(1f, height)), cursorColor)
			} else {
				renderRect(renderContext.matrixStack, rect(rect.position.y(y), Size.create(1f, height)), cursorColor)
			}
		}
	}

	fun renderText(renderContext: RenderContext) {
		val contentRect = contentRect(true)
		textRenderer.batchRenderText(renderContext.matrixStack) {
			//"渲染提示文本"
			if (text.isEmpty() && hintText != null && !focused) {
				renderAlignmentText(hintText!!, contentRect, color = uneditableColor)
			}
			//"渲染文本本体"
			val renderText = textRenderer!!.trimToWidth(text.substring(firstCharacterIndex), contentRect.width.toInt())
			renderText.isNotEmpty().ifc {
				val textColor = editable.ternary(editableColor, uneditableColor)
				renderAlignmentText(renderText, contentRect, color = textColor)
			}
			//"渲染文本建议"
			suggestion?.invoke(text)?.let {
				if (focused && cursor == text.length) {
					val renderTextWidth = textRenderer!!.getWidth(renderText).toFloat()
					val rect = rect(contentRect.position + Vector3f(renderTextWidth, 0f, 0f), contentRect.width - renderTextWidth, contentRect.height)
					renderAlignmentText(it, rect)
				}
			}
		}

		//"渲染选中的文本高亮"
		if (selectedText.isNotEmpty() || focused) {
			val (startIndex, endIndex) = (selectionStart - firstCharacterIndex).coerceAtLeast(0) to
					(selectionEnd - firstCharacterIndex).coerceAtLeast(0)
			val start = contentRect.left + if (startIndex > 0)
				textRenderer.getWidth(text.substring(firstCharacterIndex, firstCharacterIndex + startIndex)).toFloat()
			else 0f
			val end = contentRect.left + if (endIndex > 0)
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
fun ElementContainer.textInput(
	width: Float,
	height: Float = 22f,
	padding: Margin = Theme.TEXT_INPUT.PADDING,
	margin: Margin? = null,
	scope: TextInput.() -> Unit = {}
): TextInput = this.addElement(TextInput(width, height, padding, margin).apply(scope))