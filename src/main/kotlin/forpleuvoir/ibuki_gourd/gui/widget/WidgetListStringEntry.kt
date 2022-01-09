package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetListStringEntry

 * 创建时间 2021/12/28 19:14

 * @author forpleuvoir

 */
class WidgetListStringEntry(val value: String, parent: WidgetList<*>, x: Int, y: Int, width: Int, height: Int) :
	WidgetListEntry<WidgetListStringEntry>(parent, x, y, width, height) {

	private val text = LabelText(value.text, this.x, this.y, this.width, this.height, align = LabelText.Align.CENTER_LEFT)

	private val isOdd: Boolean get() = index % 2 == 1

	init {
		this.addDrawableChild(text)
		initPosition()
	}

	override fun updateBgOpacity(delta: Float) {
		if (ScreenBase.isCurrent(parentWidget.parent)) {
			bgOpacity += delta.toInt()
			bgOpacity = bgOpacity.clamp(if (isOdd) 0 else 50, maxBgOpacity).toInt()
		}
	}

	override fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {}

	override fun initPosition() {
		text.setPosition(this.x, this.y)
	}

	override fun resize() {
	}
}