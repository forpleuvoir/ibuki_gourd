package moe.forpleuvoir.ibukigourd.gui.base.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import org.joml.Vector2fc
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@GuiDslMark
fun interface GuiScope<T : Any> {
    fun owner(): T

    companion object {

        @JvmName("createScope")
        fun <T : Any> create(screen: T) = GuiScope { screen }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

        val GuiScope<out IGElement>.customData: MutableMap<String, Any> get() = this.owner().customData

        fun <W : IGWidget> WidgetContainerScope.addWidgetChild(child: W) = owner().addWidgetChild(child)

        fun <W : IGWidget> WidgetContainerScope.addWidgetChild(child: W, scope: W.() -> Unit) = owner().addWidgetChild(child.apply(scope))


        @Deprecated("should use executeRecompose() instead", replaceWith = ReplaceWith("executeRecompose()"))
        fun WidgetContainerScope.recompose() {
            owner().recompose()
        }

        fun <T> GuiScope<T>.executeRecompose() where T : WidgetContainer, T : IGElement {
            execute { owner().recompose() }
        }

        fun WidgetScope.layer(layer: GuiLayer) {
            owner().layer = layer
        }

        fun WidgetScope.clearLayer() {
            owner().clearLayer()
        }

        fun WidgetScope.active(active: Boolean) {
            owner().active = active
        }

        fun WidgetScope.visible(visible: Boolean) {
            owner().visible = visible
        }

        fun WidgetScope.placeAt(x: Float, y: Float, worldCoordinatesMode: Boolean) {
            owner().placeAt(x, y, worldCoordinatesMode)
        }

        fun WidgetScope.placeAt(position: Vector2fc, worldCoordinatesMode: Boolean) {
            owner().placeAt(position, worldCoordinatesMode)
        }

        fun ElementScope.execute(task: () -> Unit) {
            owner().screen()?.execute(task)
        }

        fun ElementScope.launch(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
        ) = owner().screen()?.launch(context, start, block)

        fun IGElement.launch(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
        ) = screen()?.launch(context, start, block)

        fun <T> ElementScope.async(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
        ): Deferred<T>? = owner().screen()?.async(context, start, block)

        fun <T> IGElement.async(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
        ): Deferred<T>? = screen()?.async(context, start, block)
    }

}

typealias WidgetScope = GuiScope<out IGWidget>

typealias WidgetContainerScope = GuiScope<out WidgetContainer>

typealias ElementScope = GuiScope<out IGElement>

typealias ScreenScope = GuiScope<IGScreen>