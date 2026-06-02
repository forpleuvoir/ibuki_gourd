package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigEnum
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration

//region Metadata
private const val UI_WRAPPER_KEY = "#ui_wrapper"

fun interface UIWrapper<C : ConfigNode> {
    @Composable
    fun content(config: C)
}

fun <C : ConfigNode> C.uiWrapper(content: UIWrapper<C>): C {
    this.setMetadata(UI_WRAPPER_KEY, content)
    return this
}

@Composable
@Suppress("UNCHECKED_CAST")
fun <C : ConfigNode> UiWrapper(config: C) {
    (config.getMetadata(UI_WRAPPER_KEY) as? UIWrapper<C>)
        ?.content(config)
        ?: UIWrappers.Wrapper(config)
}
//endregion

private typealias Predicate = (ConfigNode) -> Boolean
private typealias WrapperEntry = Pair<Predicate, UIWrapper<*>>

private inline val WrapperEntry.predicate get() = first
private inline val WrapperEntry.wrapper get() = second

@Suppress("UNCHECKED_CAST")
object UIWrappers {

    private val wrappers = LinkedList<WrapperEntry>()

    fun register(predicate: Predicate, wrapper: UIWrapper<*>) {
        wrappers.addFirst(predicate to wrapper)
    }

    fun <C : ConfigNode> register(type: KClass<C>, strict: Boolean = true, wrapper: UIWrapper<C>) {
        register({
            if (strict) it::class == type
            else it::class.isSubclassOf(type)
        }, wrapper)
    }

    inline fun <reified C : ConfigNode> register(strict: Boolean = true, wrapper: UIWrapper<C>) =
        register(type = C::class, strict, wrapper)

    fun <C : Any> registerCheckValueType(type: KClass<C>, wrapper: UIWrapper<Config<C>>) {
        register({
            it is Config<*> && it.valueType == type
        }, wrapper)
    }

    inline fun <reified C : Any> registerCheckValueType(wrapper: UIWrapper<Config<C>>) =
        registerCheckValueType(type = C::class, wrapper)

    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <C : ConfigNode> Wrapper(config: C) {
        wrappers.find { it.predicate(config) }
            ?.let {
                (it.wrapper as? UIWrapper<C>)?.content(config)
                return
            }
        UnspecifiedConfigWrapper(config)
    }

    @Composable
    fun UnspecifiedConfigWrapper(config: ConfigNode) {
        ConfigRowWrapper(config) {
            Text(IGLang.unsupported)
        }
    }

    init {
        register({ it is ConfigGroup }) {
            ConfigGroupWrapper(it as ConfigGroup)
        }
        //region Primitive
        registerCheckValueType<Int> { IntConfigWrapper(it) }
        registerCheckValueType<Long> { LongConfigWrapper(it) }
        registerCheckValueType<Float> { FloatConfigWrapper(it) }
        registerCheckValueType<Double> { DoubleConfigWrapper(it) }
        registerCheckValueType<Boolean> { BooleanConfigWrapper(it) }
        registerCheckValueType<String> { StringConfigWrapper(it) }
        //endregion
        register<ConfigEnum<*>> { EnumConfigWrapper(it) }
        registerCheckValueType<Color> { ColorConfigWrapper(it) }
        registerCheckValueType<Duration> { DurationConfigWrapper(it) }
    }

}