package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.nebula.common.runAsync

interface ModConfigHandler {

	val managers: Iterable<ModConfigManager>

	fun save() {
		managers.forEach { it.save() }
	}

	fun saveAsync() {
		managers.forEach { it.saveAsync() }
	}

	fun load() {
		managers.forEach { it.load() }
	}

	fun loadAsync() {
		managers.forEach { it.loadAsync() }
	}

}