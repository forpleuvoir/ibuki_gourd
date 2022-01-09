package forpleuvoir.ibuki_gourd.utils.color

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.common.IJsonData
import forpleuvoir.ibuki_gourd.utils.clamp
import net.minecraft.util.math.MathHelper
import java.awt.Color.red
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
	companion object {
		fun copy(color: IColor<out Number>): IColor<out Number> {
			return if (color.red is Int) {
				Color4i(color.red as Int, color.green as Int, color.blue as Int, color.alpha as Int)
			} else {
				Color4f(color.red as Float, color.green as Float, color.blue as Float, color.alpha as Float)
			}
		}
	}

	val rgba: Int
	val hexString: String
		get() = "#${String.format("%x", hashCode()).uppercase()}"

	fun rgba(alpha: T): Int
	fun fromInt(color: Int): IColor<T>
	fun alpha(alpha: T): IColor<T> {
		return fromInt(this.rgba(alpha))
	}
	fun opacity(opacity: Double):IColor<T>

	override val asJsonElement: JsonElement
		get() {
			val jsonObject = JsonObject()
			jsonObject.addProperty("red", red)
			jsonObject.addProperty("green", green)
			jsonObject.addProperty("blue", blue)
			jsonObject.addProperty("alpha", alpha)
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
		return (value as Number).clamp(minValue as Number, maxValue as Number) as T
	}

	val minValue: T
	val maxValue: T

	var red: T
	var green: T
	var blue: T
	var alpha: T

}