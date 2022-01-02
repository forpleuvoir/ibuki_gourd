package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigStringList
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 DialogConfigStringList

 * 创建时间 2021/12/24 22:39

 * @author forpleuvoir

 */
class DialogConfigStringList(
	private val config: ConfigStringList,
	private val itemHeight: Int = 24,
	private val pageSize: Int = 8,
	dialogWidth: Int,
	parent: Screen?
) :
	DialogBase<DialogConfigStringList>(dialogWidth, 0, config.displayName, parent) {

	init {
		this.dialogHeight = pageSize * itemHeight + (paddingTop + paddingBottom)
	}

	private lateinit var listWidget: WidgetListStringConfig
	private lateinit var add: ButtonIcon

	override fun init() {
		super.init()
		initAdd()
		initList(pageSize)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListStringConfig(
			config,
			this,
			left,
			top + 1,
			pageSize,
			itemHeight,
			contentWidth - 1
		)
		this.addDrawableChild(listWidget)
	}

	private fun initAdd() {
		add = ButtonIcon(
			titleLabel.x + titleLabel.width + margin,
			titleLabel.y,
			Icon.PLUS, titleLabel.height, renderBord = true
		) {
			this.config.add("")
		}
		this.addDrawableChild(add)
	}

	override fun tick() {
		listWidget.tick()
	}
}