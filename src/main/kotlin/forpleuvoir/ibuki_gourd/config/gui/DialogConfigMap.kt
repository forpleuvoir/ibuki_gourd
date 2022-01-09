package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigMap
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import net.minecraft.client.gui.screen.Screen


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
	private val pageSize: Int = 8,
	dialogWidth: Int,
	parent: Screen?
) : DialogBase<DialogConfigMap>(dialogWidth, 0, config.displayName, parent) {

	init {
		this.dialogHeight = 3 + pageSize * itemHeight + (paddingTop + paddingBottom)
	}

	private lateinit var listWidget: WidgetListMap
	private var scrollAmount: Double = 0.0

	private lateinit var add: ButtonIcon

	override fun init() {
		super.init()
		initAdd()
		initList(pageSize)
	}

	private fun initList(pageSize: Int) {
		listWidget = WidgetListMap(
			config,
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


	private fun initAdd() {
		add = ButtonIcon(
			titleLabel.x + titleLabel.width + margin,
			titleLabel.y,
			Icon.PLUS, titleLabel.height, renderBord = true
		) {
			if (!config.getValue().containsKey(""))
				this.config[""] = ""
		}
		this.addDrawableChild(add)
	}



	override fun tick() {
		listWidget.tick()
	}
}