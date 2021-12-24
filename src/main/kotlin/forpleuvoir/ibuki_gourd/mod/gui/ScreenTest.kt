package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigDouble
import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigDouble
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigInt
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.button.ButtonOnOff
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.gui

 * 文件名 ScreenTest

 * 创建时间 2021/12/13 17:50

 * @author forpleuvoir

 */
class ScreenTest(tabEntry: IScreenTabEntry) : ScreenTab(tabEntry) {

	override fun onScreenClose() {
	}

	private val buttonOnOff = ButtonOnOff(20, 60, true)


	override fun init() {
		super.init()
		buttonOnOff.setOnHoverCallback {
			it.y = if (it.y == 60) 60 else 90
		}
		this.addDrawableChild(buttonOnOff)
		this.addDrawableChild(Button(20, buttonOnOff.y + 10, Text.of("Game")) {
			openScreen(ScreenSnakeGame())
		})
		val config = ConfigDouble("aa", "remark", 20.0, 0.0, 100.0)
		val double = WidgetSliderConfigDouble(20, 90, 60, 20, config)
		this.addDrawableChild(double)
		val intConfig = ConfigInt("aa", "remark", 20, 0, 100)
		val int = WidgetSliderConfigInt(20, 120, 60, 20, intConfig)
		this.addDrawableChild(int)
	}
}