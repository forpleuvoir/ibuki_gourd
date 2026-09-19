package moe.forpleuvoir.ibukigourd.ui.sokitsu.menu

import androidx.compose.foundation.ContextMenuRepresentation
import androidx.compose.foundation.contextmenu.ContextMenuScope
import androidx.compose.foundation.contextmenu.ContextMenuState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import net.minecraft.locale.Language

/**
 * 文本框右键菜单的 Sokitsu 呈现层。
 *
 * 经 [androidx.compose.foundation.LocalContextMenuRepresentation] 提供后，
 * 所有 [androidx.compose.foundation.text.BasicTextField] 的右键菜单
 * 复用 [DropdownMenu] 的面板外观与 [DropdownMenuItem] 的条目样式。
 *
 * 条目构建块经公开构造的 [ContextMenuScope] 物化：条目的渲染完全由本类决定，
 * 内置条目的 `onClick` 已自带关闭逻辑（CMP 侧构建时包装）。
 */
object SokitsuContextMenuRepresentation : ContextMenuRepresentation {

    /** 菜单锚点相对指针位置的初始偏移：菜单不贴着光标弹出。 */
    private val PointerOffset: Dp = 16.dp

    @Composable
    override fun Representation(state: ContextMenuState, items: ContextMenuScope.() -> Unit) {
        // 包覆 Box 全程组合，positionInRoot 始终是文本区域在 root 坐标系中的原点。
        // 若仅在菜单打开时组合，首帧锚点会取到未更新的 (0,0)，菜单闪到屏幕左上角。
        var rootOrigin by remember { mutableStateOf(Offset.Zero) }
        Box(Modifier.onGloballyPositioned { rootOrigin = it.positionInRoot() })

        val status = state.status
        if (status !is ContextMenuState.Status.Open) return

        val pointerOffset = with(LocalDensity.current) {
            Offset(PointerOffset.toPx(), PointerOffset.toPx())
        }
        DropdownMenu(
            expanded = true,
            onDismissRequest = { state.status = ContextMenuState.Status.Closed },
            // 1×1 锚点：0×0 会被 DropdownMenu 定位器视为"锚点未记录"而走回退分支
            anchorBounds = Rect(rootOrigin + status.offset + pointerOffset, Size(1f, 1f)),
        ) {
            val scope = remember {
                ContextMenuScope { label, enabled, onClick ->
                    DropdownMenuItem(
                        onClick = onClick,
                        enabled = enabled,
                        leadingIcon = leadingIconFor(label),
                    ) {
                        Text(label)
                    }
                }
            }
            scope.clear()
            scope.items()
            scope.Content()
        }
    }

    /** 条目文案对应的图标槽位；无匹配图标时为 null（不渲染图标槽位）。 */
    @Composable
    private fun leadingIconFor(label: String): (@Composable () -> Unit)? {
        val sprite = iconFor(label) ?: return null
        return { Icon(sprite) }
    }

    /**
     * 条目文案 → 图标。
     *
     * 条目 label 由 CMP 经 MC 语言表键 `compose_minecraft.text_context_menu.*` 解析
     * （assets/compose_minecraft/lang 资源），此处按同一组键解析后匹配；
     * 其余条目（或语言表缺失回退为键名时）不配图标。
     */
    private fun iconFor(label: String): SokitsuSprite? {
        val lang = Language.getInstance()
        return when (label) {
            lang.getOrDefault("compose_minecraft.text_context_menu.cut") -> Icons.Cut
            lang.getOrDefault("compose_minecraft.text_context_menu.copy") -> Icons.Copy
            lang.getOrDefault("compose_minecraft.text_context_menu.paste") -> Icons.Paste
            lang.getOrDefault("compose_minecraft.text_context_menu.select_all") -> Icons.SelectAll
            else -> null
        }
    }

}
