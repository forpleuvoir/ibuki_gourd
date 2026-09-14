package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 图标总览屏：把 [Icons] 里的图标按 `id` 顺序铺开，便于逐个核对像素效果。
 *
 * 图标以 **2 倍尺寸**（32dp，对应素材 16 像素）显示，方便看清细节；
 * 颜色走 `LocalContentColor`（本屏即主题 onSurface），与放进按钮里时的取色路径一致。
 */
fun IconTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("图标总览（${Icons.all.size} 个，2× 显示）")
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val scheme = SokitsuTheme.colorScheme
                Icons.byId.forEach { (id, sprite) ->
                    val missing = sprite.isEmpty
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (missing) {
                            // 空精灵什么都画不出，用实心块占位提示"这张图没找到"
                            Box(Modifier.size(32.dp).background(scheme.error))
                        } else {
                            Icon(icon = sprite, tint = scheme.onSurface, scale = 2)
                        }
                        Text(if (missing) "$id 缺" else id)
                    }
                }
            }
        }
    }
}
