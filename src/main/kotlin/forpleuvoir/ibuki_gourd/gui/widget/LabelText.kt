package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.gui.widget.LabelText.Align.*
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawOutlinedBox
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.Selectable.SelectionType
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 LabelText

 * 创建时间 2021/12/23 17:42

 * @author forpleuvoir

 */
class LabelText(var text: Text, var x: Int, var y: Int, var width: Int, var height: Int, var align: Align = CENTER) :
	Drawable, Element,
	Selectable, IPositionElement {

	var visible = true
	var active = true
	private var onPressCallback: ((LabelText) -> Boolean)? = null
	private var onClickedCallback: ((LabelText, Int) -> Boolean)? = null
	private val parent: Screen? = MinecraftClient.getInstance().currentScreen
	private val textRenderer: TextRenderer get() = MinecraftClient.getInstance().textRenderer
	private val hoverTexts: ArrayList<Text> by lazy { ArrayList() }
	var renderHoverText: Boolean = true
	private lateinit var drawContext: DrawContext
	private val renderText: String
		get() {
			return if (this.width - this.rightPadding - this.leftPadding >= mc.textRenderer.getWidth(text)) {
				text.string
			} else {
				textRenderer.trimToWidth(text, this.width - this.rightPadding - this.leftPadding).string
			}
		}
	private var topPadding: Int = 2
	private var bottomPadding: Int = 2
	private var leftPadding: Int = 2
	private var rightPadding: Int = 2
	private val textWidth: Int
		get() = textRenderer.getWidth(this.text)
	private val textHeight: Int
		get() = textRenderer.fontHeight
	private val centerX: Int
		get() = this.x + this.width / 2
	private val centerY: Int
		get() = this.y + this.height / 2
	var textColor: IColor<out Number> = Color4i().fromInt(text.style.color?.rgb ?: Color4i.WHITE.rgba)
	var rightToLeft: Boolean = false
	var shadow: Boolean = true
	var backgroundColor: IColor<out Number> = Color4i.WHITE.apply { alpha = 0 }
	var bordColor: IColor<out Number> = Color4i.WHITE.apply { alpha = 0 }
	private var hoverCallback: ((LabelText) -> Unit)? = null
	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null

	constructor(text: Text, x: Int, y: Int, padding: Int = 2) : this(
		text,
		x,
		y,
		mc.textRenderer.getWidth(text),
		mc.textRenderer.fontHeight
	) {
		setPadding(padding, padding, padding, padding)
		this.width += rightPadding + leftPadding
		this.height += topPadding + bottomPadding
	}

	fun setOnPressCallback(onPressCallback: (LabelText) -> Boolean) {
		this.onPressCallback = onPressCallback
	}

	fun setOnClickedCallback(onClickedCallback: (LabelText, Int) -> Boolean) {
		this.onClickedCallback = onClickedCallback
	}

	fun setPadding(
		topPadding: Int = 2,
		bottomPadding: Int = 2,
		leftPadding: Int = 2,
		rightPadding: Int = 2
	) {
		this.topPadding = topPadding
		this.bottomPadding = bottomPadding
		this.leftPadding = leftPadding
		this.rightPadding = rightPadding
	}

	fun setHoverCallback(hoverCallback: (LabelText) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		if (!this::drawContext.isInitialized || this.drawContext != drawContext) this.drawContext = context
		renderBox(context)
		renderText(context)
		if (ScreenBase.isCurrent(parent) && renderHoverText)
			RenderUtil.isMouseHovered(x, y, width, height, mouseX, mouseY) {
				hoverCallback?.invoke(this)
				context.matrices.translate(0.0, 0.0, 2.0)
				context.drawTooltip(mc.textRenderer, hoverTexts, mouseX, mouseY)
				context.matrices.translate(0.0, 0.0, -2.0)
			}
	}

	private fun renderBox(context: DrawContext) {
		drawOutlinedBox(context, x, y, width, height, backgroundColor, bordColor)
	}

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

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active) return false
		RenderUtil.isMouseHovered(this.x, this.y, this.width, this.height, mouseX, mouseY) {
			if (button == 0 && this.onPressCallback?.invoke(this) == true) {
				mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
				return true
			}
			if (this.onClickedCallback?.invoke(this, button) == true) {
				mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
				return true
			}
		}
		return false
	}

	override fun setFocused(focused: Boolean) {

	}

	override fun isFocused(): Boolean {
		return false
	}

	private fun renderText(drawContext: DrawContext) {
		val textX: Int
		val textY: Int
		when (align) {
			TOP_LEFT -> {
				textX = this.x + leftPadding
				textY = this.y + topPadding
			}

			TOP_CENTER -> {
				textX = centerX - textWidth / 2
				textY = this.y + topPadding
			}

			TOP_RIGHT -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = this.y + topPadding
			}

			CENTER_LEFT -> {
				textX = this.x + leftPadding
				textY = centerY - textHeight / 2
			}

			CENTER -> {
				textX = centerX - textWidth / 2
				textY = centerY - textHeight / 2
			}

			CENTER_RIGHT -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = centerY - textHeight / 2
			}

			BOTTOM_LEFT -> {
				textX = this.x + leftPadding
				textY = this.y - textHeight - bottomPadding
			}

			BOTTOM_CENTER -> {
				textX = centerX - textWidth / 2
				textY = this.y - textHeight - bottomPadding
			}

			BOTTOM_RIGHT -> {
				textX = this.x + this.width - textWidth - rightPadding
				textY = this.y - textHeight - bottomPadding
			}
		}
		val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
		textRenderer.draw(
			renderText,
			textX.toFloat(),
			textY.toFloat(),
			textColor.rgba,
			shadow,
			drawContext.matrices.peek().positionMatrix,
			immediate,
			TextRenderer.TextLayerType.NORMAL,
			0,
			LightmapTextureManager.MAX_LIGHT_COORDINATE,
			rightToLeft
		)
		immediate.draw()
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {
		builder.put(NarrationPart.TITLE, this.text)
	}

	override fun getType(): SelectionType {
		return SelectionType.HOVERED
	}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	enum class Align {
		TOP_LEFT, TOP_CENTER, TOP_RIGHT,
		CENTER_LEFT, CENTER, CENTER_RIGHT,
		BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
	}
}