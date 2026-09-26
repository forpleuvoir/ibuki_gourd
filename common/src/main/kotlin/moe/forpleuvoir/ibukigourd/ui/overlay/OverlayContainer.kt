package moe.forpleuvoir.ibukigourd.ui.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

/**
 * 覆盖层场景根：铺满窗口，按注册顺序组合 [OverlayService] 里参与本帧渲染的条目。
 *
 * 条目内容拿到的是铺满窗口的 [BoxScope]，位置 / 对齐由条目自己决定
 * （提示按配置落在窗口的某个比例位置，其它内容可自行 `align`）。
 */
@Composable
internal fun OverlayContainer() {
    Box(Modifier.fillMaxSize()) {
        OverlayService.activeEntries.forEach { entry ->
            if (entry.present()) {
                key(entry.key) {
                    entry.content(this)
                }
            }
        }
    }
}
