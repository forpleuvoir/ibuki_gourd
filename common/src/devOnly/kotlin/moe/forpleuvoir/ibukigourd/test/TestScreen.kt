package moe.forpleuvoir.ibukigourd.test

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.FloatField
import moe.forpleuvoir.ibukigourd.ui.preset.IntField
import moe.forpleuvoir.ibukigourd.ui.preset.ItemIcon
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import java.net.URI
import kotlin.math.roundToInt
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor


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
                ItemIcon(ItemStack(Items.IRON_SWORD, 16))
                Text("?")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(modifier: Modifier = Modifier) {
    var tabIndex by remember { mutableStateOf(0) }
    var nebulaColor by remember { mutableStateOf(NebulaColor.fromARGB(255, 0, 0, 255)) }

    val composeColor = remember(nebulaColor) { nebulaColor.toComposeColor }
    val borderColor = remember(composeColor) {
        Color(
            1f - composeColor.red,
            1f - composeColor.green,
            1f - composeColor.blue,
            0.5f
        )
    }

    val hueGradient = remember {
        listOf(
            NebulaColor.fromHSV(0f / 360f, 1f, 1f),
            NebulaColor.fromHSV(60f / 360f, 1f, 1f),
            NebulaColor.fromHSV(120f / 360f, 1f, 1f),
            NebulaColor.fromHSV(180f / 360f, 1f, 1f),
            NebulaColor.fromHSV(240f / 360f, 1f, 1f),
            NebulaColor.fromHSV(300f / 360f, 1f, 1f),
            NebulaColor.fromHSV(360f / 360f, 1f, 1f),
        ).map { it.toComposeColor }
    }

    val satGradient = remember(nebulaColor.hue, nebulaColor.value) {
        listOf(
            NebulaColor.fromHSV(nebulaColor.hue, 0f, nebulaColor.value),
            NebulaColor.fromHSV(nebulaColor.hue, 1f, nebulaColor.value),
        ).map { it.toComposeColor }
    }

    val valGradient = remember(nebulaColor.hue, nebulaColor.saturation) {
        listOf(
            NebulaColor.fromHSV(nebulaColor.hue, nebulaColor.saturation, 0f),
            NebulaColor.fromHSV(nebulaColor.hue, nebulaColor.saturation, 1f),
        ).map { it.toComposeColor }
    }

    val rGradient = remember(nebulaColor.green, nebulaColor.blue) {
        listOf(
            NebulaColor.fromARGB(0, nebulaColor.green, nebulaColor.blue),
            NebulaColor.fromARGB(255, nebulaColor.green, nebulaColor.blue),
        ).map { it.toComposeColor }
    }

    val gGradient = remember(nebulaColor.red, nebulaColor.blue) {
        listOf(
            NebulaColor.fromARGB(nebulaColor.red, 0, nebulaColor.blue),
            NebulaColor.fromARGB(nebulaColor.red, 255, nebulaColor.blue),
        ).map { it.toComposeColor }
    }

    val bGradient = remember(nebulaColor.red, nebulaColor.green) {
        listOf(
            NebulaColor.fromARGB(nebulaColor.red, nebulaColor.green, 0),
            NebulaColor.fromARGB(nebulaColor.red, nebulaColor.green, 255),
        ).map { it.toComposeColor }
    }

    val aGradient = remember(nebulaColor.red, nebulaColor.green, nebulaColor.blue) {
        listOf(
            NebulaColor.fromARGB(nebulaColor.red, nebulaColor.green, nebulaColor.blue, 0),
            NebulaColor.fromARGB(nebulaColor.red, nebulaColor.green, nebulaColor.blue, 255),
        ).map { it.toComposeColor }
    }

    Card(modifier = modifier) {
        Row(Modifier.padding(24.dp)) {
            Column {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text("RGB")
                    }
                    SegmentedButton(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text("HSV")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(modifier = Modifier.width(320.dp)) {
                    AnimatedContent(
                        targetState = tabIndex,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            slideInHorizontally { width -> direction * width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -direction * width } + fadeOut()
                        },
                        label = "rgb_hsv_switch"
                    ) { currentTab ->
                        Column {
                            if (currentTab == 0) {
                                ColorChannelSlider("R", nebulaColor.red.toFloat(), 0f..255f, rGradient) {
                                    nebulaColor = nebulaColor.red(it.roundToInt())
                                }
                                Spacer(Modifier.height(2.dp))
                                ColorChannelSlider("G", nebulaColor.green.toFloat(), 0f..255f, gGradient) {
                                    nebulaColor = nebulaColor.green(it.roundToInt())
                                }
                                Spacer(Modifier.height(2.dp))
                                ColorChannelSlider("B", nebulaColor.blue.toFloat(), 0f..255f, bGradient) {
                                    nebulaColor = nebulaColor.blue(it.roundToInt())
                                }
                            } else {
                                ColorChannelSlider("H", nebulaColor.hue * 360f, 0f..360f, hueGradient, useFloat = true) {
                                    nebulaColor = nebulaColor.hue(it / 360f)
                                }
                                Spacer(Modifier.height(2.dp))
                                ColorChannelSlider("S", nebulaColor.saturation * 100f, 0f..100f, satGradient, useFloat = true) {
                                    nebulaColor = nebulaColor.saturation(it / 100f)
                                }
                                Spacer(Modifier.height(2.dp))
                                ColorChannelSlider("V", nebulaColor.value * 100f, 0f..100f, valGradient, useFloat = true) {
                                    nebulaColor = nebulaColor.value(it / 100f)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    ColorChannelSlider("A", nebulaColor.alpha.toFloat(), 0f..255f, aGradient, showCheckerboard = true) {
                        nebulaColor = nebulaColor.alpha(it.roundToInt())
                    }
                }
            }
            Spacer(Modifier.width(24.dp))
            Column(
                modifier = Modifier.width(160.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val scope = rememberCoroutineScope()
                AssistChip(
                    onClick = {
                        scope.launch {
                            MinecraftClipboard.setClipboardText(nebulaColor.hexStr)
                        }
                    },
                    label = {
                        if (tabIndex == 0) {
                            Text(nebulaColor.hexStr, style = MaterialTheme.typography.labelSmall)
                        } else {
                            Text(
                                "(${"%.1f".format(nebulaColor.hue * 360f)}, ${"%.1f".format(nebulaColor.saturation * 100)}, ${"%.1f".format(nebulaColor.value * 100)})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier.width(160.dp).height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                ) {
                    Checkerboard(Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(composeColor))
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorChannelSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    gradientColors: List<Color>,
    showCheckerboard: Boolean = false,
    useFloat: Boolean = false,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(20.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            Box(
                Modifier.fillMaxWidth()
                    .height(18.dp)
                    .align(Alignment.Center)
                    .border(0.65.dp, Color(0xFF000000), RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (showCheckerboard) {
                    Checkerboard(Modifier.fillMaxSize())
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(gradientColors)))
            }
            Slider(
                value = value.coerceIn(valueRange),
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    disabledActiveTrackColor = Color.Transparent,
                    disabledInactiveTrackColor = Color.Transparent,
                    thumbColor = Color.Transparent,
                ),
                thumb = {
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.width(8.dp))

        if (useFloat) {
            FloatField(
                value = value,
                onValueChange = { onValueChange(it) },
                range = valueRange,
                valueDisplay = { "%.1f".format(it) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp).height(22.dp)
            )
        } else {
            IntField(
                value = value.roundToInt(),
                onValueChange = { onValueChange(it.toFloat()) },
                range = 0..valueRange.endInclusive.toInt(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp).height(22.dp)
            )
        }
    }
}

@Composable
private fun Checkerboard(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tileSize = 6.dp.toPx()
        val cols = (size.width / tileSize).toInt() + 1
        val rows = (size.height / tileSize).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                drawRect(
                    color = if ((row + col) % 2 == 0) Color(0xFFCCCCCC) else Color(0xFFFFFFFF),
                    topLeft = Offset(col * tileSize, row * tileSize),
                    size = Size(tileSize, tileSize)
                )
            }
        }
    }
}