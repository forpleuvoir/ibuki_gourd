package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPickButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/** 配色方案编辑器的缺省值。 */
object ColorSchemeEditorDefaults {

    /** 槽位按钮组的最大高度；[Dp.Unspecified] = 交给所在容器（对话框里由面板高度兜底）。 */
    val MaxHeight: Dp = Dp.Unspecified

    /**
     * 对话框面板宽度上限：槽位按钮两列平分面板，太窄会把 `onPrimaryContainer` 这类长槽位名折行，
     * 太宽则按钮被拉满屏幕。
     */
    val DialogWidth: Dp = 620.dp

    /** 对话框面板最大高度；[Dp.Unspecified] = 用满窗口可用高度（槽位区内部滚动）。 */
    val DialogMaxHeight: Dp = Dp.Unspecified

    /** 槽位行之间的间距。 */
    val SlotSpacing: Dp = 8.dp
}

/**
 * 配色槽位按钮组：`xx` 与 `onXx` **成对排在同一行**，每个按钮点开取色器改该槽位。
 *
 * 按钮底色即槽位色、内容写槽位名（内容色由 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ColorButton]
 * 按底色亮度取黑白），所以整组本身就是这份配色的预览。
 *
 * @param scheme 要编辑的配色
 * @param onColorChange 某个槽位改色后回调 `(槽位名, 新色)`
 * @param modifier 作用于整组
 * @param maxHeight 按钮组的最大高度；[Dp.Unspecified] 不限制
 */
@Composable
fun ColorSchemeSlotButtons(
    scheme: ColorScheme,
    onColorChange: (String, Color) -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp = ColorSchemeEditorDefaults.MaxHeight,
) {
    Column(
        modifier = modifier
            .then(if (maxHeight != Dp.Unspecified) Modifier.heightIn(max = maxHeight) else Modifier)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ColorSchemeEditorDefaults.SlotSpacing),
    ) {
        ColorSchemeSlots.forEach { (base, on) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ColorSchemeEditorDefaults.SlotSpacing),
            ) {
                ColorSchemeSlotButton(
                    name = base,
                    color = scheme.slot(base),
                    onValueChange = { onColorChange(base, it) },
                    modifier = Modifier.weight(1f),
                )
                if (on != null) {
                    ColorSchemeSlotButton(
                        name = on,
                        color = scheme.slot(on),
                        onValueChange = { onColorChange(on, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** 单个槽位的取色按钮：底色即槽位色，内容写槽位名。 */
@Composable
private fun ColorSchemeSlotButton(
    name: String,
    color: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    ColorPickButton(
        color = color,
        onValueChange = onValueChange,
        modifier = modifier,
        title = { Text(name) },
    ) {
        Text(name)
    }
}

/**
 * 配色方案编辑器（对话框）：逐槽位改一份 [ColorScheme]，确认时把结果交回。
 *
 * 编辑在**副本**上进行，取消即丢弃；整个对话框（面板 + 内容 + 取色器弹层）都套在这份副本的
 * 主题里，所以改一格就能立刻看到该色在界面里的实际观感。
 *
 * 顶栏三件事：
 * - 「默认浅色 / 默认深色」把副本整份换成对应内置配色（等于重新开始）；
 * - 「深色模式」开关只改副本的 [ColorScheme.isLight] 标记 —— 它决定这份配色被当作亮色还是
 *   暗色方案对待，不换算任何槽位色；
 * - 复制按钮把当前方案按主题 meta 的配色段格式写进剪贴板。
 *
 * @param scheme 打开时的配色
 * @param onSchemeChange 确认后回调结果
 * @param onDismiss 关闭（取消 / 点外部 / Esc）
 * @param modifier 作用于对话框面板
 * @param lightBase 「默认浅色」用的配色
 * @param darkBase 「默认深色」用的配色
 * @param width 面板宽度
 * @param maxHeight 面板最大高度；[Dp.Unspecified] = 用满窗口可用高度
 */
@Composable
fun ColorSchemeEditorDialog(
    scheme: ColorScheme,
    onSchemeChange: (ColorScheme) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    lightBase: ColorScheme = lightColorScheme(),
    darkBase: ColorScheme = darkColorScheme(),
    width: Dp = ColorSchemeEditorDefaults.DialogWidth,
    maxHeight: Dp = ColorSchemeEditorDefaults.DialogMaxHeight,
) {
    var draft by remember { mutableStateOf(scheme) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    // 像素放大倍率沿用注册处：对话框图层不自己分档，否则会与所在屏幕的组件对不上
    SokitsuTheme(colorScheme = draft, pixelScale = LocalSokitsuPixelScale.current) {
        FlexibleDialog(
            onDismissRequest = onDismiss,
            onConfirmRequest = {
                onSchemeChange(draft)
                true
            },
            modifier = modifier,
            title = { Text(IGLang.Theme.editorTitle) },
            maxWidth = width,
            maxHeight = maxHeight,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(ColorSchemeEditorDefaults.SlotSpacing)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ColorSchemeEditorDefaults.SlotSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FlatButton(onClick = { draft = lightBase }) {
                            Text(IGLang.Theme.baseFromLight)
                        }
                        FlatButton(onClick = { draft = darkBase }) {
                            Text(IGLang.Theme.baseFromDark)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(IGLang.Theme.darkMode)
                        Switch(checked = !draft.isLight, onCheckedChange = { draft = draft.copy(isLight = !it) })
                        IconButton(onClick = {
                            scope.launch { clipboard.setClipEntry(ClipEntry(draft.toMetaJson())) }
                        }) {
                            Icon(Icons.Copy)
                        }
                    }
                    ColorSchemeSlotButtons(
                        scheme = draft,
                        onColorChange = { name, color -> draft = draft.withSlot(name, color) },
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            },
        )
    }
}
