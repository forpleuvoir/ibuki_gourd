package moe.forpleuvoir.ibukigourd.util.math.easing

import moe.forpleuvoir.ibukigourd.util.math.bezier.BezierUtil
import org.joml.Vector2f

/**
 * 三次贝塞尔缓动(类似 CSS `cubic-bezier(x1, y1, x2, y2)`)
 *
 * 起点固定为 (0, 0)、终点固定为 (1, 1),由构造传入的两个控制点决定曲线形状。
 * [easeIn] 直接使用这两个控制点求值;[easeOut] 与 [easeInOut] 按缓动函数的
 * 标准约定由 [easeIn] 推导(与 [QuadEasing] 等一致),从而从一条基曲线派生出一族三种变体。
 *
 * 若需要精确复现某个 CSS `cubic-bezier(...)` 曲线,直接调用 [easeIn] 即可。
 *
 * 为保证 x(s) 单调(每个 x 唯一对应一个 y),建议两个控制点的 x 分量都落在 [0, 1] 范围内。
 *
 * @param p1 起始控制点(P1),控制曲线离开起点 (0,0) 时的方向与弯曲程度
 * @param p2 结束控制点(P2),控制曲线进入终点 (1,1) 时的方向与弯曲程度
 *
 * 使用示例：
 * ```
 * // 等价于 CSS cubic-bezier(0.42, 0, 0.58, 1) 的 ease-in-out
 * val easing = BezierEasing(Vector2f(0.42f, 0f), Vector2f(0.58f, 1f))
 * val progress = easing.easeIn(0.5f)
 * ```
 */
class BezierEasing(
    private val p1: Vector2f,
    private val p2: Vector2f
) : Easing {

    override fun easeIn(t: Float): Float = BezierUtil.cubicBezierEasing(p1, p2, t)

    override fun easeOut(t: Float): Float = 1f - BezierUtil.cubicBezierEasing(p1, p2, 1f - t)

    override fun easeInOut(t: Float): Float =
        if (t < 0.5f) 0.5f * easeIn(2f * t)
        else 0.5f + 0.5f * easeOut(2f * t - 1f)

}
