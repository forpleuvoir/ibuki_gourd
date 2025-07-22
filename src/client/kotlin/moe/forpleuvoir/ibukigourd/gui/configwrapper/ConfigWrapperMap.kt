package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.*
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyCode
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.expandableConfigContainerLimit
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.config.item.impl.*
import java.util.*
import kotlin.reflect.KClass

private typealias Wrapper = ContainerScope.(ConfigSerializable, Modifier) -> Unit
private typealias Predicate = (ConfigSerializable) -> Boolean
private typealias WrapperEntry = Pair<Predicate, Wrapper>

private val WrapperEntry.predicate get() = first
private val WrapperEntry.wrapper get() = second

@Suppress("UNCHECKED_CAST")
object ConfigWrapperMap {

    private val wrappers: MutableList<WrapperEntry> = LinkedList()

    fun register(predicate: Predicate, wrapper: Wrapper) {
        wrappers.addFirst(predicate to wrapper)
    }

    fun <C : ConfigSerializable, T : KClass<C>> register(type: T, wrapper: ContainerScope.(C, Modifier) -> Unit) {
        register({ it::class == type }, wrapper as Wrapper)
    }

    inline fun <reified C : ConfigSerializable> register(noinline wrapper: ContainerScope.(C, Modifier) -> Unit) {
        register(C::class, wrapper)
    }

    fun <T : ConfigSerializable, S : ContainerScope> wrapper(config: T, scope: S, modifier: Modifier = Modifier) {
        wrappers.find { it.predicate(config) }
            ?.let {
                it.wrapper.invoke(scope, config, modifier)
                return
            }
        scope.UnspecifiedConfigWrapper(config, modifier)
    }


    init {
        //------------ DefaultConfigContainer ------------\\
        register(predicate = { it is ConfigContainer }, wrapper = { c, m ->
            if (c is ConfigContainer) {
                if (c.configs().size > expandableConfigContainerLimit)
                    this.ConfigContainerWrapper(c, m)
                else
                    this.ExpandableConfigContainerWrapper(c, m)
            }
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

        //------------ Vector ------------\\
        register<ConfigVector2i> { c, m -> ConfigVector2iWrapper(c, m) }
        register<ConfigVector2f> { c, m -> ConfigVector2fWrapper(c, m) }
        register<ConfigVector2d> { c, m -> ConfigVector2dWrapper(c, m) }

        register<ConfigVector3i> { c, m -> ConfigVector3iWrapper(c, m) }
        register<ConfigVector3f> { c, m -> ConfigVector3fWrapper(c, m) }
        register<ConfigVector3d> { c, m -> ConfigVector3dWrapper(c, m) }

        //------------ Collection ------------\\
        register<ConfigStringList> { c, m -> StringListConfigWrapper(c, modifier = m) }
        register<ConfigStringMap> { c, m -> StringMapConfigWrapper(c, modifier = m) }
        register<ConfigPairList<String, String>> { c, m -> StringPairListConfigWrapper(c, modifier = m) }
        register<ConfigKeyBind> { c, m -> ConfigKeyBindWrapper(c, m) }
        register<ConfigKeyCode> { c, m -> ConfigKeyCodeWrapper(c, m) }
        register<ConfigKeyBindBoolean> { c, m -> ConfigKeyBindBooleanWrapper(c, m) }

        //------------ Other ------------\\
        register<ConfigColor> { c, m -> ColorConfigWrapper(c as ConfigRGBColor<ARGBColor>, m) }
        register<ConfigHSVColor> { c, m -> ColorConfigWrapper(c as ConfigRGBColor<ARGBColor>, m) }

        register<ConfigDuration> { c, m -> ConfigDurationWrapper(c, m) }
    }

}