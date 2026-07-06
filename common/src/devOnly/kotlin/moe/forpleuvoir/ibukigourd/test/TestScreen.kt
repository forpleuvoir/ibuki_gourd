package moe.forpleuvoir.ibukigourd.test

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.MouseButton
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.render.extension.*
import moe.forpleuvoir.ibukigourd.render.extension.texture.Corner
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture
import moe.forpleuvoir.ibukigourd.render.extension.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.openComposePopupScreen
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IbukiGourdTheme
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.background
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.plainTooltip
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.tooltip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Surface
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.Direction
import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.net.URI
import kotlin.enums.enumEntries


@Composable
fun CenteredBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TestScreen1() {
    IbukiGourdTheme(
        colorScheme = darkColorScheme(),
    ) {
        CenteredBox(
            Modifier
        ) {
            var size by remember { mutableStateOf(1f) }
            Column {
                mc.player?.mainHandItem?.let { item ->
                    ItemIcon(
                        item,
                        Modifier.size(128.dp * size),
                        IntSize(256, 256),
                        showCount = true,
                    )
                }
                var text by remember { mutableStateOf("Hello World") }
                Box(modifier = Modifier.background(Color(255, 255, 255), shape = RoundedCornerShape(2.dp))) {
                    TipBox({
                        Text("悬浮测试")
                    }) {
                        Text(text = text)
                    }
                }
                Row {
                    Button(
                        onClick = {
                            text = "$text!"
                            println("按下了按钮")
                        }, modifier = Modifier.plainTooltip("悬浮测试")) {
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                    items = list.map { "这是第${it}个" },
                    modifier = Modifier.width(160.dp),
                    enabledSearch = true,
                    searchFilter = { text, item ->
                        text in item
                    }
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
                    .align(Alignment.CenterHorizontally),
//                    .background(Color(0xFF9fFF00), RoundedCornerShape(4.dp)),
                horizontalAlignment = Alignment.End
            ) {
                var target by remember { mutableStateOf(1f) }
                val alpha by animateFloatAsState(targetValue = target, animationSpec = tween(durationMillis = 5000))

                mc.player?.mainHandItem?.let {
                    CompositionLocalProvider(LocalInheritedAlpha provides alpha) {
                        ItemIconVanilla(it, showCount = true, modifier = Modifier.onClick {
                            ToastHandler.showContent {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(it.displayName)
                                    ItemIconVanilla(it)
                                }
                            }
                        })
                    }
                }
//                ItemIconVanilla(
//                    ItemStack(Items.DIAMOND_SWORD, 16),
//                    size = DpSize((114).dp, (114).dp),
//                    showCount = true,
//                    modifier = Modifier.background(Colors.BLUE)
//                )

                Button(onClick = {
                    target = if (target == 1f) 0f else 1f
                }) {
                    Text("点我 %.2f".format(alpha))
                }
            }
            var selected by remember { mutableStateOf(Direction.UP) }
            EnumSelector(
                selected,
                { selected = it },
                items = enumEntries<Direction>(),
                modifier = Modifier.width(320.dp)
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


@Composable
fun TestScreen4() {
    VanillaCanvas(modifier = Modifier.fillMaxSize()) {
        TestScreen4Render.render(this, it)
    }
}

private object TestScreen4Render {

    private val icon = IGTexture(Corner(), 0, 0, 32, 32, TextureInfo(32, 32, identifier(IbukiGourd.MOD_ID, "icon.png")))

    fun render(extractor: GuiGraphicsExtractor, context: VanillaCanvasDrawContext) {
        extractor.apply {
            pushHueGradientRect(
                Rect(Offset(0f, 0f), Size(120f, 20f)),
            )

            pushSaturationGradientRect(
                Rect(Offset(130f, 0f), Size(120f, 20f)),
            )

            pushValueGradientRect(
                Rect(Offset(260f, 0f), Size(120f, 20f)),
            )

            pushRoundRect(
                Rect(Offset(0f, 30f), Size(120f, 20f)),
                Colors.AQUA.alpha(0.5f),
                6
            )
            pushBlit(
                Rect(Offset(0f, 60f), Size(40f, 40f)),
                icon
            )
            context.area?.let {
                pushStringLines(
                    listOf(
                        "alpha : ${context.alpha}",
                        "pos: ${it.topLeft}, size: ${it.size}"
                    ), area = it.roundToIntRect()
                )
                pushRectOutline(it, Colors.LIME.alpha(0.75f * context.alpha), inner = true)
            }

        }
    }

}