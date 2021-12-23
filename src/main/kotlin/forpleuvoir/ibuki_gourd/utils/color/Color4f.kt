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
	override var red: Float = 0f,
	override var green: Float = 0f,
	override var blue: Float = 0f,
	override var alpha: Float = 1f
) : IColor<Float> {



	override val hexString: String
		get() = "#${(red * 255).toInt().toString(16).uppercase()}${(green * 255).toInt().toString(16).uppercase()}${(blue * 255).toInt().toString(16).uppercase()}${(alpha * 255).toInt().toString(16).uppercase()}"

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

	override val rgba: Int
		get() = Color(red, green, blue, alpha).rgb

	override fun rgba(alpha: Float): Int {
		return Color(red, green, blue, alpha).rgb
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

	override fun fromInt(color: Int): Color4f {
		val c = Color(color)
		this.alpha = c.alpha.toFloat() / 255f
		this.red = c.red.toFloat() / 255f
		this.green = c.green.toFloat() / 255f
		this.blue = c.blue.toFloat() / 255f
		return this
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Color4f

		if (rgba != other.rgba) return false

		return true
	}

	override fun hashCode(): Int {
		return rgba
	}

	override val minValue: Float
		get() = 0f
	override val maxValue: Float
		get() = 1f


}