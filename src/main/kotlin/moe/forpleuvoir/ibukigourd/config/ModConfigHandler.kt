package moe.forpleuvoir.ibukigourd.config

interface ModConfigHandler {

	val managers: Iterable<ModConfigManager>

	suspend fun save() {
		managers.forEach { it.save() }
	}

	fun asyncSave() {
		managers.forEach { it.asyncSave() }
	}

	suspend fun load() {
		managers.forEach { it.load() }
	}

	fun asyncLoad() {
		managers.forEach { it.asyncLoad() }
	}

}