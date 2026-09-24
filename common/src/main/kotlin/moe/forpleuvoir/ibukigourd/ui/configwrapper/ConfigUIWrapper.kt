package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeybind
import moe.forpleuvoir.ibukigourd.config.item.ConfigToggleKeybind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.nebula.config.item.ConfigEnum
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor
import org.joml.Vector2dc
import org.joml.Vector2fc
import org.joml.Vector2ic
import org.joml.Vector3dc
import org.joml.Vector3fc
import org.joml.Vector3ic
import kotlin.time.Duration
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private const val UI_WRAPPER_KEY = "#ui_wrapper"

/**
 * 单个配置节点的 UI 呈现：把 [ConfigNode] 画成一行（分组则画成一组）。
 *
 * 注册进 [UIWrappers] 后由类型自动分发；单个节点也可以在构建期用 [uiWrapper] 指定专用实现。
 */
fun interface ConfigUIWrapper<C : ConfigNode> {

    @Composable
    fun content(config: C)
}

/**
 * 给该节点指定专用 wrapper：优先级高于 [UIWrappers] 的类型分发。
 *
 * 用于"某个配置项长得特殊、不适合按类型统一呈现"的场景；重复调用以最后一次为准。
 */
fun <C : ConfigNode> C.uiWrapper(content: ConfigUIWrapper<C>): C {
    setMetadata(UI_WRAPPER_KEY, content)
    return this
}

/**
 * 按 [UIWrappers] 的类型分发呈现一个配置节点。
 *
 * 查找顺序：节点自带的 [uiWrapper] → [UIWrappers] 中第一个命中的谓词 → [UnspecifiedConfigWrapper]。
 */
@Composable
@Suppress("UNCHECKED_CAST")
fun <C : ConfigNode> ConfigUiWrapper(config: C) {
    val override = config.getMetadata(UI_WRAPPER_KEY) as? ConfigUIWrapper<C>
    if (override != null) {
        override.content(config)
        return
    }

    val wrapper = UIWrappers.find(config)
    if (wrapper != null) {
        wrapper.content(config)
    } else {
        UnspecifiedConfigWrapper(config)
    }
}

/**
 * 没有命中任何 wrapper 时的兜底行：显示名称 + "暂不支持（类型）"。
 *
 * 带上类型名是为了**能直接定位**：新增配置类型后忘了注册 wrapper 时，
 * 页面上不止看到"暂不支持"，还知道缺的是哪个类型（`Char` / `BigDecimal` …），
 * 而不是要靠翻代码比对。
 */
@Composable
fun UnspecifiedConfigWrapper(config: ConfigNode) {
    ConfigRowWrapper(config) {
        Text(IGLang.Misc.unsupported.append(" (${config.typeLabel()})"))
    }
}

/** 节点承载的值类型名；取不到时退回节点实现的类名。 */
private fun ConfigNode.typeLabel(): String =
    (this as? Config<*>)?.valueType?.simpleName ?: this::class.simpleName ?: "?"

private typealias Predicate = (ConfigNode) -> Boolean
private typealias WrapperEntry = Pair<Predicate, ConfigUIWrapper<*>>

/**
 * wrapper 注册表：按注册顺序（后注册优先）匹配第一个命中的谓词。
 *
 * 两种注册方式：按节点类型（[register]）或按配置的**值类型**（[registerCheckValueType]）。
 * 消费方 MOD 可以在自己的初始化里注册自定义类型的 wrapper —— 这是配置 GUI 的对外扩展点。
 *
 * 另两个扩展点：
 * - 单个节点可用 [uiWrapper] 指定专用 wrapper（优先于本表）；
 * - [LocalSearchFilter] 供**自行搭页面**的调用方在分组内做过滤；本仓的
 *   [ConfigManagerWrapper] 页面不用它 —— 它的搜索是跨分组平铺 + 面包屑，不走分组过滤。
 */
object UIWrappers {

    private val wrappers = LinkedList<WrapperEntry>()

    /** 按任意谓词注册（最灵活，优先级最高）。 */
    fun register(predicate: Predicate, wrapper: ConfigUIWrapper<*>) {
        wrappers.addFirst(predicate to wrapper)
    }

    /**
     * 按节点类型注册。
     *
     * @param strict true 时要求类型完全相等（默认），false 时接受子类
     */
    fun <C : ConfigNode> register(type: KClass<C>, strict: Boolean = true, wrapper: ConfigUIWrapper<C>) {
        register(
            { if (strict) it::class == type else it::class.isSubclassOf(type) },
            wrapper,
        )
    }

