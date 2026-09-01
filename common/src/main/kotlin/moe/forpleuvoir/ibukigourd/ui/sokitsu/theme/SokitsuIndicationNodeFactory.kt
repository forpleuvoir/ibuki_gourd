package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode

object SokitsuIndicationNodeFactory : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationInstance

    override fun hashCode(): Int = -1

    override fun equals(other: Any?): Boolean = other === this

    private object NoIndicationInstance : Modifier.Node(), DrawModifierNode {
        override fun ContentDrawScope.draw() {
            drawContent()
        }
    }
}