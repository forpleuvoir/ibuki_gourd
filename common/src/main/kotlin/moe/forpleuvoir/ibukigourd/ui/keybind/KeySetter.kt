package moe.forpleuvoir.ibukigourd.ui.keybind

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.KeyTriggerTiming
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.KeybindSetting
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.appendLiteral
import moe.forpleuvoir.ibukigourd.text.appendNewLine
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SimpleAlertDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyCapture

/**
 * 绑定的悬浮提示：按键组合（无按键时提示"按下以设置"）+ 冲突绑定列表。
 *
 * 冲突读取 [InputHandler.keybindVersion]，绑定表变化时重组刷新。
 */
@Composable
fun Keybind.hoverText(): MutableText {
    InputHandler.keybindVersion
    val text = Literal(keys.joinToString(" + ") { it.keyName })
    if (keys.isEmpty()) text.append(IGLang.Input.pressToSetting)

    val conflicts = InputHandler.detectKeyConflicts(this).toList()
    if (conflicts.isNotEmpty()) {
        val conflictText = IGLang.Input.keybindConflict
        conflicts.forEach { conflictText.appendNewLine().appendLiteral(" - ").append(it.name) }
        text.appendNewLine().append(conflictText)
    }
    return text
}

/**
 * 单键设置按钮：点击进入捕获态，按下任意键即写入 [onValueChange] 并退出捕获。
 *
 * 捕获期间只认**最后按下**的那一个键：按钮与气泡都只显示它，不累积组合。
 * `Esc` / `Ctrl + Backspace` 取消；按键事件在捕获回调里被取消（含按住重复），不会传给游戏。
 *
 * @param value 当前键码
 * @param onValueChange 捕获到新键码时回调
 * @param modifier 应用到按钮的 Modifier
 * @param enabled 是否可用
 */
