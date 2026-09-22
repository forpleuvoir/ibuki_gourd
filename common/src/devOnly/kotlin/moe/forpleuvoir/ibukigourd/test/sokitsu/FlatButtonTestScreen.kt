package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.MiscLang
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
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
 * 图标方案未定，[IconButton] 的内容先用文本字符占位；[TextButton] 的内容是普通槽位，
 * 这里顺带验证"文字 + 图标混排"与 [moe.forpleuvoir.ibukigourd.lang.MiscLang] 这类
 * [net.minecraft.network.chat.Component] 文案（i18n）。
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
                    TextButton(onClick = {}) { Text("文本按钮") }
                    TextButton(onClick = {}, enabled = false) { Text("禁用") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = {}) { Text(MiscLang.confirm) }
                    TextButton(onClick = {}) { Text(MiscLang.cancel) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = {}) { Icon(Icons.Add) }
                    IconButton(onClick = {}) { Icon(Icons.Close) }
                    IconButton(onClick = {}, enabled = false) { Icon(Icons.Add) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FlatButton(onClick = {}) { Text("自定义内容") }
                    TextButton(
                        onClick = {},
                        colors = FlatButtonDefaults.colors(
                            color = scheme.secondary,
                            contentColor = scheme.secondary,
                        ),
                    ) {
                        Text("次要色")
                    }
                    TextButton(
                        onClick = {},
                        colors = FlatButtonDefaults.colors(
                            color = scheme.error,
                            contentColor = scheme.error,
                        ),
                    ) {
                        Text("错误色")
                    }
                    // 文字 + 图标混排：内容槽位不限于单行文本
                    TextButton(onClick = {}) {
                        Text("新建")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Add)
                    }
                }
                Text("悬停 / 按下：hover·focus α127、pressed α200；常态与禁用态无素材 → 不画背景")
            }
        }
    }
}
