package moe.forpleuvoir.ibukigourd.util.math.easing

import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import org.joml.Vector2f

/**
 * 三次贝塞尔缓动曲线的**数值表示**：起点固定 `(0, 0)`、终点固定 `(1, 1)`，
 * 两个控制点 `(x1, y1)` / `(x2, y2)` 决定形状（等价于 CSS `cubic-bezier(x1, y1, x2, y2)`）。
 *
 * 与 [BezierEasing] 的分工：本类型只承载数值（可比较、可序列化、可进配置文件），
 * 求值交给 [toEasing] 得到的 [BezierEasing]（[Easing.easeIn] 即本条曲线本身，
 * `easeOut` / `easeInOut` 由它按缓动函数的通行约定派生）。
 *
 * x 分量的通行约定是落在 `0f..1f`：x(s) 单调时每个时间值才有唯一的曲线点；
 * y 分量不受限制，允许越界以表达回弹 / 过冲（如 `y1 = -0.2f`）。
 */
data class CubicBezier(
    /** 起始控制点 P1 的 x（约定 `0f..1f`）。 */
    val x1: Float,
    /** 起始控制点 P1 的 y（可越界）。 */
    val y1: Float,
    /** 结束控制点 P2 的 x（约定 `0f..1f`）。 */
    val x2: Float,
    /** 结束控制点 P2 的 y（可越界）。 */
    val y2: Float,
) {

    /** 对应的缓动实现。 */
    fun toEasing(): Easing = BezierEasing(Vector2f(x1, y1), Vector2f(x2, y2))

    companion object : Codec<CubicBezier> {

        /** 平台缺省曲线（compose-minecraft 的屏幕 / 对话框动画默认值 `CubicBezierEasing(0f, 0f, 0.2f, 1f)`）。 */
        val Standard = CubicBezier(0f, 0f, 0.2f, 1f)

        /** 匀速（等价于 CSS `cubic-bezier(0, 0, 1, 1)`）。 */
        val Linear = CubicBezier(0f, 0f, 1f, 1f)

        /** 慢起（CSS `ease-in`）。 */
        val EaseIn = CubicBezier(0.42f, 0f, 1f, 1f)

        /** 缓停（CSS `ease-out`）。 */
        val EaseOut = CubicBezier(0f, 0f, 0.58f, 1f)

        /** 两头缓（CSS `ease-in-out`）。 */
        val EaseInOut = CubicBezier(0.42f, 0f, 0.58f, 1f)

        /**
         * 字段依次为 `x1 / y1 / x2 / y2`（缺省取 [Standard]）。
         * 两个控制点的 x 由曲线定义限在 `0f..1f`，y 不限（允许回弹 / 过冲）。
         */
        private val codec = Codec.create<CubicBezier>()
            .field(CubicBezier::x1).default(Standard.x1).codec(Codec.float(0f..1f))
            .field(CubicBezier::y1).default(Standard.y1).codec(Codec.float)
            .field(CubicBezier::x2).default(Standard.x2).codec(Codec.float(0f..1f))
            .field(CubicBezier::y2).default(Standard.y2).codec(Codec.float)
            .build(::CubicBezier)

        override fun serialization(target: CubicBezier): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<CubicBezier> = codec.deserialization(data)
    }
}
