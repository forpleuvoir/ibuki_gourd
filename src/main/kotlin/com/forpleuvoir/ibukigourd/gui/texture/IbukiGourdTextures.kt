package com.forpleuvoir.ibukigourd.gui.texture

import com.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import com.forpleuvoir.ibukigourd.render.base.texture.Corner
import com.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.ibukigourd.util.resources
import com.forpleuvoir.nebula.event.EventSubscriber
import com.forpleuvoir.nebula.event.Subscriber
import com.forpleuvoir.nebula.serialization.json.jsonStringToObject
import com.google.common.io.CharStreams
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import kotlin.reflect.full.isSubclassOf

@EventSubscriber
object IbukiGourdTextures : SimpleSynchronousResourceReloadListener {

	private val log = logger()

	val TEXTURE_INFO_RESOURCES = resources("texture/gui/ibukigourd_widget.json")

	val TEXTURE_RESOURCES = resources("texture/gui/ibukigourd_widget.png")

	val TEXTURE_INFO = TextureInfo(256, 256, TEXTURE_RESOURCES)

	@Subscriber
	fun init(event: ClientLifecycleEvent.ClientStartingEvent) {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(this)
	}

	override fun getFabricId(): Identifier = resources("widget")


	override fun reload(manager: ResourceManager) {
		log.info("widget textures loading...")
		try {
			manager.getResource(TEXTURE_INFO_RESOURCES).ifPresent { resource ->
				CharStreams.toString(resource.inputStream.reader())
					.jsonStringToObject().apply {
						IbukiGourdTextures.javaClass.declaredFields
							.asSequence()
							.filter { field ->
								field.type.kotlin.isSubclassOf(WidgetTexture::class)
							}.forEach { widgetTexture ->
								widgetTexture.isAccessible = true
								val name = widgetTexture.name
								val oldValue = widgetTexture.get(IbukiGourdTextures) as WidgetTexture
								val newValue = WidgetTexture.deserialization(this[name], oldValue)
								if (oldValue != newValue) widgetTexture.set(IbukiGourdTextures, newValue)
							}
					}
			}
		} catch (e: Exception) {
			log.error("widget textures load fail", e)
		}

	}

	var BUTTON_IDLE_1: WidgetTexture = WidgetTexture(Corner(4), 0, 0, 16, 16, TEXTURE_INFO)
		private set

	var BUTTON_HOVERED_1: WidgetTexture = WidgetTexture(Corner(4), 0, 16, 16, 32, TEXTURE_INFO)
		private set

	var BUTTON_PRESSED_1: WidgetTexture = WidgetTexture(Corner(4), 0, 32, 16, 48, TEXTURE_INFO)
		private set

	var BUTTON_DISABLED_1: WidgetTexture = WidgetTexture(Corner(4), 0, 32, 16, 48, TEXTURE_INFO)
		private set

	var BUTTON_IDLE_2: WidgetTexture = WidgetTexture(Corner(4), 16, 0, 32, 16, TEXTURE_INFO)
		private set

	var BUTTON_HOVERED_2: WidgetTexture = WidgetTexture(Corner(4), 16, 16, 32, 32, TEXTURE_INFO)
		private set

	var BUTTON_PRESSED_2: WidgetTexture = WidgetTexture(Corner(4), 16, 32, 32, 48, TEXTURE_INFO)
		private set

	var BUTTON_DISABLED_2: WidgetTexture = WidgetTexture(Corner(4), 16, 32, 32, 48, TEXTURE_INFO)
		private set

}

