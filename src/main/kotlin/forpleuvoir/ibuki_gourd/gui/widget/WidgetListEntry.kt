package forpleuvoir.ibuki_gourd.gui.widget


import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutline
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.clamp
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
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
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		renderBackground(context, mouseX, mouseY, delta)
		if (hovered) renderBorder(context, mouseX, mouseY, delta)
		super.render(context, mouseX, mouseY, delta)
		renderEntry(context, mouseX, mouseY, delta)
		val hoverCallbacks = !hovered
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
		if (hovered && hoverCallbacks) {
			onHoverCallback?.invoke(this as E)
		}
		if (hovered) {
			hoverCallback?.invoke(this as E)
			renderHoverText(context, mouseX, mouseY, delta)
		}

	}

	protected open fun updateIndex() {}

	fun setHoverCallback(hoverCallback: (E) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	fun setOnHoverCallback(hoverCallback: (E) -> Unit) {
		this.onHoverCallback = hoverCallback
	}

	abstract fun renderEntry(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float)

	protected open fun renderHoverText(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (ScreenBase.isCurrent(parent))
			drawContext.drawTooltip(mc.textRenderer, hoverTexts, mouseX, mouseY)
	}

	protected open fun renderBackground(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		updateBgOpacity(if (hovered) delta * bgOpacityDelta else -delta * bgOpacityDelta)
		drawRect(drawContext, x, y, width, height, bgColor.apply { alpha = bgOpacity })
	}

	protected open fun updateBgOpacity(delta: Float) {
		if (ScreenBase.isCurrent(parentWidget.parent)) {
			bgOpacity += delta.toInt()
			bgOpacity = bgOpacity.clamp(0, maxBgOpacity).toInt()
		}
	}

	protected open fun renderBorder(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (ScreenBase.isCurrent(parentWidget.parent))
			drawOutline(drawContext, this.x, this.y, this.width, this.height, borderColor = Color4f.WHITE)
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