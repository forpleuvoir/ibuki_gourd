package forpleuvoir.ibuki_gourd.mod.gui

import com.google.common.collect.Lists
import forpleuvoir.ibuki_gourd.gui.BaseScreen
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.button.ButtonOnOff
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.gui

 * 文件名 TestScreen

 * 创建时间 2021/12/13 17:50

 * @author forpleuvoir

 */
class TestScreen(title: String) : BaseScreen(title = Text.of(title)) {

	override fun onScreenClose() {
		println("哈哈")
	}

	private val buttonOnOff = ButtonOnOff(20, 40, true)


	override fun init() {
		super.init()
		println("初始化")
		buttonOnOff.setOnHoverCallback {
			it.y = if (it.y == 40) 80 else 40
		}
		this.addDrawableChild(buttonOnOff)
		this.addDrawableChild(Button(
			buttonOnOff.x + buttonOnOff.width + 10, 40, Text.of("§c测试按钮"),
			Lists.newArrayList(
				Text.of("这是第一行"),
				Text.of("这是第二行"),
				Text.of("这是第三行")
			)
		) {
			val n = TestScreen("新的测试")
			n.parent = this
			openScreen(n)
		})
		this.addDrawableChild(Button(20, buttonOnOff.y + 10, Text.of("Game")){
			openScreen(SnakeGameScreen())
		})

	}
}