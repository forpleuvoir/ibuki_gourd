package com.forpleuvoir.ibukigourd.gui.texture

import com.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import com.forpleuvoir.ibukigourd.render.base.texture.Corner
import com.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import com.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import com.forpleuvoir.ibukigourd.util.resourceManager
import com.forpleuvoir.ibukigourd.util.resources
import com.forpleuvoir.nebula.event.EventSubscriber
import com.forpleuvoir.nebula.event.Subscriber
import com.forpleuvoir.nebula.serialization.json.parseToJsonObject
import com.google.common.io.CharStreams
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@EventSubscriber
object IbukiGourdTextures : SynchronousResourceReloader {

	val TEXTURE_INFO = resources("texture/gui/ibukigourd_widget.json")

	val TEXTURE = resources("texture/gui/ibukigourd_widget.png")

	val INFO = TextureInfo(256, 256, TEXTURE)

	@Subscriber
	fun init(event: ClientLifecycleEvent.ClientStartingEvent) {
		resourceManager.registerReloader(this)
	}

	override fun reload(manager: ResourceManager) {
		CharStreams.toString(
			InputStreamReader(
				manager.getResource(TEXTURE_INFO).get().inputStream,
				StandardCharsets.UTF_8
			)
		).parseToJsonObject.apply {


		}
	}

	var BUTTON_IDLE_1: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 0, 0, 15, 15), INFO)
		private set

	var BUTTON_HOVERED_1: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 0, 16, 15, 31), INFO)
		private set

	var BUTTON_PRESSED_1: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 0, 32, 15, 47), INFO)
		private set

	var BUTTON_IDLE_2: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 16, 0, 32, 15), INFO)
		private set

	var BUTTON_HOVERED_2: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 16, 16, 32, 31), INFO)
		private set

	var BUTTON_PRESSED_2: WidgetTexture = WidgetTexture(TextureUVMapping(Corner(4), 16, 32, 32, 47), INFO)
		private set


}

