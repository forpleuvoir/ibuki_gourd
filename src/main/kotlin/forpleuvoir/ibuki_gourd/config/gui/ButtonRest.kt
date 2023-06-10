package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.IConfigResettable
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.width
import net.minecraft.client.gui.DrawContext

/**
 * 重置按钮

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 ButtonRest

 * 创建时间 2021/12/18 10:34

 * @author forpleuvoir

 */
class ButtonRest(
	x: Int,
	y: Int,
	width: Int = IbukiGourdLang.Rest.tText().width + 10,
	height: Int = 20,
	val config: IConfigResettable
) : ButtonBase<ButtonRest>(x, y, width, height, message = IbukiGourdLang.Rest.tText(), onButtonPress = null) {

	init {
		this.active = !config.isDefaultValue
		setOnPressAction(this::rest)
	}

	private fun rest(buttonRest: ButtonRest) {
		config.resetDefaultValue()
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		this.active = !config.isDefaultValue
	}
}