package forpleuvoir.ibuki_gourd.gui.widget


import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
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
abstract class WidgetListEntry<E : WidgetListEntry<E>>(
	val parent: WidgetList<*>, x: Int, y: Int, width: Int, height: Int
) : ClickableWidget(x, y, width, height, "".text), ParentElement, IPositionElement {

	protected val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
	protected val textRenderer: TextRenderer by lazy { client.textRenderer }

	private var focused: Element? = null
	private var dragging = false

	private var indexFirstChanged = true

	var index: Int = 0
		set(value) {
			if (field != value && indexFirstChanged) {
				field = value
				updateIndex()
				indexFirstChanged = false
			}
		}
	protected var bgColor = Color4i(255, 255, 255)
	protected val maxBgOpacity = 80
	protected val bgOpacityDelta: Float = 20f
	protected var bgOpacity = 0

	protected val children: MutableList<Element> by lazy { ArrayList() }
	protected val drawableChildren: MutableList<Drawable> by lazy { ArrayList() }

	private var onHoverCallback: ((E) -> Unit)? = null
	private var hoverCallback: ((E) -> Unit)? = null

	var onClickedCallback: ((mouseX: Double, mouseY: Double, button: Int) -> Boolean)? = null

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!active || !visible) {
			false
		} else if(onClickedCallback?.invoke(mouseX, mouseY, button) != true) {
			 super<ParentElement>.mouseClicked(mouseX, mouseY, button)
		}else false
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

	@Suppress("UNCHECKED_CAST")
	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) {
			return
		}
		val hoverCallbacks = !hovered
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
		if (hovered && hoverCallbacks) {
			onHoverCallback?.invoke(this as E)
		}
		if (hovered) hoverCallback?.invoke(this as E)
		renderBackground(matrices, mouseX, mouseY, delta)
		if (isFocused) renderFocusedBorder(matrices, mouseX, mouseY, delta)
		renderChildren(matrices, mouseX, mouseY, delta)
		renderEntry(matrices, mouseX, mouseY, delta)
	}

	protected open fun updateIndex() {}

	fun setHoverCallback(hoverCallback: (E) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	fun setOnHoverCallback(hoverCallback: (E) -> Unit) {
		this.onHoverCallback = hoverCallback
	}

	protected fun renderChildren(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawableChildren.forEach {
			it.render(matrices, mouseX, mouseY, delta)
		}
	}

	abstract fun renderEntry(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)

	protected fun renderBackground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		updateBgOpacity(if (hovered) delta * bgOpacityDelta else -delta * bgOpacityDelta)
		RenderUtil.drawRect(
			x = x,
			y = y,
			width = this.width,
			height = this.height,
			bgColor.apply { alpha = bgOpacity },
			parent.parent.zOffset
		)
	}

	protected open fun updateBgOpacity(delta: Float) {
		if (ScreenBase.isCurrent(parent.parent)) {
			bgOpacity += delta.toInt()
			bgOpacity = MathHelper.clamp(bgOpacity, 0, maxBgOpacity)
		}
	}

	protected fun renderFocusedBorder(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawOutline(this.x, this.y, this.width, this.width, borderColor = Color4f.WHITE)
	}

	override var onPositionChanged: ((Int, Int, Int, Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		children().forEach {
			if (it is ClickableWidget) {
				it.deltaPosition(deltaX, deltaY)
			} else if (it is IPositionElement) {
				it.deltaPosition(deltaX, deltaY)
			}
		}
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	abstract fun initPosition()


	override fun appendNarrations(builder: NarrationMessageBuilder?) {
	}

	override fun setFocused(focused: Element?) {
		this.focused = focused
	}

	override fun children(): MutableList<out Element> {
		return children
	}

	protected fun addChildren(children: Element) {
		this.children.add(children)
	}

	protected open fun <T> addDrawableChild(drawableElement: T) where T : Element, T : Drawable {
		this.drawableChildren.add(drawableElement)
		this.children.add(drawableElement)
	}

	open fun tick() {

	}

	override fun isDragging(): Boolean {
		return this.dragging
	}

	override fun setDragging(dragging: Boolean) {
		this.dragging = dragging
	}

	override fun getFocused(): Element? {
		return focused
	}

}