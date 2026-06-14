package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.MouseButton
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.background
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.tooltip
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.core.Direction
import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import java.net.URI
import kotlin.enums.enumEntries


@Composable
fun CenteredBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TestScreen1() {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        CenteredBox {
            var size by remember { mutableStateOf(1f) }
            Column {
                mc.player?.mainHandItem?.let { item ->
                    ItemIcon(
                        item,
                        Modifier.size(128.dp * size).background(Color.LightGray),
                        IntSize(256, 256),
                        showCount = true
                    )
                }
                var text by remember { mutableStateOf("Hello World") }
                Box(modifier = Modifier.background(Color(255, 255, 255), shape = RoundedCornerShape(2.dp))) {
                    Text(text = text)
                }
                Row {
                    Button(onClick = {
                        text = "$text!"
                        println("按下了按钮")
                    }, modifier = Modifier.tooltip {
                        Text("悬浮测试")
                    }) {
                        Text("这是什么按钮")
                    }
                    Slider(size, {
                        size = it
                    }, modifier = Modifier.height(20.dp).width(200.dp))
                }
                Text(IGLang.Misc.content.style {
                    color(Colors.LIME)
                    clickEvent(ClickEvent.OpenUrl(URI("https://modrinth.com/mod/ibukigourd")))
                })

                TextField(rememberTextFieldState(), modifier = Modifier.size(320.dp, 120.dp))
                TextField(rememberTextFieldState(), modifier = Modifier.size(320.dp, 120.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen2() {
    CenteredBox {
        val list = remember { buildList { repeat(60) { add(it) } }.toMutableStateList() }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).background(Color(255, 255, 255, 127), shape = RoundedCornerShape(4.dp))
        ) {
            Row {
                Button(onClick = {
                    list.add(list.size + 1)
                }) {
                    Text("点我")
                }
                var selected by remember { mutableStateOf("这是第${list[0]}个") }
                StringSelector(
                    selected,
                    { selected = it },
                    options = list.map { "这是第${it}个" }
                )
            }
            LazyColumn(
                modifier = Modifier.height(300.dp).align(Alignment.CenterHorizontally).background(Color(255, 127, 0, 127), RoundedCornerShape(4.dp)),
            ) {
                list.forEach { i ->
                    item {
                        if (i == list.size / 2) {
                            Row {
                                ItemIconVanilla(ItemStack(Items.IRON_SWORD, 16), showCount = true, modifier = Modifier.background(Colors.GRAY))
                                Text("这是第${i}个")
                            }
                        } else {
                            Text("这是第${i}个")
                        }
                    }
                }
            }
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0xFF9fFF00), RoundedCornerShape(4.dp)),
                horizontalAlignment = Alignment.End
            ) {
                ItemIconVanilla(ItemStack(Items.IRON_SWORD, 16), showCount = true, modifier = Modifier.background(Colors.GRAY))
                ItemIconVanilla(
                    ItemStack(Items.DIAMOND_SWORD, 16),
                    size = DpSize((114).dp, (114).dp),
                    showCount = true,
                    modifier = Modifier.background(Colors.BLUE)
                )
            }
            var selected by remember { mutableStateOf(Direction.UP) }
            EnumSelector(
                selected,
                { selected = it },
                items = enumEntries<Direction>()
            )

            var keyCode by remember { mutableStateOf<KeyCode>(MouseButton.BUTTON_4) }
            KeyCodeSetButton(keyCode, { keyCode = it })

            val keybind = remember { Keybind() }
            Row {
                KeybindSetButton(keybind, {})
                var setting by remember { mutableStateOf(keybind.setting) }
                KeybindSettingSetButton(setting, {
                    setting = it
                    keybind.setFrom(setting)
                })

                Button(onClick = {
                    println(keybind.setting)
                }) {
                    Text("Print")
                }
            }
        }
    }
}
