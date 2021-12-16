package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.config.options.OptionListConfig
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonOptionListConfig

 * 创建时间 2021/12/13 16:06

 * @author forpleuvoir

 */
class ButtonOptionListConfig(x: Int, y: Int, width: Int, height: Int = 20, private val config: OptionListConfig) :
	ButtonBase<ButtonOptionListConfig>(
		x, y, width, height, Text.of(""), null
	) {

	init {
		this.updateText()
		this.config.getValue().getAllItem().forEach {
			this.hoverText.add(it.displayName.append(" - ").append(it.displayRemark))
		}
	}

	override fun onPress() {
		super.onPress()
		this.config.setValue(this.config.getValue().cycle())
		this.updateText()
	}

	private fun updateText() {
		message = this.config.getValue().displayName
	}

}