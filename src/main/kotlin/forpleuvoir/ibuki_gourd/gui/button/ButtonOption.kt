package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.DrawContext


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonOption

 * 创建时间 2021/12/25 11:21

 * @author forpleuvoir

 */
class ButtonOption(
	private val options: List<String>,
	private var current: String = options[0],
	x: Int,
	y: Int,
	width: Int,
	height: Int = 20,
	changedCallback: ((String) -> Unit)? = null
) :
	ButtonBase<ButtonOption>(x, y, width, height, "".text, null) {

	init {
		updateMessage()
		setOnPressAction {
			changCurrent()
			changedCallback?.invoke(getCurrent())
		}
	}

	private fun updateMessage() {
		this.message = current.text
	}

	override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderButton(context, mouseX, mouseY, delta)
	}

	fun changCurrent() {
		val size = options.size
		val index = options.indexOf(current)
		current = if (index < size - 1) {
			options[index + 1]
		} else {
			options[0]
		}
		updateMessage()
	}

	fun getCurrent(): String {
		return current
	}
}