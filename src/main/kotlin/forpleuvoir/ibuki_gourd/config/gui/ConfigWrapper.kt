package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 ConfigWrapper

 * 创建时间 2022/1/3 15:22

 * @author forpleuvoir

 */
abstract class ConfigWrapper<C : ConfigBase>(val config: C, x: Int, y: Int, width: Int, height: Int) :
	PositionParentWidget(x, y, width, height) {

	abstract fun initWidget()

	open fun tick() {}

	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		builder?.put(NarrationPart.TITLE, config.displayName)
	}

	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.HOVERED
	}
}