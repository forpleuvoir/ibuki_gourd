package moe.forpleuvoir.ibukigourd.gui.widget.icon

import com.google.common.io.CharStreams
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.render.base.texture.Corner
import moe.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.resources
import moe.forpleuvoir.nebula.common.api.ExperimentalApi
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import moe.forpleuvoir.nebula.serialization.json.JsonParser
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import kotlin.reflect.full.isSubclassOf

@Suppress("UNUSED")
@EventSubscriber
object IconTextures : SimpleSynchronousResourceReloadListener {

    private val log = logger()

    private val TEXTURE_INFO_RESOURCES = resources("texture/gui/ibukigourd_icon.json")

    private val TEXTURE_RESOURCES = resources("texture/gui/ibukigourd_icon.png")

    private val TEXTURE_INFO = TextureInfo(256, 256, TEXTURE_RESOURCES)

    @Subscriber
    @Suppress("UNUSED_PARAMETER")
    fun init(event: ClientLifecycleEvent.ClientStartingEvent) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this)
    }

    @Suppress("DuplicatedCode")
    @OptIn(ExperimentalApi::class)
    override fun reload(manager: ResourceManager) {
        log.info("icon textures loading...")
        runCatching {
            manager.getResource(TEXTURE_INFO_RESOURCES).ifPresent { resource ->
                JsonParser.parse(CharStreams.toString(resource.inputStream.reader())).asObject.apply {
                    this.javaClass.declaredFields
                        .asSequence()
                        .filter { field ->
                            field.type.kotlin.isSubclassOf(WidgetTexture::class)
                        }.forEach { widgetTexture ->
                            widgetTexture.isAccessible = true
                            val name = widgetTexture.name
                            val oldValue = widgetTexture.get(WidgetTextures) as WidgetTexture
                            val newValue = WidgetTexture.deserialization(this[name], oldValue)
                            if (oldValue != newValue) widgetTexture.set(WidgetTextures, newValue)
                        }
                }
            }
        }.onFailure { log.error("icon textures load fail", it) }
    }

    override fun getFabricId(): Identifier = resources("icon")


    var SEARCH = WidgetTexture(Corner(0), 0, 0, 16, 16, TEXTURE_INFO)
        private set

    var OPACITY = WidgetTexture(Corner(0), 16, 0, 32, 16, TEXTURE_INFO)
        private set

    var LOCK = WidgetTexture(Corner(0), 32, 0, 48, 16, TEXTURE_INFO)
        private set

    var UNLOCK = WidgetTexture(Corner(0), 48, 0, 64, 16, TEXTURE_INFO)
        private set

    var CLOSE = WidgetTexture(Corner(0), 0, 16, 16, 32, TEXTURE_INFO)
        private set

    var SAVE = WidgetTexture(Corner(0), 16, 16, 32, 32, TEXTURE_INFO)
        private set

    var DELETE = WidgetTexture(Corner(0), 16, 32, 32, 48, TEXTURE_INFO)
        private set

    var PLUS = WidgetTexture(Corner(0), 32, 16, 48, 32, TEXTURE_INFO)
        private set

    var MINUS = WidgetTexture(Corner(0), 48, 16, 64, 32, TEXTURE_INFO)
        private set

    var SETTING = WidgetTexture(Corner(0), 64, 16, 80, 32, TEXTURE_INFO)
        private set

    var SWITCH = WidgetTexture(Corner(0), 80, 16, 96, 32, TEXTURE_INFO)
        private set

    var REFRESH = WidgetTexture(Corner(0), 96, 16, 112, 32, TEXTURE_INFO)
        private set

    var LEFT = WidgetTexture(Corner(0), 112, 16, 128, 32, TEXTURE_INFO)
        private set

    var RIGHT = WidgetTexture(Corner(0), 128, 16, 144, 32, TEXTURE_INFO)
        private set

    var UP = WidgetTexture(Corner(0), 144, 16, 160, 32, TEXTURE_INFO)
        private set

    var DOWN = WidgetTexture(Corner(0), 160, 16, 176, 32, TEXTURE_INFO)
        private set

    var BACK = WidgetTexture(Corner(0), 176, 16, 192, 32, TEXTURE_INFO)
        private set

    var FILTER = WidgetTexture(Corner(0), 192, 16, 208, 32, TEXTURE_INFO)
        private set


}