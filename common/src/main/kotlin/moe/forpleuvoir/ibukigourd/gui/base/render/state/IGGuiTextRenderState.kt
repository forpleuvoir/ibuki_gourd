package moe.forpleuvoir.ibukigourd.gui.base.render.state

import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.state.GuiTextRenderState
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.phys.shapes.BooleanOp
import org.joml.Matrix3x2f

class IGGuiTextRenderState(
    font: Font,
    text: FormattedCharSequence,
    pose: Matrix3x2f,
    private val xf: Float,
    private val yf: Float,
    color: ARGBColor,
    backgroundColor: ARGBColor,
    dropShadow: Boolean,
    includeEmpty: Boolean,
    scissor: Box?
) : GuiTextRenderState(font, text, pose, xf.toInt(), yf.toInt(), color.argb, backgroundColor.argb, dropShadow, includeEmpty, scissor?.asScreenRectangle) {

    override fun ensurePrepared(): Font.PreparedText {
        if (this.preparedText == null) {
            this.preparedText = this.font.prepareText(this.text, xf, yf, this.color, this.dropShadow,this.includeEmpty, this.backgroundColor)
            var screenRectangle = this.preparedText!!.bounds()
            if (screenRectangle != null) {
                screenRectangle =
                    ScreenRectangle(screenRectangle.position.x - 1, screenRectangle.position.y - 1, screenRectangle.width + 2, screenRectangle.height + 2)
                screenRectangle = screenRectangle.transformMaxBounds(this.pose)
                this.bounds = if (this.scissor != null) this.scissor!!.intersection(screenRectangle) else screenRectangle
            }
        }

        return this.preparedText!!
    }
}