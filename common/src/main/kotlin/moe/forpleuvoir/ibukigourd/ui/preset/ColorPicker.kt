package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.debug
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContent
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor


private val hueGradient by lazy {
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

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ColorPicker(color: NebulaColor, onValueChange: (NebulaColor) -> Unit, modifier: Modifier = Modifier) {

    var nebulaColor by remember { mutableStateOf(color) }

    LaunchedEffect(color) { nebulaColor = color }

    LaunchedEffect(Unit) {
        snapshotFlow { nebulaColor }
            .drop(1)
            .debounce(16.milliseconds)
            .collect { onValueChange(it) }
    }

    var tabIndex by remember { mutableStateOf(1) }
    val composeColor = remember(nebulaColor) { nebulaColor.toComposeColor }
    val borderColor = remember(composeColor) {
        Color(
            1f - composeColor.red,
            1f - composeColor.green,
            1f - composeColor.blue,
            0.5f
        )
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

    Row(modifier) {
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
                            ColorChannelSlider("R", IGLang.Color.red.plainText, nebulaColor.red.toFloat(), 0f..255f, rGradient) {
                                nebulaColor = nebulaColor.red(it.roundToInt())
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("G", IGLang.Color.green.plainText, nebulaColor.green.toFloat(), 0f..255f, gGradient) {
                                nebulaColor = nebulaColor.green(it.roundToInt())
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("B", IGLang.Color.blue.plainText, nebulaColor.blue.toFloat(), 0f..255f, bGradient) {
                                nebulaColor = nebulaColor.blue(it.roundToInt())
                            }
                        } else {
                            ColorChannelSlider("H", IGLang.Color.hue.plainText, nebulaColor.hue * 360f, 0f..360f, hueGradient, useFloat = true) {
                                nebulaColor = nebulaColor.hue(it / 360f)
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("S", IGLang.Color.saturation.plainText, nebulaColor.saturation * 100f, 0f..100f, satGradient, useFloat = true) {
                                nebulaColor = nebulaColor.saturation(it / 100f)
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("V", IGLang.Color.value.plainText, nebulaColor.value * 100f, 0f..100f, valGradient, useFloat = true) {
                                nebulaColor = nebulaColor.value(it / 100f)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                ColorChannelSlider("A", IGLang.Color.alpha.plainText, nebulaColor.alpha.toFloat(), 0f..255f, aGradient, showCheckerboard = true) {
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
                        ToastHandler.show {
                            ToastContent { Text(IGLang.Color.copyColorSuccess(nebulaColor)) }
                        }
                    }
                },
                label = {
                    TipBox({
                        Text(IGLang.Color.clickCopyColor(nebulaColor))
                    }) {
                        if (tabIndex == 0) {
                            Text(nebulaColor.hexStr, style = MaterialTheme.typography.labelSmall)
                        } else {
                            Text(
                                "(${"%.1f".format(nebulaColor.hue * 360f)}, ${"%.1f".format(nebulaColor.saturation * 100)}, ${"%.1f".format(nebulaColor.value * 100)})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier.width(160.dp).height(160.dp)
                    .shadow(2.dp, shape)
                    .border(1.dp, borderColor, shape)
            ) {
                Checkerboard(160.dp / 12, modifier = Modifier.fillMaxSize().clip(shape))
                Box(Modifier.fillMaxSize().background(composeColor, shape))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorChannelSlider(
    label: String,
    labelTip: String,
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
        TipBox({
            Text(labelTip)
        }) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            val shape = RoundedCornerShape(4.dp)
            Box(
                Modifier.fillMaxWidth()
                    .height(22.dp)
                    .align(Alignment.Center)
                    .border(0.2.dp, Color(0xA0000000), shape)
                    .shadow(1.5.dp, shape)
            ) {
                if (showCheckerboard) {
                    Checkerboard(22.dp / 3, modifier = Modifier.fillMaxSize())
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
                    val shape = remember { RoundedCornerShape(1.dp) }
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(26.dp)
                            .shadow(2.dp, shape)
                            .clip(shape)
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
fun Checkerboard(tileSize: Dp, colorA: Color = Color(0xFFCCCCCC), colorB: Color = Color(0xFFFFFFFF), modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tileSize = tileSize.toPx()
        val cols = (size.width / tileSize).toInt() + 1
        val rows = (size.height / tileSize).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                drawRect(
                    color = if ((row + col) % 2 == 0) colorA else colorB,
                    topLeft = Offset(col * tileSize, row * tileSize),
                    size = Size(tileSize, tileSize)
                )
            }
        }
    }
}