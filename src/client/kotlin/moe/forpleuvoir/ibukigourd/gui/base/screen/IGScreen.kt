package moe.forpleuvoir.ibukigourd.gui.base.screen

import kotlinx.coroutines.*
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.parentCount
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.render.runWithZOffset
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface IGScreen : DrawableElementContainer, WidgetContainer, IGWidget {

    companion object {

        const val Z_OFFSET = 100F

        const val POPUP_Z_OFFSET = 50F

        const val TOAST_Z_OFFSET = 100f

        inline fun IGScreen.applyZOffset(block: () -> Unit) =
            runWithZOffset(Z_OFFSET * parentCount, block)

        val currentScreenZOffset: Float
            get() = when (val screen = mc.currentScreen) {
                is IGScreen -> screen.parentCount * Z_OFFSET
                else        -> 0f
            }

    }

    //------------ IGScreen ------------\\

    var parentScreen: Screen?

    val focusedWidget: MutableState<IGWidget?>

    val hoveredWidget: MutableState<IGWidget?>

    var pauseGame: Boolean

    var closeOnEsc: Boolean

    var onClose: (() -> Unit)?

    fun close()

    var onDisplayed: (() -> Unit)?

    var onResize: ((client: MinecraftClient, width: Int, height: Int) -> Unit)?

    var onFirstInit: ((client: MinecraftClient, width: Int, height: Int) -> Unit)?

    var onInit: (() -> Unit)?

    /**
     * 用于处理删除元素之类的操作,会在GUI事件执行完成之后处理任务
     */
    fun execute(task: () -> Unit)

    //------------ Override ------------\\

    override var parentData: Any?

    override fun remeasure()

    //------------ Coroutine ------------\\

    val coroutineScope: ScreenCoroutineScope

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

fun closeScreen() = mc.currentScreen?.close()

fun IGElement.execute(task: () -> Unit) {
    if (this is IGScreen) execute(task)
    else screen()?.execute(task)
}

