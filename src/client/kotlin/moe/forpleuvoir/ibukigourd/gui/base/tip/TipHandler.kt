package moe.forpleuvoir.ibukigourd.gui.base.tip

import kotlinx.coroutines.*
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.render.runWithZOffset
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

object TipHandler : Tickable {

    const val SCREEN_HOVER_TIP = "#screen_hover_tip"

    private val tips: MutableMap<String, Tip> = mutableMapOf()

    fun pushTip(type: String, parent: () -> Transform, tip: Tip) {
        if (tips[type] == tip) return
        tips[type] = tip
        with(tip) {
            init(parent)
            show()
        }
    }

    fun pushTip(type: String, duration: Duration, parent: () -> Transform, tip: Tip) {
        pushTip(type, parent, tip)
        launch {
            delay(duration)
            if (getCurrentTip(type) == tip) {
                popTip(type)
            }
        }
    }

    fun getCurrentTip(type: String): Tip? = tips[type]

    fun popTip(type: String) {
        tips.remove(type)
    }

    @JvmStatic
    fun render(drawContext: IGDrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        runWithZOffset(9999f) {
            tips.values.forEach {
                it.render(drawContext, mouseX, mouseY, delta)
            }
        }
    }

    override fun onTick() {
        tips.values.forEach {
            it.onTick()
        }
    }

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

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

    fun cancel() = coroutineScope.cancel()

}