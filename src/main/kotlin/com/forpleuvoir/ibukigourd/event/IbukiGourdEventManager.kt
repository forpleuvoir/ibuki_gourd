package com.forpleuvoir.ibukigourd.event

import com.forpleuvoir.ibukigourd.util.scanModPackage
import com.forpleuvoir.nebula.event.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object IbukiGourdEventManager : EventManager() {
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
						.filter { m ->
							m.isAnnotationPresent(Subscriber::class.java)
									&& m.parameterCount == 1
									&& (m.parameterTypes.last().kotlin.isSubclassOf(Event::class) || m.parameterTypes[0].equals(
								Event::class.java
							))
						}
						.forEach { m ->
							m.trySetAccessible()
							val parameter = m.parameters[0]
							val type = parameter.type
							bus.subscribe(
								(type as Class<out Event>).kotlin,
								m.getAnnotation(Subscriber::class.java).greedy
							) { event ->
								m.invoke(instance, event)
							}
						}
				}
			}
	}

}