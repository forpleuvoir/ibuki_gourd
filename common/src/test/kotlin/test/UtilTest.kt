package test

import moe.forpleuvoir.ibukigourd.util.page
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

class UtilTest {

    @Test
    fun test() {
        val (data,duration) = measureTimedValue {
            buildList {
                repeat(5000000) {
                    add(Random.nextInt(1500))
                }
            }.asSequence()
        }
        println(duration)

        // 预热
        repeat(50) {
            data.page(Random.nextInt(20) + 1, 500000).forEach{
                it
            }
        }

        // 多次测量
        val times = mutableListOf<Duration>()
        repeat(10000) {
            val duration = measureTime {
                data.page(Random.nextInt(20) + 1, 500000).forEach{
                    it
                }
            }
            times.add(duration)
        }


        // 计算平均值
        val averageTime = times.sumOf { it.inWholeNanoseconds } / times.size
        println()
        println("Average time: ${averageTime.nanoseconds}")
    }


    @Test
    fun test2() {
        val data =(1..100).asSequence()
        data.page(1,10).forEach {
            println(it)
        }

    }

}
