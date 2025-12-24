package moe.forpleuvoir.ibukigourd.gui.widget.icon

import com.google.common.io.CharStreams
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.common.api.ExperimentalApi
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.json.JsonParser
import net.minecraft.server.packs.resources.PreparableReloadListener
import kotlin.reflect.full.isSubclassOf

@Suppress("UNUSED")
object IconTextures : SimpleResourceReloaderListener<SerializeObject>() {

    private val log = logger()

    private val TEXTURE_INFO_RESOURCES = identifier("texture/gui/ibukigourd_icon.json")

    private val TEXTURE_RESOURCES = identifier("texture/gui/ibukigourd_icon.png")

    private val TEXTURE_INFO = TextureInfo(256, 256, TEXTURE_RESOURCES)

    val RESOURCE_ID = identifier("icon")

    @OptIn(ExperimentalApi::class)
    override fun prepare(sharedState: PreparableReloadListener.SharedState): SerializeObject {
        log.info("icon textures loading...")
        return runCatching {
            sharedState.resourceManager().getResource(TEXTURE_INFO_RESOURCES).get().let { resource ->
                JsonParser.parse(CharStreams.toString(resource.open().reader())).asObject
            }
        }.onFailure {
            log.warn("failed to load icon texture", it)
        }.getOrDefault(SerializeObject())
    }

    override fun apply(prepared: SerializeObject, sharedState: PreparableReloadListener.SharedState) {
        log.info("icon textures parsing...")
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
            log.warn("failed to parse icon texture", it)
        }
    }


    var SEARCH = WidgetTexture(Corner.Unspecified, 2, 2, 14, 14, TEXTURE_INFO)
        private set

    var OPACITY = WidgetTexture(Corner.Unspecified, 16, 0, 32, 16, TEXTURE_INFO)
        private set

    var LOCK = WidgetTexture(Corner.Unspecified, 36, 3, 44, 13, TEXTURE_INFO)
        private set

    var UNLOCK = WidgetTexture(Corner.Unspecified, 52, 3, 60, 13, TEXTURE_INFO)
        private set

    var EDIT = WidgetTexture(Corner.Unspecified, 67, 3, 77, 13, TEXTURE_INFO)
        private set

    var COPY = WidgetTexture(Corner.Unspecified, 83, 2, 93, 14, TEXTURE_INFO)
        private set

    var PASTE = WidgetTexture(Corner.Unspecified, 99, 2, 109, 14, TEXTURE_INFO)
        private set

    var CUT = WidgetTexture(Corner.Unspecified, 115, 3, 125, 13, TEXTURE_INFO)
        private set

    var CLOSE = WidgetTexture(Corner.Unspecified, 3, 19, 13, 29, TEXTURE_INFO)
        private set

    var SAVE = WidgetTexture(Corner.Unspecified, 18, 18, 30, 30, TEXTURE_INFO)
        private set

    var DELETE = WidgetTexture(Corner.Unspecified, 18, 34, 30, 47, TEXTURE_INFO)
        private set

    var PLUS = WidgetTexture(Corner.Unspecified, 35, 19, 45, 29, TEXTURE_INFO)
        private set

    var MINUS = WidgetTexture(Corner.Unspecified, 51, 19, 61, 29, TEXTURE_INFO)
        private set

    var SETTING = WidgetTexture(Corner.Unspecified, 67, 19, 77, 29, TEXTURE_INFO)
        private set

    var SWITCH = WidgetTexture(Corner.Unspecified, 83, 19, 93, 29, TEXTURE_INFO)
        private set

    var REFRESH = WidgetTexture(Corner.Unspecified, 99, 19, 109, 29, TEXTURE_INFO)
        private set

    var LEFT = WidgetTexture(Corner.Unspecified, 117, 19, 122, 28, TEXTURE_INFO)
        private set

    var RIGHT = WidgetTexture(Corner.Unspecified, 134, 19, 139, 28, TEXTURE_INFO)
        private set

    var UP = WidgetTexture(Corner.Unspecified, 148, 21, 157, 26, TEXTURE_INFO)
        private set

    var DOWN = WidgetTexture(Corner.Unspecified, 164, 22, 173, 27, TEXTURE_INFO)
        private set

    var BACK = WidgetTexture(Corner.Unspecified, 179, 20, 189, 28, TEXTURE_INFO)
        private set

    var FILTER = WidgetTexture(Corner.Unspecified, 194, 19, 206, 29, TEXTURE_INFO)
        private set

}