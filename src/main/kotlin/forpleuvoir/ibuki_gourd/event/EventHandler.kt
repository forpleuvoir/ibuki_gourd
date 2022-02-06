package forpleuvoir.ibuki_gourd.event

import forpleuvoir.ibuki_gourd.utils.ReflectionUtil.isExtended
import java.lang.reflect.Method
import java.util.*


/**
 * 事件处理程序

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 EvnetHandler

 * 创建时间 2021/12/9 12:21

 * @author forpleuvoir

 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler

@Suppress("UNCHECKED_CAST")
fun Subscriber.subscribe(eventBus: EventBus) {
	val methods = this.getMethods()
	for (method in methods) {
		val parameter = method.parameters[0]
		val type = parameter.type as Class<out Event?>
		eventBus.subscribe(type) { event: Event? ->
			try {
				method.invoke(this, event)
			} catch (_: Exception) {
			}
		}
	}
}

private fun Subscriber.getMethods(): List<Method> {
	val methods = LinkedList<Method>()
	for (method in this.javaClass.methods) {
		method.isAccessible = true
		if (method.returnType == Void.TYPE) {
			if (method.getAnnotation(EventHandler::class.java) != null) {
				if (method.parameterCount == 1) {
					val parameter = method.parameters[0]
					if (parameter.type.isExtended(Event::class.java)) {
						methods.add(method)
					}
				}
			}
		}
	}
	return methods
}