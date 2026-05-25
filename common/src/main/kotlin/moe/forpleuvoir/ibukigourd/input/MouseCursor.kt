package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.util.mc
import org.lwjgl.glfw.GLFW.*

@JvmInline
value class MouseCursor private constructor(private val value: Int) {

    companion object {

        val ArrowCursor = MouseCursor(GLFW_ARROW_CURSOR)

        val IbeamCursor = MouseCursor(GLFW_IBEAM_CURSOR)

        val CrosshairCursor = MouseCursor(GLFW_CROSSHAIR_CURSOR)

        val PointingHandCursor = MouseCursor(GLFW_POINTING_HAND_CURSOR)

        val ResizeEwCursor = MouseCursor(GLFW_RESIZE_EW_CURSOR)

        val ResizeNsCursor = MouseCursor(GLFW_RESIZE_NS_CURSOR)

        val ResizeNwseCursor = MouseCursor(GLFW_RESIZE_NWSE_CURSOR)

        val ResizeNeswCursor = MouseCursor(GLFW_RESIZE_NESW_CURSOR)

        val ResizeAllCursor = MouseCursor(GLFW_RESIZE_ALL_CURSOR)

        val NotAllowedCursor = MouseCursor(GLFW_NOT_ALLOWED_CURSOR)

        fun reset() {
            ArrowCursor.apply()
        }
    }

    fun apply() {
        glfwSetCursor(mc.window.handle(), glfwCreateStandardCursor(value))
    }



}