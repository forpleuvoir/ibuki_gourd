package moe.forpleuvoir.ibukigourd.config

interface ModConfigHandler {

	val managers: Iterable<ModConfigManager>

	suspend fun save() {
		managers.forEach { it.save() }
	}

	fun saveAsync() {
		managers.forEach { it.saveAsync() }
	}

	suspend fun load() {
		managers.forEach { it.load() }
	}

	fun loadAsync() {
		managers.forEach { it.loadAsync() }
	}

}