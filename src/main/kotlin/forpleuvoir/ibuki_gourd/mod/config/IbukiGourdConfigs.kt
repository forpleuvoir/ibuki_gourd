package forpleuvoir.ibuki_gourd.mod.config

import forpleuvoir.ibuki_gourd.config.IConfigHandler
import forpleuvoir.ibuki_gourd.config.options.*
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

/**
 * mod配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config

 * 文件名 IbukiGourdConfigs

 * 创建时间 2021/12/18 12:08

 * @author forpleuvoir

 */
object IbukiGourdConfigs : IConfigHandler {

	object Values {
		@JvmStatic
		val TEST_COLOR = ConfigColor(name = "ibuki_gourd.test.color", defaultValue = Color4i(0, 127, 127, 255))

		@JvmStatic
		val TEST_BOOLEAN = ConfigBoolean(name = "ibuki_gourd.test.boolean", defaultValue = true)

		@JvmStatic
		val TEST_INT = ConfigInt(name = "ibuki_gourd.test.int", defaultValue = 0, minValue = 0, maxValue = 999)

		@JvmStatic
		val TEST_DOUBLE = ConfigDouble(name = "ibuki_gourd.test.double", defaultValue = 666.0, minValue = 0.0, maxValue = 999.0)

		@JvmStatic
		val TEST_STRING = ConfigString(name = "ibuki_gourd.test.string", defaultValue = "true")

		@JvmStatic
		val TEST_OPTIONS = ConfigOptions(name = "ibuki_gourd.test.options", defaultValue = WhiteListMode.None)

		@JvmStatic
		val TEST_BOOLEAN_HOTKEY = ConfigBooleanHotkey(KeyBind(InputUtil.GLFW_KEY_LEFT_CONTROL,InputUtil.GLFW_KEY_R), TEST_BOOLEAN)


		@JvmStatic
		val OPEN_GUI = ConfigHotkey(
			name = "ibuki_gourd.test.open_gui",
			defaultValue = KeyBind(InputUtil.GLFW_KEY_I, InputUtil.GLFW_KEY_G, keyEnvironment = KeyEnvironment.IN_GAME) {
				IbukiGourdScreen.openScreen()
			}
		)

		@JvmStatic
		val TEST_GROUP = ConfigGroup(name =  "ibuki_gourd.test.group", defaultValue = listOf(
			TEST_COLOR, TEST_BOOLEAN, TEST_INT, TEST_DOUBLE, TEST_STRING, TEST_OPTIONS, OPEN_GUI,TEST_BOOLEAN_HOTKEY
		)
		)


		val CONFIGS: List<ConfigBase> = listOf(
			TEST_COLOR, TEST_BOOLEAN, TEST_INT, TEST_DOUBLE, TEST_STRING, TEST_OPTIONS, OPEN_GUI,TEST_BOOLEAN_HOTKEY,TEST_GROUP
		)
	}

	override fun save() {
		TODO("Not yet implemented")
	}

	override fun load() {
		TODO("Not yet implemented")
	}
}