package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.cursor.CursorType
import com.mojang.blaze3d.platform.cursor.CursorTypes
import moe.forpleuvoir.ibukigourd.util.mc

@JvmInline
value class MouseCursor private constructor(private val value: CursorType) {

    companion object {

        val ArrowCursor = MouseCursor(CursorTypes.ARROW)

        val IbeamCursor = MouseCursor(CursorTypes.IBEAM)

        val CrosshairCursor = MouseCursor(CursorTypes.CROSSHAIR)

        val PointingHandCursor = MouseCursor(CursorTypes.POINTING_HAND)

        val ResizeEwCursor = MouseCursor(CursorTypes.RESIZE_EW)

        val ResizeNsCursor = MouseCursor(CursorTypes.RESIZE_NS)

        val ResizeNwseCursor = MouseCursor(CursorType.createStandardCursor(5, "resize_nwse", CursorType.DEFAULT))

        val ResizeNeswCursor = MouseCursor(CursorType.createStandardCursor(6, "resize_nesw", CursorType.DEFAULT))

        val ResizeAllCursor = MouseCursor(CursorTypes.RESIZE_ALL)

        val NotAllowedCursor = MouseCursor(CursorTypes.NOT_ALLOWED)

        fun reset() {
            ArrowCursor.apply()
        }
    }

    fun apply() {
        mc.window.selectCursor(value)
    }

}
