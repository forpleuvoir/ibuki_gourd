package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.preset.LongField
import moe.forpleuvoir.ibukigourd.ui.preset.LocalNumberFieldStyle
import moe.forpleuvoir.ibukigourd.ui.preset.NumberFieldStyle
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange

/**
 * 物品渲染 GPU 图集大小配置包装器。
 *
 * 直接提供一个数值编辑框，以 MB 为单位编辑图集上限；
 * 已使用缓存信息显示在输入框的 Label 中（含使用进度条）。
 *
 * 输入过程只更新本地草稿，失焦或回车时才写回配置并请求重建图集，
 * 避免每击一键就触发一次图集重建。
 *
 * 配置值本身仍为像素面积（RGBA8 每像素 4 字节），显示与编辑时换算为 MB。
 */
@Composable
fun CacheConfigWrapper(
    config: Config<Long>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    /**
     * 图集可观察版本读取器。Compose 组合期间读取以订阅缓存变化。
     * 物品图集传 [moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper.cacheRevision]，
     * 纹理图集传 [moe.forpleuvoir.ibukigourd.ui.preset.SkiaTextureHelper.cacheRevision]。
     */
    cacheRevision: () -> Long,
    /**
     * 当前已使用（含 padding）像素读取器。
     */
    usedAllocationPixels: () -> Long,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    val configValue by config.asState()
    val pixelRange = remember { (config as? ConfigRange<Long>)?.let { it.minValue..it.maxValue } }
    val mbRange = remember(pixelRange) {
        pixelRange?.let { pixelsToMb(it.first)..pixelsToMb(it.last) }
    }

    // 草稿值：输入过程中不直接写回配置
    var draftMb by remember { mutableStateOf(pixelsToMb(configValue)) }

    // 外部值变化（如重置）时同步草稿
    LaunchedEffect(configValue) {
        draftMb = pixelsToMb(configValue)
    }

    Row(
        modifier = Modifier.size(ConfigRowWrapper.entrySize),
        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LongField(
            value = draftMb,
            onValueChange = { draftMb = it },
            range = mbRange,
            valueToText = { it.toString() },
            suffix = { Text(IGLang.ConfigWrapper.cacheMb.plainText) },
            label = {
                CacheUsageLabel(
                    config = config,
                    cacheRevision = cacheRevision,
                    usedAllocationPixels = usedAllocationPixels,
                )
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    // 失焦时提交草稿，仅触发一次图集重建
                    if (!focusState.isFocused) {
                        config.setValue(mbToPixels(draftMb))
                    }
                },
            onKeyboardAction = {
                // 回车提交草稿
                config.setValue(mbToPixels(draftMb))
                it()
            },
        )
    }
}

/**
 * 输入框 Label：显示当前图集已使用缓存（MB）与使用进度条。
 *
 * 通过读取 [SkiaItemRenderHelper.cacheRevision] 订阅缓存变化，
 * 仅在每次批次上传完成后重组，不在绘制阶段修改 Compose State。
 */
@Composable
private fun CacheUsageLabel(
    config: Config<Long>,
    cacheRevision: () -> Long,
    usedAllocationPixels: () -> Long,
) {
    val value by config.asState()
    // 订阅图集版本变化，上传完成后自动刷新统计
    @Suppress("UNUSED_EXPRESSION")
    cacheRevision()
    val usedPixels = usedAllocationPixels()
    val usedMb = pixelsToMb(usedPixels)
    val limitMb = pixelsToMb(value).coerceAtLeast(1L)
    Text(
        text = "${IGLang.ConfigWrapper.cacheUsed.plainText}: $usedMb / $limitMb ${IGLang.ConfigWrapper.cacheMb.plainText}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** RGBA8 每像素 4 字节：像素面积 → MB */
private const val BYTES_PER_PIXEL = 4L
private const val BYTES_PER_MB = 1024L * 1024

private fun pixelsToMb(pixels: Long): Long = pixels * BYTES_PER_PIXEL / BYTES_PER_MB

private fun mbToPixels(mb: Long): Long = mb * BYTES_PER_MB / BYTES_PER_PIXEL
