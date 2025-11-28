package moe.forpleuvoir.ibukigourd.gui.base.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElement
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import org.joml.Vector2fc
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@GuiDslMark
fun interface GuiScope<T : Any> {
    fun owner(): T

    companion object {

//        @JvmName("createScope")
//        fun <T : Any> create(screen: T) = GuiScope { screen }
//
//        @Suppress("NOTHING_TO_INLINE")
//        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

        val GuiScope<out GuiElement>.userData: MutableMap<String, Any> get() = this.owner().userData

        infix fun <W : GuiWidget> ContainerScope.addWidgetChild(child: W) = owner().addWidgetChild(child)

        fun <W : GuiWidget> ContainerScope.addWidgetChild(child: W, scope: W.() -> Unit) = owner().addWidgetChild(child.apply(scope))


        @Deprecated("should use executeRecompose() instead", replaceWith = ReplaceWith("executeRecompose()"))
        fun ContainerScope.recompose() {
            owner().recompose()
        }

        fun <T> GuiScope<T>.executeRecompose() where T : GuiWidgetContainer, T : GuiElement {
            execute { owner().recompose() }
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

        fun GuiElement.launch(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
        ) = screen()?.launch(context, start, block)

        fun <T> ElementScope.async(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
        ): Deferred<T>? = owner().screen()?.async(context, start, block)

        fun <T> GuiElement.async(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
        ): Deferred<T>? = screen()?.async(context, start, block)
    }

}


typealias WidgetScope = GuiScope<out GuiWidget>

typealias ContainerScope = GuiScope<out GuiWidgetContainer>

typealias ElementScope = GuiScope<out GuiElement>

typealias ScreenScope = GuiScope<IGScreen>