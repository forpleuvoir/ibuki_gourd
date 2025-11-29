package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.util.lateInitValueOf

fun TestScreen2() = ColumnScreen {
    val list = mutableListOf<String>("test")
    var recompose by lateInitValueOf {}
    Button {
        Icon(IconTextures.PLUS)
        click {
            list.add("测试用的字符串")
            recompose()
        }
    }
    ColumnListWrapped(Modifier.width(120f).height(120f), listModifier = { Modifier.weight(1) }) {
        for (string in list) {
            Text(string)
        }
    }.apply {
        recompose = { this.executeRecompose() }
    }
}.open()
