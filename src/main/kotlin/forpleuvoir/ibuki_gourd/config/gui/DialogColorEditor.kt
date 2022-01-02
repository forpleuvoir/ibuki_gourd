package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.gui.dialog.DialogConfirm
import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang.*
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 DialogBase

 * 创建时间 2021/12/23 16:57

 * @author forpleuvoir

 */
class DialogColorEditor(
	private val color: IColor<Number>,
	dialogWidth: Int,
	dialogHeight: Int,
	title: Text,
	parent: Screen?,
	private val onApply: (IColor<Number>) -> Unit
) : DialogConfirm(dialogWidth, dialogHeight, title, parent, {
	it as DialogColorEditor
	onApply.invoke(it.color)
}, null) {


	private lateinit var red: WidgetSliderNumber
	private lateinit var green: WidgetSliderNumber
	private lateinit var blue: WidgetSliderNumber
	private lateinit var alpha: WidgetSliderNumber

	private lateinit var redText: LabelText
	private lateinit var greenText: LabelText
	private lateinit var blueText: LabelText
	private lateinit var alphaText: LabelText

	override fun init() {
		super.init()
		initText()
		iniSlider()
		initColorBlock()
	}

	private fun initColorBlock() {
		val colorBlock = Drawable { _, _, _, _ ->
			RenderUtil.drawOutlinedBox(
				red.x + red.width + margin * 2,
				red.y + margin,
				alpha.y + alpha.height - red.y - margin * 2,
				alpha.y + alpha.height - red.y - margin * 2,
				color,
				Color4i.WHITE
			)
		}
		this.addDrawable(colorBlock)
	}


	private fun initText() {
		redText = LabelText(Red.tText(), this.x + margin, this.topPadding + margin, 30, 20)
		greenText = LabelText(Green.tText(), this.x + margin, redText.y + redText.height + margin, 30, 20)
		blueText = LabelText(Blue.tText(), this.x + margin, greenText.y + greenText.height + margin, 30, 20)
		alphaText = LabelText(Alpha.tText(), this.x + margin, blueText.y + blueText.height + margin, 30, 20)
		this.addDrawableChild(redText)
		this.addDrawableChild(greenText)
		this.addDrawableChild(blueText)
		this.addDrawableChild(alphaText)
	}

	private fun iniSlider() {
		red = WidgetSliderNumber(
			this.redText.width + this.redText.x + margin,
			this.topPadding + margin,
			100,
			20,
			{ color.red },
			color.minValue,
			color.maxValue
		).apply { setConsumer { color.red = it } }
		green = WidgetSliderNumber(
			this.greenText.width + this.greenText.x + margin,
			red.y + red.height + margin,
			100,
			20,
			{ color.green },
			color.minValue,
			color.maxValue
		).apply { setConsumer { color.green = it } }
		blue = WidgetSliderNumber(
			this.blueText.width + this.blueText.x + margin,
			green.y + green.height + margin,
			100,
			20,
			{ color.blue },
			color.minValue,
			color.maxValue
		).apply { setConsumer { color.blue = it } }
		alpha = WidgetSliderNumber(
			this.alphaText.width + this.alphaText.x + margin,
			blue.y + blue.height + margin,
			100,
			20,
			{ color.alpha },
			color.minValue,
			color.maxValue
		).apply { setConsumer { color.alpha = it } }
		this.addDrawableChild(red)
		this.addDrawableChild(green)
		this.addDrawableChild(blue)
		this.addDrawableChild(alpha)
	}


}