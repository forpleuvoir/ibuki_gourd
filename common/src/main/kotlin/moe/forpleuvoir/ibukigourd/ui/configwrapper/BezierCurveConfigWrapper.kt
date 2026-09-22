package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditor
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlot
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlotDefaults
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContent
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContentCards
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.ui.util.values
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigList
import java.util.Locale

/**
 * 缓动曲线：行上只有控制点数值摘要，编辑按钮打开完整 [BezierCurveEditor] 浮层。
 *
 * 行内不放画布：速览尺寸下画布既看不清也难点准，数值摘要 + 浮层反而更好用。
 *
 * 编辑**即时写回**配置（弹窗的确认 / 取消都只是关闭，不回滚）——需要恢复默认值时用行尾的重置按钮。
 *
 * @param config 曲线配置项
 * @param modifier 作用于整行
 * @param yRange y 轴显示区间（同时作为编辑器的显示区间）
 */
@Composable
fun BezierCurveConfigWrapper(
    config: Config<CubicBezier>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
) {
    val value by config.asState()
    var editing by remember(config) { mutableStateOf(false) }

    ConfigRowWrapper(config, modifier) {
        ConfigControlBlock(
            action = {
                IconButton(
                    onClick = { editing = true },
                    contentPadding = ConfigControlDefaults.IconButtonPadding,
                ) {
                    Icon(Icons.Edit, scale = configIconScale())
                }
            },
        ) {
            Surface(
                modifier = Modifier.weight(1f).height(configControlHeight()),
                contentAlignment = Alignment.Center,
            ) {
                Text(ConfigBezierSummary(value))
            }
        }
    }

    if (editing) {
        FlexibleDialog(
            onDismissRequest = { editing = false },
            onConfirmRequest = { true },
            title = { ConfigDialogTitle(config) },
            content = {
                BezierCurveEditor(
                    value = value,
                    onValueChange = { config.setValue(it) },
                    yRange = yRange,
                    modifier = Modifier.padding(ConfigControlDefaults.CurveDialogPadding),
                )
            },
        )
    }
}

/**
 * 缓动曲线列表：**一个条目一张卡片**（一行 [BezierCardColumns] 张）—— 卡片头部两端是拖拽手柄与
 * "编辑 + 删除"一组按钮，卡片体是可直接拖的曲线画布；要填数值 / 套预设点编辑按钮开完整编辑器。
 *
 * 编辑即时写回副本，确认时才把整份列表写回配置；条目增删与拖拽排序都在浮层里做。
 *
 * @param config 曲线列表配置项
 * @param modifier 作用于整行
 * @param yRange y 轴显示区间（同时作为编辑器的显示区间）
 */
@Composable
fun BezierListConfigWrapper(
    config: ConfigList<CubicBezier>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }

    ConfigListRow(config, IGLang.ConfigWrapper.listConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        BezierListEditDialog(config, yRange, onDismiss = { editing = false })
    }
}

/**
 * 曲线列表编辑浮层：卡片式条目 + 增删 + 拖拽排序。
 *
 * 卡片体是完整编辑器，因此不走表格（表格是"多列同行"的骨架），改用 [EditDialogContentCards]。
 */
@Composable
private fun BezierListEditDialog(
    config: ConfigList<CubicBezier>,
    yRange: ClosedFloatingPointRange<Float>,
    onDismiss: () -> Unit,
) {
    val keyed = rememberKeyedList(config.getValue(), key = config)
    val newElement: () -> CubicBezier = { CubicBezier.Standard }
    // 正在展开编辑的条目：存条目 key 而不是下标 —— 拖拽重排后下标会变，key 不会
    var editingKey by remember { mutableStateOf<Long?>(null) }

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            config.setValue(keyed.entries.values())
            true
        },
        title = { ConfigDialogTitle(config) },
        minWidth = ConfigDialogDefaults.MinWidth,
        maxHeight = ConfigDialogDefaults.MaxHeight,
        content = {
            EditDialogContent(
                modifier = Modifier.width(ConfigDialogDefaults.CardContentWidth),
                header = {},
                addButton = {
                    Button(
                        onClick = { keyed.add(newElement()) },
                        contentPadding = ConfigControlDefaults.IconButtonPadding,
                    ) {
                        Icon(Icons.Add, scale = configIconScale())
                    }
                },
            ) {
                EditDialogContentCards(
                    state = keyed,
                    columns = BezierCardColumns,
                    actions = { index, _ ->
                        IconButton(
                            onClick = { editingKey = keyed.entries[index].key },
                            contentPadding = ConfigControlDefaults.IconButtonPadding,
                        ) {
                            Icon(Icons.Edit, scale = configIconScale())
                        }
                    },
                    content = { _, value, onValueChange ->
                        // 卡片体就是画布本体（铺满卡片宽度、可直接拖），要填数值 / 套预设再点头部编辑按钮
                        BezierCurvePlot(
                            value = value,
                            onValueChange = onValueChange,
                            yRange = yRange,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                    },
                )

                // 单张卡片的完整编辑器：叠在卡片浮层之上，改动即时写回副本
                val target = editingKey
                val index = target?.let { key -> keyed.entries.indexOfFirst { it.key == key } } ?: -1
                if (target != null && index >= 0) {
                    val value = keyed.entries[index].value
                    FlexibleDialog(
                        onDismissRequest = { editingKey = null },
                        onConfirmRequest = { true },
                        title = { Text(IGLang.Misc.edit) },
                        content = {
                            BezierCurveEditor(
                                value = value,
                                onValueChange = { keyed.setValue(index, it) },
                                yRange = yRange,
                                modifier = Modifier.padding(ConfigControlDefaults.CurveDialogPadding),
                            )
                        },
                    )
                }
            }
        },
    )
}

/** 卡片列表的列数：一行两张卡片（卡片只放速览，宽度够放两张即可）。 */
private const val BezierCardColumns = 2

/** 行上的控制点摘要：四个分量各两位小数（只说明"当前曲线长什么样"，编辑进弹窗）。 */
private fun ConfigBezierSummary(value: CubicBezier): String =
    "(%.2f, %.2f, %.2f, %.2f)".format(Locale.ROOT, value.x1, value.y1, value.x2, value.y2)
