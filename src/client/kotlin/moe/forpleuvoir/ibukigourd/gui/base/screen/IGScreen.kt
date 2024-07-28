package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import net.minecraft.client.MinecraftClient

interface IGScreen : DrawableElementContainer, WidgetContainer, IGWidget {

    //------------ IGScreen ------------\\

    var focusedWidget: IGWidget?

    var pauseGame: Boolean

    var closeOnEsc: Boolean

    var onClose: (() -> Unit)?

    var onDisplayed: (() -> Unit)?

    /**
     * GUI层
     */
    val layers: List<GuiLayer>

    fun pushData(key: String, data: Any)

    fun getData(key: String): Any?

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getDataOr(key: String, default: T): T = getData(key) as? T ?: default

    //------------ Override ------------\\

    override var parentData: Any?

}

fun MinecraftClient.pushScreenData(key: String, data: Any) {
    if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).pushData(key, data)
    }
}

fun MinecraftClient.getScreenData(key: String): Any? {
    return if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).getData(key)
    } else null
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> MinecraftClient.getScreenDataOr(key: String, default: T): T {
    return if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).getData(key) as? T ?: default
    } else default
}