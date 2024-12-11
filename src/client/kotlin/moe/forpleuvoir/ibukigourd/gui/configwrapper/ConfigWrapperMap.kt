package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigDurationObject
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.item.impl.*
import kotlin.reflect.KClass

object ConfigWrapperMap {

    private val maps: MutableMap<KClass<out ConfigSerializable>, WidgetContainerScope.(ConfigSerializable, Modifier) -> Unit> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <C : ConfigSerializable, T : KClass<C>> register(type: T, wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        maps[type] = wrapper as WidgetContainerScope.(ConfigSerializable, Modifier) -> Unit
    }

    inline fun <reified C : ConfigSerializable> register(noinline wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        register(C::class, wrapper)
    }

    fun <T : ConfigSerializable, S : WidgetContainerScope> wrapper(config: T, scope: S, modifier: Modifier = Modifier) {
        maps[config::class]?.invoke(scope, config, modifier)
        if (maps[config::class] == null) {
            when (config) {
                is ConfigContainer -> scope.ConfigContainerWrapper(config, modifier)
                else               -> scope.UnspecifiedConfigWrapper(config, modifier)
            }
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
        register<ConfigStringList> { c, m -> StringListConfigWrapper(c, m) }
        register<ConfigStringMap> { c, m -> StringMapConfigWrapper(c, m) }
        register<ConfigKeyBind> { c, m -> ConfigKeyBindWrapper(c, m) }
        register<ConfigKeyBindBoolean> { c, m -> ConfigKeyBindBooleanWrapper(c, m) }
    }

}