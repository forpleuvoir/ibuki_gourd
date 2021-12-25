package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigMap
import forpleuvoir.ibuki_gourd.config.options.ConfigStringList
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 DialogConfigMap

 * 创建时间 2021/12/25 12:57

 * @author forpleuvoir

 */
class DialogConfigMap(
	private val config: ConfigMap,
	private val itemHeight: Int = 24,
	dialogWidth: Int,
	dialogHeight: Int,
	parent: Screen?
) : DialogBase<DialogConfigMap>(dialogWidth, dialogHeight,  config.displayName, parent) {

	private lateinit var listWidget: WidgetListMap
	private lateinit var add: ButtonIcon

	override fun init() {
		super.init()
		initAdd()
		initList((this.dialogHeight - (topPadding - this.y + margin)) / itemHeight)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListMap(
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
			Icon.PLUS, titleLabel.height, renderBord = true
		) {
			if (!config.getValue().containsKey(""))
				this.config.put("", "")
		}
		this.addDrawableChild(add)
	}

	override fun tick() {
		listWidget.tick()
	}
}