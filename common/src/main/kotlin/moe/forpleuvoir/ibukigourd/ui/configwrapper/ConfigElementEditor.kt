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
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keybind
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
import kotlin.reflect.KClass
import kotlin.time.Duration

/** 选项数超过该值时给枚举选择器挂搜索框。 */
private const val EnumSearchThreshold = 10

/**
 * 列表 / 映射条目编辑器的取控件分发：按**值的运行时类型**给一个不带行骨架的控件。
 *
 * 与 `UIWrappers` 的区别：注册表分发的是"整行"（名称、注释、重置按钮），这里只要控件本身，
 * 用在编辑弹窗的行内容槽位里。因此不做谓词注册表，直接按类型分支 —— 支持的类型是显式的，
 * 未列出的类型退化为只读文本（不会崩、也不会写坏配置）。
 *
 * 分支顺序有讲究：[KeyCode] 在前、`Enum<*>` 在后 —— `Keyboard` 一类枚举同时实现 [KeyCode]，
 * 列表里出现按键码时应当给"按键捕获"而不是上百项的下拉。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param modifier 作用于控件
 */
@Composable
internal fun ConfigElementEditor(
    value: Any,
    onValueChange: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (value) {
        is String -> StringElementEditor(value, onValueChange, modifier)
        is Boolean -> Switch(checked = value, onCheckedChange = { onValueChange(it) })
        is Int -> IntField(value, { onValueChange(it) }, modifier = modifier)
        is Long -> LongField(value, { onValueChange(it) }, modifier = modifier)
        is Float -> FloatField(value, { onValueChange(it) }, modifier = modifier)
        is Double -> DoubleField(value, { onValueChange(it) }, modifier = modifier)
        is Duration -> DurationField(value, { onValueChange(it) }, modifier = modifier)

        is KeyCode -> KeyCodeSetButton(value, { onValueChange(it) }, modifier = modifier)
        is Keybind -> KeybindElementEditor(value, onValueChange, modifier)
        is Enum<*> -> EnumElementEditor(value, onValueChange, modifier)

        is CubicBezier -> BezierCurvePlot(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = modifier.size(ConfigControlDefaults.CurvePreviewSize),
        )

        is NebulaColor -> ColorPickButton(
            color = value.toComposeColor(),
            onValueChange = { onValueChange(it.toNebulaColor()) },
            modifier = modifier,
        )

        else -> Text(value.toString(), modifier = modifier.fillMaxWidth())
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
        searchFilter = if (items.size > EnumSearchThreshold) {
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

/**
 * 元素类型的默认值工厂（新增条目的初值）。
 *
 * 只覆盖能给出安全初值的常见类型；返回 null 表示"不知道该造什么" —— 调用方据此**不提供新增按钮**，
 * 而不是塞一个会序列化失败的值进去。
 */
internal fun defaultElementFactory(elementType: KClass<*>): (() -> Any)? = when (elementType) {
    String::class -> { { "" } }
    Boolean::class -> { { false } }
    Int::class -> { { 0 } }
    Long::class -> { { 0L } }
    Float::class -> { { 0f } }
    Double::class -> { { 0.0 } }
    Duration::class -> { { Duration.ZERO } }
    CubicBezier::class -> { { CubicBezier.Standard } }
    NebulaColor::class -> { { NebulaColor.fromARGB(0xFFFFFFFF.toInt()) } }
    // 枚举：第一个常量（枚举至少有一个常量，且一定是合法取值）
    else -> elementType.java.enumConstants?.firstOrNull()?.let { first -> { first } }
}
