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
     * Create a new [ComposeSceneHost] that renders the given Compose content
     * within a Minecraft screen.
     *
     * @param content The Compose UI content to render
     * @return A new [ComposeSceneHost] instance
     */
    fun create(content: @Composable () -> Unit): ComposeSceneHost {
        return DefaultComposeSceneHost(content)
    }
}
