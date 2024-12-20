package moe.forpleuvoir.ibukigourd.gui.base.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ScreenData(
    val screen: IGScreen
) : AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<ScreenData>

}

class ScreenCoroutineScope(screen: IGScreen) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + ScreenData(screen) + Job()

}

val CoroutineScope.screen: IGScreen? get() = this.coroutineContext[ScreenData]?.screen