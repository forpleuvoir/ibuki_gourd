package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeybind
import moe.forpleuvoir.ibukigourd.config.item.ConfigToggleKeybind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigEnum
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration

//region Metadata
private const val UI_WRAPPER_KEY = "#ui_wrapper"

fun interface ConfigUIWrapper<C : ConfigNode> {
    @Composable
    fun content(config: C)
}

fun <C : ConfigNode> C.uiWrapper(content: ConfigUIWrapper<C>): C {
    this.setMetadata(UI_WRAPPER_KEY, content)
    return this
}

@Composable
@Suppress("UNCHECKED_CAST")
fun <C : ConfigNode> ConfigUiWrapper(config: C) {
    (config.getMetadata(UI_WRAPPER_KEY) as? ConfigUIWrapper<C>)
        ?.content(config)
        ?: UIWrappers.Wrapper(config)
}
//endregion

private typealias Predicate = (ConfigNode) -> Boolean
private typealias WrapperEntry = Pair<Predicate, ConfigUIWrapper<*>>

private inline val WrapperEntry.predicate get() = first
private inline val WrapperEntry.wrapper get() = second

@Suppress("UNCHECKED_CAST")
object UIWrappers {

    private val wrappers = LinkedList<WrapperEntry>()

    fun register(predicate: Predicate, wrapper: ConfigUIWrapper<*>) {
        wrappers.addFirst(predicate to wrapper)
    }

    fun <C : ConfigNode> register(type: KClass<C>, strict: Boolean = true, wrapper: ConfigUIWrapper<C>) {
        register({
            if (strict) it::class == type
            else it::class.isSubclassOf(type)
        }, wrapper)
    }

    inline fun <reified C : ConfigNode> register(strict: Boolean = true, wrapper: ConfigUIWrapper<C>) =
        register(type = C::class, strict, wrapper)

    fun <C : Any> registerCheckValueType(type: KClass<C>, wrapper: ConfigUIWrapper<Config<C>>) {
        register({
            it is Config<*> && it.valueType == type
        }, wrapper)
    }

    inline fun <reified C : Any> registerCheckValueType(wrapper: ConfigUIWrapper<Config<C>>) =
        registerCheckValueType(type = C::class, wrapper)

    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <C : ConfigNode> Wrapper(config: C) {
        wrappers.find { it.predicate(config) }
            ?.let {
                (it.wrapper as? ConfigUIWrapper<C>)?.content(config)
                return
            }
        UnspecifiedConfigWrapper(config)
    }

    @Composable
    fun UnspecifiedConfigWrapper(config: ConfigNode) {
        ConfigRowWrapper(config) {
            Text(IGLang.Misc.unsupported)
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
        register({ it is Config<*> && it.valueType?.isSubclassOf(KeyCode::class) == true }) { KeyCodeConfigWrapper(it as Config<KeyCode>) }
        register<ConfigKeybind> { KeybindConfigWrapper(it) }
        register<ConfigToggleKeybind> { ToggleKeybindConfigWrapper(it) }
        register({ it is ConfigList<*> && it.elementType == String::class }) {
            @Suppress("UNCHECKED_CAST")
            StringListConfigWrapper(it as ConfigList<String>)
        }
        register({ it is ConfigMap<*> && it.entryValueType == String::class }) {
            @Suppress("UNCHECKED_CAST")
            StringMapConfigWrapper(it as ConfigMap<String>)
        }
        register({ it is ConfigList<*> && it.elementType == Pair::class && (it.isEmpty() || it[0].let { v -> v is Pair<*, *> && v.first is String && v.second is String }) }) {
            @Suppress("UNCHECKED_CAST")
            StringPairListConfigWrapper(it as ConfigList<Pair<String, String>>)
        }
    }

}