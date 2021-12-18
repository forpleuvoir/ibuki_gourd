package forpleuvoir.ibuki_gourd.gui.button

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
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
) :
	ButtonWidget(x, y, width, height, message, PressAction {}) {

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
	private var hoverCallback: ((T) -> Unit)? = null

	@Suppress("UNCHECKED_CAST")
	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		val hoverCallbackAble = !hovered
		super.render(matrices, mouseX, mouseY, delta)
		if (hovered && hoverCallbackAble) {
			hoverCallback?.invoke(this as T)
		}
	}

	fun setPosition(x: Int, y: Int) {
		this.x = x
		this.y = y
	}

	fun setOnHoverCallback(hoverCallback: (T) -> Unit) {
		this.hoverCallback = hoverCallback
	}

	@Suppress("UNCHECKED_CAST")
	override fun onPress() {
		super.onPress()
		onButtonPress?.invoke(this as T)
	}

	fun setHoverText(hoverText: List<Text>) {
		this.hoverText.clear()
		this.hoverText.addAll(hoverText)
	}

	fun setOnPressAction(onButtonPress: (T) -> Unit) {
		this.onButtonPress = onButtonPress
	}

	override fun renderTooltip(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {
		if (hovered) mc.currentScreen?.renderTooltip(matrices, hoverText, mouseX, mouseY)
	}
}