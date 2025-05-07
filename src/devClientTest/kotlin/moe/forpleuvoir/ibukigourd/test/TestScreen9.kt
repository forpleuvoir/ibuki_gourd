package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.hoverTip
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.gui.modifier.debugInfo
import moe.forpleuvoir.ibukigourd.gui.modifier.renderBackgroundBox
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ItemIcon
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.item.Items

fun testScreen9() = ColumnScreen(
    Modifier.debugInfo()
) {
    Row {
        Button {
            TextLabel("Toast")
            click {
                Toast.showToast {
                    Row {
                        TextLabel("绿宝石")
                        ItemIcon(Items.EMERALD)
                    }
                }
            }
        }
        Button(modifier = Modifier.hoverTip {
            Row {
                TextLabel("绿宝石")
                ItemIcon(Items.EMERALD, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
            }
        }) {
            TextLabel("Hover")
            ItemIcon(Items.EMERALD, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
        }
        ItemIcon(Items.GRASS_BLOCK, scale = .5f, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
    }
    Row {
        Button {
            TextLabel("Toast")
            click {
                Toast.showToast {
                    Row {
                        TextLabel("绿宝石")
                        ItemIcon(Items.EMERALD)
                    }
                }
            }
        }
        Button(modifier = Modifier.hoverTip {
            Row {
                TextLabel("绿宝石")
                ItemIcon(Items.EMERALD, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
            }
        }) {
            TextLabel("Hover")
//        ItemIcon(Items.EMERALD, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
        }
//    ItemIcon(Items.GRASS_BLOCK, scale = .5f, modifier = Modifier.renderBackgroundBox(Colors.CYAN.alpha(.5f)))
    }
}

