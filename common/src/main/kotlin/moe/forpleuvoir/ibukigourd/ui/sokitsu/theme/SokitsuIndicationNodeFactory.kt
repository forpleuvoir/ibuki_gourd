package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode

/**
 * Sokitsu 无视效指示（占位实现，后续可在此追加按压/悬停像素特效）。
 *
 * 注意：[IndicationNodeFactory.create] **每次调用必须返回全新节点实例**——
 * 返回共享单例会在第二个 clickable attach 时抛
 * `IllegalStateException: Cannot delegate to an already delegated node`。
 * Factory 自身的 equals/hashCode 参与重组差分，保持 object 单例没有问题。
 */
object SokitsuIndicationNodeFactory : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationNode()

    override fun hashCode(): Int = -1

    override fun equals(other: Any?): Boolean = other === this
}

private class NoIndicationNode : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() {
        drawContent()
    }
}