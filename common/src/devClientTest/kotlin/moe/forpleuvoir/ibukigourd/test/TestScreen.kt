package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.ComposeScreen


@Composable
fun CenteredBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun testScreen1() = ComposeScreen {
    CenteredBox {
        Column {
            var text by remember { mutableStateOf("Hello World") }
            Box(modifier = Modifier.background(Color(255, 255, 255), shape = RoundedCornerShape(2.dp))) {
                Text(text = text)
            }
            Button(onClick = {
                text = "$text!"
                println("按下了按钮")
            }) {
                Text("这是什么按钮")
            }

            TextField(rememberTextFieldState(), modifier = Modifier.size(320.dp, 120.dp))
            TextField(rememberTextFieldState(), modifier = Modifier.size(320.dp, 120.dp))
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
fun testScreen2() = ComposeScreen {
    CenteredBox {

        val list = remember { buildList { repeat(60) { add(it) } }.toMutableStateList() }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).background(Color(255, 255, 255), shape = RoundedCornerShape(4.dp))
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
                                onClick = {
                                    selected = "这是第${item}个"
                                    expend = false
                                }
                            ) {
                                Text(text = "这是第${item}个")
                            }
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                list.forEach { i ->
                    item {
                        Text("则是第${i}个")
                    }
                }
            }
        }
    }
}
