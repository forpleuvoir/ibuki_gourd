package moe.forpleuvoir.ibukigourd.ui.scene.internal

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.scene.ComposeScene
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftPlatformContext
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaSurface
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFWCharCallbackI

/**
 * 场景内部共享状态。
 *
 * 所有内部组件（[SceneLifecycle]、[SceneRenderer]、[SceneInputBridge]）
 * 通过此对象读写共同状态，避免组件之间直接耦合。
 */
@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
internal class SceneContext(
    /** Minecraft 客户端实例 */
    val minecraft: Minecraft = mc,
    /** Skia 渲染目标 */
    val surface: SkiaSurface = SkiaSurface(),
    /** Compose 场景实例 */
    val scene: ComposeScene,
    /** 平台服务绑定（剪贴板、光标、输入法、语言环境） */
    val platformContext: MinecraftPlatformContext,
    // ── 可变状态 ──────────────────────────────────────────────
    /** 当前 UI 缩放系数 */
    var scale: Float = Float.NaN,
    /** GLFW 字符输入回调（用于输入法提交文本） */
    var charCallback: GLFWCharCallbackI? = null,
)
