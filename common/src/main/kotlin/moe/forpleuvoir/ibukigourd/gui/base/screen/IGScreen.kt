package moe.forpleuvoir.ibukigourd.gui.base.screen

import kotlinx.coroutines.*
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElement
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiRenderableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.parentCount
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.render.runWithZOffset
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface IGScreen : GuiRenderableElementContainer, GuiWidgetContainer {

    companion object {

        const val Z_OFFSET = 100F

        const val POPUP_Z_OFFSET = 50F

        const val TOAST_Z_OFFSET = 100f

        inline fun IGScreen.applyZOffset(block: () -> Unit) =
            runWithZOffset(Z_OFFSET * parentCount, block)

        val currentScreenZOffset: Float
            get() = when (val screen = mc.screen) {
                is IGScreen -> screen.parentCount * Z_OFFSET
                else        -> 0f
            }

    }

    //------------ IGScreen ------------\\

    var parentScreen: Screen?

    val focusedWidget: MutableState<GuiWidget?>

    val hoveredWidget: MutableState<GuiWidget?>

    var pauseGame: Boolean

    var closeOnEsc: Boolean

    var onClose: (() -> Unit)?

    fun close()

    var onDisplayed: (() -> Unit)?

    var onResize: ((width: Int, height: Int) -> Unit)?

    var onFirstInit: ((width: Int, height: Int) -> Unit)?

    var onInit: (() -> Unit)?

    /**
     * 用于处理删除元素之类的操作,会在GUI事件执行完成之后处理任务
     */
    fun execute(task: () -> Unit)

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

/**
 * Just Init
 */
fun Screen.init() {
//    (this as ScreenMixin).`ibukigourd$init`()
    if (this is IGScreenImpl) this.igInit()
}

fun closeScreen() {
    mc.screen?.onClose()
}

fun GuiElement.execute(task: () -> Unit) {
    if (this is IGScreen) execute(task)
    else screen()?.execute(task)
}

