package moe.forpleuvoir.ibukigourd.ui.scene

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

/**
 * Defines the contract between Minecraft's Screen and the Compose UI integration layer.
 *
 * A [ComposeSceneHost] manages the lifecycle of a Compose scene within Minecraft,
 * routes input events from Minecraft to the Compose scene, and coordinates the
 * rendering pipeline (Skia → Minecraft texture).
 *
 * Implementations handle:
 * - Scene initialization and GPU resource setup
 * - Per-frame rendering and state synchronization
 * - Mouse and keyboard event forwarding
 * - Platform services (clipboard, cursors, input methods)
 */
interface ComposeSceneHost {

    /**
     * Initialize the Compose scene. Called during Screen#init().
     * Sets up the Skia render target, configures the Compose scene
     * density and size, and registers any necessary GL callbacks.
     */
    fun init()

    /**
     * Clean up all resources. Called during Screen#onClose().
     * Destroys the Compose scene, GL resources, cursors, and callbacks.
     */
    fun onClose()

    /**
     * Render the Compose scene into Minecraft's GUI graphics pipeline.
     * This is called during Screen#extractRenderState().
     *
     * Responsibilities:
     * - Sync locale state from Minecraft options
     * - Dispatch pointer move and scroll events
     * - Render the Compose scene into the Skia surface
     * - Blit the result into the GuiGraphics buffer
     * - Process any deferred draw calls
     */
    fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float)

    /**
     * Forward a mouse press event from Minecraft to the Compose scene.
     * @return true if the event was handled
     */
    fun mouseClicked(event: MouseButtonEvent): Boolean

    /**
     * Forward a mouse release event from Minecraft to the Compose scene.
     * @return true if the event was handled
     */
    fun mouseReleased(event: MouseButtonEvent): Boolean

    /**
     * Forward a scroll event from Minecraft to the Compose scene.
     * @return true if the event was handled
     */
    fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean

    /**
     * Forward a key press event from Minecraft to the Compose scene.
     * @return true if the event was handled or resulted in an edit command (e.g. backspace)
     */
    fun keyPressed(event: KeyEvent): Boolean

    /**
     * Forward a key release event from Minecraft to the Compose scene.
     * @return true if the event was handled
     */
    fun keyReleased(event: KeyEvent): Boolean
}
