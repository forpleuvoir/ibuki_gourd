package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigDouble
import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigDouble
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigInt
import forpleuvoir.ibuki_gourd.event.events.Events
import forpleuvoir.ibuki_gourd.event.events.KeyReleaseEvent
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.button.ButtonOnOff
import forpleuvoir.ibuki_gourd.gui.button.ButtonOption
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.gui.widget.MultilineTextField
import forpleuvoir.ibuki_gourd.gui.widget.WidgetDropList
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.utils.text
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
		addDrawableChild(buttonOnOff)
		val envButton = ButtonOption(
			listOf("aa", "bb", "cc", "dd", "ee"),
			"cc",
			x = 60,
			y = 80,
			width =80,
			20
		) {
			println(it)
		}
		addDrawableChild(envButton)
		val dropList = WidgetDropList(
			items = Events.getEvents(),
			default = KeyReleaseEvent::class.java,
			stringAdapter = { e -> e!!.simpleName },
			entryAdapter = { s -> Events.getEventByName(s) },
			this,
			5,
			16,
			120,
			100,
			100
		)
		val bt = Button(120, buttonOnOff.y + 10, Text.of("get")) { bt ->
			dropList.current?.let { bt.message = it.simpleName.text }
		}
		dropList.toggleCallback = {
			bt.message = it!!.simpleName.text
		}
		this.addDrawableChild(bt)
		this.addDrawableChild(dropList)
		this.addDrawableChild(MultilineTextField(220, 60, 233, 50).apply {
			val stringBuilder = StringBuilder()
			for (i in 1..30) {
				stringBuilder.append("超长的文本啊啊啊啊啊啊啊啊啊啊啊")
			}
			text = stringBuilder.toString()
		})
	}
}