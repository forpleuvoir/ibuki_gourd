package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.impl.ConfigDouble
import moe.forpleuvoir.nebula.config.item.impl.ConfigFloat
import moe.forpleuvoir.nebula.config.item.impl.ConfigInt
import moe.forpleuvoir.nebula.config.item.impl.ConfigLong
import kotlin.reflect.KClass

object ConfigWrapperMap {

    private val maps: MutableMap<KClass<out Config<*, *>>, WidgetContainerScope.(Config<*, *>, Modifier) -> Unit> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <C : Config<*, *>, T : KClass<C>> register(type: T, wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        maps[type] = wrapper as WidgetContainerScope.(Config<*, *>, Modifier) -> Unit
    }

    inline fun <reified C : Config<*, *>> register(noinline wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        register(C::class, wrapper)
    }

    fun <T : Config<*, *>, S : WidgetContainerScope> wrapper(config: T, scope: S, modifier: Modifier = Modifier) {
        maps[config::class]?.invoke(scope, config, modifier)
    }

    init {
        //------------ Number ------------\\
        register<ConfigInt> { c, m -> IntConfigWrapper(c, m) }
        register<ConfigLong> { c, m -> LongConfigWrapper(c, m) }
        register<ConfigFloat> { c, m -> FloatConfigWrapper(c, m) }
        register<ConfigDouble> { c, m -> DoubleConfigWrapper(c, m) }
    }

}