    /** 按节点类型注册（reified 版本），见 [register]。 */
    inline fun <reified C : ConfigNode> register(strict: Boolean = true, wrapper: ConfigUIWrapper<C>) =
        register(type = C::class, strict = strict, wrapper = wrapper)

    /**
     * 按配置的**值类型**注册（`Config<T>` 的 `T`）。
     *
     * @param strict true 时要求值类型完全相等，false 时接受子类（如 `Vector2fc` 之于 `Vector2f`）
     */
    fun <C : Any> registerCheckValueType(
        type: KClass<C>,
        strict: Boolean = true,
        wrapper: ConfigUIWrapper<Config<C>>,
    ) {
        register(
            { node ->
                node is Config<*> &&
                        if (strict) node.valueType == type else node.valueType?.isSubclassOf(type) == true
            },
            wrapper,
        )
    }

    /** 按配置的值类型注册（reified 版本），见 [registerCheckValueType]。 */
    inline fun <reified C : Any> registerCheckValueType(
        strict: Boolean = true,
        wrapper: ConfigUIWrapper<Config<C>>,
    ) = registerCheckValueType(type = C::class, strict = strict, wrapper = wrapper)

    /** 找到第一个命中的 wrapper；调用方负责 invoke（找不到返回 null，由兜底行处理）。 */
    @Suppress("UNCHECKED_CAST")
    fun <C : ConfigNode> find(config: C): ConfigUIWrapper<C>? =
        wrappers.find { it.first(config) }?.second as? ConfigUIWrapper<C>

    init {
        // 用谓词而不是 register<ConfigGroup>：分组普遍写成 `object X : ConfigGroup("x")`
        // （子类），而 register 缺省 strict = true 只认"类完全相同"，会把子类全漏到兜底行
        register({ it is ConfigGroup }) { ConfigGroupWrapper(it as ConfigGroup) }

        // 基础类型
        registerCheckValueType<Boolean> { BooleanConfigWrapper(it) }
        registerCheckValueType<Int> { IntConfigWrapper(it) }
        registerCheckValueType<Long> { LongConfigWrapper(it) }
        registerCheckValueType<Float> { FloatConfigWrapper(it) }
        registerCheckValueType<Double> { DoubleConfigWrapper(it) }
        registerCheckValueType<String> { StringConfigWrapper(it) }

        // 其它单值类型
        registerCheckValueType<CubicBezier> { BezierCurveConfigWrapper(it) }
        register<ConfigEnum<*>> { EnumConfigWrapper(it) }
        registerCheckValueType<NebulaColor> { ColorConfigWrapper(it) }
        registerCheckValueType<Duration> { DurationConfigWrapper(it) }

        // 列表 / 映射（值类型都是 MutableList / Map，靠 elementType / entryValueType 区分）
        register({ it is ConfigList<*> }) {
            @Suppress("UNCHECKED_CAST")
            ConfigListWrapper(it as ConfigList<Any>)
        }
        register({ it is ConfigMap<*> }) {
            @Suppress("UNCHECKED_CAST")
            ConfigMapWrapper(it as ConfigMap<Any>)
        }
        // 对列表比对列表更特殊，后注册（优先级更高）以免被上面那条吃掉
        register({ it is ConfigList<*> && it.elementType == Pair::class }) {
            @Suppress("UNCHECKED_CAST")
            PairListConfigWrapper(it as ConfigList<Pair<Any, Any>>)
        }
        // 曲线列表：每条曲线一张卡片（卡片体是完整编辑器），同样要比普通列表特殊
        register({ it is ConfigList<*> && it.elementType == CubicBezier::class }) {
            @Suppress("UNCHECKED_CAST")
            BezierListConfigWrapper(it as ConfigList<CubicBezier>)
        }

        // 输入
        registerCheckValueType<KeyCode>(strict = false) { KeyCodeConfigWrapper(it) }
        register<ConfigKeybind> { KeybindConfigWrapper(it) }
        register<ConfigToggleKeybind> { ToggleKeybindConfigWrapper(it) }

        // 向量：值类型是 Vector2f 等实现类，故按接口宽松匹配
        registerCheckValueType<Vector2ic>(strict = false) { Vector2iConfigWrapper(it) }
        registerCheckValueType<Vector2fc>(strict = false) { Vector2fConfigWrapper(it) }
        registerCheckValueType<Vector2dc>(strict = false) { Vector2dConfigWrapper(it) }
        registerCheckValueType<Vector3ic>(strict = false) { Vector3iConfigWrapper(it) }
        registerCheckValueType<Vector3fc>(strict = false) { Vector3fConfigWrapper(it) }
        registerCheckValueType<Vector3dc>(strict = false) { Vector3dConfigWrapper(it) }
    }
}
