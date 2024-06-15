package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ColumnLayout(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
) : AbstractElement(modifier) {

    override val layout: Layout = LinearLayout(Orientation.Vertical, alignment, spacing)

}

@OptIn(ExperimentalContracts::class)
inline fun ElementContainer.column(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: ColumnLayout.() -> Unit
): ColumnLayout {
    contract {
        callsInPlace(scope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Column(modifier, spacing, alignment, scope))
}

@OptIn(ExperimentalContracts::class)
inline fun Column(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    noinline alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    scope: ColumnLayout.() -> Unit
): ColumnLayout {
    contract {
        callsInPlace(scope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return ColumnLayout(modifier, spacing, alignment).apply {
        scope()
    }
}
