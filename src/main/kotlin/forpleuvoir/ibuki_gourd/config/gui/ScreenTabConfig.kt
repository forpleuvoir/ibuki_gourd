package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.LiteralText

/**
 * 配置界面

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 ScreenTabConfig

 * 创建时间 2021/12/22 16:45

 * @author forpleuvoir

 */
class ScreenTabConfig(private val itemHeight: Int = 24, private val configGroup: IConfigGroup) : ScreenTab(configGroup) {

	private lateinit var searchBar: TextFieldWidget
	private val searchBarHeight: Int = 15

	private lateinit var listWidget: WidgetListConfig

	override fun init() {
		super.init()
		initSearchBar()
		initListWidget((this.height - (searchBarHeight + searchBar.y + margin)) / itemHeight)
	}

	private fun initListWidget(pageSize: Int) {
		searchBar.let {
			listWidget = WidgetListConfig(configGroup.configs, this, 0, it.y + searchBarHeight + margin, pageSize, itemHeight, this.width)
			listWidget.setFilter { configEntry ->
				configEntry.config.displayName.string.contains(it.text)
						|| configEntry.config.displayRemark.string.contains(it.text)
						|| configEntry.config.name.contains(it.text)
						|| configEntry.config.remark.contains(it.text)
			}
			listWidget.setHoverCallback { entry -> drawTopMessage(entry.config.displayRemark) }
			this.addDrawableChild(listWidget)
		}
	}

	private fun initSearchBar() {
		searchBar =
			TextFieldWidget(
				textRenderer,
				15 + 2,
				top + margin + 20,
				this.width - 15 * 2 - 2,
				searchBarHeight,
				LiteralText.EMPTY
			).apply {
				text = ""
				setMaxLength(65535)
			}
		this.addDrawableChild(searchBar)
	}

}