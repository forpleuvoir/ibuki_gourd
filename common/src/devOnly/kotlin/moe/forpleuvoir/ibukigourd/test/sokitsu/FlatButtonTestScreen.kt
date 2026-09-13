package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 扁平按钮测试屏：[FlatButton] 底座与基于它的 [IconButton] / [TextButton]。
 *
 * 重点验证「无素材状态不渲染背景」：`ui/flat_button/` 只画了 `pressed`(白 × tone，α200) 与
 * `focused`(α127)，所以常态与禁用态应当是**完全透明**的（只有内容可见）；
 * 用鼠标悬停 / 按住即可看到那两层淡化色块。
 *
 * 图标方案未定，[IconButton] 的内容先用文本字符占位。
 */
fun FlatButtonTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            val scheme = SokitsuTheme.colorScheme
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = {}, text = "文本按钮")
                    TextButton(onClick = {}, text = "禁用", enabled = false)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 图标占位：图标资源方案待定
                    IconButton(onClick = {}) { Text("＋") }
                    IconButton(onClick = {}) { Text("×") }
                    IconButton(onClick = {}, enabled = false) { Text("＋") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FlatButton(onClick = {}) { Text("自定义内容") }
                    TextButton(
                        onClick = {},
                        text = "次要色",
                        colors = FlatButtonDefaults.colors(
                            color = scheme.secondary,
                            contentColor = scheme.secondary,
                        ),
                    )
                    TextButton(
                        onClick = {},
                        text = "错误色",
                        colors = FlatButtonDefaults.colors(
                            color = scheme.error,
                            contentColor = scheme.error,
                        ),
                    )
                }
                Text("悬停 / 按下：hover·focus α127、pressed α200；常态与禁用态无素材 → 不画背景")
            }
        }
    }
}
