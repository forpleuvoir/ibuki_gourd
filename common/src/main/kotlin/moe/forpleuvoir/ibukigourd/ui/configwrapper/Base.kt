package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigRowWrapper.LocalIcon
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.Replay
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.common.api.Observable
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigNode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ConfigRowWrapper {

    val padding: PaddingValues
        @Composable @ReadOnlyComposable get() = LocalPadding.current

    val valuePollInterval: Duration
        @Composable @ReadOnlyComposable get() = LocalPollInterval.current

    val entrySize: DpSize
        @Composable @ReadOnlyComposable get() = LocalEntrySize.current

    val spacing: Dp
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val iconAnimationDuration: Duration
        @Composable @ReadOnlyComposable get() = LocalIconAnimationDuration.current

    val LocalSpacing = staticCompositionLocalOf { 12.dp }

    val LocalPollInterval = compositionLocalOf { 50.milliseconds }

    val LocalPadding = staticCompositionLocalOf {
        PaddingValues(20.dp, 8.dp, 12.dp, 8.dp)
    }

    val LocalEntrySize = staticCompositionLocalOf { DpSize(320.dp, 64.dp) }

    val LocalIconAnimationDuration = compositionLocalOf { 400.milliseconds }

    /** 配置行层级，0 为顶层（圆角矩形），>0 为嵌套（无圆角矩形） */
    val LocalLevel = compositionLocalOf { 0 }

    val LocalIcon = compositionLocalOf<@Composable (() -> Unit)?> { null }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfigRowWrapper(
    config: ConfigNode,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    icon: @Composable (() -> Unit)? = LocalIcon.current,
    resettable: Boolean = true,
    onClick: (() -> Unit)? = null,
    onReset: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val level = ConfigRowWrapper.LocalLevel.current
    // level == 0 顶层：常驻圆角矩形背景 + hover 加深
    // level > 0 嵌套：微弱常态底色 + hover 加深（无圆角边框，由父容器统一裁剪）
    // 透明度控制在较低区间，避免与右侧滚动条轨道(同为灰色系)混淆
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        animationSpec = tween(200),
        label = "configRowBackground"
    )
    Row(
        modifier = modifier
            .hoverable(interactionSource)
            .then(if (level == 0) Modifier.clip(MaterialTheme.shapes.medium) else Modifier)
            .background(backgroundColor)
            .onClick { onClick?.invoke() }
            .fillMaxWidth()
            .padding(ConfigRowWrapper.padding),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                it()
                Spacer(Modifier.width(ConfigRowWrapper.spacing))
            }
            ConfigName(config, modifier = Modifier.weight(1f, false))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
            if (resettable) ResetButton(config, onReset = { onReset() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigName(
    config: ConfigNode,
    modifier: Modifier = Modifier
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    var isTruncated by remember { mutableStateOf(false) }
    val isTruncatedState = rememberUpdatedState(isTruncated)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(250.milliseconds)
            if (isTruncatedState.value) {
                tooltipState.transition.targetState = true
            }
        } else {
            tooltipState.transition.targetState = false
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(maxWidth = 600.dp) {
                Text(InlineStyleText(config.translateComment.plainText))
            }
        },
        state = tooltipState,
        modifier = modifier,
        enableUserInput = false,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.hoverable(interactionSource)
        ) {
            Text(
                component = InlineStyleText(config.translateText.plainText),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                component = InlineStyleText(config.translateComment.plainText),
                modifier = Modifier.widthIn(max = 512.dp),
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    isTruncated = textLayoutResult.hasVisualOverflow
                },
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Resettable> ResetButton(
    resettable: T,
    onReset: ((T) -> Unit)? = null,
) {
    val state by resettable.asState()

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
    IconButton(
        onClick = {
            resettable.resetDefault()
            onReset?.invoke(resettable)
            scope.launch {
                rotation.animateTo(
                    targetValue = rotation.value - 360f,
                    animationSpec = tween(durationMillis = duration)
                )
            }
        },
        modifier = Modifier.size(32.dp),
        enabled = !state
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip {
                    Text(IGLang.Misc.reset.plainText)
                }
            },
            state = rememberTooltipState(),
        ) {
            Icon(
                Icons.Replay,
                contentDescription = IGLang.Misc.reset.plainText,
                modifier = Modifier.rotate(rotation.value)
            )
        }
    }
}


@Composable
fun <T, C : Config<T>> C.asState(): State<T> = produceState(
    initialValue = this.getValue(),
    key1 = this
) {
    val disposable = this@asState.observe {
        value = it.getValue()
    }
    awaitDispose { disposable.dispose() }
}

@Composable
fun <T, C : Config<T>, R> C.asDerivedState(
    forceRefresh: Boolean = false,
    derive: (C) -> R
): State<R> {
    var version by remember { mutableIntStateOf(0) }
    return produceState(
        initialValue = derive(this@asDerivedState),
        key1 = version
    ) {
        val disposable = this@asDerivedState.observe {
            value = derive(this@asDerivedState)

            if (forceRefresh) {
                version++
            }
        }
        awaitDispose {
            disposable.dispose()
        }
    }
}

@Composable
fun Resettable.asState(): State<Boolean> {
    return if (this is Observable<*>) {
        produceState(initialValue = this.isDefault(), key1 = this) {
            val disposable = this@asState.observe {
                value = this@asState.isDefault()
            }
            awaitDispose { disposable.dispose() }
        }

    } else {
        val interval = ConfigRowWrapper.valuePollInterval
        produceState(initialValue = this.isDefault(), key1 = this) {
            while (isActive) {
                value = this@asState.isDefault()
                delay(interval)
            }
        }
    }
}