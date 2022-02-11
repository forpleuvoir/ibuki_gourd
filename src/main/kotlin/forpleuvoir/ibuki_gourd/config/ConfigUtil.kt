package forpleuvoir.ibuki_gourd.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import forpleuvoir.ibuki_gourd.common.ModInfo
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import forpleuvoir.ibuki_gourd.utils.JsonUtil
import forpleuvoir.ibuki_gourd.utils.JsonUtil.getNestedObject
import forpleuvoir.ibuki_gourd.utils.JsonUtil.gson
import net.fabricmc.loader.api.FabricLoader
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigUtil

 * 创建时间 2021/12/12 19:13

 * @author forpleuvoir

 */
object ConfigUtil {
	private val log = IbukiGourdLogger.getLogger(this::class.java)

	fun configFileDir(mod: ModInfo): Path {
		val file = File(FabricLoader.getInstance().configDir.toFile(), mod.modId)
		if (!file.exists())
			file.mkdir()
		return file.toPath()
	}

	fun configFile(mod: ModInfo, create: Boolean = true): File {
		val file = File(configFileDir(mod).toFile(), "${mod.modId}.json")
		if (!file.exists() && create) {
			file.createNewFile()
		}
		return file
	}

	fun readConfigBase(root: JsonObject, category: String, configs: List<ConfigBase>) {
		val obj = JsonUtil.getObject(root, category)
		if (obj != null) {
			configs.forEach {
				if (obj.has(it.name)) {
					it.setValueFromJsonElement(obj[it.name])
				}
			}
		}
	}

	fun writeJsonToFile(root: JsonObject, file: File): Boolean {
		var fileTmp = File(file.parentFile, file.name + ".tmp")
		if (fileTmp.exists()) {
			fileTmp = File(file.parentFile, UUID.randomUUID().toString() + ".tmp")
		}
		try {
			OutputStreamWriter(FileOutputStream(fileTmp), StandardCharsets.UTF_8).use { writer ->
				writer.write(gson.toJson(root))
				writer.close()
				if (file.exists() && file.isFile && !file.delete()) {
					log.warn("Failed to delete file '{}'", file.absolutePath)
				}
				return fileTmp.renameTo(file)
			}
		} catch (e: Exception) {
			log.warn("Failed to write JSON data to file '{}'", fileTmp.absolutePath, e)
		}
		return false
	}

	fun paresJsonFile(file: File): JsonElement? {
		if (file.exists() && file.isFile && file.canRead()) {
			val fileName = file.absolutePath
			try {
				InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader -> return JsonParser.parseReader(reader) }
			} catch (e: java.lang.Exception) {
				log.error("Failed to parse the JSON file '{}'", fileName, e)
			}

		}
		return null
	}

	fun writeConfigBase(root: JsonObject, key: String, configs: List<IConfigBase>) {
		root.getNestedObject(key, true)?.let {
			for (config in configs) {
				it.add(config.name, config.asJsonElement)
			}
		}
	}

}