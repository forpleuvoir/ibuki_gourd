package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigGroup
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import net.minecraft.client.gui.screen.Screen

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
	private val maxPageSize: Int = 8,
	dialogWidth: Int,
	parent: Screen?
) :
	DialogBase<DialogConfigGroup>(
		dialogWidth,
		0,
		title = config.displayName,
		parent
	) {

	val pageSize get() = if (maxPageSize >= config.getValue().size) config.getValue().size else maxPageSize

	init {
		this.dialogHeight = 2 + (pageSize * itemHeight) + (paddingTop + paddingBottom)
	}

	private lateinit var listWidget: WidgetListConfig

	override fun init() {
		super.init()
		initList(pageSize)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListConfig(
			config.getValue(),
			this,
			left,
			top + 1,
			pageSize,
			itemHeight,
			contentWidth - 1
		)
		this.addDrawableChild(listWidget)
	}

	override fun tick() {
		listWidget.tick()
	}
}