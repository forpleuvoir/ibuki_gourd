package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import org.joml.Vector2fc

@GuiDslMark
fun interface GuiScope<T : Any> {
    fun owner(): T

    companion object {

        @JvmName("createScope")
        fun <T : Any> create(screen: T) = GuiScope { screen }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

        fun <W : IGWidget> WidgetContainerScope.addWidgetChild(child: W) = owner().addWidgetChild(child)

        fun <W : IGWidget> WidgetContainerScope.addWidgetChild(child: W, scope: W.() -> Unit) = owner().addWidgetChild(child.apply(scope))

        fun WidgetContainerScope.recompose() {
            owner().recompose()
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
    }

}

typealias WidgetScope = GuiScope<out IGWidget>

typealias WidgetContainerScope = GuiScope<out WidgetContainer>

typealias ElementScope = GuiScope<out IGElement>

typealias ScreenScope = GuiScope<IGScreen>