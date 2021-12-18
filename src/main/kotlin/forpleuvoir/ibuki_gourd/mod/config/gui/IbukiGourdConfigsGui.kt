package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetListConfigBase
import forpleuvoir.ibuki_gourd.gui.ScreenBase
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
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
	private val topPadding = titlePadding.toInt() + MinecraftClient.getInstance().textRenderer.fontHeight + 5
	private var listWidget: WidgetListConfigBase? = null
	private var searchBar: TextFieldWidget? = null

	override fun init() {
		super.init()
		val list = ArrayList(IbukiGourdConfigs.Values.OPTION)
		for (i in 1..10) {
			list.add(ConfigInt("凑数的", "凑数的", 0, 0, 99999))
		}
		listWidget =
			WidgetListConfigBase(list, this.width, this.height - topPadding, topPadding, this.height, 26)

		this.addDrawableChild(listWidget)

	}


	override fun tick() {
		super.tick()
		listWidget?.tick()
	}
}