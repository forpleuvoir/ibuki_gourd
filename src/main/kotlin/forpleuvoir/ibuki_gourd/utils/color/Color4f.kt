package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement


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

	constructor(color: IColor<out Number>) : this() {
		fromInt(color.rgba)
	}


	override val hexString: String
		get() = "#${(red * 255).toInt().toString(16).run { if (this == "0") return@run "00" else this }.uppercase()}" +
				(green * 255).toInt().toString(16).run { if (this == "0") return@run "00" else this }.uppercase() +
				(blue * 255).toInt().toString(16).run { if (this == "0") return@run "00" else this }.uppercase() +
				(alpha * 255).toInt().toString(16).run { if (this == "0") return@run "00" else this }.uppercase()

	init {
		fixAllValue()
	}

	override fun opacity(opacity: Double): Color4f {
		return Color4f().fromInt(rgba).apply { this.alpha = (this.alpha * opacity).toFloat() }
	}

	companion object {
		val WHITE get() = Color4f(1f, 1f, 1f, 1f)
		val BLACK get() = Color4f(0f, 0f, 0f, 1f)
		val RED get() = Color4f(1f, 0f, 0f, 1f)
		val YELLOW get() = Color4f(1f, 1f, 0f, 1f)
		val GREEN get() = Color4f(0f, 1f, 0f, 1f)
		val CYAN get() = Color4f(0f, 1f, 1f, 1f)
		val BLUE get() = Color4f(0f, 0f, 1f, 1f)
		val MAGENTA get() = Color4f(1f, 0f, 1f, 1f)
	}

	override val rgba: Int
		get() = (alpha * 255 + 0.5).toInt() and 0xFF shl 24 or
				((red * 255 + 0.5).toInt() and 0xFF shl 16) or
				((green * 255 + 0.5).toInt() and 0xFF shl 8) or
				((blue * 255 + 0.5).toInt() and 0xFF shl 0)

	override fun rgba(alpha: Float): Int {
		return (alpha * 255 + 0.5).toInt() and 0xFF shl 24 or
				((red * 255 + 0.5).toInt() and 0xFF shl 16) or
				((green * 255 + 0.5).toInt() and 0xFF shl 8) or
				((blue * 255 + 0.5).toInt() and 0xFF shl 0)
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
		this.alpha = (color shr 24 and 0xFF).toFloat() / 255f
		this.red = (color shr 16 and 0xFF).toFloat() / 255f
		this.green = (color shr 8 and 0xFF).toFloat() / 255f
		this.blue = (color shr 0 and 0xFF).toFloat() / 255f
		fixAllValue()
		return this
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (other is IColor<out Number>) return other.rgba == this.rgba
		return false
	}

	override fun hashCode(): Int {
		return rgba
	}

	override val minValue: Float
		get() = 0f
	override val maxValue: Float
		get() = 1f


}