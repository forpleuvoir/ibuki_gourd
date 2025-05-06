package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.screen.BoxScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ItemIcon
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.item.Items

fun testScreen9() = BoxScreen(
) {
    FlatButton(idleColor = Colors.CYAN.alpha(.5f)) {
        ItemIcon(Items.EMERALD)
        click {
            SimpleDialog(stateOf(Literal("TEST"))) {
                Row(modifier = Modifier.bgHoverHighlightBox()) {
                    ItemIcon(Items.MELON, scale = .6f)
                }
            }.open()
        }
    }
}

