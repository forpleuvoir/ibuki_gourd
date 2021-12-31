package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.gui.screen.ScreenTab
import forpleuvoir.ibuki_gourd.gui.widget.SearchBar
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.LiteralText
import java.util.regex.PatternSyntaxException

/**
 * 配置界面

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 ScreenTabConfig

 * 创建时间 2021/12/22 16:45

 * @author forpleuvoir

 */
class ScreenTabConfig(private val itemHeight: Int = 24, private val configGroup: IConfigGroup) : ScreenTab(configGroup) {

	private lateinit var searchBar: SearchBar
	private val searchBarHeight: Int = 16

	private lateinit var listWidget: WidgetListConfig

	override fun init() {
		super.init()
		initSearchBar()
		initListWidget((this.height - (searchBarHeight + searchBar.y + margin)) / itemHeight)
	}

	private fun initListWidget(pageSize: Int) {
		listWidget = WidgetListConfig(configGroup.configs, this, 0, searchBar.y + searchBarHeight + margin, pageSize, itemHeight, this.width)
		listWidget.setFilter {
			try {
				val regex = Regex(searchBar.text)
				it.config.matched(regex)
			} catch (_: PatternSyntaxException) {
				true
			}
		}
		listWidget.setHoverCallback { entry -> drawTopMessage(entry.config.displayRemark) }
		this.addDrawableChild(listWidget)
	}

	private fun initSearchBar() {
		searchBar =
			SearchBar(
				15 + 2,
				top + margin + 20,
				this.width - 15 * 2 - 2,
				searchBarHeight
			)
		this.addDrawableChild(searchBar)
	}

	override fun tick() {
		listWidget.tick()
		searchBar.tick()
	}
}