package moe.forpleuvoir.ibukigourd.config.metadata

//private const val GUI_WRAPPER_KEY = "#gui_wrapper"
//private typealias GUIWrapper = (ContainerScope, Modifier) -> Unit
//
//fun <C : ConfigNode> C.setGuiWrapper(
//    scope: ContainerScope.(C, Modifier) -> Unit,
//): C {
//    this.setMetadata(GUI_WRAPPER_KEY, { s: ContainerScope, modifier: Modifier ->
//        s.scope(this, modifier)
//    })
//    return this
//}
//
//@Suppress("UNCHECKED_CAST")
//fun <S : ContainerScope> ConfigNode.guiWrapper(
//    scope: S,
//    modifier: Modifier
//) {
//    (this.getMetadata(GUI_WRAPPER_KEY) as? GUIWrapper)?.invoke(scope, modifier)
//        ?: ConfigWrapperMap.wrapper(this, scope, modifier)
//}