package com.forpleuvoir.ibukigourd.event

import com.forpleuvoir.ibukigourd.util.scanModPackage
import com.forpleuvoir.nebula.event.EventManager
import kotlin.reflect.KClass

object IbukiGourdEventManager : EventManager() {
	override fun scanPackage(predicate: (KClass<*>) -> Boolean): Set<KClass<*>> {
		return buildSet {
			for (set in scanModPackage(predicate).values) {
				addAll(set)
			}
		}
	}

}