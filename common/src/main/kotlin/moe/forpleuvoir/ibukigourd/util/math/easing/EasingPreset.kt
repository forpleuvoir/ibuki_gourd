package moe.forpleuvoir.ibukigourd.util.math.easing

import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 配置里的缓动选择：几个内置效果 + [Custom]。
 *
 * 内置的贝塞尔项直接复用 [CubicBezier] 的常量，与曲线编辑器的预设行是同一套曲线；
 * 带 `Out` 的三项取自 [EasingCurve] 的实现（回弹类曲线不是三次贝塞尔，无法用四点表达）。
 * [Custom] 时取外部传入的四点（见 [resolve]）。
 */
enum class EasingPreset(val mode: String) {
    Standard("standard"),
    Linear("linear"),
    EaseIn("ease_in"),
    EaseOut("ease_out"),
    EaseInOut("ease_in_out"),
    BackOut("back_out"),
    BounceOut("bounce_out"),
    ElasticOut("elastic_out"),
    Custom("custom");

    companion object : Codec<EasingPreset> {

        override fun serialization(target: EasingPreset): SerializeElement = SerializePrimitive(target.mode)

        override fun deserialization(data: SerializeElement): Result<EasingPreset> =
            DeserializationException.runCatching {
                val value = data.asString
                entries.firstOrNull { it.mode == value }
                    ?: error("Invalid EasingPreset value: expected ${entries.joinToString("/") { it.mode }}, actual '$value'")
            }
    }
}

/**
 * 把预设解析成缓动函数（输入输出均为 `0f..1f`）。
 *
 * @param custom [EasingPreset.Custom] 时使用的自定义曲线；其余预设忽略该参数
 */
fun EasingPreset.resolve(custom: CubicBezier): Ease = when (this) {
    EasingPreset.Standard -> CubicBezier.Standard.toEasing()::easeIn
    EasingPreset.Linear -> CubicBezier.Linear.toEasing()::easeIn
    EasingPreset.EaseIn -> CubicBezier.EaseIn.toEasing()::easeIn
    EasingPreset.EaseOut -> CubicBezier.EaseOut.toEasing()::easeIn
    EasingPreset.EaseInOut -> CubicBezier.EaseInOut.toEasing()::easeIn
    EasingPreset.BackOut -> EasingCurve.Back.ease(EasingDirection.Out)
    EasingPreset.BounceOut -> EasingCurve.Bounce.ease(EasingDirection.Out)
    EasingPreset.ElasticOut -> EasingCurve.Elastic.ease(EasingDirection.Out)
    EasingPreset.Custom -> custom.toEasing()::easeIn
}
