package forpleuvoir.ibuki_gourd.gui.widget


import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutline
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetListEntry

 * 创建时间 2021/12/19 18:02

 * @author forpleuvoir

 */
abstract class WidgetListEntry<E : WidgetListEntry<E>>(
	val parentWidget: WidgetList<*>, x: Int, y: Int, width: Int, height: Int
) : PositionParentWidget(x, y, width, height) {

	protected val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
	protected val textRenderer: TextRenderer by lazy { client.textRenderer }

	private var indexFirstChanged = true

	var index: Int = 0
		set(value) {
			if (field != value && indexFirstChanged) {
				field = value
				updateIndex()
				indexFirstChanged = false
			}
		}

	protected open var bgColor = Color4i(255, 255, 255)
	protected open var maxBgOpacity = 80
	protected open var bgOpacityDelta: Float = 20f
	protected open var bgOpacity = 0


	private var onHoverCallback: ((E) -> Unit)? = null
	private var hoverCallback: ((E) -> Unit)? = null
	private val hoverTexts: ArrayList<Text> by lazy { ArrayList() }

	fun setHoverTexts(vararg texts: Text) {
		hoverTexts.clear()
		hoverTexts.addAll(texts)
	}

	fun addHoverText(vararg texts: Text) {
		hoverTexts.addAll(texts)
	}

	fun clearHoverText() {
		hoverTexts.clear()
	}

	var onClickedCallback: ((mouseX: Double, mouseY: Double, button: Int) -> Boolean)? = null

	override var width: Int = width
		set(value) {
			field = value
			resize()
			initPosition()
		}

	@Suppress("UNCHECKED_CAST")
	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		renderBackground(matrices, mouseX, mouseY, delta)
		if (hovered) renderBorder(matrices, mouseX, mouseY, delta)
		super.render(matrices, mouseX, mouseY, delta)
		renderEntry(matrices, mouseX, mouseY, delta)
		val hoverCallbacks = !hovered
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
		if (hovered && hoverCallbacks) {
			onHoverCallback?.invoke(this as E)
		}
		if (hovered) {
			hoverCallback?.invoke(this as E)
			renderHoverText(matrices, mouseX, mouseY, delta)
		}

	}

	protected open fun updateIndex() {}

	fun setHoverCallback(hoverCallback: (E) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	fun setOnHoverCallback(hoverCallback: (E) -> Unit) {
		this.onHoverCallback = hoverCallback
	}

	abstract fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)

	protected open fun renderHoverText(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (ScreenBase.isCurrent(parent))
			ScreenBase.current?.renderTooltip(matrices, hoverTexts, mouseX, mouseY)
	}

	protected open fun renderBackground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		updateBgOpacity(if (hovered) delta * bgOpacityDelta else -delta * bgOpacityDelta)
		drawRect(matrices, x, y, width, height, bgColor.apply { alpha = bgOpacity })
	}

	protected open fun updateBgOpacity(delta: Float) {
		if (ScreenBase.isCurrent(parentWidget.parent)) {
			bgOpacity += delta.toInt()
			bgOpacity = bgOpacity.clamp(0, maxBgOpacity).toInt()
		}
	}

	protected open fun renderBorder(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (ScreenBase.isCurrent(parentWidget.parent))
			drawOutline(matrices, this.x, this.y, this.width, this.height, borderColor = Color4f.WHITE)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active) return false
		this.focused?.mouseReleased(mouseX, mouseY, button)
		return false
	}

	abstract fun initPosition()

	abstract fun resize()

	override fun appendNarrations(builder: NarrationMessageBuilder?) {
	}

	override fun children(): MutableList<out Element> {
		return children
	}

	open fun tick() {

	}

	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.HOVERED
	}

}