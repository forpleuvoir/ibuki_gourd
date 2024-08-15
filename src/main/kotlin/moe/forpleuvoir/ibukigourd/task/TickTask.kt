package moe.forpleuvoir.ibukigourd.task


data class TickTask<T>(
    val delay: Int = 0,
    val period: Int = 1,
    val times: Int = 1,
    val action: (T) -> Unit
) {

    constructor(task: TickTask<T>) : this(task.delay, task.period, task.times, task.action)

    init {
        check(delay >= 0) { "delay must be >=0" }
        check(period >= 1) { "period must be >=1" }
        check(times >= 1) { "period must be >=1" }
    }

    var counter: Int = 0
        private set(value) {
            field = value.coerceIn(0, times)
        }

    private var tickCounter: Int = 0

    private val shouldExecute: Boolean
        get() {
            tickCounter++
            return if (counter == 0) {
                tickCounter >= delay
            } else {
                tickCounter >= period
            }
        }

    val isOver: Boolean get() = counter >= times

    fun tryExecute(context: T) {
        if (isOver || !shouldExecute) return
        action(context)
        counter++
        tickCounter = 0
    }

}