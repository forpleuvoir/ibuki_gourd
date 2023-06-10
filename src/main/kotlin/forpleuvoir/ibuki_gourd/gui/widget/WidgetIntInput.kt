package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.gui.icon.ArrowIcon
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutlinedBox
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import java.util.function.Consumer

/**
 * 带加减按钮的整数输入框

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetIntInput

 * 创建时间 2022/1/20 16:42

 * @author forpleuvoir

 */
class WidgetIntInput(
	x: Int,
	y: Int,
	width: Int,
	height: Int = 16,
	value: Int = 0,
	var minValue: Int = Int.MIN_VALUE,
	var maxValue: Int = Int.MAX_VALUE
) : PositionParentWidget(x, y, width, height) {

	var value: Int = value
		set(value) {
			field = value.clamp(minValue, maxValue).toInt()
			text.text = field.toString().text
			onValueChangedCallback?.accept(field)
		}

	private var onValueChangedCallback: Consumer<Int>? = null

	fun setOnValueChangedCallback(callback: Consumer<Int>) {
		this.onValueChangedCallback = callback
	}

	var backgroundColor: IColor<out Number> = Color4f.BLACK
	var borderColor: IColor<out Number> = Color4f.WHITE

	private var text: LabelText = LabelText(
		value.toString().text,
		this.x,
		this.y,
		this.width - this.height / 2 - 6,
		this.height,
		LabelText.Align.CENTER_LEFT
	).apply {
		bordColor = borderColor
		setPadding(leftPadding = 5)
	}

	private var upButton: ButtonIcon =
		ButtonIcon(this.x + text.width + 4, this.y + 1, ArrowIcon.Up, this.height / 2, padding = -3).apply {
			setOnPressAction {
				this@WidgetIntInput.value += delta(1)
			}
		}

	private var downButton: ButtonIcon =
		ButtonIcon(upButton.x, this.y + this.height / 2 - 1, ArrowIcon.Down, this.height / 2, padding = -3).apply {
			setOnPressAction {
				this@WidgetIntInput.value += delta(-1)
			}
		}

	private fun delta(delta: Int): Int {
		if (Screen.hasShiftDown()) {
			return delta * 5
		}
		if (Screen.hasControlDown()) {
			return delta * 10
		}
		if (Screen.hasAltDown()) {
			return delta * 3
		}
		return delta
	}

	init {
		addDrawableChild(text)
		addDrawableChild(upButton)
		addDrawableChild(downButton)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		drawOutlinedBox(
			context,
			this.x,
			this.y,
			this.width,
			this.height,
			colorBg = backgroundColor,
			colorBorder = borderColor
		)
		super.render(context, mouseX, mouseY, delta)
		text.setHoverTexts(text.text)
	}

	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		builder?.put(NarrationPart.TITLE, text.text)
	}

	override fun getType(): Selectable.SelectionType {
		return if (hovered) {
			Selectable.SelectionType.HOVERED
		} else Selectable.SelectionType.NONE
	}
}