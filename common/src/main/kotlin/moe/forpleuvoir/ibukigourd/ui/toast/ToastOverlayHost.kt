@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.toast

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import moe.forpleuvoir.ibukigourd.mod.IGConfig
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IbukiGourdTheme
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftPlatformContext
import moe.forpleuvoir.ibukigourd.ui.scene.ComposeSceneHost
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaSurface
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.jetbrains.skiko.currentNanoTime

object ToastOverlayHost {

    private val surface = SkiaSurface()
    private val binding = MinecraftPlatformContext()
    private lateinit var scene: ComposeScene
    private var scale = 1f
    private var lastNanoTime = 0L
    private var pendingResize = true

    fun init() {
        scene = CanvasLayersComposeScene(platformContext = binding)
        scene.setContent {
            IGCompositionLocalProvider(
                LocalSkiaSurface provides surface
            ) {
                IbukiGourdTheme {
                    ToastContainer()
                }
            }
        }
        pendingResize = true
    }

    fun onWindowResized() {
        pendingResize = true
    }

    private fun resize() {
        val window = mc.window
        surface.resize(window.width, window.height)
        if (::scene.isInitialized) {
            scene.size = IntSize(window.width, window.height)
            scale = window.guiScale.toFloat()
            val density = Density(scale * ComposeSceneHost.DENSITY_RATIO, ComposeSceneHost.FONT_SCALE)
            scene.density = density
            binding.windowInfo.containerSize = IntSize(window.width, window.height)
            binding.windowInfo.containerDpSize = density.run { IntSize(window.width, window.height).toSize().toDpSize() }
        }
    }

    fun render(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (!::scene.isInitialized) return

        val now = System.nanoTime()
        if (lastNanoTime != 0L) {
            val delta = (now - lastNanoTime).toDuration(DurationUnit.NANOSECONDS)
            ToastHandler.tick(delta)
        }
        lastNanoTime = now

        if (pendingResize) {
            pendingResize = false
            resize()
        }

        scene.sendPointerEvent(
            PointerEventType.Move,
            Offset(mouseX * scale, mouseY * scale)
        )

        surface.update(guiGraphics) {
            scene.render(it, currentNanoTime())
        }
    }

    fun onClose() {
        if (::scene.isInitialized) {
            scene.close()
        }
        surface.dispose()
        binding.resetCursors()
    }
}
