package forpleuvoir.ibuki_gourd.gui.widget

import com.google.common.base.Predicate
import forpleuvoir.ibuki_gourd.gui.common.PositionClickableWidget
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutline
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.render.RenderUtil.isMouseHovered
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Consumer


/**
 * 多行文本输入框

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 MultilineTextField

 * 创建时间 2021/12/29 17:35

 * @author forpleuvoir

 */
open class MultilineTextField(x: Int, y: Int, width: Int, height: Int, text: String = "") :
	PositionClickableWidget(x, y, width, height, "".text) {

	init {
		parent = ScreenBase.current
	}

	private val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }
	private val textRenderer: TextRenderer by lazy { mc.textRenderer }
	private val fontHeight: Int get() = textRenderer.fontHeight

	var maxLength: Int = 65535
		set(value) {
			field = value.clamp(0, 65535).toInt()
		}
	var text: String = text
		set(value) {
			if (!textPredicate.test(value)) {
				return
			}
			field = if (value.length > maxLength) value.substring(0, maxLength) else value
			onTextChangedCallback?.accept(field)
		}

	private val multilineText: List<String>
		get() {
			val width = this.width - padding * 2
			return wrapToWidth(text, width)
		}

	private val visibleText: List<String>
		get() {
			val fast = (scrollbar.amount / fontHeight).toInt()
			return ArrayList<String>().apply {
				for (i in 0 until pageSize) {
					val index = fast + i
					if (index < multilineText.size)
						add(multilineText[index])
				}
			}
		}

	private val pageSize: Int
		get() = contentHeight / fontHeight

	private var textPredicate: Predicate<String> = Predicate { it != null }
	fun setTextPredicate(textPredicate: Predicate<String>) {
		this.textPredicate = textPredicate
	}

	private val contentHeight: Int get() = this.height - (padding * 2)

	private var onTextChangedCallback: Consumer<String>? = null
	fun onTextChangedCallback(consumer: Consumer<String>) {
		this.onTextChangedCallback = consumer
	}

	var editable: Boolean = true

	var cursor: String = "_"
	private var cursorPos: Int = 0
	private var cursorPosX: Int = 0
	private var cursorPosY: Int = 0

	private var selectedStart: Int = 0
	private var selectedEnd: Int = 0
	private var selectedText: String = ""

	private var focusedTicks: Int = 0
	fun tick() {
		focusedTicks++
	}

	var padding: Int = 4

	private val scrollbarWidth: Int = 4
	private val scrollbar: Scrollbar = Scrollbar(
		this.x + this.width - scrollbarWidth - padding,
		this.y + padding,
		scrollbarWidth,
		contentHeight,
		{ 0.coerceAtLeast(multilineText.size * fontHeight - contentHeight) },
		{ contentHeight.toDouble() / (multilineText.size * fontHeight) }
	)

	override var onPositionChanged: ((Int, Int, Int, Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		scrollbar.deltaPosition(deltaX, deltaY)
	}

	var textColor: IColor<out Number> = Color4i.WHITE
	var backgroundColor: IColor<out Number> = Color4i.BLACK
	var borderColor: IColor<out Number> = Color4i.WHITE

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		scrollbar - amount * fontHeight
		return super.mouseScrolled(mouseX, mouseY, amount)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		isMouseHovered(mouseX,mouseY){
			if (button == 0) setCursorPos(mouseX.toInt(), mouseY.toInt())
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (Screen.isCopy(keyCode)) mc.keyboard.clipboard = this.selectedText
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		text = text.insert(chr.toString(), cursorPos)
		cursorPos++
		if (cursorPosX + 1 >= visibleText[cursorPosY].length) {
			if (cursorPosY + 1 >= visibleText.size) {
				scrollbar + fontHeight
			} else {
				cursorPosY++
			}
			cursorPosX = 0
		} else {
			cursorPosX++
		}

		return super.charTyped(chr, modifiers)
	}

	protected open fun setCursorPos(mouseX: Int, mouseY: Int) {
		cursorPos = 0
		cursorPosY = (((mouseY - this.y + this.padding) / fontHeight) - 1).clamp(0, visibleText.size - 1).toInt()
		cursorPosX = textRenderer.trimToWidth(visibleText[cursorPosY], (mouseX - this.x + this.padding)).length - 1
		val amount = scrollbar.amount / fontHeight
		for (i in 0 until (cursorPosY + amount).toInt()) {
			cursorPos += multilineText[i].length
		}
		println("x:$cursorPosX,y:$cursorPosY")
		cursorPos += cursorPosX
		cursorPos += text.substring(0, cursorPos).filter { it == '\n' }.length
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		matrices.translate(0.0, 0.0, parent?.zOffset?.toDouble() ?: 0.0)
		renderBackground(matrices, mouseX, mouseY, delta)
		renderBorder(matrices, mouseX, mouseY, delta)
		scrollbar.render(matrices, mouseX, mouseY, delta)
		renderText(matrices, mouseX, mouseY, delta)
		renderCursor(matrices, mouseX, mouseY, delta)
	}

	protected open fun renderBackground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawRect(matrices, this.x, this.y, this.width, this.height, backgroundColor)
	}

	protected open fun renderBorder(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawOutline(matrices, this.x, this.y, this.width, this.height, 1, borderColor)
	}

	protected open fun renderText(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		val textHeight = textRenderer.fontHeight
		visibleText.forEachIndexed { index, s ->
			textRenderer.draw(matrices, s, this.x + this.padding.toFloat(), this.y + this.padding.toFloat() + textHeight * index, textColor.rgba)
		}
	}

	protected open fun renderCursor(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		val x = this.x + padding + textRenderer.getWidth(visibleText[cursorPosY].substring(0, cursorPosX)) - 1
		val y = this.y + padding + (cursorPosY) * fontHeight
		textRenderer.drawWithShadow(matrices, "|", x.toFloat(), y.toFloat(), Color4f.RED.rgba)
	}

	private fun String.insert(insert: String, pos: Int): String {
		return this.substring(0, pos) + insert + this.substring(pos)
	}

	companion object {

		@JvmStatic
		fun wrapToWidth(str: String, wrapWidth: Int): MutableList<String> {
			val strings: MutableList<String> = ArrayList()
			var temp = StringBuilder()
			for (element in str) {
				run {
					if (element != '\n') {
						val text = temp.toString()
						if (mc.textRenderer.getWidth(text + element) < wrapWidth) {
							return@run
						}
					}
					strings.add(temp.toString())
					temp = StringBuilder()
				}
				if (element != '\n') {
					temp.append(element)
				}
			}
			strings.add(temp.toString())
			return strings
		}
	}
}