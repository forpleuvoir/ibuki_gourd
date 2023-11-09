package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.api.Tickable

class HistoryRecord(var maxRecord: Int = 50, var currentRecord: Record) : Tickable {

    data class Record(val text: String, val cursor: Int)

    /**
     * 撤回列表
     */
    private val undoStack: ArrayDeque<Record> = ArrayDeque()

    /**
     * 重做列表
     */
    private val redoStack: ArrayDeque<Record> = ArrayDeque()

    private val pushDelay: Int = 10

    private var tickCount: Int = 0

    val undoes: Iterable<Record> get() = undoStack

    val redoes: Iterable<Record> get() = redoStack

    val lastUndo: String? get() = if (undoStack.isNotEmpty()) undoStack.last().text else null

    val lastRedo: String? get() = if (redoStack.isNotEmpty()) redoStack.last().text else null

    fun textChange(text: String, cursor: Int) {
        val bl = tickCount == 0
        if (bl) tickCount = pushDelay

        //收到之后立马保存一个，如果还未到时间则持续更新最后的undo
        TODO()

        if (!bl) tickCount = pushDelay
    }

    fun pushUndo(text: String, cursor: Int) {
        if (undoStack.size >= maxRecord)
            undoStack.removeFirst()
        undoStack.addLast(Record(text, cursor))
    }

    fun pushUndo(record: Record) {
        if (undoStack.size >= maxRecord)
            undoStack.removeFirst()
        undoStack.addLast(record)
    }

    fun pushRedo(text: String, cursor: Int) {
        if (redoStack.size >= maxRecord)
            redoStack.removeFirst()
        redoStack.addLast(Record(text, cursor))
    }

    fun pushRedo(record: Record) {
        if (redoStack.size >= maxRecord)
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

    override fun tick() {
        if (tickCount > 0) {
            tickCount--
        }
    }
}