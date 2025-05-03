package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.TableWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.copyToText
import moe.forpleuvoir.ibukigourd.util.math.plus
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries

fun testScreen9() = RowScreen {
    val userData = listOf(
        ItemStack(Items.AIR),
        ItemStack(Items.GRASS_BLOCK).copyWithCount(32),
        ItemStack(Items.STONE).copyWithCount(16),
        ItemStack(Items.NETHERITE_SWORD),
        ItemStack(Items.DIAMOND_PICKAXE).copyWithCount(1),
        ItemStack(Items.BOW).copyWithCount(1),
        ItemStack(Items.ARROW).copyWithCount(64),
        ItemStack(Items.COOKED_BEEF).copyWithCount(10),
        ItemStack(Items.WOODEN_AXE).copyWithCount(1),
        ItemStack(Items.OAK_LOG).copyWithCount(32),
        ItemStack(Items.WATER_BUCKET).copyWithCount(1),
        ItemStack(Items.LAVA_BUCKET).copyWithCount(1),
        ItemStack(Items.SAND).copyWithCount(64),
        ItemStack(Items.REDSTONE).copyWithCount(16),
        ItemStack(Items.GOLD_INGOT).copyWithCount(8),
        ItemStack(Items.IRON_INGOT).copyWithCount(16),
        ItemStack(Items.COAL).copyWithCount(32),
        ItemStack(Items.EMERALD).copyWithCount(4),
        ItemStack(Items.DIAMOND).copyWithCount(2),
        ItemStack(Items.NETHER_STAR).copyWithCount(1)
    )
    Table1(userData)
    Table2(userData)
}

fun WidgetContainerScope.Table1(userData: List<ItemStack>) = TableWrapped(
    userData,
    modifier = Modifier.height(160f),
    rowGap = 1f,
    columnGap = 1f,
    fixedHeader = true
) {
    onRenderHeader { header, box, rowIndex, context, mouseX, mouseY, delta ->
        context.renderBox(Box(transform.worldPosition + box.position, box), Colors.GRAY.alpha(1f))
        renderHeader(header, box, rowIndex, context, mouseX, mouseY, delta)
    }

    onRenderCell { cell, box, rowIndex, columnIndex, context, mouseX, mouseY, delta ->
        val color = if (columnIndex % 2 == 0) {
            Colors.GREEN.alpha(.5F)
        } else Colors.RED.alpha(.5F)
        context.renderBox(Box(this.transform.worldPosition + box.position, box), color)
        renderCell(cell, box, rowIndex, columnIndex, context, mouseX, mouseY, delta)
    }


    Header(0) {
        TextLabel("物品名称")
    }.Column {
        TextLabel(it.name.copyToText(), modifier = Modifier.align(Alignment.CenterLeft))
    }

    Header(0) {
        TextLabel("物品数量")
    }.Column {
        TextLabel(it.count.toString(), modifier = Modifier.align(Alignment.Center))
    }

    Header(0) {
        FlatButton(round = 1, hoveredColor = Colors.RED.alpha(.2f), modifier = Modifier.align(Alignment.CenterRight)) {
            click { Toast.showToast("物品注册ID") }
            TextLabel("物品注册ID")
        }
    }.Column { stack ->
        FlatButton(round = 1, hoveredColor = Colors.RED.alpha(.2f), modifier = Modifier.align(Alignment.CenterRight)) {
            click { Toast.showToast(Registries.ITEM.getId(stack.item).toString()) }
            TextLabel(Registries.ITEM.getId(stack.item).toString())
        }
    }
}

fun WidgetContainerScope.Table2(userData: List<ItemStack>) = TableWrapped(
    userData,
    modifier = Modifier.height(160f),
    rowGap = 1f,
    columnGap = 1f,
    fixedHeader = true,
    tableModifier = { Modifier.padding(10f) }
) {
    onRenderHeader { header, box, rowIndex, context, mouseX, mouseY, delta ->
        context.renderBox(Box(transform.worldPosition + box.position, box), Colors.GRAY.alpha(1f))
        renderHeader(header, box, rowIndex, context, mouseX, mouseY, delta)
    }

    onRenderCell { cell, box, rowIndex, columnIndex, context, mouseX, mouseY, delta ->
        val color = if (columnIndex % 2 == 0) {
            Colors.GREEN.alpha(.5F)
        } else Colors.RED.alpha(.5F)
        context.renderBox(Box(this.transform.worldPosition + box.position, box), color)
        renderCell(cell, box, rowIndex, columnIndex, context, mouseX, mouseY, delta)
    }


    ColumnBuilder {
        TextLabel(it.name.copyToText(), modifier = Modifier.align(Alignment.CenterLeft))
    }

    ColumnBuilder {
        TextLabel(it.count.toString(), modifier = Modifier.align(Alignment.Center))
    }

    ColumnBuilder { stack ->
        FlatButton(round = 1, hoveredColor = Colors.RED.alpha(.2f), modifier = Modifier.align(Alignment.CenterRight)) {
            click { Toast.showToast(Registries.ITEM.getId(stack.item).toString()) }
            TextLabel(Registries.ITEM.getId(stack.item).toString())
        }
    }
}