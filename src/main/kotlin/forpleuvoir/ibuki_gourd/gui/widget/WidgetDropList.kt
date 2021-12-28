package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.render.RenderUtil.isMouseHovered
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Function


/**
 * 下拉菜单

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetDropList

 * 创建时间 2021/12/28 18:58

 * @author forpleuvoir

 */
class WidgetDropList<E>(
	private val items: Collection<E>,
	private val default: E,
	private val stringAdapter: Function<E, String>,
	private val entryAdapter: Function<String, E>,
	val parent: Screen,
	private val pageSize: Int,
	private val itemHeight: Int,
	x: Int,
	y: Int,
	width: Int,
) :
	ClickableWidget(x, y, width, itemHeight * (pageSize + 1), "".text), IPositionElement, ParentElement {

	val current: E
		get() = entryAdapter.apply(text.text.string)

	private var focused: Element? = null
	private var dragging = false

	var toggleCallback: ((now: E) -> Unit)? = null

	protected val children: MutableList<Element> by lazy { ArrayList() }
	protected val drawableChildren: MutableList<Drawable> by lazy { ArrayList() }

	protected val text: LabelText = LabelText(
		stringAdapter.apply(default).text,
		this.x,
		this.y,
		this.width,
		this.itemHeight,
		LabelText.Align.CENTER_LEFT
	).apply {
		bordColor = Color4i.WHITE
	}


	private val listWidget: WidgetListString = WidgetListString(
		ArrayList<String>().apply { items.forEach { this.add(stringAdapter.apply(it)) } },
		this.parent,
		this.x,
		this.y + this.itemHeight,
		pageSize,
		this.itemHeight,
		this.width
	).apply {
		renderBord = true
		renderBackground = true
		visible = false
		active = false
		onClickedCallback = { entry, _, _, _ ->
			this.active = false
			this.visible = false
			toggle(entryAdapter.apply(entry.value))
			true
		}
	}

	init {
		addDrawableChild(text)
		addDrawableChild(listWidget)
	}

	private fun toggle(entry: E) {
		if (stringAdapter.apply(current) != stringAdapter.apply(entry)) {
			this.text.text = stringAdapter.apply(entry).text
			toggleCallback?.invoke(current)
		}
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		drawableChildren.forEach { it.render(matrices, mouseX, mouseY, delta) }
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

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		return listWidget.mouseScrolled(mouseX, mouseY, amount)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		isMouseHovered(text.x, text.y, text.width, text.height, mouseX, mouseY) {
			if (button == 0) {
				listWidget.visible = !listWidget.visible
				listWidget.active = !listWidget.active
			}
			if (super<ClickableWidget>.mouseClicked(mouseX, mouseY, button)) return true
		}
		return (super<ParentElement>.mouseClicked(mouseX, mouseY, button))
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (super<ClickableWidget>.mouseReleased(mouseX, mouseY, button)) true
		else super<ParentElement>.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return if (super<ClickableWidget>.mouseReleased(mouseX, mouseY, button)) true
		else super<ParentElement>.mouseReleased(mouseX, mouseY, button)
	}

	override fun changeFocus(lookForwards: Boolean): Boolean {
		return if (super<ClickableWidget>.changeFocus(lookForwards)) true
		else super<ParentElement>.changeFocus(lookForwards)
	}

	override fun children(): MutableList<out Element> {
		return children
	}

	protected fun addChildren(children: Element) {
		this.children.add(children)
	}

	protected fun <T> addDrawableChild(drawableElement: T) where T : Element, T : Drawable {
		this.drawableChildren.add(drawableElement)
		this.children.add(drawableElement)
	}

	override fun isDragging(): Boolean {
		return this.dragging
	}

	override fun setDragging(dragging: Boolean) {
		this.dragging = dragging
	}

	override fun getFocused(): Element? {
		return this.focused
	}

	override fun setFocused(focused: Element?) {
		this.focused = focused
	}

	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		super.appendDefaultNarrations(builder)
	}


}