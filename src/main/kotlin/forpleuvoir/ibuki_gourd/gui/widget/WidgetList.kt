package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.common.mText
import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutline
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.AbstractParentElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.Selectable.SelectionType
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TranslatableTextContent
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
) : AbstractParentElement(), Drawable, Selectable, IPositionElement {

	protected val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
	protected open val children: MutableList<E> = ArrayList()

	val height: Int get() = this.contentHeight + this.topPadding + this.bottomPadding
	val top: Int get() = this.y
	val bottom: Int get() = this.y + this.height
	val left: Int get() = this.x
	val right: Int get() = this.x + this.width

	var active: Boolean = true
	var visible: Boolean = true

	var renderBackground: Boolean = false
	var backgroundColor: IColor<out Number> = Color4f.BLACK
	var renderBord: Boolean = false
	var bordColor: IColor<out Number> = Color4f.WHITE

	var scrollbarColor: IColor<out Number> = Color4f.WHITE
	var scrollbarBgColor: IColor<out Number> = Color4f(1f, 1f, 1f, 0.3f)
	protected open val scrollbarPadding: Int = 1
	protected val scrollbarWidth: Int = 6
		get() {
			return if (maxScroll > 0) field else 0
		}
	private val scrollbarHeight: Int get() = this.contentHeight
	private val scrollbarX: Int get() = this.x + this.width - this.scrollbarWidth
	private val scrollbarY: Int get() = this.contentTop
	private val maxScroll: Int get() = 0.coerceAtLeast(this.maxPosition - (this.contentHeight))

	val scrollbar: Scrollbar = Scrollbar(
		scrollbarX,
		scrollbarY,
		scrollbarWidth,
		scrollbarHeight,
		{ maxScroll },
		{ this.pageSize.toDouble() / this.entryCount.toDouble() }
	)

	fun setScrollAmountConsumer(consumer: (Double) -> Unit) {
		scrollbar.amountConsumer = consumer
	}

	protected fun scrollbarHovered(mouseX: Double, mouseY: Double): Boolean {
		return RenderUtil.isMouseHovered(
			this.scrollbarX,
			this.scrollbarY,
			this.scrollbarWidth,
			this.scrollbarHeight,
			mouseX,
			mouseY
		)
	}

	protected inline fun scrollbarHovered(mouseX: Double, mouseY: Double, callback: () -> Unit) {
		if (scrollbarHovered(mouseX, mouseY)) callback.invoke()
	}

	protected open val maxPosition: Int get() = entryCount * this.itemHeight

	private var scrolling = false
	var selectedEntry: E? = null
	private var hoveredEntry: E? = null


	protected open val contentWidth: Int get() = this.width - this.leftPadding - this.rightPadding - this.scrollbar.width
	protected open val contentHeight: Int get() = this.pageSize * this.itemHeight
	protected open val contentLeft: Int get() = this.left + this.leftPadding
	protected open val contentRight: Int get() = this.right - this.rightPadding
	protected open val contentTop: Int get() = this.top + this.topPadding
	protected open val contentBottom: Int get() = this.bottom - this.bottomPadding


	protected open fun getContentTop(index: Int): Int {
		return top - scrollbar.amount.toInt() + index * this.itemHeight + this.topPadding
	}

	private fun getContentBottom(index: Int): Int {
		return getContentTop(index) + itemHeight
	}

	protected open val entryCount: Int get() = this.children().size

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

	fun allChildren(): List<E> {
		return children
	}

	@Suppress("unchecked_cast")
	override fun children(): List<E> {
		return if (children.isNotEmpty())
			children.stream().filter(childFilter).toList().also { list ->
				list.forEach { it.setOnHoverCallback { e -> onHoverCallback?.invoke(e as E) } }
				list.forEach { it.setHoverCallback { e -> hoverCallback?.invoke(e as E) } }
			}
		else emptyList()
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
		scrollbar.apply {
			this.x = scrollbarX
			this.y = scrollbarY
			this.width = scrollbarWidth
			this.height = scrollbarHeight
		}
		return children.size - 1
	}


	protected open fun isSelectedEntry(index: Int): Boolean {
		return selectedEntry == children()[index]
	}

	protected open fun getEntryAtPosition(x: Double, y: Double): E? {
		return children().filter {
			it.isMouseOver(x, y)
		}.getOrNull(0)
	}


	override var onPositionChanged: ((Int, Int, Int, Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		children().forEach {
			it.deltaPosition(deltaX, deltaY)
			scrollbar.deltaPosition(deltaX, deltaY)
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


	protected open fun renderBord(drawContext: DrawContext) {
		drawOutline(
			drawContext,
			this.x - 1,
			this.y - 1,
			this.width + 2,
			this.height + 2,
			borderColor = bordColor
		)
	}

	protected open fun renderBackground(drawContext: DrawContext) {
		drawRect(
			drawContext,
			this.x,
			this.y,
			this.width,
			this.height,
			color = backgroundColor
		)
	}

	protected open fun renderDecorations(drawContext: DrawContext, mouseX: Int, mouseY: Int) {}

	protected open fun updateChildren() {
		for (index in 0 until children().size) {
			children()[index].index = index
			children()[index].setPosition(
				this.contentLeft,
				this.contentTop + this.itemHeight * index - this.scrollbar.amount.toInt()
			)
			val active =
				!(children()[index].y < this.contentTop || children()[index].y + children()[index].height > this.contentBottom)
			children()[index].active = active
			children()[index].visible = active
			if (active) children()[index].width = this.contentWidth
		}
	}


	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (visible) {
			updateChildren()
			if (renderBackground) renderBackground(context)
			if (renderBord) renderBord(context)
			hoveredEntry =
				if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) getEntryAtPosition(
					mouseX.toDouble(),
					mouseY.toDouble()
				) else null
			scrollbar.render(context, mouseX, mouseY, delta)
			children().forEach {
				it.render(context, mouseX, mouseY, delta)
			}
			renderDecorations(context, mouseX, mouseY)
		}
	}

	var onClickedCallback: ((entry: E, mouseX: Double, mouseY: Double, button: Int) -> Boolean)? = null

	var onPressCallback: ((entry: E) -> Boolean)? = null

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active) return false
		return if (!isMouseOver(mouseX, mouseY)) {
			false
		} else {
			val entry = getEntryAtPosition(mouseX, mouseY)
			if (entry != null) {
				if (onClickedCallback?.invoke(entry, mouseX, mouseY, button) == true) {
					IbukiGourdMod.mc.soundManager.play(
						PositionedSoundInstance.master(
							SoundEvents.UI_BUTTON_CLICK,
							1.0f
						)
					)
					return true
				}
				if (button == 0 && onPressCallback?.invoke(entry) == true) {
					IbukiGourdMod.mc.soundManager.play(
						PositionedSoundInstance.master(
							SoundEvents.UI_BUTTON_CLICK,
							1.0f
						)
					)
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
		children().forEach {
			if (it.isDragging) {
				it.mouseReleased(mouseX, mouseY, button)
			}
		}
		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (!active) return false
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		if (!active) return false
		scrollbar - amount * itemHeight
		return true
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

	override fun isFocused(): Boolean {
		return false
	}

	override fun getType(): SelectionType {
		return if (isFocused()) {
			SelectionType.FOCUSED
		} else {
			if (hoveredEntry != null) SelectionType.HOVERED else SelectionType.NONE
		}
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

	private fun appendNarrations(builder: NarrationMessageBuilder, entry: E) {
		val list = children()
		if (list.size > 1) {
			val i = list.indexOf(entry)
			if (i != -1) {
				builder.put(
					NarrationPart.POSITION,
					TranslatableTextContent("narrator.position.list", null, arrayOf(i + 1, list.size)).mText
				)
			}
		}
	}

}
