package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement


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

	constructor(color: IColor<out Number>) : this() {
		fromInt(color.rgba)
	}

	override val hexString: String
		get() = "#${red.toString(16).run { if (this == "0") return@run "00" else this }.uppercase()}" +
				green.toString(16).run { if (this == "0") return@run "00" else this }.uppercase() +
				blue.toString(16).run { if (this == "0") return@run "00" else this }.uppercase() +
				alpha.toString(16).run { if (this == "0") return@run "00" else this }.uppercase()

	init {
		fixAllValue()
	}

	override val rgba: Int
		get() = alpha and 0xFF shl 24 or
				(red and 0xFF shl 16) or
				(green and 0xFF shl 8) or
				(blue and 0xFF shl 0)

	override fun rgba(alpha: Int): Int {
		return alpha and 0xFF shl 24 or
				(red and 0xFF shl 16) or
				(green and 0xFF shl 8) or
				(blue and 0xFF shl 0)
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

	override fun opacity(opacity: Double): Color4i {
		return Color4i().fromInt(rgba).apply { this.alpha = (this.alpha * opacity).toInt() }
	}

	companion object {
		val WHITE get() = Color4i(255, 255, 255, 255)
		val RED get() = Color4i(255, 0, 0, 255)
		val YELLOW get() = Color4i(127, 127, 0, 255)
		val GREEN get() = Color4i(0, 255, 0, 255)
		val CYAN get() = Color4i(0, 127, 127, 255)
		val BLUE get() = Color4i(0, 0, 255, 255)
		val MAGENTA get() = Color4i(127, 0, 127, 255)
		val BLACK get() = Color4i(0, 0, 0, 255)
	}

	override fun fromInt(color: Int): Color4i {
		this.alpha = color shr 24 and 0xFF
		this.red = color shr 16 and 0xFF
		this.green = color shr 8 and 0xFF
		this.blue = color shr 0 and 0xFF
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

	override val minValue: Int
		get() = 0
	override val maxValue: Int
		get() = 255

}


