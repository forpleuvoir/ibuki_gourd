package moe.forpleuvoir.ibukigourd.gui.base.screen

import kotlinx.coroutines.*
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.util.State
import net.minecraft.client.MinecraftClient
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface IGScreen : DrawableElementContainer, WidgetContainer, IGWidget {

    //------------ IGScreen ------------\\

    var focusedWidget: State<IGWidget?>

    var hoveredWidget: State<IGWidget?>

    var pauseGame: Boolean

    var closeOnEsc: Boolean

    var onClose: (() -> Unit)?

    var onDisplayed: (() -> Unit)?

    val scope: ScreenScope<*>

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

    fun remeasure()

    //------------ Coroutine ------------\\

    val coroutineScope: CoroutineScope

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job = coroutineScope.launch(context, start, block)

    fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> = coroutineScope.async(context, start, block)

}

fun ScreenScope<*>.remember(key: String, value: Any) {
    owner().pushData(key, value)
}

@Suppress("nothing_to_inline")
inline fun ScreenScope<*>.remember(key: Any, value: Any) = remember(key.toString(), value)

fun ScreenScope<*>.byRemember(key: String): Any? = owner().getData(key)

@Suppress("nothing_to_inline")
inline fun ScreenScope<*>.byRemember(key: Any): Any? = byRemember(key.toString())

@Suppress("UNCHECKED_CAST")
fun <T : Any> ScreenScope<*>.byRemember(key: String, default: T): T = owner().getData(key) as? T ?: default

@Suppress("nothing_to_inline")
inline fun <T : Any> ScreenScope<*>.byRemember(key: Any, default: T): T = byRemember(key.toString(), default)


fun MinecraftClient.remember(key: String, data: Any) {
    if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).pushData(key, data)
    }
}

@Suppress("nothing_to_inline")
inline fun MinecraftClient.remember(key: Any, value: Any) = remember(key.toString(), value)

fun MinecraftClient.byRemember(key: String): Any? {
    return if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).getData(key)
    } else null
}

@Suppress("nothing_to_inline")
inline fun MinecraftClient.byRemember(key: Any): Any? = byRemember(key.toString())

@Suppress("UNCHECKED_CAST")
fun <T : Any> MinecraftClient.byRemember(key: String, default: T): T {
    return if (currentScreen is IGScreen) {
        (currentScreen as IGScreen).getData(key) as? T ?: default
    } else default
}

@Suppress("nothing_to_inline")
inline fun <T : Any> MinecraftClient.byRemember(key: Any, default: T): T = byRemember(key.toString(), default)