package moe.forpleuvoir.ibukigourd.util.math.bezier

import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.abs

object BezierUtil {

    /**
     * 生成二维三次贝塞尔曲线的插值点序列
     *
     * 三次贝塞尔曲线由四个点定义：起点、两个控制点、终点。
     * 曲线从起点开始，受到两个控制点的影响，最终到达终点。
     * 控制点决定了曲线的弯曲方向和程度，但曲线本身不会穿过控制点。
     *
     * @param start 起点（P0），曲线的起始位置
     * @param startControl 起始控制点（P1），控制曲线离开起点时的方向和弯曲程度
     * @param end 终点（P3），曲线的结束位置
     * @param endControl 结束控制点（P2），控制曲线进入终点时的方向和弯曲程度
     * @param precision 生成点的数量，控制曲线的平滑度（至少为2）
     * @return 包含从起点到终点均匀采样的插值点列表，类型为 Vector2f
     *
     * 使用示例：
     * ```
     * val start = Vector2f(0f, 0f)
     * val startControl = Vector2f(1f, 2f)
     * val end = Vector2f(4f, 0f)
     * val endControl = Vector2f(3f, 2f)
     * val points = get2DBezierPoints(start, startControl, end, endControl, 20)
     * ```
     */
    fun get2DBezierPoints(
        start: Vector2f,
        startControl: Vector2f,
        end: Vector2f,
        endControl: Vector2f,
        precision: Int
    ): List<Vector2f> {
        val n = precision.coerceAtLeast(2)
        val step = 1f / (n - 1)

        return (0 until n).map { i ->
            val t = i * step
            Vector2f(
                bezierValue(t, start.x, startControl.x, endControl.x, end.x),
                bezierValue(t, start.y, startControl.y, endControl.y, end.y)
            )
        }
    }


    /**
     * 生成三维三次贝塞尔曲线的插值点序列
     *
     * 三次贝塞尔曲线由四个点定义：起点、两个控制点、终点。
     * 曲线从起点开始，受到两个控制点的影响，最终到达终点。
     * 控制点决定了曲线的弯曲方向和程度，但曲线本身不会穿过控制点。
     *
     * @param start 起点（P0），曲线的起始位置
     * @param startControl 起始控制点（P1），控制曲线离开起点时的方向和弯曲程度
     * @param end 终点（P3），曲线的结束位置
     * @param endControl 结束控制点（P2），控制曲线进入终点时的方向和弯曲程度
     * @param precision 生成点的数量，控制曲线的平滑度（至少为2）
     * @return 包含从起点到终点均匀采样的插值点列表，类型为 Vector3f
     *
     * 使用示例：
     * ```
     * val start = Vector3f(0f, 0f, 0f)
     * val startControl = Vector3f(1f, 2f, 0f)
     * val endControl = Vector3f(3f, 2f, 1f)
     * val end = Vector3f(4f, 0f, 1f)
     * val points = get3DBezierPoints(start, startControl, endControl, end, 20)
     * ```
     */
    fun get3DBezierPoints(
        start: Vector3f,
        startControl: Vector3f,
        end: Vector3f,
        endControl: Vector3f,
        precision: Int
    ): List<Vector3f> {
        val n = precision.coerceAtLeast(2)
        val step = 1f / (n - 1)
        return (0 until n).map { i ->
            val t = i * step
            // 三次贝塞尔曲线公式：B(t) = (1-t)³*P0 + 3(1-t)²*t*P1 + 3(1-t)*t²*P2 + t³*P3
            Vector3f(
                bezierValue(t, start.x, startControl.x, endControl.x, end.x),
                bezierValue(t, start.y, startControl.y, endControl.y, end.y),
                bezierValue(t, start.z, startControl.z, endControl.z, end.z)
            )
        }
    }

    /**
     * 三次贝塞尔缓动曲线(类似 CSS `cubic-bezier(x1, y1, x2, y2)`)
     *
     * 起点固定为 (0, 0)、终点固定为 (1, 1),由两个控制点决定曲线形状。
     * 给定时间进度 [t](对应曲线上的 x 坐标),返回对应的动画进度(曲线上的 y 坐标)。
     *
     * 由于三次贝塞尔的 x、y 都是参数 s 的三次多项式,无法直接由 x 求出 y,
     * 这里先用牛顿迭代法求解使 X(s) = [t] 的参数 s,若未收敛则回退到二分法,
     * 再将得到的 s 代入 Y(s) 得到缓动值。
     *
     * 为保证 x(s) 单调(从而每个 x 唯一对应一个 y),建议两个控制点的 x 分量
     * 都落在 [0, 1] 范围内;超出 [0, 1] 范围的 [t] 会被夹取到端点值。
     *
     * @param p1 起始控制点(P1),控制曲线离开起点 (0,0) 时的方向与弯曲程度
     * @param p2 结束控制点(P2),控制曲线进入终点 (1,1) 时的方向与弯曲程度
     * @param t 时间进度,通常在 0f..1f 范围内,表示曲线上的 x 坐标
     * @return 与 [t] 对应的动画进度值(曲线上的 y 坐标)
     *
     * 使用示例：
     * ```
     * // 等价于 CSS cubic-bezier(0.42, 0, 0.58, 1) 的 ease-in-out
     * val progress = cubicBezierEasing(Vector2f(0.42f, 0f), Vector2f(0.58f, 1f), 0.5f)
     * ```
     */
    fun cubicBezierEasing(p1: Vector2f, p2: Vector2f, t: Float): Float {
        if (t <= 0f) return 0f
        if (t >= 1f) return 1f

        val x1 = p1.x
        val x2 = p2.x

        // 牛顿迭代法求解 s,使 X(s) = t
        var s = t
        for (i in 0 until 8) {
            val xVal = bezierValue(s, 0f, x1, x2, 1f) - t
            if (abs(xVal) < 1e-5f) return bezierValue(s, 0f, p1.y, p2.y, 1f)
            val dX = bezierDerivative(s, 0f, x1, x2, 1f)
            if (abs(dX) < 1e-6f) break
            val next = s - xVal / dX
            if (!next.isFinite() || next < 0f || next > 1f) break
            s = next
        }

        // 牛顿法未收敛,回退到二分法
        var lo = 0f
        var hi = 1f
        s = t
        for (i in 0 until 24) {
            val xVal = bezierValue(s, 0f, x1, x2, 1f)
            if (abs(xVal - t) < 1e-5f) break
            if (xVal < t) lo = s else hi = s
            s = (lo + hi) * 0.5f
        }
        return bezierValue(s, 0f, p1.y, p2.y, 1f)
    }

    private fun bezierValue(s: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val u = 1f - s
        return u * u * u * p0 +
                3 * u * u * s * p1 +
                3 * u * s * s * p2 +
                s * s * s * p3
    }

    private fun bezierDerivative(s: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val u = 1f - s
        return 3 * u * u * (p1 - p0) +
                6 * u * s * (p2 - p1) +
                3 * s * s * (p3 - p2)
    }

}