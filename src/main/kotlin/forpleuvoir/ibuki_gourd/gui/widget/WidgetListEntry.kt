package forpleuvoir.ibuki_gourd.gui.widget


import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetListEntry

 * 创建时间 2021/12/19 18:02

 * @author forpleuvoir

 */
abstract class WidgetListEntry(
	val parent: WidgetList<*>, x: Int, y: Int, width: Int, height: Int
) : ClickableWidget(x, y, width, height, "".text()), ParentElement {

	protected val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
	protected val textRenderer: TextRenderer by lazy { client.textRenderer }

	private val maxBgOpacity = 80
	private val bgOpacityDelta: Float = 20f
	private var bgOpacity = 0

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!active || !visible) {
			false
		} else super<ParentElement>.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!active || !visible) {
			false
		} else super<ParentElement>.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return if (!active || !visible) {
			false
		} else super<ParentElement>.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun changeFocus(lookForwards: Boolean): Boolean {
		return if (!active || !visible) {
			false
		} else super<ParentElement>.changeFocus(lookForwards)
	}


	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) {
			return
		}
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
		renderBackground(matrices, mouseX, mouseY, delta)
		if (isFocused) renderFocusedBorder(matrices, mouseX, mouseY, delta)
		renderEntry(matrices, mouseX, mouseY, delta)
	}

	abstract fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)

	protected fun renderBackground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		updateBgOpacity(if (hovered) delta * bgOpacityDelta else -delta * bgOpacityDelta)
		RenderUtil.drawRect(x = x, y = y, width = this.width, height = this.height, Color4i(255, 255, 255, bgOpacity))
	}

	protected fun updateBgOpacity(delta: Float) {
		bgOpacity += delta.toInt()
		bgOpacity = MathHelper.clamp(bgOpacity, 0, maxBgOpacity)
	}

	protected fun renderFocusedBorder(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawOutline(this.x, this.y, this.width, this.width, borderColor = Color4f.WHITE)
	}

	fun setPosition(x: Int, y: Int) {
		this.x = x
		this.y = y
	}

}