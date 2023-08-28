package moe.forpleuvoir.ibukigourd.event

import moe.forpleuvoir.ibukigourd.util.scanModPackage
import moe.forpleuvoir.nebula.event.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object IbukiGourdEventManager : EventManager() {

	override fun init() {
		super.init()
		//todo delete
//		println("已注册事件")
//		eventSet().forEach{
//			println(it.simpleName)
//		}
	}

	override fun scanPackage(predicate: (KClass<*>) -> Boolean): Set<KClass<*>> {
		return buildSet {
			for (set in scanModPackage(predicate).values) {
				addAll(set)
			}
		}
	}

	@Suppress("unchecked_cast")
	override fun subscribe() {
		scanPackage { it.java.isAnnotationPresent(EventSubscriber::class.java) }
			.map { it.java }
			.forEach { clazz ->
				val eventSubscriber = clazz.getAnnotation(EventSubscriber::class.java)
				EventBus[eventSubscriber.eventBus]?.let { bus ->
					var instance = clazz.kotlin.objectInstance
					if (instance == null) {
						clazz.constructors.find { it.parameterCount == 0 }?.let {
							instance = it.newInstance()
						}
					}
					clazz.methods.toList()
						.stream()
						.filter { method ->
							method.isAnnotationPresent(Subscriber::class.java)
									&& method.parameterCount == 1
									&& (method.parameterTypes.last().kotlin.isSubclassOf(Event::class) || method.parameterTypes[0].equals(
								Event::class.java
							))
						}
						.forEach { method ->
							method.trySetAccessible()
							val parameter = method.parameters[0]
							val type = parameter.type
							bus.subscribe((type as Class<out Event>).kotlin, method.getAnnotation(Subscriber::class.java).greedy) { event ->
								method.invoke(instance, event)
							}
						}
				}
			}
	}

}