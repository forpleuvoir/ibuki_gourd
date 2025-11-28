package moe.forpleuvoir.ibukigourd.gui.widget.tip

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHelper
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.NotifiableArrayList
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.pick

fun WidgetScope.PopupTip(
    parentTransform: () -> Transform = { owner().transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen? = mc.screen as IGScreen?,
    content: BoxScope.() -> Unit,
): IGScreenImpl = PopupScreen(screenModifier, screen) {
    val direction = mutableStateOf(optionalDirection.isNotEmpty().pick(optionalDirection.first(), Top))
    optionalDirection.subscribe {
        if (it.isEmpty()) {
            direction.setValue(Top)
        } else if (direction.getValue() !in it) {
            direction.setValue(it.first())
        }
    }
    Absolute {
        Box(
            Modifier
                .name("PopupTip")
                .mousePress {
                    onMousePress(it)
                    it.tryUse(!wasMouseOver).onSuccess {
                        screen()?.close()
                    }
                }
                .margin(4f)
                .padding(4f)
                .renderBackground { context, _, _, _ ->
                    TipHelper.updatePosition(this.transform, this.margin, direction, parentTransform(), optionalDirection)
                }
                .render { context, _, _, _ ->
                    TipHelper.tipRender(this.transform, context, direction.getValue(), parentTransform(), bgColor.getValue())
                }
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier,
            content
        )
    }
}


