package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.gui.common.Widget
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.text.Text

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonExtension

 * 创建时间 2022/1/29 4:58

 * @author forpleuvoir

 */

@Widget
inline fun ScreenBase.Button(
	text: Text = "Button".text,
	noinline onClick: ((Button) -> Unit)? = null,
	width: Int = mc.textRenderer.getWidth(text) + 20,
	height: Int = 20,
	init: Button.() -> Unit
) {
	this.addElement(Button(0, 0, width, height, text, onClick).apply {
		this.init()
	})

}