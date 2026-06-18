package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.widget.ComposeWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class TestVanillaScreen : Screen(Component.literal("")) {

    override fun init() {
        addRenderableWidget(ComposeWidget {
            Column {
                Button({
                    println("按了按钮")
                }) {
                    Text("按钮的文本")
                }
                TextField(rememberTextFieldState(), modifier = Modifier.size(320.dp, 120.dp))
            }
        })
        addRenderableWidget(Button.Builder(Component.literal("按钮"), {
            println("按了原版按钮")
        }).width(120).pos(120, 120).build())
    }
}