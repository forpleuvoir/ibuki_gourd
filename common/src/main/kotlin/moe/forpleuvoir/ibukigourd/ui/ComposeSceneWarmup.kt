@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mojang.blaze3d.opengl.GlConst.GL_RGBA8
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.*
import moe.forpleuvoir.ibukigourd.ui.icon.filled.DarkMode
import moe.forpleuvoir.ibukigourd.ui.icon.filled.LightMode
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftPlatformContext
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.util.logger
import org.jetbrains.skia.*
import org.jetbrains.skiko.currentNanoTime
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

/**
 * 游戏启动时的一次性 Compose 预热。
 *
 * 创建临时 GPU 表面和 [CanvasLayersComposeScene]，用丰富的 UI 内容
 * 做一次完整渲染，提前完成 Compose 运行时、Material3 主题、字体加载、
 * 自定义图标解析、Skia 着色器编译等全局一次性初始化。
 *
 * 预热完成后立即清理临时资源，不影响后续屏幕的正常创建流程。
 */
object ComposeSceneWarmup {

    private val logger = logger()

    fun warmUp(content: @Composable () -> Unit = { WarmupBody() }) {
        SkiaContext.submit {
            val ctx = SkiaContext.sharedContext

            try {
                val texId = GL11.glGenTextures()
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId)
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

                val fboId = GL30.glGenFramebuffers()
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId)
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texId, 0)
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

                BackendRenderTarget.makeGL(1280, 720, 0, 8, fboId, GL_RGBA8).use { bt ->
                    Surface.makeFromBackendRenderTarget(
                        ctx, bt,
                        SurfaceOrigin.TOP_LEFT,
                        SurfaceColorFormat.RGBA_8888,
                        ColorSpace.sRGB
                    )!!.use { surface ->
                        val scene = CanvasLayersComposeScene(platformContext = MinecraftPlatformContext())
                        scene.setContent {
                            IGCompositionLocalProvider {
                                MaterialTheme {
                                    content()
                                }
                            }
                        }
                        scene.size = IntSize(2, 2)
                        scene.density = Density(1f)

                        scene.render(surface.canvas.asComposeCanvas(), currentNanoTime())
                        surface.flushAndSubmit()

                        scene.close()
                    }
                }

                GL30.glDeleteFramebuffers(fboId)
                GL11.glDeleteTextures(texId)

                logger.info("Compose warm-up completed")
            } catch (e: Exception) {
                logger.warn("Compose warm-up failed, skipping: ${e.message}")
            }
        }
    }

    @Composable
    private fun WarmupBody() {
        var switchChecked by remember { mutableStateOf(false) }
        var sliderValue by remember { mutableStateOf(0.5f) }
        var textValue by remember { mutableStateOf("") }
        var checked by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0.5f) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Warm-up", style = MaterialTheme.typography.headlineMedium)

                Button(onClick = { }) { Text("Button") }
                OutlinedButton(onClick = { }) { Text("OutlinedButton") }
                TextButton(onClick = { }) { Text("TextButton") }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Switch", modifier = Modifier.weight(1f))
                    Switch(checked = switchChecked, onCheckedChange = { switchChecked = it })
                }

                Text("Slider: ${"%.2f".format(sliderValue)}")
                Slider(value = sliderValue, onValueChange = { sliderValue = it })

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("OutlinedTextField") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("TextField") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Card content", modifier = Modifier.padding(16.dp))
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text("Surface", modifier = Modifier.padding(8.dp))
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                        .background(Color(0xFFFF0000), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        "Box with background + shape", color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                    Text("Checkbox", modifier = Modifier.padding(start = 4.dp))
                }

                Text("Progress: ${"%.0f".format(progress * 100)}%")
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
                CircularProgressIndicator(modifier = Modifier.size(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { }) { Icon(Icons.EditNote, null) }
                    IconButton(onClick = { }) { Icon(Icons.Delete, null) }
                }

                HorizontalDivider()

                AnimatedVisibility(visible = true) {
                    Text("AnimatedVisibility")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FloatingActionButton(onClick = { }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                    SmallFloatingActionButton(onClick = { }) { Text("+", style = MaterialTheme.typography.titleSmall) }
                }

                Text("Dropdown:", style = MaterialTheme.typography.labelLarge)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) { Text("Open Menu") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Item 1") }, onClick = { expanded = false })
                        DropdownMenuItem(text = { Text("Item 2") }, onClick = { expanded = false })
                    }
                }

                Text("LazyColumn:", style = MaterialTheme.typography.labelLarge)
                LazyColumn(modifier = Modifier.height(60.dp)) {
                    items(listOf("A", "B", "C")) { item ->
                        Text("  Lazy item: $item")
                    }
                }

                Text("Icons:", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Add, null, Modifier.size(24.dp))
                    Icon(Icons.Check, null, Modifier.size(24.dp))
                    Icon(Icons.Delete, null, Modifier.size(24.dp))
                    Icon(Icons.DragIndicator, null, Modifier.size(24.dp))
                    Icon(Icons.EditNote, null, Modifier.size(24.dp))
                    Icon(Icons.HighlightKeyboardFocus, null, Modifier.size(24.dp))
                    Icon(Icons.KeyboardAlt, null, Modifier.size(24.dp))
                    Icon(Icons.KeyboardArrowDown, null, Modifier.size(24.dp))
                    Icon(Icons.KeyboardArrowUp, null, Modifier.size(24.dp))
                    Icon(Icons.KeyboardArrowLeft, null, Modifier.size(24.dp))
                    Icon(Icons.KeyboardArrowRight, null, Modifier.size(24.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.MoreVert, null, Modifier.size(24.dp))
                    Icon(Icons.Palette, null, Modifier.size(24.dp))
                    Icon(Icons.Replay, null, Modifier.size(24.dp))
                    Icon(Icons.SyncAlt, null, Modifier.size(24.dp))
                    Icon(Icons.Filled.LightMode, null, Modifier.size(24.dp))
                    Icon(Icons.Filled.DarkMode, null, Modifier.size(24.dp))
                }
            }
        }
    }
}
