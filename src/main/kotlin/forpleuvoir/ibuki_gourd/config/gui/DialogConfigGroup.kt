package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.ConfigGroup
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 DialogConfigGroup

 * 创建时间 2021/12/24 16:48

 * @author forpleuvoir

 */
class DialogConfigGroup(
	private val config: ConfigGroup,
	private val itemHeight: Int = 24,
	dialogWidth: Int,
	dialogHeight: Int,
	parent: Screen?
) :
	DialogBase<DialogConfigGroup>(dialogWidth, dialogHeight, title = config.displayName, parent) {

	private lateinit var listWidget: WidgetListConfig

	override fun init() {
		super.init()
		initList((this.dialogHeight - (topPadding - this.y + margin)) / itemHeight)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListConfig(
			config.getValue(),
			this,
			this.x + margin,
			this.topPadding + margin,
			pageSize,
			itemHeight,
			this.dialogWidth - margin * 2
		)
		this.addDrawableChild(listWidget)
	}

	override fun tick() {
		listWidget.tick()
	}
}