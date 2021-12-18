package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.awt.Color


/**
 * 颜色

 * R G B A

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 Color4i

 * 创建时间 2021/12/9 19:18

 * @author forpleuvoir

 */
class Color4i(
	override var red: Int = 0,
	override var green: Int = 0,
	override var blue: Int = 0,
	override var alpha: Int = 255
) : IColor<Int> {

	override val hexString: String
		get() = "#${red.toString(16).uppercase()}${green.toString(16).uppercase()}${blue.toString(16).uppercase()}${alpha.toString(16).uppercase()}"

	init {
		fixAllValue()
	}

	override val rgb: Int
		get() = Color(red, green, blue, alpha).rgb

	override fun rgb(alpha: Int): Int {
		return Color(red, green, blue, alpha).rgb
	}

	private fun fixAllValue() {
		red = fixValue(red)
		green = fixValue(green)
		blue = fixValue(blue)
		alpha = fixValue(alpha)
	}

	private fun fixValue(value: Int): Int {
		var v = value
		v = 0.coerceAtLeast(v)
		v = 255.coerceAtMost(v)
		return v
	}

	@Throws(Exception::class)
	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		if (jsonElement.isJsonObject) {
			val jsonObject = jsonElement.asJsonObject
			red = jsonObject["red"].asInt
			green = jsonObject["green"].asInt
			blue = jsonObject["blue"].asInt
			alpha = jsonObject["alpha"].asInt
			fixAllValue()
		}
	}

	override val asJsonElement: JsonElement
		get() {
			val jsonObject = JsonObject()
			jsonObject.addProperty("red", red)
			jsonObject.addProperty("green", red)
			jsonObject.addProperty("blue", red)
			jsonObject.addProperty("alpha", red)
			return jsonObject
		}

	companion object {
		val WHITE = Color4i(255, 255, 255, 255)
		val RED = Color4i(255, 0, 0, 255)
		val YELLOW = Color4i(127, 127, 0, 255)
		val GREEN = Color4i(0, 255, 0, 255)
		val CYAN = Color4i(0, 127, 127, 255)
		val BLUE = Color4i(0, 0, 255, 255)
		val MAGENTA = Color4i(127, 0, 127, 255)
		val BLACK = Color4i(0, 0, 0, 255)
	}

	override fun fromInt(color: Int): Color4i {
		val c = Color(color)
		this.alpha = c.alpha
		this.red = c.red
		this.green = c.green
		this.blue = c.blue
		return this
	}

}


