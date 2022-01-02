package forpleuvoir.ibuki_gourd.gui.common

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.common

 * 文件名 PositionClickableWidget

 * 创建时间 2021/12/29 17:38

 * @author forpleuvoir

 */
open class PositionClickableWidget(x: Int, y: Int, width: Int, height: Int, message: Text?) : ClickableWidget(x, y, width, height, message),
	IPositionElement {

	var parent: Screen? = null


	override fun appendNarrations(builder: NarrationMessageBuilder?) {}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null
}