package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.awt.Color
import java.awt.Color.*


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils.color

 * 文件名 Color4f

 * 创建时间 2021/12/13 18:58

 * @author forpleuvoir

 */
class Color4f(
	var red: Float = 0f,
	var green: Float = 0f,
	var blue: Float = 0f,
	var alpha: Float = 1f
) : IColor<Float> {

	init {
		fixAllValue()
	}

	companion object {
		val WHITE = Color4f(1f, 1f, 1f, 1f)
		val BLACK = Color4f(0f, 0f, 0f, 1f)
		val RED = Color4f(1f, 0f, 0f, 1f)
		val YELLOW = Color4f(0.5f, 0.5f, 0f, 1f)
		val GREEN = Color4f(0f, 1f, 0f, 1f)
		val CYAN = Color4f(0f, 0.5f, 0.5f, 1f)
		val BLUE = Color4f(0f, 0f, 1f, 1f)
		val MAGENTA = Color4f(0.5f, 0f, 0.5f, 1f)
	}

	override val intValue: Int
		get() = Color(red, green, blue, alpha).rgb

	override fun intValue(alpha: Float): Int {
		return Color(red, green, blue, alpha).rgb
	}

	private fun fixAllValue() {
		red = fixValue(red)
		green = fixValue(green)
		blue = fixValue(blue)
		alpha = fixValue(alpha)
	}

	private fun fixValue(value: Float): Float {
		var v = value
		if (v < 0f) v = 0f
		if (v > 1f) v = 1f
		return v
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		if (jsonElement.isJsonObject) {
			val jsonObject = jsonElement.asJsonObject
			red = jsonObject["red"].asFloat
			green = jsonObject["green"].asFloat
			blue = jsonObject["blue"].asFloat
			alpha = jsonObject["alpha"].asFloat
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

	override fun fromInt(color: Int): Color4f {
		val c = Color(color)
		this.alpha = c.alpha.toFloat() / 255f
		this.red = c.red.toFloat() / 255f
		this.green = c.green.toFloat() / 255f
		this.blue = c.blue.toFloat() / 255f
		return this
	}


}