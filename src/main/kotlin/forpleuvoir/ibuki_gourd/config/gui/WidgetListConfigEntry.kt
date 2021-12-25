package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.gui.widget.IPositionElement
import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.gui.widget.WidgetList
import forpleuvoir.ibuki_gourd.gui.widget.WidgetListEntry
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 WidgetListConfigEntry

 * 创建时间 2021/12/21 13:48

 * @author forpleuvoir

 */
class WidgetListConfigEntry(val config: ConfigBase, parent: WidgetList<*>, x: Int, y: Int, width: Int, height: Int) :
	WidgetListEntry<WidgetListConfigEntry>(parent, x, y, width, height) {

	private val leftPadding: Int = 15
	private val rightPadding: Int = 15

	private val rightMargin: Int = 10

	val left: Int get() = this.x + this.leftPadding
	val right: Int get() = this.x + this.width - this.rightPadding
	val top: Int get() = this.y
	val bottom: Int get() = this.y + height


	private val text: MutableText = config.displayName
	private val textHoverText: MutableText = config.displayRemark

	private val textLabel: LabelText = LabelText(text, 0, 0).apply {
		this.align = LabelText.Align.CENTER_LEFT
		this.height = this@WidgetListConfigEntry.height
		this.addHoverText(textHoverText)
	}

	private val textX: Int
		get() = this.left

	private val textY: Int
		get() {
			return this.y + this.height / 2 - this.textLabel.height / 2
		}

	private val configClickableWidget: ClickableWidget = ConfigWrapper.wrap(config)

	private val restButton: ButtonRest = ButtonRest(x = 0, y = 0, config = config)

	init {
		addDrawableChild(configClickableWidget)
		addDrawableChild(restButton)
		addDrawableChild(textLabel)
		initPosition()
	}



	 override fun initPosition() {
		restButton.x = this.right - restButton.width
		restButton.y = (this.y + this.height / 2) - (restButton.height / 2)

		val x = restButton.x - configClickableWidget.width - this.rightMargin
		val y = (this.y + this.height / 2) - (configClickableWidget.height / 2)
		if (configClickableWidget is IPositionElement) {
			configClickableWidget.setPosition(x, y)
		} else {
			configClickableWidget.x = x
			configClickableWidget.y = y
		}
		textLabel.setPosition(textX,textY)
		textLabel.width = this.width - restButton.width - configClickableWidget.width - this.rightMargin * 3 - 40
	}


	override fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {

	}

	override fun tick() {
		if (configClickableWidget is TextFieldWidget) {
			configClickableWidget.tick()
		}
	}


}