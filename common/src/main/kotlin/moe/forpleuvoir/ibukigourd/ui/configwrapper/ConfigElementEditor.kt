package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPickButton
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlot
import moe.forpleuvoir.ibukigourd.ui.keybind.KeyCodeSetButton
import moe.forpleuvoir.ibukigourd.ui.keybind.KeybindSetButton
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DoubleField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DurationField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LongField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.toNebulaColor
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor
import moe.forpleuvoir.nebula.common.util.primitive.toTitleCase
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * 列表 / 映射条目里**单个值**的控件：注册进 [ConfigElementEditors] 后按值类型自动分发。
 *
 * 与 [ConfigUIWrapper] 的分工：那个注册的是"整行"（名称、注释、重置按钮），这个只要控件本身，
 * 用在编辑浮层的行内容槽位里。
 */
@Deprecated("列表/映射的元素控件应由调用方直接提供 Composable，勿再扩展此分发")
fun interface ConfigElementEditor {

    @Composable
    fun content(value: Any, onValueChange: (Any) -> Unit, modifier: Modifier)
}

/**
 * 条目控件与新增初值的注册表：[ConfigListWrapper] / [ConfigMapWrapper] 的浮层据此决定
 * "每行怎么编辑值、新增条目造什么初值"。
 *
 * 消费方 MOD 在这里注册自己的值类型，就能直接复用这两个浮层，而不必各写一份编辑浮层。
 * 两条扩展点都按**值类型**匹配（后注册优先）：
 * - [register]：命中时渲染条目控件；
 * - [registerFactory]：命中时作为新增条目的初值；两条都没命中则**不给新增按钮** ——
 *   塞一个无法序列化的初值只会把配置写坏。
 *
 * 控件也没命中时，条目退化成只读的 `toString()` 文本：不崩，也不会写坏配置。
 */
@Deprecated("列表/映射的元素控件与初值应由调用方直接提供，勿再扩展此注册表")
object ConfigElementEditors {

    private val editors = LinkedList<Pair<(KClass<*>) -> Boolean, ConfigElementEditor>>()
    private val factories = LinkedList<Pair<(KClass<*>) -> Boolean, (KClass<*>) -> (() -> Any)?>>()

    /** 按值类型谓词注册条目控件；后注册优先。 */
    fun register(predicate: (KClass<*>) -> Boolean, editor: ConfigElementEditor) {
        editors.addFirst(predicate to editor)
    }

    /** 按值类型注册条目控件：命中 [T] 本身、其子类与其接口实现。 */
    inline fun <reified T : Any> register(editor: ConfigElementEditor) =
        register({ T::class.java.isAssignableFrom(it.java) }, editor)

    /**
     * 按值类型谓词注册新增初值工厂；后注册优先。
     *
     * 工厂拿到元素类型，返回该类型的初值；返回 null 表示"给不出安全初值"（与未注册同样处理）。
     */
    fun registerFactory(predicate: (KClass<*>) -> Boolean, factory: (KClass<*>) -> (() -> Any)?) {
        factories.addFirst(predicate to factory)
    }

    /** 按值类型注册固定初值：命中 [T] 本身、其子类与其接口实现。 */
    inline fun <reified T : Any> registerFactory(noinline value: () -> Any) =
        registerFactory({ T::class.java.isAssignableFrom(it.java) }) { value }

    /** 该值类型命中的条目控件；未注册返回 null。 */
    fun editorOf(type: KClass<*>): ConfigElementEditor? = editors.firstOrNull { it.first(type) }?.second

    /** 该值类型命中的新增初值工厂；未注册（或给不出初值）返回 null。 */
    fun factoryOf(type: KClass<*>): (() -> Any)? = factories.firstOrNull { it.first(type) }?.second?.invoke(type)

