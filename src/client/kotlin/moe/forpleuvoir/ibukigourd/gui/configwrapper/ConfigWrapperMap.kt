package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigDurationObject
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.impl.*
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
        if (maps[config::class] == null) {
            scope.UnspecifiedConfigWrapper(config, modifier)
        }
    }

    init {
        //------------ Number ------------\\
        register<ConfigInt> { c, m -> IntConfigWrapper(c, m) }
        register<ConfigLong> { c, m -> LongConfigWrapper(c, m) }
        register<ConfigFloat> { c, m -> FloatConfigWrapper(c, m) }
        register<ConfigDouble> { c, m -> DoubleConfigWrapper(c, m) }
        //------------ Primitive ------------\\
        register<ConfigString> { c, m -> StringConfigWrapper(c, m) }
        register<ConfigBoolean> { c, m -> BooleanConfigWrapper(c, m) }
        register<ConfigEnum<*>> { c, m -> EnumConfigWrapper(c, m) }
        //------------ Other ------------\\
        @Suppress("UNCHECKED_CAST")
        register<ConfigColor> { c, m -> ColorConfigWrapper(c as ConfigRGBColor<ARGBColor>, m) }
        @Suppress("UNCHECKED_CAST")
        register<ConfigHSVColor> { c, m -> ColorConfigWrapper(c as ConfigRGBColor<ARGBColor>, m) }
        register<ConfigDurationObject> { c, m -> ConfigDurationWrapper(c, m) }
        //TODO ConfigStringList
        //TODO ConfigStringMap
        register<ConfigKeyBind> { c, m -> ConfigKeyBindWrapper(c, m) }
        register<ConfigKeyBindBoolean> { c, m -> ConfigKeyBindBooleanWrapper(c, m) }


    }

}