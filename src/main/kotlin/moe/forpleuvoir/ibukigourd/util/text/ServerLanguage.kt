package moe.forpleuvoir.ibukigourd.util.text

import com.google.common.collect.Lists
import com.google.gson.JsonObject
import com.ibm.icu.lang.UCharacter
import com.ibm.icu.text.ArabicShaping
import com.ibm.icu.text.Bidi
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.mod.config.IbukiGourdServerConfig
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.resources
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import moe.forpleuvoir.nebula.serialization.gson.getOr
import moe.forpleuvoir.nebula.serialization.gson.gson
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType.SERVER_DATA
import net.minecraft.text.OrderedText
import net.minecraft.text.StringVisitable
import net.minecraft.text.TextReorderingProcessor
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.Language
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

@EventSubscriber
object ServerLanguage : Language() {

	private val log = logger()

	private val UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]")

	private val map: MutableMap<LanguagePair, MutableMap<String, String>> = HashMap()

	private val current: String
		get() {
			val serverLanguage = IbukiGourdServerConfig.SERVER_LANGUAGE
			return if (hasTranslation(serverLanguage)) {
				serverLanguage
			} else "zh_cn"
		}

	private val currentMap: Map<String, String>
		get() {
			return map.asSequence().find { it.key.language == current }?.value ?: emptyMap()
		}

	data class LanguagePair(val language: String, val rightToLeft: Boolean) {
		override fun hashCode(): Int {
			return language.hashCode()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as LanguagePair

			if (language != other.language) return false
			if (rightToLeft != other.rightToLeft) return false

			return true
		}

	}

	@Subscriber
	fun modInit(event: ModInitializerEvent) {
		if (event.meta.id == IbukiGourd.MOD_ID) {
			ResourceManagerHelper.get(SERVER_DATA)
				.registerReloadListener(object : SimpleSynchronousResourceReloadListener {
					override fun reload(manager: ResourceManager) {
						log.info("load server language...")
						map.clear()
						manager.allNamespaces.forEach { nameSpace ->
							manager.findResources("server_language") { it.path.endsWith(".json") }
								.forEach { (identifier, resource) ->
									runCatching {
										appendFrom(identifier.path, resource)
									}.onFailure {
										log.error("Skipped language file: {}:{} ({})", nameSpace, name, it)
									}
								}
						}
					}

					override fun getFabricId(): Identifier {
						return resources("server_language")
					}

				})
		}
	}


	private fun appendFrom(languageName: String, resource: Resource) {
		val path = languageName.replace(".json", "").replace("server_lang/", "")
		runCatching {
			resource.inputStream.use {
				val json = gson.fromJson(InputStreamReader(it, StandardCharsets.UTF_8) as Reader, JsonObject::class.java)
				val languagePair = LanguagePair(path, json.getOr("rightToLft", false))
				val map = HashMap<String, String>()
				json.entrySet().forEach { entry ->
					map[entry.key] = UNSUPPORTED_FORMAT_PATTERN.matcher(JsonHelper.asString(entry.value, entry.key)).replaceAll("%$1s")
				}
				if (this.map.containsKey(languagePair)) {
					this.map[languagePair]!!.putAll(map)
				} else {
					this.map[languagePair] = map
				}
			}
		}.onFailure {
			if (it is IOException) log.error("Failed to load translations for {} from pack {}", path, resource.resourcePackName, it)
			else throw it
		}
	}

	override fun get(key: String): String {
		return currentMap[key] ?: key
	}

	override fun get(key: String, fallback: String): String {
		return currentMap[key] ?: fallback
	}

	override fun hasTranslation(key: String): Boolean {
		return map.filterKeys { it.language == key }.isNotEmpty()
	}

	override fun isRightToLeft(): Boolean {
		return map.asSequence().find { it.key.language == current }?.key?.rightToLeft ?: false
	}

	override fun reorder(text: StringVisitable): OrderedText {
		return reorder(text, this.isRightToLeft)
	}

	private fun reorder(text: StringVisitable, rightToLeft: Boolean): OrderedText {
		val textReorderingProcessor = TextReorderingProcessor.create(text, UCharacter::getMirror, this::shapeArabic)
		val bidi = Bidi(textReorderingProcessor.string, if (rightToLeft) 127 else 126)
		bidi.reorderingMode = 0
		val list = Lists.newArrayList<OrderedText>()
		val i = bidi.countRuns()
		for (j in 0 until i) {
			val bidiRun = bidi.getVisualRun(j)
			list.addAll(textReorderingProcessor.process(bidiRun.start, bidiRun.length, bidiRun.isOddRun))
		}
		return OrderedText.concat(list)
	}

	private fun shapeArabic(string: String): String? {
		return runCatching { ArabicShaping(8).shape(string) }.getOrDefault(string)
	}

}