package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.ConfigVector2f
import moe.forpleuvoir.ibukigourd.config.item.ConfigVector3f
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.item.impl.*
import java.util.*
import kotlin.reflect.KClass

private typealias Wrapper = WidgetContainerScope.(ConfigSerializable, Modifier) -> Unit
private typealias Predicate = (ConfigSerializable) -> Boolean


object ConfigWrapperMap {

    private val wrappers: MutableList<Pair<Predicate, Wrapper>> = LinkedList()

    fun register(predicate: Predicate, wrapper: Wrapper) {
        wrappers.addFirst(predicate to wrapper)
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : ConfigSerializable, T : KClass<C>> register(type: T, wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        register({ it::class == type }, wrapper as Wrapper)
    }

    inline fun <reified C : ConfigSerializable> register(noinline wrapper: WidgetContainerScope.(C, Modifier) -> Unit) {
        register(C::class, wrapper)
    }

    fun <T : ConfigSerializable, S : WidgetContainerScope> wrapper(config: T, scope: S, modifier: Modifier = Modifier) {
        wrappers.find { it.first(config) }
            ?.let {
                it.second.invoke(scope, config, modifier)
                return
            }
        scope.UnspecifiedConfigWrapper(config, modifier)
    }

    init {
        //------------ DefaultConfigContainer ------------\\
        register(predicate = { it is ConfigContainer }, wrapper = { c, m ->
            if (c is ConfigContainer) this.ConfigContainerWrapper(c, m)
        })
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
        register<ConfigDuration> { c, m -> ConfigDurationWrapper(c, m) }
        register<ConfigStringList> { c, m -> StringListConfigWrapper(c, m) }
        register<ConfigStringMap> { c, m -> StringMapConfigWrapper(c, m) }
        register<ConfigKeyBind> { c, m -> ConfigKeyBindWrapper(c, m) }
        register<ConfigKeyBindBoolean> { c, m -> ConfigKeyBindBooleanWrapper(c, m) }
        register<ConfigVector2f> { c, m -> ConfigVector2fWrapper(c, m) }
        register<ConfigVector3f> { c, m -> ConfigVector3fWrapper(c, m) }
    }

}