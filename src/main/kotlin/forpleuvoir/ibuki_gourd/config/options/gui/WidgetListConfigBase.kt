package forpleuvoir.ibuki_gourd.config.options.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.EntryListWidget


/**
 * 配置列表组件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetListConfigBase

 * 创建时间 2021/12/17 23:34

 * @author forpleuvoir

 */
class WidgetListConfigBase(width: Int, height: Int, top: Int, bottom: Int, itemHeight: Int) :
	EntryListWidget<ConfigBaseEntry>(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight) {

	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		builder?.put(NarrationPart.TITLE,"")
	}
}