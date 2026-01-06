package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.hoverTip
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.ItemIcon
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.text.Literal
import net.minecraft.world.item.Items

fun TestScreen2() = ColumnScreen(
    Modifier.renderOverlay { guiGraphics, x, y, d ->
        onRenderOverlay(guiGraphics, x, y, d)
        val lines = listOf(
            Literal(screen()?.focusedWidget.toString()),
            Literal(screen()?.hoveredWidget.toString())
        )
        guiGraphics.pushTextLines(lines, transform.asWorldCoordinateBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top)
    }
) {
    ItemIcon(Items.MELON, scale = 1f)
    Box(Modifier.height(10f)) {}
    Button(
        Modifier.margin(right = 1f).hoverTip {
            Row {
                Text("物品的数量")
            }
        }
    ) {
        Text("物品测试")
        click {
            ConfirmDialog(Literal("测试"), screenModifier = Modifier.padding(vertical = 20f)) {
                Box(Modifier.width(200f).height(500f)) {

                }
            }.open()
        }
    }
}.open()
