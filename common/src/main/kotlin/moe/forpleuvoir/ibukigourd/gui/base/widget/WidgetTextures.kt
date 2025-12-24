package moe.forpleuvoir.ibukigourd.gui.base.widget

import com.google.common.io.CharStreams
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.common.api.ExperimentalApi
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.json.JsonParser
import net.minecraft.server.packs.resources.PreparableReloadListener
import kotlin.reflect.full.isSubclassOf

object WidgetTextures : SimpleResourceReloaderListener<SerializeObject>() {

    private val log = logger()

    private val TEXTURE_INFO_RESOURCES = identifier("texture/gui/ibukigourd_widget.json")

    private val TEXTURE_RESOURCES = identifier("texture/gui/ibukigourd_widget.png")

    private val TEXTURE_INFO = TextureInfo(256, 256, TEXTURE_RESOURCES)

    val RESOURCE_ID = identifier("id")

    @OptIn(ExperimentalApi::class)
    override fun prepare(sharedState: PreparableReloadListener.SharedState): SerializeObject {
        log.info("widget textures loading...")
        return runCatching {
            sharedState.resourceManager().getResource(TEXTURE_INFO_RESOURCES).get().let { resource ->
                JsonParser.parse(CharStreams.toString(resource.open().reader())).asObject
            }
        }.onFailure {
            log.warn("failed to load widget texture", it)
        }.getOrDefault(SerializeObject())
    }

    override fun apply(prepared: SerializeObject, sharedState: PreparableReloadListener.SharedState) {
        log.info("widget textures parsing...")
        runCatching {
            this.javaClass.declaredFields
                .asSequence()
                .filter { field ->
                    field.type.kotlin.isSubclassOf(WidgetTexture::class)
                }.forEach { widgetTexture ->
                    widgetTexture.isAccessible = true
                    val name = widgetTexture.name
                    val oldValue = widgetTexture.get(WidgetTextures) as WidgetTexture
                    val newValue = WidgetTexture.deserialization(prepared[name], oldValue)
                    if (oldValue != newValue) widgetTexture.set(WidgetTextures, newValue)
                }
        }.onFailure {
            log.warn("failed to parse widget texture", it)
        }
    }

    var ALPHA: WidgetTexture = WidgetTexture(Corner(0), 144, 32, 160, 48, TEXTURE_INFO)
        private set

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

    var BUTTON_IDLE_3: WidgetTexture = WidgetTexture(Corner(4), 0, 48, 16, 64, TEXTURE_INFO)
        private set

    var BUTTON_HOVERED_3: WidgetTexture = WidgetTexture(Corner(4), 0, 64, 16, 80, TEXTURE_INFO)
        private set

    var BUTTON_PRESSED_3: WidgetTexture = WidgetTexture(Corner(4), 0, 80, 16, 96, TEXTURE_INFO)
        private set

    var BUTTON_DISABLED_3: WidgetTexture = WidgetTexture(Corner(4), 0, 80, 16, 96, TEXTURE_INFO)
        private set

    var COLOR_BUTTON_BORDER_IDLE: WidgetTexture = WidgetTexture(Corner(4), 16, 48, 32, 64, TEXTURE_INFO)
        private set

    var COLOR_BUTTON_BORDER_HOVERED: WidgetTexture = WidgetTexture(Corner(4), 16, 64, 32, 80, TEXTURE_INFO)
        private set

    var COLOR_BUTTON_BORDER_PRESSED: WidgetTexture = WidgetTexture(Corner(4), 16, 80, 32, 96, TEXTURE_INFO)
        private set

    var COLOR_BUTTON_BORDER_DISABLED: WidgetTexture = WidgetTexture(Corner(4), 16, 80, 32, 96, TEXTURE_INFO)
        private set

    var COLOR_BUTTON_HOVERED_OUTLINE: WidgetTexture = WidgetTexture(Corner(4), 32, 64, 48, 80, TEXTURE_INFO)
        private set

    var TIP: WidgetTexture = WidgetTexture(Corner(4), 48, 32, 64, 48, TEXTURE_INFO)
        private set

    var TIP_ARROW_LEFT: WidgetTexture = WidgetTexture(Corner(left = -4), 68, 32, 73, 39, TEXTURE_INFO)
        private set

    var TIP_ARROW_RIGHT: WidgetTexture = WidgetTexture(Corner(right = -4), 64, 41, 69, 48, TEXTURE_INFO)
        private set

    var TIP_ARROW_TOP: WidgetTexture = WidgetTexture(Corner(top = -4), 64, 53, 71, 58, TEXTURE_INFO)
        private set

    var TIP_ARROW_BOTTOM: WidgetTexture = WidgetTexture(Corner(bottom = -4), 73, 49, 80, 54, TEXTURE_INFO)
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

    var SLIDER_BORDER: WidgetTexture = WidgetTexture(Corner(3, 4, 4, 3), 80, 16, 96, 32, TEXTURE_INFO)
        private set

    var SLIDER_CONTENT: WidgetTexture = WidgetTexture(Corner(4), 96, 16, 112, 32, TEXTURE_INFO)
        private set

    var COLOR_SLIDER_BG: WidgetTexture = WidgetTexture(Corner(2), 80, 32, 88, 40, TEXTURE_INFO)
        private set

    var COLOR_SLIDER_ARROW: WidgetTexture = WidgetTexture(Corner(vertical = 2, horizontal = 1), 91, 32, 96, 40, TEXTURE_INFO)
        private set

