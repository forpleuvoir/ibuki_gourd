package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.gui.common.IPositionElement
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.NarrationSupplier
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonBase

 * 创建时间 2021/12/12 19:52

 * @author forpleuvoir

 */
abstract class ButtonBase<T : ButtonWidget>(
	x: Int,
	y: Int,
	width: Int,
	height: Int = 20,
	message: Text,
	private var onButtonPress: ((T) -> Unit)? = null
) : ButtonWidget(x, y, width, height, message, PressAction {}, NarrationSupplier { Text.literal(message.string) }),
	IPositionElement {

	protected val parent: Screen? = ScreenBase.current

	constructor(x: Int, y: Int, message: Text, onButtonPress: ((T) -> Unit)? = null) : this(
		x = x, y = y,
		width = MinecraftClient.getInstance().textRenderer.getWidth(message) + 20,
		message = message,
		onButtonPress = onButtonPress
	)

	constructor(x: Int, y: Int, message: Text, hoverText: List<Text>, onButtonPress: ((T) -> Unit)? = null) : this(
		x = x,
		y = y,
		message = message,
		onButtonPress = onButtonPress
	) {
		this.hoverText.clear()
		this.hoverText.addAll(hoverText)
	}

	protected val mc: MinecraftClient = MinecraftClient.getInstance()
	protected val textRenderer: TextRenderer = mc.textRenderer
	protected val hoverText: ArrayList<Text> = ArrayList()
	private var onHoverCallback: ((T) -> Unit)? = null
	private var hoverCallback: ((T) -> Unit)? = null
	private var onClickCallback: ((Double, Double, Int) -> Unit)? = null
	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null

	init {
		val text = Text.empty()
		hoverText.forEach {
			text.append(it)
		}
		this.setTooltip(Tooltip.of(text))
	}

	@Suppress("UNCHECKED_CAST")
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val hoverCallbacks = !hovered
		super.render(context, mouseX, mouseY, delta)
		if (hovered && hoverCallbacks) {
			onHoverCallback?.invoke(this as T)
		}
		if (hovered) hoverCallback?.invoke(this as T)
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

	fun setHoverCallback(hoverCallback: (T) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	fun setOnHoverCallback(hoverCallback: (T) -> Unit) {
		this.onHoverCallback = hoverCallback
	}

	fun setOnClickCallback(onClickCallback: ((Double, Double, Int) -> Unit)) {
		this.onClickCallback = onClickCallback
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!active || !visible) {
			return false
		}
		if (clicked(mouseX, mouseY)) {
			playDownSound(MinecraftClient.getInstance().soundManager)
			onClickCallback?.invoke(mouseX, mouseY, button)
			if (isValidClickButton(button)) onClick(mouseX, mouseY)
			return true
		}
		return false
	}

	@Suppress("UNCHECKED_CAST")
	override fun onPress() {
		onButtonPress?.invoke(this as T)
	}

	fun setHoverText(hoverText: List<Text>) {
		this.hoverText.clear()
		this.hoverText.addAll(hoverText)
	}

	fun setOnPressAction(onButtonPress: (T) -> Unit) {
		this.onButtonPress = onButtonPress
	}

	fun renderTooltip(context: DrawContext, mouseX: Int, mouseY: Int) {
		if (hovered && ScreenBase.isCurrent(parent)) {
			mc.currentScreen?.let {
				val height = this.hoverText.size * textRenderer.fontHeight
				var y = this.y + this.height + this.height * 0.7
				if (it.height - y < height) {
					y = this.y - height - this.height * 0.7
				}
				context.drawTooltip(mc.textRenderer, hoverText, mouseX, y.toInt())
			}
		}
	}
}