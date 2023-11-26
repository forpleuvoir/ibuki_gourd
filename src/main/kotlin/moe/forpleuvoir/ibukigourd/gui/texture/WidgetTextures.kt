package moe.forpleuvoir.ibukigourd.gui.texture

import com.google.common.io.CharStreams
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
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

@EventSubscriber
object WidgetTextures : SimpleSynchronousResourceReloadListener {

    private val log = logger()

    private val TEXTURE_INFO_RESOURCES = resources("texture/gui/ibukigourd_widget.json")

    private val TEXTURE_RESOURCES = resources("texture/gui/ibukigourd_widget.png")

    private val TEXTURE_INFO = TextureInfo(256, 256, TEXTURE_RESOURCES)

    @Subscriber
    fun init(event: ClientLifecycleEvent.ClientStartingEvent) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this)
    }

    override fun getFabricId(): Identifier = resources("widget")

    @Suppress("DuplicatedCode")
    @OptIn(ExperimentalApi::class)
    override fun reload(manager: ResourceManager) {
        log.info("widget textures loading...")
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
        }.onFailure { log.error("widget textures load fail", it) }
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

    var TIP: WidgetTexture = WidgetTexture(Corner(4), 48, 32, 64, 48, TEXTURE_INFO)
        private set

    var TIP_ARROW_LEFT: WidgetTexture = WidgetTexture(Corner(0), 73, 41, 80, 48, TEXTURE_INFO)
        private set

    var TIP_ARROW_RIGHT: WidgetTexture = WidgetTexture(Corner(0), 64, 41, 71, 48, TEXTURE_INFO)
        private set

    var TIP_ARROW_TOP: WidgetTexture = WidgetTexture(Corner(0), 73, 32, 80, 39, TEXTURE_INFO)
        private set

    var TIP_ARROW_BOTTOM: WidgetTexture = WidgetTexture(Corner(0), 64, 32, 71, 39, TEXTURE_INFO)
        private set

    var SCROLLER_BAR_IDLE: WidgetTexture = WidgetTexture(Corner(4), 48, 0, 64, 16, TEXTURE_INFO)
        private set

    var SCROLLER_BAR_HOVERED: WidgetTexture = WidgetTexture(Corner(4), 64, 0, 80, 16, TEXTURE_INFO)
        private set

    var SCROLLER_BAR_PRESSED: WidgetTexture = WidgetTexture(Corner(4), 48, 0, 64, 16, TEXTURE_INFO)
        private set

    var SCROLLER_BAR_DISABLED: WidgetTexture = WidgetTexture(Corner(4), 48, 16, 64, 32, TEXTURE_INFO)
        private set

    var SCROLLER_BACKGROUND: WidgetTexture = WidgetTexture(Corner(4), 64, 16, 80, 32, TEXTURE_INFO)
        private set

    var LIST_BACKGROUND: WidgetTexture = WidgetTexture(Corner(4), 32, 0, 48, 16, TEXTURE_INFO)
        private set

    var TEXT_INPUT: WidgetTexture = WidgetTexture(Corner(5), 80, 0, 96, 16, TEXTURE_INFO)
        private set

    var TEXT_SELECTED_INPUT: WidgetTexture = WidgetTexture(Corner(5), 96, 0, 112, 16, TEXTURE_INFO)
        private set

    var DROP_MENU_BACKGROUND = WidgetTexture(Corner(4), 112, 0, 128, 16, TEXTURE_INFO)
        private set

    var DROP_MENU_EXPEND_BACKGROUND = WidgetTexture(Corner(4), 128, 0, 144, 16, TEXTURE_INFO)
        private set

    var DROP_MENU_ARROW_UP = WidgetTexture(Corner(0), 144, 0, 151, 4, TEXTURE_INFO)
        private set

    var DROP_MENU_ARROW_DOWN = WidgetTexture(Corner(0), 153, 0, 160, 4, TEXTURE_INFO)
        private set

    var CHECK_BOX_TRUE_IDLE = WidgetTexture(Corner(0), 162, 2, 174, 14, TEXTURE_INFO)
        private set

    var CHECK_BOX_TRUE_HOVERED = WidgetTexture(Corner(0), 162, 18, 174, 30, TEXTURE_INFO)
        private set

    var CHECK_BOX_TRUE_PRESSED = WidgetTexture(Corner(0), 162, 34, 174, 46, TEXTURE_INFO)
        private set

    var CHECK_BOX_TRUE_DISABLED = WidgetTexture(Corner(0), 162, 34, 174, 46, TEXTURE_INFO)
        private set

    var CHECK_BOX_FALSE_IDLE = WidgetTexture(Corner(0), 178, 2, 190, 14, TEXTURE_INFO)
        private set

    var CHECK_BOX_FALSE_HOVERED = WidgetTexture(Corner(0), 178, 18, 190, 30, TEXTURE_INFO)
        private set

    var CHECK_BOX_FALSE_PRESSED = WidgetTexture(Corner(0), 178, 34, 190, 46, TEXTURE_INFO)
        private set

    var CHECK_BOX_FALSE_DISABLED = WidgetTexture(Corner(0), 178, 34, 190, 46, TEXTURE_INFO)
        private set

    var SWITCH_BUTTON_ON_BACKGROUND = WidgetTexture(Corner(4), 192, 16, 208, 32, TEXTURE_INFO)
        private set

    var SWITCH_BUTTON_OFF_BACKGROUND = WidgetTexture(Corner(4), 192, 0, 208, 16, TEXTURE_INFO)
        private set

}

