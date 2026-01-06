package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.active
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.hoverText
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.either

fun ContainerScope.MoveButton(
    recompose: () -> Unit,
    listValue: MutableList<*>,
    index: Int,
) = Column {
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveUp).padding(1).active(index > 0)
    ) {
        Icon(IconTextures.UP, modifier = Modifier, color = Colors.GRAY.alpha((index > 0).either(1f, .25f)))

        click {
            listValue.moveElement(index, (index - 1).coerceAtLeast(0))
            recompose()
        }
    }
    FlatButton(
        hoveredColor = Colors.BLACK.alpha(.15f),
        round = 0,
        modifier = Modifier.hoverText(IGLang.moveDown, Tip.DefaultSetting.copy(optionalDirection = Direction.clockwiseFromBottom)).padding(1)
            .active(index != listValue.lastIndex)
    ) {
        Icon(
            IconTextures.DOWN,
            modifier = Modifier,
            color = Colors.GRAY.alpha((index != listValue.lastIndex).either(1f, .25f))
        )

        click {
            listValue.moveElement(index, (index + 1).coerceAtMost(listValue.lastIndex))
            recompose()
        }
    }
}

fun <K, V> mapEntry(key: K, value: V) = object : Map.Entry<K, V> {
    override val key: K = key
    override val value: V = value
}
