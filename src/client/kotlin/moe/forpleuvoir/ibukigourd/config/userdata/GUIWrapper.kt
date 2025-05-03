package moe.forpleuvoir.ibukigourd.config.userdata

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigWrapperMap
import moe.forpleuvoir.nebula.config.ConfigSerializable

private const val GUI_WRAPPER_KEY = "#gui_wrapper"
private typealias GUIWrapper = (WidgetContainerScope, Modifier) -> Unit

fun <C : ConfigSerializable> C.setGuiWrapper(
    scope: WidgetContainerScope.(C, Modifier) -> Unit,
): C {
    this.setUserData(GUI_WRAPPER_KEY, { s: WidgetContainerScope, modifier: Modifier ->
        s.scope(this, modifier)
    })
    return this
}

@Suppress("UNCHECKED_CAST")
fun <S : WidgetContainerScope> ConfigSerializable.guiWrapper(
    scope: S,
    modifier: Modifier
) {
    (this.getUserData(GUI_WRAPPER_KEY) as? GUIWrapper)?.invoke(scope, modifier)
        ?: ConfigWrapperMap.wrapper(this, scope, modifier)
}