    var LIST_BACKGROUND: WidgetTexture = WidgetTexture(Corner(4), 32, 0, 48, 16, TEXTURE_INFO)
        private set

    var TEXT_INPUT: WidgetTexture = WidgetTexture(Corner(5), 80, 0, 96, 16, TEXTURE_INFO)
        private set

    var TEXT_SELECTED_INPUT: WidgetTexture = WidgetTexture(Corner(5), 96, 0, 112, 16, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_BACKGROUND = WidgetTexture(Corner(4), 112, 0, 128, 16, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_EXPEND_BACKGROUND = WidgetTexture(Corner(4), 128, 0, 144, 16, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_ARROW_UP = WidgetTexture(Corner(0), 144, 0, 151, 4, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_ARROW_DOWN = WidgetTexture(Corner(0), 153, 0, 160, 4, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_SEPARATOR_VERTICAL = WidgetTexture(Corner(vertical = 2), 146, 11, 147, 16, TEXTURE_INFO)
        private set

    var DROP_DOWN_MENU_SEPARATOR_HORIZONTAL = WidgetTexture(Corner(horizontal = 2), 144, 13, 149, 14, TEXTURE_INFO)
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

    var LOCK_ON_IDLE = WidgetTexture(Corner(0), 163, 51, 173, 61, TEXTURE_INFO)
        private set

    var LOCK_ON_HOVERED = WidgetTexture(Corner(0), 163, 67, 173, 77, TEXTURE_INFO)
        private set

    var LOCK_ON_PRESSED = WidgetTexture(Corner(0), 163, 83, 173, 93, TEXTURE_INFO)
        private set

    var LOCK_ON_DISABLED = WidgetTexture(Corner(0), 163, 83, 173, 93, TEXTURE_INFO)
        private set

    var UNLOCK_IDLE = WidgetTexture(Corner(0), 179, 51, 189, 61, TEXTURE_INFO)
        private set

    var UNLOCK_HOVERED = WidgetTexture(Corner(0), 179, 67, 189, 77, TEXTURE_INFO)
        private set

    var UNLOCK_PRESSED = WidgetTexture(Corner(0), 179, 83, 189, 93, TEXTURE_INFO)
        private set

    var UNLOCK_DISABLED = WidgetTexture(Corner(0), 179, 83, 189, 93, TEXTURE_INFO)
        private set

    var SWITCH_BUTTON_BACKGROUND_BORDER = WidgetTexture(Corner(4), 192, 0, 208, 16, TEXTURE_INFO)
        private set

    var SWITCH_BUTTON_BACKGROUND_CONTENT = WidgetTexture(Corner(horizontal = 5, vertical = 4), 192, 16, 208, 32, TEXTURE_INFO)
        private set

    var SWITCH_BUTTON = WidgetTexture(Corner(4), 192, 32, 208, 48, TEXTURE_INFO)
        private set

    var TABS_BACKGROUND = WidgetTexture(Corner(top = 3, right = 4, left = 4, bottom = 5), 208, 0, 224, 16, TEXTURE_INFO)
        private set

    var TABS_SCREEN_BACKGROUND = WidgetTexture(Corner(top = 2, bottom = 0, left = 0, right = 0), 208, 16, 224, 32, TEXTURE_INFO)
        private set

    var TAB_ACTIVE_TOP = WidgetTexture(Corner(top = 4, right = 4, left = 4, bottom = -2), 224, 0, 240, 14, TEXTURE_INFO)
        private set

    var TAB_INACTIVE_TOP = WidgetTexture(Corner(top = 5, right = 4, left = 4, bottom = 0), 224, 32, 240, 48, TEXTURE_INFO)
        private set

    var TAB_ACTIVE_BOTTOM = WidgetTexture(Corner(top = -3, right = 4, left = 4, bottom = 4), 240, 3, 256, 16, TEXTURE_INFO)
        private set

    var TAB_INACTIVE_BOTTOM = WidgetTexture(Corner(top = -1, right = 4, left = 4, bottom = 5), 240, 33, 256, 48, TEXTURE_INFO)
        private set

    var TAB_ACTIVE_LEFT = WidgetTexture(Corner(top = 4, right = -3, left = 4, bottom = 4), 224, 16, 237, 32, TEXTURE_INFO)
        private set

    var TAB_INACTIVE_LEFT = WidgetTexture(Corner(top = 4, right = -1, left = 5, bottom = 4), 224, 48, 239, 64, TEXTURE_INFO)
        private set

    var TAB_ACTIVE_RIGHT = WidgetTexture(Corner(top = 4, right = 4, left = -2, bottom = 4), 242, 16, 256, 32, TEXTURE_INFO)
        private set

    var TAB_INACTIVE_RIGHT = WidgetTexture(Corner(top = 4, right = 5, left = 0, bottom = 4), 240, 48, 256, 64, TEXTURE_INFO)
        private set

    var DIALOG_BG = WidgetTexture(Corner(4), 112, 16, 128, 32, TEXTURE_INFO)
        private set

    var DIALOG_CONTENT_OUTLINE = WidgetTexture(Corner(4), 128, 16, 144, 32, TEXTURE_INFO)
        private set

    var DIALOG_CONTENT_INNER = WidgetTexture(Corner(4), 128, 32, 144, 48, TEXTURE_INFO)
        private set


}