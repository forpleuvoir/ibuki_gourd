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
	dialogWidth: Int,
	dialogHeight: Int,
	parent: Screen?
) :
	DialogBase<DialogConfigStringList>(dialogWidth, dialogHeight, config.displayName, parent) {


	private lateinit var listWidget: WidgetListStringConfig
	private lateinit var add: ButtonIcon

	override fun init() {
		super.init()
		initAdd()
		initList((this.dialogHeight - (topPadding - this.y + margin)) / itemHeight)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListStringConfig(
			config,
			this,
			this.x + margin,
			this.topPadding + margin,
			pageSize,
			itemHeight,
			this.dialogWidth - margin * 2
		)
		this.addDrawableChild(listWidget)
	}

	private fun initAdd() {
		add = ButtonIcon(
			titleLabel.x + titleLabel.width + margin,
			titleLabel.y,
			Icon.PLUS, titleLabel.height , renderBord = true
		) {
			this.config.add("")
		}
		this.addDrawableChild(add)
	}

	override fun tick() {
		listWidget.tick()
	}
}