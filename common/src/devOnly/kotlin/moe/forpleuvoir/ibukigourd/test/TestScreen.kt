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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.preset.ItemIcon
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import java.net.URI


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
                ItemIcon(
                    ItemStack(Blocks.PISTON, 16),
                    Modifier.size(128.dp * size).background(Color.LightGray),
                    IntSize(256, 256)
                )
                var text by remember { mutableStateOf("Hello World") }
                Box(modifier = Modifier.background(Color(255, 255, 255), shape = RoundedCornerShape(2.dp))) {
                    Text(text = text)
                }
                Row {
                    Button(onClick = {
                        text = "$text!"
                        println("按下了按钮")
                    }) {
                        Text("这是什么按钮")
                    }
                    Slider(size, {
                        size = it
                    }, modifier = Modifier.height(20.dp).width(200.dp))
                }
                Text(IGLang.content.style {
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
                var expend by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expend, onExpandedChange = { expend = !expend }, Modifier.width(200.dp)) {
                    Text(text = selected)
                    ExposedDropdownMenu(expend, onDismissRequest = { expend = false }) {
                        list.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text("这是第${item}个")
                                },
                                onClick = {
                                    selected = "这是第${item}个"
                                    expend = false
                                }
                            )
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.height(300.dp).background(Color(255, 127, 0, 127), RoundedCornerShape(4.dp)),
            ) {
                list.forEach { i ->
                    item {
                        Text("则是第${i}个")
                    }
                }
            }
            Column(
                Modifier.width(128.dp)
                    .height(64.dp)
                    .align(Alignment.End)
                    .background(Color(0xFF9fFF00), RoundedCornerShape(4.dp)),
                horizontalAlignment = Alignment.End
            ) {
                ItemIcon(ItemStack(Items.IRON_SWORD, 16), modifier = Modifier.size(32.dp), scale = 0.75f)
                Text("?")
            }
        }
    }
}
