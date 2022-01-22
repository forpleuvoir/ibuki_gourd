package forpleuvoir.ibuki_gourd.mod.gui

import forpleuvoir.ibuki_gourd.event.events.Events
import forpleuvoir.ibuki_gourd.event.events.KeyReleaseEvent
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.button.ButtonOnOff
import forpleuvoir.ibuki_gourd.gui.button.ButtonOption
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry
import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.gui.widget.MultilineTextField
import forpleuvoir.ibuki_gourd.gui.widget.WidgetDropList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetIntInput
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW


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

	override fun tick() {
		children().forEach {
			if (it is MultilineTextField) it.tick()
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			children().find { it is MultilineTextField }?.let {
				if ((it as MultilineTextField).isFocused) {
					it.keyPressed(keyCode, scanCode, modifiers)
				}
				return true
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

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
			width = 80,
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
			hoverTextAdapter = { s -> listOf(Events.getDescription(s)!!) },
			parent = this,
			pageSize = 5,
			itemHeight = 16,
			x = 120,
			y = 100,
			width = 100
		)
		val bt = Button(120, buttonOnOff.y + 10, Text.of("get")) { bt ->
			dropList.current?.let { bt.message = it.simpleName.text }
			openScreen(ScreenGameOfLife().apply { parent = this@ScreenTest })
		}
		dropList.toggleCallback = {
			bt.message = it!!.simpleName.text
		}
		this.addDrawableChild(bt)
		this.addDrawableChild(dropList)
		this.addDrawableChild(MultilineTextField(250, 60, 233, 120).apply {
			val stringBuilder = StringBuilder("换行测试\n")
			for (i in 1..30) {
				stringBuilder.append("$i 超长的文本啊啊啊啊啊啊啊啊啊啊啊${if (i != 30) '\n' else ""}")
			}
			text = stringBuilder.toString()
		})
		this.addDrawableChild(WidgetIntInput(120, 220, 45, 16, 0, -10).apply {
			setOnValueChangedCallback {
				bt.message = it.toString().text
			}
		})
	}
}