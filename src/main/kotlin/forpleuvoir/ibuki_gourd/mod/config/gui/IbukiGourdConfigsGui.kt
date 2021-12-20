package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetListConfigBase
import forpleuvoir.ibuki_gourd.gui.ScreenBase
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.TextFieldWidget

/**
 * mod配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config.gui

 * 文件名 IbukiGourdConfigsGui

 * 创建时间 2021/12/18 12:08

 * @author forpleuvoir

 */
class IbukiGourdConfigsGui : ScreenBase("mod配置".text()) {
	private val topPadding = titleTopPadding.toInt() + MinecraftClient.getInstance().textRenderer.fontHeight + 5
	private var listWidget: WidgetListConfigBase? = null
	private var searchBar: TextFieldWidget? = null
	private val itemHeight: Int = 24
	private val pageSize: Int = 10
	private val listWidgetHeight: Int
		get() = pageSize * itemHeight

	override fun init() {
		super.init()
		val list = ArrayList(IbukiGourdConfigs.Values.OPTION)
		for (i in 1..10) {
			list.add(ConfigInt("凑数的", "凑数的", 0, 0, 99999))
		}
		listWidget =
			WidgetListConfigBase(list, this.width, listWidgetHeight, topPadding + 30, topPadding + 30 + listWidgetHeight, itemHeight)
		searchBar =
			TextFieldWidget(textRenderer, 10, topPadding + 10, this.width - (10 * 2), 15, "".text())
		searchBar?.setChangedListener { str ->
			listWidget?.setFilter {
				it.displayName.string.contains(str)
			}
		}
		this.addDrawableChild(Button(x = titleLeftPadding.toInt(), titleTopPadding.toInt(), "ea".text()) {
			println("我被点击了")
		})
		this.addDrawableChild(listWidget)
		this.addDrawableChild(searchBar)

	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		searchBar?.let {
			RenderUtil.isMouseHovered(it.x, it.y, it.width, it.height, mouseX, mouseY) {
				this.setInitialFocus(searchBar)
				it.active = true
				return it.mouseClicked(mouseX, mouseY, button)
			}
			it.mouseClicked(mouseX, mouseY, button)
		}
		listWidget?.let {
			RenderUtil.isMouseHovered(0, topPadding + 30, this.width, listWidgetHeight, mouseX, mouseY) {
				this.setInitialFocus(listWidget)
				return it.mouseClicked(mouseX, mouseY, button)
			}
			it.mouseClicked(mouseX, mouseY, button)
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		listWidget?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun tick() {
		super.tick()
		searchBar?.tick()
		listWidget?.tick()
	}
}