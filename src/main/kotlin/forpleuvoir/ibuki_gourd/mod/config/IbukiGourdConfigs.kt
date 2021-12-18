package forpleuvoir.ibuki_gourd.mod.config

import forpleuvoir.ibuki_gourd.config.IConfigHandler
import forpleuvoir.ibuki_gourd.config.options.*
import forpleuvoir.ibuki_gourd.utils.color.Color4i

/**
 * mod配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config

 * 文件名 IbukiGourdConfigs

 * 创建时间 2021/12/18 12:08

 * @author forpleuvoir

 */
class IbukiGourdConfigs : IConfigHandler {

	object Values {
		@JvmStatic
		val TEST_COLOR = ConfigColor(name = "ibuki_gourd.test.color", defaultValue = Color4i(0, 127, 127, 0))

		@JvmStatic
		val TEST_BOOLEAN = ConfigBoolean(name = "ibuki_gourd.test.boolean", defaultValue = true)

		@JvmStatic
		val TEST_INT = ConfigInt(name = "ibuki_gourd.test.int", defaultValue = 0, minValue = 0, maxValue = 999)

		@JvmStatic
		val TEST_DOUBLE = ConfigDouble(name = "ibuki_gourd.test.double", defaultValue = 666.0, minValue = 0.0, maxValue = 999.0)

		@JvmStatic
		val TEST_STRING = ConfigString(name = "ibuki_gourd.test.string", defaultValue = "true")


		val OPTION: List<ConfigBase> = listOf(
			TEST_COLOR, TEST_BOOLEAN, TEST_INT, TEST_DOUBLE, TEST_STRING
		)
	}

	override fun save() {
		TODO("Not yet implemented")
	}

	override fun load() {
		TODO("Not yet implemented")
	}
}