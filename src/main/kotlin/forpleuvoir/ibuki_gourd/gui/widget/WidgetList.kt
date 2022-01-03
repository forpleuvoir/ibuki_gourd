package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.AbstractParentElement
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.Selectable.SelectionType
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper
import java.lang.Double.max
import java.util.function.Consumer
import java.util.function.Predicate


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui

 * 文件名 WidgetList

 * 创建时间 2021/12/13 16:45

 * @author forpleuvoir

 */

abstract class WidgetList<E : WidgetListEntry<*>>(
	var parent: Screen,
	var x: Int,
	var y: Int,
	/**
	 * 分页大小 每一页包含的Entry数量
	 */
	private val pageSize: Int,
	val itemHeight: Int,
	var width: Int,
	private val leftPadding: Int = 0,
	private val rightPadding: Int = 0,
	private val topPadding: Int = 0,
	private val bottomPadding: Int = 0
) :
	AbstractParentElement(), Drawable, Selectable, IPositionElement {

	protected val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
	private val children: MutableList<E> = ArrayList()

	val height: Int get() = this.rowHeight + this.topPadding + this.bottomPadding
	val top: Int get() = this.y
	val bottom: Int get() = this.y + this.height
	val left: Int get() = this.x
	val right: Int get() = this.x + this.width

	var active: Boolean = true
	var visible: Boolean = true

	var renderBackground: Boolean = false
	var backgroundColor: IColor<*> = Color4f.BLACK
	var renderBord: Boolean = false
	var bordColor: IColor<*> = Color4f.WHITE

	var scrollAmountConsumer: ((Double) -> Unit)? = null

	var scrollAmount = 0.0
		set(value) {
			field = MathHelper.clamp(value, 0.0, maxScroll.toDouble())
			scrollAmountConsumer?.invoke(field)
		}

	var scrollbarColor: IColor<*> = Color4f.WHITE
	var scrollbarBgColor: IColor<*> = Color4f(1f, 1f, 1f, 0.3f)
	protected open val scrollbarPadding: Int = 1
	protected open val scrollbarWidth: Int = 6
		get() {
			return if (maxScroll > 0) field else 0
		}
	private val scrollbarHeight: Int get() = this.rowHeight
	private val scrollbarX: Int get() = this.x + this.width - this.scrollbarWidth
	private val scrollbarY: Int get() = this.rowTop
	private val maxScroll: Int get() = 0.coerceAtLeast(this.maxPosition - (this.rowHeight))

	protected fun scrollbarHovered(mouseX: Double, mouseY: Double): Boolean {
		return RenderUtil.isMouseHovered(this.scrollbarX, this.scrollbarY, this.scrollbarWidth, this.scrollbarHeight, mouseX, mouseY)
	}

	protected inline fun scrollbarHovered(mouseX: Double, mouseY: Double, callback: () -> Unit) {
		if (scrollbarHovered(mouseX, mouseY)) callback.invoke()
	}

	protected val maxPosition: Int get() = entryCount * this.itemHeight

	private var scrolling = false
	var selectedEntry: E? = null
	private var hoveredEntry: E? = null


	protected val rowWidth: Int get() = this.width - this.leftPadding - this.rightPadding - this.scrollbarWidth

	protected val rowHeight: Int get() = this.pageSize * this.itemHeight

	protected val rowLeft: Int get() = this.left + this.leftPadding

	protected val rowRight: Int get() = this.right - this.rightPadding

	protected val rowTop: Int get() = this.top + this.topPadding

	protected val rowBottom: Int get() = this.bottom - this.bottomPadding


	protected open fun getRowTop(index: Int): Int {
		return top - scrollAmount.toInt() + index * this.itemHeight + this.topPadding
	}

	private fun getRowBottom(index: Int): Int {
		return getRowTop(index) + itemHeight
	}

	protected val entryCount: Int get() = this.children().size

	private var childFilter: Predicate<E> = Predicate { true }

	private var onHoverCallback: ((E) -> Unit)? = null
	private var hoverCallback: ((E) -> Unit)? = null

	fun setHoverCallback(hoverCallback: (E) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	fun setOnHoverCallback(hoverCallback: (E) -> Unit) {
		this.onHoverCallback = hoverCallback
	}

	fun resetDefaultFilter() {
		childFilter = Predicate { true }
	}

	fun setFilter(filter: Predicate<E>) {
		childFilter = filter
	}

	@Suppress("unchecked_cast")
	override fun children(): List<E> {
		return children.stream().filter(childFilter).toList().also { list ->
			list.forEach { it.setOnHoverCallback { e -> onHoverCallback?.invoke(e as E) } }
			list.forEach { it.setHoverCallback { e -> hoverCallback?.invoke(e as E) } }
		}
	}

	protected fun clearEntries() {
		children.clear()
	}

	protected fun replaceEntries(newEntries: Collection<E>?) {
		children.clear()
		children.addAll(newEntries!!)
	}

	protected fun getEntry(index: Int): E {
		return children()[index]
	}

	protected fun addEntry(entry: E): Int {
		children.add(entry)
		return children.size - 1
	}


	protected open fun isSelectedEntry(index: Int): Boolean {
		return selectedEntry == children()[index]
	}

	protected fun getEntryAtPosition(x: Double, y: Double): E? {
		return children().filter {
			it.isMouseOver(x, y)
		}.getOrNull(0)
	}


	override var onPositionChanged: ((Int, Int, Int, Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		children().forEach {
			it.deltaPosition(deltaX, deltaY)
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


	protected open fun renderBord(matrices: MatrixStack) {
		RenderUtil.drawOutline(
			this.x - 1,
			this.y - 1,
			this.width + 2,
			this.height + 2,
			borderColor = bordColor,
			zLevel = parent.zOffset
		)
	}

	protected open fun renderBackground(matrices: MatrixStack) {
		RenderUtil.drawRect(
			this.x,
			this.y,
			this.width,
			this.height,
			color = backgroundColor,
			zLevel = parent.zOffset
		)
	}

	protected open fun renderDecorations(matrices: MatrixStack, mouseX: Int, mouseY: Int) {}

	protected open fun updateChildren() {
		for (index in 0 until children().size) {
			children()[index].index = index
			children()[index].setPosition(this.rowLeft, this.rowTop + this.itemHeight * index - this.scrollAmount.toInt())
			val active = !(children()[index].y < this.rowTop || children()[index].y + children()[index].height > this.rowBottom)
			children()[index].active = active
			children()[index].visible = active
			if (active) children()[index].width = this.rowWidth
		}
	}


	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (visible) {
			updateChildren()
			if (renderBackground) renderBackground(matrices)
			if (renderBord) renderBord(matrices)
			hoveredEntry =
				if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) getEntryAtPosition(mouseX.toDouble(), mouseY.toDouble()) else null
			renderScrollbar()
			children().forEach {
				it.render(matrices, mouseX, mouseY, delta)
			}
			renderDecorations(matrices, mouseX, mouseY)
		}
	}


	protected fun renderScrollbar() {
		if (this.maxScroll > 0) {
			//draw scrollbar background
			RenderUtil.drawRect(this.scrollbarX, this.scrollbarY, this.scrollbarWidth, this.rowHeight, this.scrollbarBgColor, parent.zOffset * 2)

			val renderWidth = this.scrollbarWidth.toDouble() - this.scrollbarPadding * 2
			val height = (this.pageSize.toDouble() / this.entryCount.toDouble()) * this.scrollbarHeight
			val renderHeight = height - this.scrollbarPadding * 2
			val maxScrollLength = this.scrollbarHeight - height
			val posY = this.scrollbarY + this.scrollbarPadding + (this.scrollAmount / this.maxScroll) * maxScrollLength
			val posX = this.scrollbarX + this.scrollbarPadding

			RenderUtil.drawRect(posX, posY, renderWidth, renderHeight, this.scrollbarColor, parent.zOffset * 2)
		}
	}

	protected fun centerScrollOn(entry: E) {
		scrollAmount = (children().indexOf(entry) * itemHeight + itemHeight / 2 - (bottom - top) / 2).toDouble()
	}

	protected fun ensureVisible(entry: E) {
		val i = getRowTop(children().indexOf(entry))
		val j = i - top - 4 - itemHeight
		if (j < 0) {
			scroll(j)
		}
		val k = bottom - i - itemHeight - itemHeight
		if (k < 0) {
			scroll(-k)
		}
	}

	private fun scroll(amount: Int) {
		scrollAmount += amount.toDouble()
	}


	protected open fun updateScrollingState(mouseX: Double, mouseY: Double, button: Int) {
		scrolling = button == 0 && mouseX >= scrollbarX.toDouble() && mouseX < (scrollbarX + 6).toDouble()
	}

	var onClickedCallback: ((entry: E, mouseX: Double, mouseY: Double, button: Int) -> Boolean)? = null

	var onPressCallback: ((entry: E) -> Boolean)? = null

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active) return false
		updateScrollingState(mouseX, mouseY, button)
		return if (!isMouseOver(mouseX, mouseY)) {
			false
		} else {
			val entry = getEntryAtPosition(mouseX, mouseY)
			if (entry != null) {
				if (onClickedCallback?.invoke(entry, mouseX, mouseY, button) == true) {
					IbukiGourdMod.mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
					return true
				}
				if (button == 0 && onPressCallback?.invoke(entry) == true) {
					IbukiGourdMod.mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
					return true
				}
				if (entry.mouseClicked(mouseX, mouseY, button)) {
					focused = entry
					this.isDragging = true
					return true
				}
			}
			scrolling
		}
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active) return false
		this.focused?.mouseReleased(mouseX, mouseY, button)
		return false
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (!active) return false
		return if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			true
		} else if (button == 0 && scrolling) {
			if (mouseY < top.toDouble()) {
				scrollAmount = 0.0
			} else if (mouseY > bottom.toDouble()) {
				scrollAmount = maxScroll.toDouble()
			} else {
				val d = 1.coerceAtLeast(maxPosition).toDouble()
				val i = bottom - top
				val j = MathHelper.clamp(((i * i).toFloat() / maxPosition.toFloat()).toInt(), 32, i - 8)
				val e = max(1.0, d / (i - j).toDouble())
				scrollAmount += deltaY * e
			}
			true
		} else {
			false
		}
	}


	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		if (!active) return false
		scrollAmount -= amount * itemHeight.toDouble()
		return true
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (!active) return false
		return if (super.keyPressed(keyCode, scanCode, modifiers)) {
			true
		} else if (keyCode == 264) {
			moveSelection(MoveDirection.DOWN)
			true
		} else if (keyCode == 265) {
			moveSelection(MoveDirection.UP)
			true
		} else {
			false
		}
	}

	protected fun moveSelection(direction: MoveDirection) {
		if (!active) return
		moveSelectionIf(direction) { true }
	}

	protected fun ensureSelectedEntryVisible() {
		if (!active) return
		val entry = selectedEntry
		if (entry != null) {
			selectedEntry = entry
			ensureVisible(entry)
		}
	}

	protected fun moveSelectionIf(direction: MoveDirection, predicate: Predicate<E>) {
		val i = if (direction == MoveDirection.UP) -1 else 1
		if (children().isNotEmpty()) {
			var j = children().indexOf(selectedEntry)
			while (true) {
				val k = MathHelper.clamp(j + i, 0, entryCount - 1)
				if (j == k) {
					break
				}
				val entry: E = children()[k]
				if (predicate.test(entry)) {
					selectedEntry = entry
					ensureVisible(entry)
					break
				}
				j = k
			}
		}
	}

	fun tick() {
		if (!active) return
		children().forEach { it.tick() }
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		if (!active) return false
		return RenderUtil.isMouseHovered(x, y, width, height, mouseX, mouseY)
	}

	@Suppress("unchecked_cast")
	inline fun <W : WidgetListEntry<*>> isMouseOver(mouseX: Double, mouseY: Double, callback: (W) -> Unit) {
		if (!active) return
		if (isMouseOver(mouseX, mouseY)) {
			callback.invoke(this as W)
		}
	}

	protected open fun isFocused(): Boolean {
		return false
	}

	override fun getType(): SelectionType {
		return if (isFocused()) {
			SelectionType.FOCUSED
		} else {
			if (hoveredEntry != null) SelectionType.HOVERED else SelectionType.NONE
		}
	}

	protected fun removeEntry(entry: E): Boolean {
		val bl = children.remove(entry)
		if (bl && entry === selectedEntry) {
			selectedEntry = null
		}
		return bl
	}

	protected open fun getHoveredEntry(): E? {
		return hoveredEntry
	}


	override fun appendNarrations(builder: NarrationMessageBuilder) {
		if (!active) return
		getHoveredEntry()?.let {
			appendNarrations(builder, it)
		}
	}

	protected fun appendNarrations(builder: NarrationMessageBuilder, entry: E) {
		val list = children()
		if (list.size > 1) {
			val i = list.indexOf(entry)
			if (i != -1) {
				builder.put(NarrationPart.POSITION, TranslatableText("narrator.position.list", i + 1, list.size))
			}
		}
	}

	@Environment(EnvType.CLIENT)
	protected enum class MoveDirection {
		UP, DOWN
	}


}
