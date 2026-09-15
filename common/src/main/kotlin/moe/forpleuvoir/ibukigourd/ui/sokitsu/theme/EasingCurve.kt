package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import moe.forpleuvoir.ibukigourd.util.math.easing.BackEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.BounceEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.CircEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.Ease
import moe.forpleuvoir.ibukigourd.util.math.easing.Easing
import moe.forpleuvoir.ibukigourd.util.math.easing.ElasticEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.ExpoEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.QuadEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.QuartEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.QuintEasing
import moe.forpleuvoir.ibukigourd.util.math.easing.SineEasing
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 动画缓动曲线名 —— 组件 meta 里描述"用哪条曲线"的那一半，另一半是 [EasingDirection]。
 *
 * 名字与实现直接取自项目已有的 `moe.forpleuvoir.ibukigourd.util.math.easing`
 * （[moe.forpleuvoir.ibukigourd.util.math.easing.Easing] 的各个 `object` 实现），
 * 不另造曲线；[mode] 是 meta 里的字面量（小写，手写 [Codec] 而非 `Codec.enum`，
 * 与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.CenterFill] 的既有风格一致）。
 */
enum class EasingCurve(val mode: String) {
    Linear("linear"),
    Sine("sine"),
    Quad("quad"),
    Cubic("cubic"),
    Quart("quart"),
    Quint("quint"),
    Expo("expo"),
    Circ("circ"),
    Back("back"),
    Bounce("bounce"),
    Elastic("elastic");

    /** 对应的缓动实现。 */
    val easing: Easing
        get() = when (this) {
            Linear  -> Easing.LINEAR
            Sine    -> SineEasing
            Quad    -> QuadEasing
            Cubic   -> CubicEasing
            Quart   -> QuartEasing
            Quint   -> QuintEasing
            Expo    -> ExpoEasing
            Circ    -> CircEasing
            Back    -> BackEasing
            Bounce  -> BounceEasing
            Elastic -> ElasticEasing
        }

    companion object : Codec<EasingCurve> {

        override fun serialization(target: EasingCurve): SerializeElement = SerializePrimitive(target.mode)

        override fun deserialization(data: SerializeElement): Result<EasingCurve> =
            DeserializationException.runCatching {
                val value = data.asString
                entries.firstOrNull { it.mode == value }
                    ?: error("Invalid EasingCurve value: expected ${entries.joinToString("/") { it.mode }}, actual '$value'")
            }
    }
}

/**
 * 缓动方向 —— [EasingCurve] 的三个求值端，对应
 * [moe.forpleuvoir.ibukigourd.util.math.easing.Easing.easeIn] /
 * [moe.forpleuvoir.ibukigourd.util.math.easing.Easing.easeOut] /
 * [moe.forpleuvoir.ibukigourd.util.math.easing.Easing.easeInOut]。
 *
 * 与曲线名合起来才是完整的一条缓动：`"cubic"` + `"in"` 即 `easeIn(t) = t³`。
 */
enum class EasingDirection(val mode: String) {
    In("in"),
    Out("out"),
    InOut("in_out");

    companion object : Codec<EasingDirection> {

        override fun serialization(target: EasingDirection): SerializeElement = SerializePrimitive(target.mode)

        override fun deserialization(data: SerializeElement): Result<EasingDirection> =
            DeserializationException.runCatching {
                val value = data.asString
                entries.firstOrNull { it.mode == value }
                    ?: error("Invalid EasingDirection value: expected ${entries.joinToString("/") { it.mode }}, actual '$value'")
            }
    }
}

/**
 * 把「曲线 + 方向」解析成缓动函数（输入输出均为 0..1）。
 */
fun EasingCurve.ease(direction: EasingDirection): Ease = when (direction) {
    EasingDirection.In    -> easing::easeIn
    EasingDirection.Out   -> easing::easeOut
    EasingDirection.InOut -> easing::easeInOut
}
