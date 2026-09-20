package moe.forpleuvoir.ibukigourd.test.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.item.ItemIcon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * 物品图标测试屏。
 *
 * 验证点：
 * 1. 尺寸 —— 同一物品在 16dp / 32dp / 默认尺寸下的绘制；
 * 2. 数量 —— 堆叠数 64 显示、1 不显示，文本贴右下角；
 * 3. 交互 —— 悬停出现原版物品 tooltip，且 `scaleOnHover > 1` 时图标整体放大；
 * 4. 着色 —— `color` 对物品模型的调制。
 */
@Composable
fun ItemIconTestContent() {
    val diamond = remember { ItemStack(Items.DIAMOND) }
    val sticks = remember { ItemStack(Items.STICK, 64) }
    val single = remember { ItemStack(Items.STICK, 1) }
    val sword = remember { ItemStack(Items.DIAMOND_SWORD) }
    val apple = remember { ItemStack(Items.GOLDEN_APPLE) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("尺寸：16dp / 32dp / 默认 45.5dp")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemIcon(diamond, size = DpSize(16.dp, 16.dp))
            ItemIcon(diamond, size = DpSize(32.dp, 32.dp))
            ItemIcon(diamond)
        }

        Text("数量：64 显示、1 不显示")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemIcon(sticks, showCount = true)
            ItemIcon(single, showCount = true)
        }

        Text("悬停：原版 tooltip + 放大 1.2 倍")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemIcon(sword, scaleOnHover = 1.2f)
            ItemIcon(apple, scaleOnHover = 1.2f)
        }

        Text("tooltip 缩放：原版 GUI 缩放（默认，左）/ 1:1 像素（右）")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemIcon(diamond)
            ItemIcon(diamond, tooltipGuiScale = false)
        }

        Text("着色：color 调制（半透明白）")
        ItemIcon(diamond, color = Color.White.copy(alpha = 0.5f))
    }
}

fun ItemIconTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            ItemIconTestContent()
        }
    }
}
