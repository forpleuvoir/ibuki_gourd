package forpleuvoir.ibuki_gourd.gui.button

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 Button

 * 创建时间 2021/12/14 21:40

 * @author forpleuvoir

 */
class Button(x: Int, y: Int, width: Int, height: Int = 20, message: Text, onButtonPress: ((Button) -> Unit)? = null) : ButtonBase<Button>
	(x, y, width, height, message, onButtonPress) {

	constructor(x: Int, y: Int, message: Text, onButtonPress: ((Button) -> Unit)? = null) : this(
		x = x, y = y,
		width = MinecraftClient.getInstance().textRenderer.getWidth(message) + 20,
		message = message,
		onButtonPress = onButtonPress
	)

	constructor(x: Int, y: Int, message: Text, hoverText: List<Text>, onButtonPress: ((Button) -> Unit)? = null) : this(
		x = x,
		y = y,
		message = message,
		onButtonPress = onButtonPress
	) {
		this.hoverText.clear()
		this.hoverText.addAll(hoverText)
	}


}