@Composable
fun KeyCodeSetButton(
    value: KeyCode,
    onValueChange: (KeyCode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var inputting by remember { mutableStateOf(false) }

    val captured = rememberKeyCapture(
        enabled = inputting,
        onCaptured = { keys ->
            keys.lastOrNull()?.let(onValueChange)
            inputting = false
        },
        onCancelled = { inputting = false },
    )
    val last = captured.lastOrNull()

    Button(
        onClick = { if (!inputting) inputting = true },
        modifier = modifier.tooltip(pinned = inputting) {
            if (inputting) CaptureHint(listOfNotNull(last))
            else Text(value.keyNameText.plainText, textAlign = TextAlign.Center)
        },
        enabled = enabled,
    ) {
        Icon(Icons.Keyboard)
        Spacer(Modifier.width(8.dp))
        Text(
            if (inputting) singleCaptureLabel(last) else value.keyNameText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 组合键设置按钮：点击进入捕获态，按下若干键（按按下顺序累计）后**全部松开**写入绑定。
 *
 * 捕获期间 `Esc` / `Ctrl + Backspace` 清空绑定；按键事件在捕获回调里被取消（含按住重复）。
 * 绑定与其它绑定冲突时按钮换成主题 `error` 配色。
 *
 * @param keybind 目标绑定，捕获完成后由 `setKey` 写入
 * @param onValueChange 写入后回调（参数即 [keybind] 自身）
 * @param modifier 应用到按钮的 Modifier
 * @param enabled 是否可用
 */
@Composable
fun KeybindSetButton(
    keybind: Keybind,
    onValueChange: (Keybind) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var inputting by remember { mutableStateOf(false) }

    val captured = rememberKeyCapture(
        enabled = inputting,
        onCaptured = { keys ->
            keybind.setKey(*keys.toTypedArray())
            onValueChange(keybind)
            inputting = false
        },
        onCancelled = {
            keybind.setKey()
            onValueChange(keybind)
            inputting = false
        },
    )
    InputHandler.keybindVersion
    val conflicted = InputHandler.detectKeyConflicts(keybind).any()
    val scheme = LocalColorScheme.current

    Button(
        onClick = { if (!inputting) inputting = true },
        modifier = modifier.tooltip(pinned = inputting) {
            if (inputting) CaptureHint(captured)
            else Text(keybind.hoverText().plainText, textAlign = TextAlign.Center)
        },
        enabled = enabled,
        colors = if (conflicted) ButtonDefaults.colors(color = scheme.error, contentColor = scheme.onError)
        else ButtonDefaults.colors(),
    ) {
        Icon(Icons.Keyboard)
        Spacer(Modifier.width(8.dp))
        Text(
            if (inputting) captureLabel(captured) else keybind.asText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 绑定设置按钮：图标按钮打开设置弹窗，确认后把整份 [KeybindSetting] 交回。
 *
 * @param keybindSetting 当前设置
 * @param onValueChange 确认后回调
 * @param modifier 应用到按钮的 Modifier
 * @param enabled 是否可用
 */
@Composable
fun KeybindSettingSetButton(
    keybindSetting: KeybindSetting,
    onValueChange: (KeybindSetting) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconScale: Int = 3,
    contentPadding: PaddingValues = IconButtonDefaults.contentPadding,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(keybindSetting) }
    val focusManager = LocalFocusManager.current

    /** 关闭设置弹窗：顺带清掉按钮焦点（`Button` 在 focused 时画描边，不清会"永久变色"）。 */
    fun closeDialog() {
        showDialog = false
        focusManager.clearFocus()
    }

    IconButton(
        onClick = {
            editing = keybindSetting
            showDialog = true
        },
        modifier = modifier.tooltip { Text(IGLang.Misc.edit) },
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Icon(Icons.Edit, scale = iconScale)
    }

    if (showDialog) {
        SimpleAlertDialog(
            onDismissRequest = { closeDialog() },
            onConfirmRequest = {
                onValueChange(editing)
                closeDialog()
                true
            },
            title = { Text(IGLang.Input.KeybindSetting.title) },
            content = {
                KeybindSettingColumn(
                    keybindSetting = editing,
                    onValueChange = { editing = it },
                )
            },
        )
    }
}

/**
 * 绑定设置表单：穿透 / 严格模式开关、触发环境与触发模式选择、长按阈值与重复间隔。
 *
 * 两个数值项只在对应触发模式下出现（长按类显示阈值、按住类显示间隔）。
 *
 * @param keybindSetting 当前设置
 * @param onValueChange 任一项变化时回调新设置
 * @param modifier 应用到表单列的 Modifier
 */
@Composable
fun KeybindSettingColumn(
    keybindSetting: KeybindSetting,
    onValueChange: (KeybindSetting) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingSwitchRow(
            label = { Text(IGLang.Input.KeybindSetting.passthrough, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.passthroughComment) }) },
            checked = keybindSetting.passthrough,
            onCheckedChange = { onValueChange(keybindSetting.copy(passthrough = it)) },
        )

        SettingSwitchRow(
            label = { Text(IGLang.Input.KeybindSetting.strict, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.strictComment) }) },
            checked = keybindSetting.strict,
            onCheckedChange = { onValueChange(keybindSetting.copy(strict = it)) },
        )

        Column {
            Text(IGLang.Input.KeybindSetting.env, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.envComment) })
            Spacer(Modifier.padding(top = 4.dp))
            Selector(
                selected = keybindSetting.env,
                onSelect = { onValueChange(keybindSetting.copy(env = it)) },
                items = KeyEnvironment.entries,
                content = { Text(environmentText(it)) },
                itemContent = { item, _ -> Text(environmentText(item)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column {
            Text(IGLang.Input.KeybindSetting.trigger, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.triggerComment) })
            Spacer(Modifier.padding(top = 4.dp))
            Selector(
                selected = keybindSetting.trigger,
                onSelect = { onValueChange(keybindSetting.copy(trigger = it)) },
                items = KeyTriggerTiming.entries,
                content = { Text(it.displayName) },
                itemContent = { item, _ -> Text(item.displayName, Modifier.tooltip { Text(item.comment) }) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = keybindSetting.trigger == KeyTriggerTiming.LongPress ||
                    keybindSetting.trigger == KeyTriggerTiming.WhileLongPressed,
        ) {
            Column {
                Text(IGLang.Input.KeybindSetting.longPressThreshold, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.longPressThresholdComment) })
                Spacer(Modifier.padding(top = 4.dp))
                IntField(
                    value = keybindSetting.longPressThreshold,
                    onValueChange = { value -> onValueChange(keybindSetting.copy(longPressThreshold = value)) },
                    valueRange = 1..200,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = keybindSetting.trigger == KeyTriggerTiming.WhilePressed ||
                    keybindSetting.trigger == KeyTriggerTiming.WhileLongPressed,
        ) {
            Column {
                Text(IGLang.Input.KeybindSetting.repeatInterval, Modifier.tooltip { Text(IGLang.Input.KeybindSetting.repeatIntervalComment) })
                Spacer(Modifier.padding(top = 4.dp))
                IntField(
                    value = keybindSetting.repeatInterval,
                    onValueChange = { value -> onValueChange(keybindSetting.copy(repeatInterval = value)) },
                    valueRange = 1..100,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** 一行「说明文字 + 开关」。 */
@Composable
private fun SettingSwitchRow(
    label: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        label()
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** 捕获过程中显示的按键组合；尚未按下任何键时提示按下。 */
private fun captureLabel(keys: List<KeyCode>): MutableText =
    if (keys.isEmpty()) IGLang.Input.pressToSetting
    else Literal(keys.joinToString(" + ") { it.keyName })

/**
 * 捕获中的气泡内容：按键行 + 释放保存说明行，逐行独立成节点并在列内居中
 * （合成单个多行文本时，短行的居中取决于渲染路径对 `textAlign` 的支持，故拆行）。
 */
@Composable
private fun CaptureHint(keys: List<KeyCode>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (keys.isEmpty()) IGLang.Input.pressToSetting.plainText else keys.joinToString(" + ") { it.keyName })
        Text(IGLang.Input.releaseToSaveSetting.plainText)
    }
}

/** 单键捕获过程中显示的按键：只显示最后按下的那一个，尚未按下时提示按下。 */
private fun singleCaptureLabel(key: KeyCode?): MutableText =
    key?.keyNameText ?: IGLang.Input.pressToSetting

/** 触发环境名称：`ibukigourd.input.key_environment.<key>`，缺翻译时回落枚举名。 */
private fun environmentText(environment: KeyEnvironment): MutableText =
    Translatable("${IbukiGourd.MOD_ID}.input.key_environment.${environment.key}", environment.name)
