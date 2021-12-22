package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.ConfigType.*
import forpleuvoir.ibuki_gourd.config.options.*
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ClickableWidget

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ConfigWrapper

 * 创建时间 2021/12/18 11:08

 * @author forpleuvoir

 */
object ConfigWrapper {

	private val textRenderer = MinecraftClient.getInstance().textRenderer

	@JvmStatic
	fun wrap(config: ConfigBase, width: Int = 120): ClickableWidget {
		return when (config.type) {
			BOOLEAN     -> ButtonConfigBoolean(x = 0, y = 0, width = width, config = config as ConfigBoolean)
			INTEGER     -> WidgetSliderConfigInt(x = 0, y = 0, width = width, height = 20, config = config as ConfigInt)
			DOUBLE      -> WidgetSliderConfigDouble(x = 0, y = 0, width = width, height = 20, config = config as ConfigDouble)
			COLOR       -> ButtonConfigColor(x = 0, y = 0, width = width, config = config as ConfigColor)
			STRING      -> WidgetConfigString(x = 0, y = 0, width = width , height = 16, config as ConfigString)
			STRING_LIST -> Button(x = 0, y = 0, width = width, message = "null".text()) {}
			OPTIONS     -> ButtonConfigOptions(x = 0, y = 0, width = width, config = config as ConfigOptions)
			HOTKEY      -> ButtonConfigHotkey(x = 0, y = 0, width = width, config = config as ConfigHotkey)
			LIST        -> Button(x = 0, y = 0, width = width, message = "null".text()) {}
		}
	}

}