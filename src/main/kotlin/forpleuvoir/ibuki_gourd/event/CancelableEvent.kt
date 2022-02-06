package forpleuvoir.ibuki_gourd.event

/**
 * 可取消事件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 CancelableEvent

 * 创建时间 2022/2/7 6:13

 * @author forpleuvoir

 */
open class CancelableEvent : Event {
	override fun cancelable(): Boolean = true

	override var isCanceled: Boolean = false

	override fun cancel() {
		isCanceled = true
	}
}