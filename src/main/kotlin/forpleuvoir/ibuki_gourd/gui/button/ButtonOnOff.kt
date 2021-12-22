package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonOnOff

 * 创建时间 2021/12/12 20:42

 * @author forpleuvoir

 */
class ButtonOnOff(
	x: Int,
	y: Int,
	width: Int,
	height: Int = 20,
	private var status: Boolean = false,
) :
	ButtonBase<ButtonOnOff>(x, y, width, height, message = "".text()) {

	private var on: Text = IbukiGourdLang.On.tText()
	private var off: Text = IbukiGourdLang.Off.tText()

	init {
		updateText()
		setOnPressAction(this::toggle)
	}

	private var onCallback: ((ButtonOnOff) -> Unit)? = null
	private var offCallback: ((ButtonOnOff) -> Unit)? = null

	constructor(x: Int, y: Int, status: Boolean) : this(
		x = x, y = y,
		width = 0,
		status = status,
	) {
		width = MinecraftClient.getInstance().textRenderer.getWidth(this.message) + 20
	}

	constructor(x: Int, y: Int, status: Boolean, hoverText: List<Text>) : this(
		x = x,
		y = y,
		status = status,
	) {
		this.hoverText.clear()
		this.hoverText.addAll(hoverText)
	}

	private fun updateText() {
		message = if (status) on else off
	}

	private fun setOnOffText(on: Text = this.on, off: Text = this.off) {
		this.on = on
		this.off = off
	}


	private fun toggle(button: ButtonOnOff) {
		status = !status
		updateText()
		if (status) onCallback?.invoke(this)
		else offCallback?.invoke(this)
	}


	fun getStatus(): Boolean {
		return status
	}

	fun setOnCallback(onCallback: (ButtonOnOff) -> Unit) {
		this.onCallback = onCallback
	}

	fun setOffCallback(offCallback: (ButtonOnOff) -> Unit) {
		this.offCallback = offCallback
	}
}