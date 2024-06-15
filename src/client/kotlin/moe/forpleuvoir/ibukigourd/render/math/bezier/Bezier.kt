package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.pow

/**
 * 贝塞尔曲线缓动函数
 * @param time Float 时间  0f..1.0f
 * @param controlPoints FloatArray 控制点
 * @return Float 缓动后的值
 */
fun nBezier(time: Float, vararg controlPoints: Float): Float {
    fun binomialCoefficient(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        if (k == 0 || k == n) return 1

        // 使用递推关系计算二项式系数
        var result = 1
        var j = 1

        while (j <= k) {
            result = result * (n - j + 1) / j
            j++
        }

        return result
    }

    val n = controlPoints.size - 1
    var result = 0.0f

    // 计算每个控制点的贡献
    for (i in 0..n) {
        // 二项式系数 * (1 - t)^(n-i) * t^i * 控制点的值
        result += binomialCoefficient(n, i) * (1 - time).pow(n - i) * time.pow(i) * controlPoints[i]
    }

    return result
}
