package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.render.RenderUtil.isMouseHovered
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.EntryListWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 ConfigBaseEntry

 * 创建时间 2021/12/17 23:34

 * @author forpleuvoir

 */
class ConfigBaseEntry(private val parent: WidgetListConfigBase, private val config: ConfigBase) : EntryListWidget.Entry<ConfigBaseEntry>() {

	private val mc = MinecraftClient.getInstance()
	private val textRenderer = mc.textRenderer
	private val message: MutableText = config.displayName as TranslatableText
	private val messageHoverText = config.displayRemark
	private val restButton: ButtonRest = ButtonRest(x = 0, y = 0, config = config)
	private val configWidget: ClickableWidget
	private val leftPadding = 10
	private val rightPadding = 10
	private val topPadding = 2
	private val bottomPadding = 2
	private val maxBgOpacity = 80
	private val bgOpacityDelta: Float = 20f

	private var index: Int = 0
	private var y: Int = 0
	private var x: Int = 0
	private var entryWidth: Int = 0
	private var entryHeight: Int = 0
	private var mouseX: Int = 0
	private var mouseY: Int = 0
	private var hovered: Boolean = false
	private var tickDelta: Float = 0f
	private var bgOpacity = 0

	private val messageX: Int
		get() = this.x + leftPadding

	private val messageY: Int
		get() {
			val textHeight = textRenderer.fontHeight
			return this.y + this.entryHeight / 2 - textHeight / 2
		}

	init {
		message.styled {
			it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, messageHoverText))
			return@styled it
		}
		this.configWidget = ConfigWrapper.wrap(config)
	}

	override fun render(
		matrices: MatrixStack?,
		index: Int,
		y: Int,
		x: Int,
		entryWidth: Int,
		entryHeight: Int,
		mouseX: Int,
		mouseY: Int,
		hovered: Boolean,
		tickDelta: Float
	) {
		this.update(index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta)
		this.updateWidgetPos()
		this.renderBackground()
		this.textRenderer.drawWithShadow(
			matrices,
			message,
			messageX.toFloat(),
			messageY.toFloat(),
			Color4i.WHITE.rgba
		)
		this.renderWidget(matrices)
		this.onHoverText(matrices)
	}

	private fun onHoverText(matrices: MatrixStack?) {
		val textWidth = textRenderer.getWidth(message)
		val textHeight = textRenderer.fontHeight
		isMouseHovered(messageX, messageY, textWidth, textHeight, mouseX, mouseY) {
			mc.currentScreen?.renderTooltip(matrices, messageHoverText, mouseX, mouseY)
		}

	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		this.restButton.mouseClicked(mouseX, mouseY, button)
		this.configWidget.mouseClicked(mouseX, mouseY, button)
		if (isMouseHovered(this.configWidget, mouseX, mouseY)) {
			if (!this.configWidget.isFocused)
				configWidget.changeFocus(true)
		} else {
			if (this.configWidget.isFocused)
				configWidget.changeFocus(true)
		}
		return false
	}


	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (configWidget.isFocused) {
			return this.configWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	private fun renderWidget(matrices: MatrixStack?) {
		this.configWidget.render(matrices, mouseX, mouseY, tickDelta)
		this.restButton.render(matrices, mouseX, mouseY, tickDelta)
	}

	private fun updateWidgetPos() {
		this.restButton.y = this.y + (this.entryHeight / 2 - this.restButton.height / 2)
		this.restButton.x = this.entryWidth + this.x - rightPadding - this.restButton.width
		this.configWidget.x = this.restButton.x - rightPadding - this.configWidget.width
		this.configWidget.y = this.y + (this.entryHeight / 2 - this.configWidget.height / 2)
	}

	private fun update(
		index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float
	) {
		this.index = index
		this.x = x
		this.y = y
		this.entryWidth = entryWidth
		this.entryHeight = entryHeight
		this.mouseX = mouseX
		this.mouseY = mouseY
		this.hovered = isMouseHovered(x, y, entryWidth, entryHeight, mouseX, mouseY)
		this.tickDelta = tickDelta
	}

	fun renderSelected() {
		RenderUtil.drawOutline(x = x, y = y, width = entryWidth, height = entryHeight, borderColor = Color4i(255, 255, 255, 255))
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return this.configWidget.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return this.configWidget.keyReleased(keyCode, scanCode, modifiers)
	}

	fun tick() {
		if (configWidget is TextFieldWidget) {
			configWidget.tick()
		}
	}


	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		return this.configWidget.charTyped(chr, modifiers)
	}

	private fun renderBackground() {
		val delta: Float = if (hovered) tickDelta * bgOpacityDelta else -tickDelta * bgOpacityDelta
		updateBgOpacity(delta)
		RenderUtil.drawRect(x = x, y = y, width = entryWidth, height = entryHeight, Color4i(255, 255, 255, bgOpacity))
	}

	private fun updateBgOpacity(delta: Float) {
		bgOpacity += delta.toInt()
		bgOpacity = MathHelper.clamp(bgOpacity, 0, maxBgOpacity)
	}


	private fun isOdd(index: Int): Boolean {
		return index % 2 == 1
	}
}