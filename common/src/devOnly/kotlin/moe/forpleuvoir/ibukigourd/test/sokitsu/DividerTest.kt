package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Divider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 分割线测试屏：验证厚度、颜色与回退链。
 *
 * 验证点：
 * 1. 默认 —— 取 `divider` meta 的 thickness 与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.DividerTokens.Line] 色；
 * 2. 厚度 —— 1 / 2 / 4dp 逐级加粗（纯色矩形，不受 pixel_scale 影响）；
 * 3. 颜色 —— 调用点传参优先于组件 token。
 *
 * 容器给固定宽度，否则 `fillMaxWidth` 的线条会把父布局撑满。
 */
@Composable
fun DividerTestContent() {
    Column(
        modifier = Modifier.width(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("默认（1dp / outline 色）")
        Divider()

        Text("厚度 2dp")
        Divider(thickness = 2.dp)

        Text("厚度 4dp")
        Divider(thickness = 4.dp)

        Text("自定义颜色")
        Divider(color = Color(0xFF7ED957))

        Text("分组用法：线条与相邻内容同列，宽度随容器")
        Column(Modifier.fillMaxWidth()) {
            Text("第一段内容")
            Divider()
            Text("第二段内容")
        }
    }
}

fun DividerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            DividerTestContent()
        }
    }
}
