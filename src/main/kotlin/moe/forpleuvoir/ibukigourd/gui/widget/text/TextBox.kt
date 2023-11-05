package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TEXT_INPUT
import moe.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.TEXT_SELECTED_INPUT
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.batchRenderText
import moe.forpleuvoir.ibukigourd.render.helper.renderTextLines
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.ternary
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.SharedConstants
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.CursorMovement
import net.minecraft.client.input.CursorMovement.*
import net.minecraft.text.Style
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
	var editableColor: ARGBColor = Theme.TEXT_INPUT.TEXT_EDITABLE_COLOR,
	var uneditableColor: ARGBColor = Theme.TEXT_INPUT.TEXT_UNEDITABLE_COLOR,
	var backgroundColor: ARGBColor = Theme.TEXT_INPUT.BACKGROUND_COLOR,
	var selectedColor: ARGBColor = Theme.TEXT_INPUT.SELECTED_COLOR,
	var cursorColor: ARGBColor = Theme.TEXT_INPUT.CURSOR_COLOR,
	private val spacing: Float = 1f,
	private val showScroller: Boolean = true,
	private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : ClickableElement() {
	companion object {

		const val UNLIMITED_LENGTH = Int.MAX_VALUE

		const val UNDERSCORE = "_"

		private const val CURSOR_WIDTH = 2

	}

	data class Substring(val beginIndex: Int, val endIndex: Int) {
		companion object {
			val EMPTY = Substring(0, 0)
		}
	}

	override val focusable: Boolean = true

	init {
		transform.width = width.also { transform.fixedWidth = true }
		transform.height = height.also { transform.fixedHeight = true }
		padding(padding)
		margin?.let(::margin)

	}

	private val visibleLineCount: Int
		get() = ceil(contentHeight / fontHeight + spacing).toInt()


	/**
	 * 单页高度
	 */
	private val contentHeight: Float get() = height - padding.height

	/**
	 * 文本总高度
	 */
	private val contentsHeight: Float
		get() = (lineCount + spacing) * fontHeight - spacing

	override fun init() {
		text = ""
		super.init()
		if (!this::scrollerBar.isInitialized) {
			scrollerBar = scroller(
				transform.height - padding.height,
				scrollerThickness,
				{ fontHeight / 2f },
				{ (contentsHeight - contentHeight).coerceAtLeast(0f) },
				{ (contentHeight / contentsHeight).clamp(0f..1f) },
				Arrangement.Vertical
			) {
				fixed = true
				visible = showScroller
			}
			scrollerBar.transform.worldX = this.transform.worldRight - scrollerThickness - this.padding.right / 2
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

	private val firstVisibleLineCount: Int get() = floor(amount / fontHeight + spacing).toInt() - 1

	private val lastVisibleLineCount: Int get() = (firstVisibleLineCount + visibleLineCount).coerceAtMost(lines.size - 1)

	private val renderLines: List<String>
		get() {
			return buildList {
				for (i in firstVisibleLineCount..lastVisibleLineCount) {
					val substring: Substring = lines[i]
					add(text.substring(substring.beginIndex, substring.endIndex))
				}
			}
		}

	val currentLineIndex: Int
		get() {
			for (i in lines.indices) {
				val substring: Substring = lines[i]
				if (cursor < substring.beginIndex || cursor > substring.endIndex) continue
				return i
			}
			return -1
		}

	private val currentLine: Substring
		get() = this.getOffsetLine(0)

	private fun getOffsetLine(offsetFromCurrent: Int): Substring {
		val i: Int = this.currentLineIndex
		check(i >= 0) { "Cursor is not within text (cursor = " + cursor + ", length = " + text.length + ")" }
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
			onChange()
		}

	val selectedText: String
		get() = this.selection.let { text.substring(it.beginIndex, it.endIndex) }


	var cursor: Int = 0

	private var selectionEnd: Int = 0

	var selecting: Boolean = false

	var maxLength: Int = maxLength
		set(value) {
			field = value.coerceAtLeast(0)
		}

	val hasMaxLength: Boolean get() = maxLength != UNLIMITED_LENGTH

	val hasSelection: Boolean get() = selectionEnd != cursor

	var onTextChanged: (text: String) -> Unit = {}

	var onCursorChanged: () -> Unit = {
		var amount = this.amount
		val substring: Substring = getLine((amount / fontHeight).toInt())
		if (this.cursor <= substring.beginIndex) {
			amount = (this.currentLineIndex + spacing) * fontHeight - spacing
		} else {
			val substring2: Substring = this.getLine(((amount + this.height) / fontHeight).toInt() - 1)
			if (this.cursor > substring2.endIndex) {
				amount = (this.currentLineIndex + spacing) * fontHeight - spacing - this.height + fontHeight + this.padding.height
			}
		}
		this.amount = amount
	}

	val selection: Substring
		get() = Substring(
			min(selectionEnd.toDouble(), cursor.toDouble()).toInt(),
			max(selectionEnd.toDouble(), cursor.toDouble()).toInt()
		)

	val previousWordAtCursor: Substring
		get() {
			if (text.isEmpty()) {
				return Substring.EMPTY
			}
			var i: Int = cursor.clamp(0, text.length - 1)
			while (i > 0 && Character.isWhitespace(text[i - 1])) {
				--i
			}
			while (i > 0 && !Character.isWhitespace(text[i - 1])) {
				--i
			}
			return Substring(i, this.getWordEndIndex(i))
		}

	val nextWordAtCursor: Substring
		get() {
			if (text.isEmpty()) {
				return Substring.EMPTY
			}
			var i: Int = cursor.clamp(0, text.length - 1)
			while (i < text.length && !Character.isWhitespace(text[i])) {
				++i
			}
			while (i < text.length && Character.isWhitespace(text[i])) {
				++i
			}
			return Substring(i, getWordEndIndex(i))
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


	fun replaceSelection(string: String) {
		if (string.isEmpty() && !this.hasSelection) {
			return
		}
		val string2 = truncate(SharedConstants.stripInvalidChars(string, true))
		val substring: Substring = this.selection
		text = StringBuilder(text).replace(substring.beginIndex, substring.endIndex, string2).toString()
		cursor = substring.beginIndex + string2.length
		selectionEnd = cursor
		onChange()
	}

	fun delete(offset: Int) {
		if (!this.hasSelection) {
			selectionEnd = (cursor + offset).clamp(0, text.length)
		}
		replaceSelection("")
	}

	fun moveCursor(movement: CursorMovement, amount: Int) {
		when (movement) {
			ABSOLUTE -> {
				cursor = amount
			}

			RELATIVE -> {
				cursor += amount
			}

			END -> {
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
		val j = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), i).length
		moveCursor(ABSOLUTE, substring.beginIndex + j)
	}

	private fun getWordEndIndex(startIndex: Int): Int {
		var i: Int = startIndex
		while (i < text.length && !Character.isWhitespace(text[i])) {
			++i
		}
		return i
	}

	private fun moveCursor(mouseX: Float, mouseY: Float) {
		val x = floor(mouseX - this.transform.worldX - padding.left).toInt()
		val y = floor(mouseY - this.transform.worldY - padding.top + amount / fontHeight.toDouble()).toInt()
		val substring: Substring = lines[y.clamp(0, lines.size - 1).coerceAtLeast(0)]
		val k = textRenderer.trimToWidth(text.substring(substring.beginIndex, substring.endIndex), x).length
		this.moveCursor(ABSOLUTE, substring.beginIndex + k)
	}

	private fun onChange() {
		this.rewrap()
		this.onTextChanged(text)
		this.onCursorChanged()
	}

	private fun rewrap() {
		lines.clear()
		if (text.isEmpty()) {
			lines.add(Substring.EMPTY)
			return
		}
		textRenderer.textHandler
			.wrapLines(text, width.toInt(), Style.EMPTY, false) { _, start: Int, end: Int ->
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

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
		mouseHover {
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
		if (super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
		mouseHover {
			selecting = true
			moveCursor(mouseX, mouseY)
			selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
			return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override fun onKeyPress(keyCode: KeyCode): NextAction {
		if (!this.active) return NextAction.Continue

		selecting = InputHandler.hasKeyPressed(Keyboard.LEFT_SHIFT)
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.A)) {
			cursor = text.length
			selectionEnd = 0
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.C)) {
			mc.keyboard.clipboard = this.selectedText
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.V)) {
			replaceSelection(mc.keyboard.clipboard)
			return NextAction.Cancel
		}
		if (InputHandler.hasKeyPressed(Keyboard.LEFT_CONTROL, Keyboard.X)) {
			mc.keyboard.clipboard = this.selectedText
			replaceSelection("")
			return NextAction.Cancel
		}
		when (keyCode) {
			Keyboard.LEFT -> {
				if (Screen.hasControlDown()) {
					val substring: Substring = this.previousWordAtCursor
					this.moveCursor(ABSOLUTE, substring.beginIndex)
				} else {
					this.moveCursor(RELATIVE, -1)
				}
				return NextAction.Cancel
			}

			Keyboard.RIGHT -> {
				if (Screen.hasControlDown()) {
					val substring: Substring = this.nextWordAtCursor
					this.moveCursor(ABSOLUTE, substring.beginIndex)
				} else {
					this.moveCursor(RELATIVE, 1)
				}
				return NextAction.Cancel
			}

			Keyboard.UP -> {
				if (!Screen.hasControlDown()) {
					moveCursorLine(-1)
				}
				return NextAction.Cancel
			}

			Keyboard.DOWN -> {
				if (!Screen.hasControlDown()) {
					moveCursorLine(1)
				}
				return NextAction.Cancel
			}

			Keyboard.PAGE_UP -> {
				this.moveCursor(ABSOLUTE, 0)
				return NextAction.Cancel
			}

			Keyboard.PAGE_DOWN -> {
				this.moveCursor(END, 0)
				return NextAction.Cancel
			}

			Keyboard.HOME -> {
				if (Screen.hasControlDown()) {
					this.moveCursor(ABSOLUTE, 0)
				} else NextAction.Cancel
				return NextAction.Cancel
			}

			Keyboard.END -> {
				if (Screen.hasControlDown()) {
					this.moveCursor(END, 0)
				} else {
					this.moveCursor(ABSOLUTE, this.currentLine.endIndex)
				}
				return NextAction.Cancel
			}

			Keyboard.BACKSPACE -> {
				if (Screen.hasControlDown()) {
					val substring: Substring = this.previousWordAtCursor
					delete(substring.beginIndex - cursor)
				} else {
					delete(-1)
				}
				return NextAction.Cancel
			}

			Keyboard.DELETE -> {
				if (Screen.hasControlDown()) {
					val substring: Substring = this.nextWordAtCursor
					delete(substring.beginIndex - cursor)
				} else {
					delete(1)
				}
				return NextAction.Cancel
			}

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
		if (!(this.visible && this.focused && SharedConstants.isValidChar(chr))) {
			return NextAction.Continue
		}
		replaceSelection(chr.toString())
		return NextAction.Cancel
	}

	override fun onRender(renderContext: RenderContext) {
		if (!visible) return
		renderBackground.invoke(renderContext)
		renderContext.scissor(contentRect(true)) {
			renderText(renderContext)
		}
		fixedElements.forEach { it.render(renderContext) }
		scrollerBar.render(renderContext)
		renderOverlay.invoke(renderContext)
	}

	override fun onRenderBackground(renderContext: RenderContext) {
		renderTexture(renderContext.matrixStack, this.transform, focused.ternary(TEXT_SELECTED_INPUT, TEXT_INPUT), Colors.WHITE)
	}

	//绘制选择高亮
	override fun onRenderOverlay(renderContext: RenderContext) {

	}


	private fun renderText(renderContext: RenderContext) {
		val contentRect = contentRect(true)
		//渲染提示文本
		if (text.isEmpty() && !focused) {
			if (hintText != null) {
				textRenderer.renderTextLines(renderContext.matrixStack, hintText!!, contentRect, spacing, PlanarAlignment::TopLeft, color = uneditableColor)
			}
			return
		}
		//渲染文本本体
		if (text.isNotEmpty())
			textRenderer.batchRenderText(renderContext.matrixStack) {
				var y = amount - firstVisibleLineCount * (fontHeight + 1) + transform.worldTop + padding.top
				renderLines.forEach {
					renderText(it, contentRect.left, y, contentRect.z, color = editableColor)
					y += fontHeight + spacing
				}
			}


	}

	private fun renderCursor(renderContext: RenderContext) {
		if (focusedTicks % 15 >= 5 && focused) {
			val rect = contentRect(true)
			val height = fontHeight.toFloat()
			cursor

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
	editableColor: ARGBColor = Theme.TEXT_INPUT.TEXT_EDITABLE_COLOR,
	uneditableColor: ARGBColor = Theme.TEXT_INPUT.TEXT_UNEDITABLE_COLOR,
	backgroundColor: ARGBColor = Theme.TEXT_INPUT.BACKGROUND_COLOR,
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