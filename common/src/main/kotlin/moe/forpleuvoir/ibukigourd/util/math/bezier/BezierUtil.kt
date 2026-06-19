package moe.forpleuvoir.ibukigourd.util.math.bezier

import org.joml.Vector2f
import org.joml.Vector3f

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
            val u = 1f - t

            val x = u * u * u * start.x +
                    3 * u * u * t * startControl.x +
                    3 * u * t * t * endControl.x +
                    t * t * t * end.x

            val y = u * u * u * start.y +
                    3 * u * u * t * startControl.y +
                    3 * u * t * t * endControl.y +
                    t * t * t * end.y
            Vector2f(x, y)
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
            val u = 1f - t

            // 三次贝塞尔曲线公式：B(t) = (1-t)³*P0 + 3(1-t)²*t*P1 + 3(1-t)*t²*P2 + t³*P3
            val x = u * u * u * start.x +
                    3 * u * u * t * startControl.x +
                    3 * u * t * t * endControl.x +
                    t * t * t * end.x

            val y = u * u * u * start.y +
                    3 * u * u * t * startControl.y +
                    3 * u * t * t * endControl.y +
                    t * t * t * end.y

            val z = u * u * u * start.z +
                    3 * u * u * t * startControl.z +
                    3 * u * t * t * endControl.z +
                    t * t * t * end.z

            Vector3f(x, y, z)
        }
    }

}