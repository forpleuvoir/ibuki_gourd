package moe.forpleuvoir.ibukigourd.ui.scene

import androidx.compose.runtime.Composable

/**
 * Factory for creating [ComposeSceneHost] instances.
 *
 * Centralizes the construction of the Minecraft-Compose bridge,
 * decoupling the Screen from the concrete implementation.
 *
 * Usage:
 * ```kotlin
 * class MyScreen : Screen(...) {
 *     val gui: ComposeSceneHost = ComposeSceneFactory.create {
 *         MyComposeContent()
 *     }
 * }
 * ```
 */
object ComposeSceneFactory {

    /**
     * 创建一个新的 [ComposeSceneHost] 实例，用于在 Minecraft 屏幕中渲染 Compose UI 内容。
     *
     * @param content 需要渲染的 Compose UI 内容
     * @return 新的 [ComposeSceneHost] 实例
     */
    fun create(content: @Composable () -> Unit): ComposeSceneHost {
        return DefaultComposeSceneHost(content)
    }
}
