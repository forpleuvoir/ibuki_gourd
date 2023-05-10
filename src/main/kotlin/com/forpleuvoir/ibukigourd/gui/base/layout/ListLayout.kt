package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.gui.base.mouseHover
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.LIST_BACKGROUND
import com.forpleuvoir.ibukigourd.gui.widget.Scroller
import com.forpleuvoir.ibukigourd.gui.widget.scroller
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import com.forpleuvoir.ibukigourd.render.base.Size
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.render.scissor
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.util.math.MatrixStack

class ListLayout(
	width: Float = 0f,
	height: Float = 0f,
	val arrangement: Arrangement = Arrangement.Vertical,
	private val scrollerThickness: Float = 8f,
) : AbstractElement() {

	lateinit var scrollerBar: Scroller
		private set

	init {
		if (width != 0f) {
			transform.fixedWidth = true
			transform.width = width
		}
		if (height != 0f) {
			transform.fixedHeight = true
			transform.height = height
		}
		padding(6f)
	}

	var amount: Float
		get() {
			return if (this::scrollerBar.isInitialized) {
				scrollerBar.amount
			} else 0f
		}
		set(value) {
			if (this::scrollerBar.isInitialized) {
				scrollerBar.amount = value
			}
		}

	override fun init() {
		for (e in elements) e.init.invoke()
		val contentSize = arrangement.contentSize(layout.alignRects(elements))
		if (!::scrollerBar.isInitialized) {
			scrollerBar = scroller(
				arrangement.switch({ transform.height - padding.height }, { transform.width - padding.width }),
				scrollerThickness,
				{ (layout.alignRects(elements).minOf { r -> arrangement.switch({ r.height }, { r.width }) } / 2f) },
				{
					arrangement.switch(
						{ contentSize.height - contentRect(false).height },
						{ contentSize.width - contentRect(false).width }
					).coerceAtLeast(0f)
				},
				{
					arrangement.switch(
						{ contentRect(false).height / contentSize.height },
						{ contentRect(false).width / contentSize.width }
					).clamp(0f..1f)
				},
				arrangement
			) {
				fixed = true
			}
			scrollerBar.amountReceiver = {
				arrange()
			}
		}
		arrange()
		if (this::scrollerBar.isInitialized) {
			arrangement.switch({
				scrollerBar.transform.x = transform.right - scrollerThickness - padding.right / 2
				scrollerBar.transform.y = padding.top
			}, {
				scrollerBar.transform.y = transform.bottom - scrollerThickness - padding.bottom / 2
				scrollerBar.transform.x = padding.left
			})
		}
		tip?.init?.invoke()
	}


	override var layout: Layout = object : Layout {
		override val elementContainer: () -> ElementContainer
			get() = { this@ListLayout }

		override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
			val alignElements = elements.filter { !it.fixed }
			if (alignElements.isEmpty()) return null

			val alignRects = alignRects(alignElements)

			val alignment = arrangement.switch({
				PlanarAlignment.TopLeft(arrangement)
			}, {
				PlanarAlignment.CenterLeft(arrangement)
			})

			val container = elementContainer()
			val size = arrangement.contentSize(alignRects)
			val contentRect = when {
				//固定高度和宽度
				container.transform.fixedWidth && container.transform.fixedHeight -> {
					container.contentRect(false)
				}
				//固定宽度 不固定高度
				container.transform.fixedWidth && !container.transform.fixedHeight -> {
					rect(container.contentRect(false).position, container.transform.width, size.height)
				}
				//不固定宽度 固定高度
				!container.transform.fixedWidth && container.transform.fixedHeight -> {
					rect(container.contentRect(false).position, size.width, container.transform.height)
				}
				//不固定宽度 不固定高度
				else -> {
					rect(container.contentRect(false).position, size)
				}
			}
			alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
				val element = alignElements[index]
				val v = vector3f + arrangement.switch(
					{ Vector3f(0f, -amount, 0f) },
					{ Vector3f(-amount, 0f, 0f) }
				)
				element.transform.translateTo(v + Vector3f(element.margin.left, element.margin.top))
			}
			return arrangement.switch({
				Size.create(contentRect.width + padding.width + scrollerThickness, contentRect.height + padding.height)
			}, {
				Size.create(contentRect.width + padding.width, contentRect.height + padding.height + scrollerThickness)
			})
		}

	}
		@Deprecated("Do not set the layout value of ListLayout") set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError("Do not set the layout value of ListLayout")
		}

	override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
		val top = if (isWorld) transform.worldTop + padding.top else padding.top

		val bottom =
			if (isWorld) transform.worldBottom - padding.bottom + arrangement.switch({ 0f }, { -scrollerThickness })
			else transform.height - padding.bottom + arrangement.switch({ 0f }, { -scrollerThickness })

		val left = if (isWorld) transform.worldLeft + padding.left else padding.left

		val right =
			if (isWorld) transform.worldRight - padding.right + arrangement.switch({ -scrollerThickness }, { 0f })
			else transform.width - padding.right + arrangement.switch({ -scrollerThickness }, { 0f })

		return rect(
			vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
		)
	}

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		if (!mouseHover()) return NextAction.Continue
		return super.onMouseClick(mouseX, mouseY, button)
	}

	override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
		mouseHover {
			if (!scrollerBar.mouseHover()) {
				scrollerBar.amount -= scrollerBar.amountStep() * amount
			}
		}
		return super.onMouseScrolling(mouseX, mouseY, amount)
	}

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (!visible) return
		renderBackground.invoke(matrixStack, delta)
		scissor(super.contentRect(true)) {
			renderTree.filter { it != scrollerBar }.forEach { it.render(matrixStack, delta) }
		}
		scrollerBar.render(matrixStack, delta)
		renderOverlay.invoke(matrixStack, delta)
	}

	override fun onRenderBackground(matrixStack: MatrixStack, delta: Float) {
		renderTexture(matrixStack, this.transform, LIST_BACKGROUND)
	}

}

fun ElementContainer.list(
	width: Float,
	height: Float,
	arrangement: Arrangement = Arrangement.Vertical,
	scrollerThickness: Float = 8f,
	scope: ListLayout.() -> Unit = {}
) = this.addElement(ListLayout(width, height, arrangement, scrollerThickness).apply(scope))