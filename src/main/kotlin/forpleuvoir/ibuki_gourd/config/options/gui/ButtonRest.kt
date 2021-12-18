package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.IConfigResettable
import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack

/**
 * 重置按钮

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ButtonRest

 * 创建时间 2021/12/18 10:34

 * @author forpleuvoir

 */
class ButtonRest(
	x: Int,
	y: Int,
	width: Int = MinecraftClient.getInstance().textRenderer.getWidth(IbukiGourdLang.Rest.tText()) + 10,
	height: Int = 20,
	val config: IConfigResettable
) :
	ButtonBase<ButtonRest>(x, y, width, height, message = IbukiGourdLang.Rest.tText(), onButtonPress = null) {

	init {
		this.active = !config.isDefaultValue
	}

	override fun onPress() {
		config.resetDefaultValue()
		super.onPress()
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(matrices, mouseX, mouseY, delta)
		this.active = !config.isDefaultValue
	}
}