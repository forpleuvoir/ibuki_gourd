package forpleuvoir.ibuki_gourd.mod.config

import com.google.common.collect.ImmutableList
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.ConfigUtil
import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.IConfigHandler
import forpleuvoir.ibuki_gourd.config.options.*
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs.Setting.CONFIGS
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen
import forpleuvoir.ibuki_gourd.utils.color.Color4f
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

	object Setting {

		@JvmStatic
		val OPEN_GUI = ConfigHotkey(
			name = "ibuki_gourd.config.open_gui",
			defaultValue = KeyBind(
				InputUtil.GLFW_KEY_I,
				InputUtil.GLFW_KEY_G
			) {
				IbukiGourdScreen.openScreen(ScreenBase.current)
			}
		)

		@JvmStatic
		val SCREEN_BACKGROUND_COLOR = ConfigColor(
			name = "ibuki_gourd.config.screen_background_color",
			defaultValue = Color4f.BLACK.apply { alpha = 0.5f })

		val CONFIGS: List<ConfigBase> = listOf(
			OPEN_GUI,
			SCREEN_BACKGROUND_COLOR
		)
	}

	object Test {
		@JvmStatic
		val TEST_BOOLEAN: ConfigBoolean = ConfigBoolean("测试布尔", "测试布尔", false)

		@JvmStatic
		val TEST_BOOLEAN_HOTKEY: ConfigBooleanHotkey = ConfigBooleanHotkey(KeyBind(), TEST_BOOLEAN)

		@JvmStatic
		val TEST_BOOLEAN_WITH_KEY_BIND: ConfigBooleanWithKeyBind = ConfigBooleanWithKeyBind(
			"测试布尔带热键", "测试布尔带热键", false,
			KeyBind()
		)

		@JvmStatic
		val TEST_COLOR: ConfigColor = ConfigColor("测试颜色", "测试颜色", Color4f.WHITE)

		@JvmStatic
		val TEST_DOUBLE: ConfigDouble = ConfigDouble("测试浮点", "测试浮点", 0.0, -20.0, 20.0)

		@JvmStatic
		val TEST_INTEGER: ConfigInt = ConfigInt("测试整数", "测试整数", 0, -20, 20)

		@JvmStatic
		val TEST_HOTKEY: ConfigHotkey = ConfigHotkey("测试热键", "测试热键", KeyBind())

		@JvmStatic
		val TEST_MAP: ConfigMap = ConfigMap("测试Map", "测试Map", mapOf("1" to "2"))

		@JvmStatic
		val TEST_OPTIONS: ConfigOptions = ConfigOptions("测试选项", "测试选项", WhiteListMode.None)

		@JvmStatic
		val TEST_STRING: ConfigString = ConfigString("测试字符串", "测试字符串", "测试字符串")

		@JvmStatic
		val TEST_STRING_LIST: ConfigStringList = ConfigStringList("测试字符串列表", "测试字符串列表", ImmutableList.of("测试字符串列表"))

		@JvmStatic
		val TEST_GROUP: ConfigGroup = ConfigGroup(
			"测试配置组", "测试配置组", listOf(
				TEST_BOOLEAN,
				TEST_BOOLEAN_HOTKEY,
				TEST_BOOLEAN_WITH_KEY_BIND,
				TEST_COLOR,
				TEST_DOUBLE,
				TEST_INTEGER,
				TEST_HOTKEY,
				TEST_MAP,
				TEST_OPTIONS,
				TEST_STRING,
				TEST_STRING_LIST,
			)
		)

		val CONFIGS: List<ConfigBase> = listOf(
			TEST_BOOLEAN,
			TEST_BOOLEAN_HOTKEY,
			TEST_BOOLEAN_WITH_KEY_BIND,
			TEST_COLOR,
			TEST_DOUBLE,
			TEST_INTEGER,
			TEST_HOTKEY,
			TEST_MAP,
			TEST_OPTIONS,
			TEST_STRING,
			TEST_STRING_LIST,
			TEST_GROUP
		)
	}

	override fun save() {
		val configFile = ConfigUtil.configFile(IbukiGourdMod)
		val json = JsonObject()
		ConfigUtil.writeConfigBase(json, "Setting", CONFIGS)
		ConfigUtil.writeConfigBase(json, "Test", Test.CONFIGS)
		ConfigUtil.writeJsonToFile(json, configFile)

	}

	override fun load() {
		ConfigUtil.paresJsonFile(ConfigUtil.configFile(IbukiGourdMod))?.let {
			if (it is JsonObject) {
				ConfigUtil.readConfigBase(it, "Setting", CONFIGS)
				ConfigUtil.readConfigBase(it, "Test", Test.CONFIGS)
			}
		}
	}

	override fun allConfig(): List<IConfigBase> {
		return ArrayList<IConfigBase>().apply {
			addAll(CONFIGS)
			addAll(Test.CONFIGS)
		}
	}
}