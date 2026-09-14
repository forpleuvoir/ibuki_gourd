package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 图标按钮测试屏：真图标放进 [IconButton] / [FlatButton] 的实际效果。
 *
 * 重点看两件事：
 * - **图标取色是否自动跟随**：`Icon` 的 `tint` 缺省取 `LocalContentColor`，所以按钮内容色变了
 *   （含 `contentBlend` / `disabledBlend` 的状态修正）图标应当同步变，无需逐个传色；
 * - **悬停/按下**：图标与按钮背景一起变化（背景只在这两个状态有素材）。
 */
fun IconButtonTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            val scheme = SokitsuTheme.colorScheme
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // 常用操作：纯图标按钮
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {}) { Icon(Icons.Add) }
                    IconButton(onClick = {}) { Icon(Icons.Edit) }
                    IconButton(onClick = {}) { Icon(Icons.Copy) }
                    IconButton(onClick = {}) { Icon(Icons.Delete) }
                    IconButton(onClick = {}) { Icon(Icons.Reset) }
                    IconButton(onClick = {}) { Icon(Icons.Search) }
                    IconButton(onClick = {}) { Icon(Icons.Setting) }
                    IconButton(onClick = {}) { Icon(Icons.Close) }
                }
                // 禁用态：内容色应被 disabledBlend 削弱
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {}) { Icon(Icons.Add) }
                    IconButton(onClick = {}, enabled = false) { Icon(Icons.Add) }
                    IconButton(onClick = {}, enabled = false) { Icon(Icons.Delete) }
                    IconButton(onClick = {}, enabled = false) { Icon(Icons.Lock) }
                }
                // 换个色调：底色与内容色一起换，图标应跟着走
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {}) { Icon(Icons.Palette) }
                    IconButton(
                        onClick = {},
                        colors = FlatButtonDefaults.colors(
                            color = scheme.secondary,
                            contentColor = scheme.secondary,
                        ),
                    ) { Icon(Icons.DarkMode) }
                    IconButton(
                        onClick = {},
                        colors = FlatButtonDefaults.colors(
                            color = scheme.error,
                            contentColor = scheme.error,
                        ),
                    ) { Icon(Icons.Delete) }
                    IconButton(
                        onClick = {},
                        colors = FlatButtonDefaults.colors(
                            color = scheme.onSurfaceVariant,
                            contentColor = scheme.onSurfaceVariant,
                        ),
                    ) { Icon(Icons.Menu) }
                }
                // 图标 + 文字（FlatButton 自定义内容）+ 放大尺寸
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FlatButton(onClick = {}) {
                        Icon(Icons.Save, modifier = Modifier.padding(end = 6.dp))
                        Text("保存")
                    }
                    FlatButton(onClick = {}) {
                        Icon(Icons.Export, modifier = Modifier.padding(end = 6.dp))
                        Text("导出")
                    }
                    IconButton(onClick = {}, minSize = DpSize(56.dp, 56.dp)) {
                        Icon(Icons.Palette, size = DpSize(32.dp, 32.dp))
                    }
                }
            }
        }
    }
}
