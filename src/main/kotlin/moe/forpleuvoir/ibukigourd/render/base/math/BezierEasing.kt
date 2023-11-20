package moe.forpleuvoir.ibukigourd.render.base.math

import moe.forpleuvoir.ibukigourd.util.decimalPlaces
import kotlin.math.max
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

fun bezierEasing(
    time: Float,
    start: Float,
    startControlPointTime: Float,
    startControlPointValue: Float,
    end: Float,
    endControlPointTime: Float,
    endControlPointValue: Float
): Float {
    val decimalPlaces = 10.0.pow(max(startControlPointTime.decimalPlaces, endControlPointTime.decimalPlaces)).toInt()
    val gcd = if (startControlPointTime == endControlPointTime) {
        gcd((startControlPointTime * decimalPlaces).toInt(), 1 * decimalPlaces).toFloat() / decimalPlaces
    } else {
        gcd((startControlPointTime * decimalPlaces).toInt(), (endControlPointTime * decimalPlaces).toInt()).toFloat() / decimalPlaces
    }
    val controlPoints = mutableListOf<Float>()
    val total = end - start
    val size = (1.0f / gcd).toInt()

    val startIndex = (startControlPointTime / gcd).toInt()
    val endIndex = (endControlPointTime / gcd).toInt()
    val s = startControlPointValue - start / startIndex
    val m = endControlPointValue - startControlPointValue / (startIndex - endIndex)
    val e = end - endControlPointValue / (size - endIndex)
    repeat(size) {
        if (it <= startIndex) {
            controlPoints.add(s * it + start)
        } else if (it in startIndex + 1..endIndex) {
            controlPoints.add(m * it + start)
        } else {
            controlPoints.add(e * it + start)
        }
    }
    if ((1.0f % gcd) != 0f) {
        controlPoints.add(end)
    }
    return nBezier(time, *controlPoints.toFloatArray())
}

fun gcd(a: Int, b: Int): Int {
    var num1 = a
    var num2 = b

    while (num2 != 0) {
        val temp = num2
        num2 = num1 % num2
        num1 = temp
    }
    return num1
}
