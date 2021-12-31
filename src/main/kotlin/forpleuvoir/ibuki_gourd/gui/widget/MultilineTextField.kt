package forpleuvoir.ibuki_gourd.gui.widget

import com.google.common.base.Predicate
import forpleuvoir.ibuki_gourd.gui.common.PositionClickableWidget
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Style
import net.minecraft.util.math.MathHelper
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

	var maxLength: Int = 65535
		set(value) {
			field = MathHelper.clamp(value, 0, 65535)
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
			val wrapLines = textRenderer.textHandler.wrapLines(this.text.text, width, Style.EMPTY)
			return ArrayList<String>().apply {
				wrapLines.forEach { this.add(it.string) }
			}
		}

	private var textPredicate: Predicate<String> = Predicate { it != null }
	fun setTextPredicate(textPredicate: Predicate<String>) {
		this.textPredicate = textPredicate
	}

	private var onTextChangedCallback: Consumer<String>? = null
	fun onTextChangedCallback(consumer: Consumer<String>) {
		this.onTextChangedCallback = consumer
	}

	var editable: Boolean = true

	var cursor: String = "_"
	private var cursorPos: Int = 0

	private var selectedStart: Int = 0
	private var selectedEnd: Int = 0
	private var selectedText: String = ""

	private var focusedTicks: Int = 0
	fun tick() {
		focusedTicks++
	}

	var padding: Int = 4

	var textColor: IColor<out Number> = Color4i.WHITE
	var backgroundColor: IColor<out Number> = Color4i.BLACK
	var borderColor: IColor<out Number> = Color4i.WHITE

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (Screen.isCopy(keyCode)) mc.keyboard.clipboard = this.selectedText
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		matrices.translate(0.0, 0.0, parent?.zOffset?.toDouble() ?: 0.0)
		renderBackground(matrices, mouseX, mouseY, delta)
		renderBorder(matrices, mouseX, mouseY, delta)
		renderText(matrices, mouseX, mouseY, delta)
		renderCursor(matrices, mouseX, mouseY, delta)
	}

	protected open fun renderBackground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawRect(this.x, this.y, this.width, this.height, backgroundColor, parent?.zOffset ?: 0)
	}

	protected open fun renderBorder(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawOutline(this.x, this.y, this.width, this.height, 1, borderColor, parent?.zOffset ?: 0)
	}

	protected open fun renderText(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		val textHeight = textRenderer.fontHeight
		multilineText.forEachIndexed { index, s ->
			textRenderer.draw(matrices, s, this.x + this.padding.toFloat(), this.y + this.padding.toFloat() + textHeight * index, textColor.rgba)
		}
	}

	protected open fun renderCursor(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float){

	}

}