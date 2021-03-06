package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.options.IConfigGroup
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
	private val config: IConfigBase,
	private val group: IConfigGroup,
	private val itemHeight: Int = 24,
	private val maxPageSize: Int = 9,
	dialogWidth: Int,
	parent: Screen?
) :
	DialogBase<DialogConfigGroup>(
		dialogWidth,
		0,
		title = config.displayName,
		parent
	) {

	val pageSize get() = if (maxPageSize >= group.getValue().size) group.getValue().size else maxPageSize

	init {
		this.dialogHeight = 3 + (pageSize * itemHeight) + (paddingTop + paddingBottom)
	}

	private lateinit var listWidget: WidgetListConfig
	private var scrollAmount: Double = 0.0

	override fun init() {
		super.init()
		initList(pageSize)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListConfig(
			group.getValue(),
			this,
			left,
			top + 1,
			pageSize,
			itemHeight,
			contentWidth - 1
		)
		listWidget.scrollbar.amount = scrollAmount
		listWidget.setScrollAmountConsumer {
			scrollAmount = it
		}
		this.addDrawableChild(listWidget)
	}

	override fun tick() {
		listWidget.tick()
	}
}