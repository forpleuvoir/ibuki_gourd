package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.common.IJsonData
import net.minecraft.util.math.MathHelper
import kotlin.math.max
import kotlin.math.min


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils.color

 * 文件名 Color

 * 创建时间 2021/12/13 18:55

 * @author forpleuvoir

 */
interface IColor<T : Number> : IJsonData {
	val rgba: Int
	val hexString: String
		get() = "0x${rgba.toString(16).uppercase()}"

	fun rgba(alpha: T): Int
	fun fromInt(color: Int): IColor<T>
	fun alpha(alpha: T): IColor<T> {
		return fromInt(this.rgba(alpha))
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

	fun fixAllValue() {
		red = fixValue(red)
		green = fixValue(green)
		blue = fixValue(blue)
		alpha = fixValue(alpha)
	}

	@Suppress("unchecked_cast")
	fun fixValue(value: T): T {
		return MathHelper.clamp(value.toDouble(), minValue.toDouble(), maxValue.toDouble()) as T
	}

	val minValue: T
	val maxValue: T

	var red: T
	var green: T
	var blue: T
	var alpha: T

}