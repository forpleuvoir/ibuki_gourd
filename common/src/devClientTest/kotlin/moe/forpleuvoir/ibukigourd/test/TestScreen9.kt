package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.modifier.*
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.mod.what.GameOfLife

fun testScreen9() = RowScreen(
    Modifier.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MouseCursor()
        MousePosition()
    }
) {
    GameOfLife(Size(320, 180), 2, gridUnitSize = 1.5f)
}

