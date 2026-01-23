package moe.forpleuvoir.ibukigourd.gui.widget.tip

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.fadeInDirection
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHelper
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.either

fun WidgetScope.PopupTip(
    parentTransform: () -> Transform = { owner().transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: List<Direction> = Direction.entries.notification(),
    screen: IGScreen? = mc.screen as IGScreen?,
    content: BoxScope.() -> Unit,
): IGScreenImpl = PopupScreen(screenModifier, screen) {
    require(optionalDirection.isNotEmpty()) { "optionalDirection must not be empty" }
    val direction = mutableStateOf(optionalDirection.isNotEmpty().either(optionalDirection.first(), Top))
    direction.subscribe {
        this.owner().screen()?.let { it.fadeInDirection = direction.getValue() }
    }
    val (size, dir) = TipHelper.evaluatePlacementOptions(null, parentTransform().asWorldCoordinateBox, Margin(4f), optionalDirection)
    direction.setValue(dir)
    Absolute {
        //第一次测量之后才能选择合适的方向,所以第一次渲染会重新测量一次选择更好的位置
        var firstRemeasure = true
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
                .process {
                    this as BoxWidget
                    val parentBox = parentTransform().asWorldCoordinateBox
                    //是否需要重新放置
                    //如果当前Box不在可放置的方向上,则重新测量最合适的方向
                    TipHelper.canPlaceDirections(transform, margin, parentBox, optionalDirection).let { directions ->
                        if (directions.isEmpty()) {
                            val (maxConstraints, dir) = TipHelper.evaluatePlacementOptions(transform, parentBox, margin, optionalDirection)
                            constraints = Constraints.of(minSize = Tip.minSize, maxSize = maxConstraints)
                            remeasure()
                            direction.setValue(dir)
                            if (!firstRemeasure) firstRemeasure = true
                            return@let
                        }
                        if (direction.getValue() !in directions || firstRemeasure) {
                            val (maxConstraints, dir) = TipHelper.evaluatePlacementOptions(transform, parentBox, margin, directions)
                            constraints = Constraints.of(minSize = Tip.minSize, maxSize = maxConstraints)
                            remeasure()
                            direction.setValue(dir)
                            if (firstRemeasure) firstRemeasure = false
                        }
                    }
                    TipHelper.updatePosition(transform, margin, parentBox, direction.getValue())
                }
                .render { guiGraphics, _, _, _ ->
                    TipHelper.tipRender(this.transform, guiGraphics, direction.getValue(), parentTransform().asWorldCoordinateBox, bgColor.getValue())
                }
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier,
        ) {
            owner().constraints = Constraints.of(minSize = Tip.minSize, maxSize = size)
            content()
        }
    }
}

