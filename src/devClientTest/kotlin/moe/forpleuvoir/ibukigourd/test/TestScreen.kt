package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon

class TestScreen : IGScreenImpl() {
    override fun init() {
        icon(
            IconTextures.CLOSE,
            modifier = Modifier.width(16f)
        )

        measure(Constraints())
    }

}