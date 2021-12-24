package forpleuvoir.ibuki_gourd.gui.screen

import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.utils.color.Color4f.Companion.BLACK
import forpleuvoir.ibuki_gourd.utils.color.Color4f.Companion.WHITE
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.screen

 * 文件名 ScreenBase

 * 创建时间 2021/12/13 16:47

 * @author forpleuvoir

 */
abstract class ScreenBase(title: Text) : Screen(title), IScreenBase {
	var shouldCloseOnEsc: Boolean = true
	protected var pauseScreen: Boolean = false


	constructor() : this("".text())
	constructor(title: String) : this(title.text())

	companion object {

		@JvmStatic
		protected val guiScale: Int
			get() = MinecraftClient.getInstance().options.guiScale

		@JvmStatic
		fun openScreen(screen: Screen?) {
			MinecraftClient.getInstance().setScreen(screen)
		}

		fun isCurrent(screen: Screen?): Boolean {
			return screen == MinecraftClient.getInstance().currentScreen
		}
	}

	protected val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

	var backgroundColor: IColor<*> = BLACK.apply { alpha = 0.5f }
	protected val titleWidth: Int
		get() = textRenderer.getWidth(title)
	protected val titleHeight: Int
		get() {
			return if (title.string.isNotEmpty()) textRenderer.fontHeight
			else 0
		}
	protected val titleTop: Int
		get() = titleTopPadding
	protected val titleBottom: Int
		get() = titleTop + titleHeight
	protected val titleRight: Int
		get() {
			return if (title.string.isNotEmpty()) titleLeftPadding + titleWidth
			else 0
		}
	protected val titleLeft: Int
		get() {
			return if (title.string.isNotEmpty()) titleLeftPadding
			else 0
		}

	open var titleLeftPadding = 15
	open var titleTopPadding = 10

	protected lateinit var titleLabel: LabelText


	protected val topPadding: Int
		get() {
			return if (title.string.isNotEmpty())
				titleTopPadding + MinecraftClient.getInstance().textRenderer.fontHeight + 5
			else titleTopPadding
		}
	final override var parent: Screen? = null

	override fun init() {
		super.init()
		titleLabel = LabelText(title, titleLeftPadding, titleTopPadding).apply { align = LabelText.Align.CENTER_LEFT }
		this.addDrawableChild(titleLabel)
	}

	override fun isPauseScreen(): Boolean {
		return this.pauseScreen
	}

	protected open fun drawBackgroundColor(matrices: MatrixStack) {
		DrawableHelper.fill(matrices, 0, 0, this.width, this.height, backgroundColor.rgba)
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		this.drawBackgroundColor(matrices)
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun shouldCloseOnEsc(): Boolean {
		return shouldCloseOnEsc
	}


	override fun onScreenClose() {

	}

	override fun onClose() {
		this.onScreenClose()
		openScreen(parent)
	}
}