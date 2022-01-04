package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import java.util.function.Consumer
import java.util.function.Supplier


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetSliderInt

 * 创建时间 2021/12/23 17:02

 * @author forpleuvoir

 */
open class WidgetSliderNumber(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	protected var number: Supplier<Number>,
	private val minValue: Number,
	private val maxValue: Number,
) : SliderWidget(x, y, width, height, LiteralText.EMPTY, number.get().toDouble() / ((maxValue.toDouble() - minValue.toDouble()))),
	IPositionElement {

	var onStopCallback: ((mouseX: Double, mouseY: Double) -> Unit)? = null
	private var status: Boolean = false

	private var isDragging: Boolean = false
		set(value) {
			field = value
		}

	init {
		this.updateMessage()
	}

	private var consumer: Consumer<Number>? = null

	fun setConsumer(consumer: Consumer<Number>) {
		this.consumer = consumer
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		if(!active) return
		status = true
		super.onClick(mouseX, mouseY)
		MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if(!visible) return
		this.updateMessage()
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun onRelease(mouseX: Double, mouseY: Double) {
		if(!active) return
		status = false
		onStopCallback?.invoke(mouseX, mouseY)
	}

	override fun updateMessage() {
		if (getNumber() is Double || getNumber() is Float) {
			if ((getNumber().toDouble() - getNumber().toInt()) != 0.0)
				this.message = String.format("%.2f", getNumber()).text
			else
				this.message = String.format("%d", getNumber().toInt()).text
		} else if (getNumber() is Int || getNumber() is Short) {
			this.message = getNumber().toInt().toString().text
		} else {
			this.message = getNumber().toString().text
		}
		this.message = "${if (status) "§6" else "§r"}${this.message.string}".text
		this.value = number.get().toDouble() / ((maxValue.toDouble() - minValue.toDouble()))
	}

	protected open fun getNumber(): Number {
		return this.value * (this.maxValue.toDouble() - this.minValue.toDouble())
	}

	override fun applyValue() {
		consumer?.accept(getNumber())
	}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null
}