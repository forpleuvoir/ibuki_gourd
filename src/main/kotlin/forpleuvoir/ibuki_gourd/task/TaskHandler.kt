package forpleuvoir.ibuki_gourd.task

import forpleuvoir.ibuki_gourd.event.EventBus
import forpleuvoir.ibuki_gourd.event.events.ClientEndTickEvent
import forpleuvoir.ibuki_gourd.event.events.ClientStartTickEvent
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * 任务处理程序

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.task

 * 文件名 TaskHandler

 * 创建时间 2021/12/9 13:58

 * @author forpleuvoir

 */
object TaskHandler {
	private val startTickTask: Queue<ITask> = ConcurrentLinkedQueue()
	private val endTickTask: Queue<ITask> = ConcurrentLinkedQueue()

	init {
		EventBus.subscribe<ClientStartTickEvent> {
			iterateRun(startTickTask.iterator())
		}
		EventBus.subscribe<ClientEndTickEvent> {
			iterateRun(endTickTask.iterator())
		}
	}

	@JvmStatic
	fun addStartTickTask(task: ITask) {
		startTickTask.add(task)
	}

	fun addStartTickTask(task: () -> Unit) {
		addStartTickTask(object : ITask {
			override fun execute() {
				task.invoke()
			}
		})
	}

	@JvmStatic
	fun addEndTickTask(task: ITask) {
		startTickTask.add(task)
	}

	fun addEndTickTask(task: () -> Unit) {
		addEndTickTask(object : ITask {
			override fun execute() {
				task.invoke()
			}
		})
	}

	private fun iterateRun(iterator: MutableIterator<ITask>) {
		while (iterator.hasNext()) {
			val next = iterator.next()
			next.execute()
			iterator.remove()
		}
	}
}