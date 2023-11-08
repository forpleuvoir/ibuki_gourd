package moe.forpleuvoir.ibukigourd.gui.widget.text

class HistoryRecord(var maxRecord: Int = 50) {

	/**
	 * 撤回列表
	 */
	private val undoList: ArrayDeque<String> = ArrayDeque()

	/**
	 * 重做列表
	 */
	private val redoList: ArrayDeque<String> = ArrayDeque()

	val lastUndo: String? get() = if (undoList.isNotEmpty()) undoList.last() else null

	val lastRedo: String? get() = if (redoList.isNotEmpty()) redoList.last() else null

	fun pushUndo(text: String) {
		if (undoList.size >= maxRecord)
			undoList.removeFirst()
		undoList.addLast(text)
	}

	fun pushRedo(text: String) {
		if (redoList.size >= maxRecord)
			redoList.removeFirst()
		redoList.addLast(text)
	}

	fun undo(text: String): String {
		var result = text
		if (undoList.isNotEmpty()) {
			pushRedo(text)
			result = undoList.removeLast()
		}
		return result
	}

	fun redo(text: String): String {
		var result = text
		if (redoList.isNotEmpty()) {
			pushUndo(text)
			result = redoList.removeLast()
		}
		return result
	}
}