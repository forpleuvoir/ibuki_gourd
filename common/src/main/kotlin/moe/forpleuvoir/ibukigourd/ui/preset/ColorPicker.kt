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
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.background
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContent
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import kotlin.math.roundToInt
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

private val hueGradient by lazy {
    listOf(
        Color.hsv(0f, 1f, 1f),
        Color.hsv(60f, 1f, 1f),
        Color.hsv(120f, 1f, 1f),
        Color.hsv(180f, 1f, 1f),
        Color.hsv(240f, 1f, 1f),
        Color.hsv(300f, 1f, 1f),
        Color.hsv(360f, 1f, 1f),
    )
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ColorPicker(color: NebulaColor, onValueChange: (NebulaColor) -> Unit, modifier: Modifier = Modifier) {

    var tabIndex by remember { mutableStateOf(0) }

    val borderColor = remember(color) {
        color.reverse(false).toComposeColor
    }

    Row(modifier) {
        Column {
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("HSV")
                }
                SegmentedButton(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("RGB")
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
                            var hue by remember { mutableStateOf(color.hue * 360f) }
                            LaunchedEffect(hue) { onValueChange(color.hue(hue / 360f)) }
                            var saturation by remember { mutableStateOf(color.saturation * 100f) }
                            LaunchedEffect(saturation) { onValueChange(color.saturation(saturation / 100f)) }
                            var value by remember { mutableStateOf(color.value * 100f) }
                            LaunchedEffect(value) { onValueChange(color.value(value / 100f)) }

                            val satGradient =
                                remember(hue, value) { listOf(Color.hsv(hue, 0f, value / 100f), Color.hsv(hue, 1f, value / 100f)) }
                            val valGradient =
                                remember(hue, saturation) { listOf(Color.hsv(hue, saturation / 100f, 0f), Color.hsv(hue, saturation / 100f, 1f)) }

                            ColorChannelSlider("H", IGLang.Color.hue.plainText, hue, 0f..360f, hueGradient, useFloat = true) {
                                hue = it
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("S", IGLang.Color.saturation.plainText, saturation, 0f..100f, satGradient, useFloat = true) {
                                saturation = it
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("V", IGLang.Color.value.plainText, value, 0f..100f, valGradient, useFloat = true) {
                                value = it
                            }

                        } else {
                            var red by remember { mutableStateOf(color.red) }
                            LaunchedEffect(red) { onValueChange(color.red(red)) }
                            var green by remember { mutableStateOf(color.green) }
                            LaunchedEffect(green) { onValueChange(color.green(green)) }
                            var blue by remember { mutableStateOf(color.blue) }
                            LaunchedEffect(blue) { onValueChange(color.blue(blue)) }

                            val rGradient = remember(green, blue) { listOf(Color(0, green, blue), Color(255, green, blue)) }
                            val gGradient = remember(red, blue) { listOf(Color(red, 0, blue), Color(red, 255, blue)) }
                            val bGradient = remember(color.red, color.green) { listOf(Color(red, green, 0), Color(red, green, 255)) }

                            ColorChannelSlider("R", IGLang.Color.red.plainText, red.toFloat(), 0f..255f, rGradient) {
                                red = it.fastRoundToInt()
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("G", IGLang.Color.green.plainText, green.toFloat(), 0f..255f, gGradient) {
                                green = it.fastRoundToInt()
                            }
                            Spacer(Modifier.height(2.dp))
                            ColorChannelSlider("B", IGLang.Color.blue.plainText, blue.toFloat(), 0f..255f, bGradient) {
                                blue = it.fastRoundToInt()
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))
                val aGradient = remember(color.red, color.green, color.blue) {
                    listOf(Color(color.red, color.green, color.blue, 0), Color(color.red, color.green, color.blue, 255))
                }
                var alpha by remember { mutableStateOf(color.alpha) }
                LaunchedEffect(alpha) { onValueChange(color.alpha(alpha)) }
                ColorChannelSlider("A", IGLang.Color.alpha.plainText, alpha.toFloat(), 0f..255f, aGradient, showCheckerboard = true) {
                    alpha = it.fastRoundToInt()
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(
            modifier = Modifier.width(160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val colorText = if (tabIndex == 1) color.hexStr
            else "(${"%.1f".format(color.hue * 360f)}, ${"%.1f".format(color.saturation * 100)}, ${"%.1f".format(color.value * 100)})"
            val scope = rememberCoroutineScope()

            TipBox({
                Text(IGLang.Color.clickCopyColor(color))
            }) {
                AssistChip(
                    onClick = {
                        scope.launch {
                            MinecraftClipboard.setClipboardText(colorText)
                            ToastHandler.show {
                                ToastContent { Text(IGLang.Color.copyColorSuccess(color)) }
                            }
                        }
                    },
                    label = {
                        Text(colorText, style = MaterialTheme.typography.labelSmall)
                    }
                )
            }

            Spacer(Modifier.height(32.dp))

            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier.width(160.dp).height(160.dp)
                    .shadow(2.dp, shape)
                    .border(1.dp, borderColor, shape)
            ) {
                Checkerboard(160.dp / 12, modifier = Modifier.fillMaxSize().clip(shape))
                Box(Modifier.fillMaxSize().background(color, shape))
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