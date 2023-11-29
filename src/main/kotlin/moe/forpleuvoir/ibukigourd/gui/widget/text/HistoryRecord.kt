package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.nebula.common.ternary

class HistoryRecord(val maxStackSize: Int = 50, var currentRecord: Record) : Tickable {

    data class Record(val text: String, val cursor: Int)

    /**
     * 撤回列表
     */
    private val undoStack: ArrayDeque<Record> = ArrayDeque()

    /**
     * 重做列表
     */
    private val redoStack: ArrayDeque<Record> = ArrayDeque()

    val undoes: Iterable<Record> get() = undoStack

    val redoes: Iterable<Record> get() = redoStack

    val lastUndo: String? get() = if (undoStack.isNotEmpty()) undoStack.last().text else null

    val lastRedo: String? get() = if (redoStack.isNotEmpty()) redoStack.last().text else null

    /**
     * 单次输入时间 单位 tick  1s = 20tick
     *
     * 每次输入都会重新计时，直到连续[inputTime]tick未输入则认为输入结束
     *
     */
    private val inputTime: Int = 10

    private var tickCount: Int = 0

    /**
     * 当前输入中的纪录
     * 为空则当前没有在输入中
     */
    private var inputtingRecord: Record? = null

    /**
     * 是否处于输入中状态
     */
    private var inputting: Boolean
        set(value) {
            tickCount = value.ternary(inputTime, 0)
        }
        get() = tickCount > 0

    /**
     * 当文本发生变化时首先检测是否为输入状态，每次输入开始时[inputting] == false
     *
     * 先把当前的文本push进撤回列表
     *
     * 设置未开始输入状态[inputting] = true
     *
     * 设置当前输入记录[inputtingRecord] = [Record]
     *
     * 如果[inputTime]时间内持续输入则持续更新当前的纪录[currentRecord]
     *
     * 当持续时间结束则把[inputtingRecord]设置为空 null
     *
     * @param text String
     * @param cursor Int
     */
    fun textChange(text: String, cursor: Int) {
        if (!inputting) pushUndo(currentRecord)
        inputting = true
        inputtingRecord = Record(text, cursor)
    }

    override fun tick() {
        if (!inputting) {
            if (inputtingRecord != null) inputtingRecord = null
        } else {
            currentRecord = inputtingRecord!!
        }
        if (tickCount > 0) {
            tickCount--
        }
    }

    fun pushUndo(text: String, cursor: Int) {
        if (undoStack.size >= maxStackSize)
            undoStack.removeFirst()
        undoStack.addLast(Record(text, cursor))
    }

    fun pushUndo(record: Record) {
        if (undoStack.size >= maxStackSize)
            undoStack.removeFirst()
        undoStack.addLast(record)
    }

    fun pushRedo(text: String, cursor: Int) {
        if (redoStack.size >= maxStackSize)
            redoStack.removeFirst()
        redoStack.addLast(Record(text, cursor))
    }

    fun pushRedo(record: Record) {
        if (redoStack.size >= maxStackSize)
            redoStack.removeFirst()
        redoStack.addLast(record)
    }

    fun undo(text: String, cursor: Int): Record {
        var result = Record(text, cursor)
        if (undoStack.isNotEmpty()) {
            pushRedo(text, cursor)
            result = undoStack.removeLast()
        }
        return result
    }

    fun redo(text: String, cursor: Int): Record {
        var result = Record(text, cursor)
        if (redoStack.isNotEmpty()) {
            pushUndo(text, cursor)
            result = redoStack.removeLast()
        }
        return result
    }

}