    init {
        register<String> { value, onValueChange, modifier ->
            StringElementEditor(value as String, onValueChange, modifier)
        }
        register<Boolean> { value, onValueChange, _ ->
            Switch(checked = value as Boolean, onCheckedChange = { onValueChange(it) })
        }
        register<Int> { value, onValueChange, modifier ->
            IntField(value as Int, { onValueChange(it) }, modifier = modifier)
        }
        register<Long> { value, onValueChange, modifier ->
            LongField(value as Long, { onValueChange(it) }, modifier = modifier)
        }
        register<Float> { value, onValueChange, modifier ->
            FloatField(value as Float, { onValueChange(it) }, modifier = modifier)
        }
        register<Double> { value, onValueChange, modifier ->
            DoubleField(value as Double, { onValueChange(it) }, modifier = modifier)
        }
        register<Duration> { value, onValueChange, modifier ->
            DurationField(value as Duration, { onValueChange(it) }, modifier = modifier)
        }
        register<CubicBezier> { value, onValueChange, modifier ->
            BezierCurvePlot(
                value = value as CubicBezier,
                onValueChange = { onValueChange(it) },
                modifier = modifier.size(ConfigControlDefaults.CurvePreviewSize),
            )
        }
        register<NebulaColor> { value, onValueChange, modifier ->
            ColorPickButton(
                color = (value as NebulaColor).toComposeColor(),
                onValueChange = { onValueChange(it.toNebulaColor()) },
                modifier = modifier,
            )
        }
        register({ it.java.isEnum }) { value, onValueChange, modifier ->
            EnumElementEditor(value as Enum<*>, onValueChange, modifier)
        }
        register<Keybind> { value, onValueChange, modifier ->
            KeybindElementEditor(value as Keybind, onValueChange, modifier)
        }
        // 按键码后注册（优先级高于枚举）：`Keyboard` 一类枚举同时实现 KeyCode，
        // 列表里出现按键码时应当给"按键捕获"而不是上百项的下拉
        register<KeyCode> { value, onValueChange, modifier ->
            KeyCodeSetButton(value as KeyCode, { onValueChange(it) }, modifier = modifier)
        }

        registerFactory<String> { "" }
        registerFactory<Boolean> { false }
        registerFactory<Int> { 0 }
        registerFactory<Long> { 0L }
        registerFactory<Float> { 0f }
        registerFactory<Double> { 0.0 }
        registerFactory<Duration> { Duration.ZERO }
        registerFactory<CubicBezier> { CubicBezier.Standard }
        registerFactory<NebulaColor> { NebulaColor.fromARGB(0xFFFFFFFF.toInt()) }
        // 枚举：第一个常量（枚举至少有一个常量，且一定是合法取值）
        registerFactory({ it.java.isEnum }) { type ->
            type.java.enumConstants?.firstOrNull()?.let { first -> { first } }
        }
    }
}

/**
 * 条目控件的分发：按**值的运行时类型**从 [ConfigElementEditors] 取控件，未注册时给只读文本。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param modifier 作用于控件
 */
@Deprecated("元素控件分发将由调用方直接提供，勿再使用")
@Composable
internal fun ConfigElementValue(
    value: Any,
    onValueChange: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    val editor = ConfigElementEditors.editorOf(value::class)
    if (editor == null) {
        Text(value.toString(), modifier = modifier.fillMaxWidth())
    } else {
        editor.content(value, onValueChange, modifier)
    }
}

/**
 * 字符串元素：单行框 + 尾部「编辑」按钮，按钮打开多行编辑浮层（与字符串配置同一套）。
 *
 * 单行框塞不下长文本，行内又没法换行，所以长字符串统一走浮层编辑。
 */
@Composable
private fun StringElementEditor(
    value: String,
    onValueChange: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StringValueField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { editing = true },
            contentPadding = ConfigControlDefaults.IconButtonPadding,
        ) {
            Icon(Icons.Edit, scale = LocalSokitsuPixelScale.current)
        }
    }

    if (editing) {
        StringEditDialog(
            title = { Text(IGLang.Misc.edit) },
            initial = value,
            onDismiss = { editing = false },
            onConfirm = {
                onValueChange(it)
                editing = false
            },
        )
    }
}

/**
 * 组合键元素：捕获是**原地修改** keybind 对象，列表快照看到的是同一个实例、不会触发重组，
 * 因此这里保留一个版本号并用 `key(version)` 重建按钮让显示文本跟上（与 `KeybindConfigWrapper` 同款做法）。
 */
@Composable
private fun KeybindElementEditor(
    keybind: Keybind,
    onValueChange: (Any) -> Unit,
    modifier: Modifier,
) {
    var version by remember { mutableIntStateOf(0) }
    key(version) {
        KeybindSetButton(
            keybind = keybind,
            onValueChange = {
                onValueChange(it)
                version++
            },
            modifier = modifier,
        )
    }
}

/** 枚举元素：下拉选择，选项文案见 [enumLabel]。 */
@Composable
private fun EnumElementEditor(
    value: Enum<*>,
    onValueChange: (Any) -> Unit,
    modifier: Modifier,
) {
    val items = enumConstantsOf(value)
    Selector(
        selected = value,
        onSelect = { onValueChange(it) },
        items = items,
        content = { Text(component = it.enumLabel()) },
        itemContent = { item, _ -> Text(component = item.enumLabel()) },
        searchFilter = if (items.size > ConfigControlDefaults.EnumSelectSearchThreshold) {
            { item, query -> item.enumLabel().plainText.contains(query, ignoreCase = true) }
        } else {
            null
        },
        modifier = modifier,
    )
}

/** 枚举常量的全部取值；`declaringJavaClass` 的用意见 [enumLabel]。 */
private fun enumConstantsOf(value: Enum<*>): List<Enum<*>> =
    value.declaringJavaClass.enumConstants?.toList() ?: listOf(value)

/**
 * 枚举文案：键与回落规则同 `Enum.translateText`（`enum.<类名>.<常量名>`，回落 `TitleCase(常量名)`）。
 *
 * 与那个扩展的区别是取 [Enum.declaringJavaClass]：带常量体的枚举其 `javaClass` 是匿名子类，
 * 用它拼键会拼出匿名类名。
 */
private fun Enum<*>.enumLabel(): MutableText =
    Translatable("enum.${declaringJavaClass.name}.$name", name.toTitleCase())
