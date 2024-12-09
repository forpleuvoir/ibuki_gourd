package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.nebula.common.util.collection.notifiableList

fun testScreen9() = ColumnScreen {
    val list = notifiableList(1, 2, 3)

    RowListWrapped {
        list.forEach {
            TextLabel(it.toString(), modifier = Modifier.width(20f))
        }
    }.apply {
        list.subscribe { recompose() }
    }

    Button {
        TextLabel("添加")
        click {
            list.add(list.size + 1)
        }
    }

}