package forpleuvoir.ibuki_gourd.gui.screen

import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs.Setting.SCREEN_BACKGROUND_COLOR
import forpleuvoir.ibuki_gourd.render.RenderUtil.drawRect
import forpleuvoir.ibuki_gourd.utils.color.IColor
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.util.*


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

	private val lastDrawable: LinkedList<Drawable> = LinkedList()

	constructor() : this("".text)
	constructor(title: String) : this(title.text)

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

		val current: Screen? get() = MinecraftClient.getInstance().currentScreen

		fun inCurrent(element: Element): Boolean {
			return MinecraftClient.getInstance().currentScreen?.children()?.contains(element) ?: false
		}


	}

	val mc: MinecraftClient by lazy { MinecraftClient.getInstance() }

	open val backgroundColor: IColor<out Number> get() = SCREEN_BACKGROUND_COLOR.getValue()

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

	protected var titleLabel: LabelText =
		LabelText(title, 0, 0).apply { align = LabelText.Align.CENTER_LEFT }


	protected val topPadding: Int
		get() {
			return if (title.string.isNotEmpty())
				titleTopPadding + MinecraftClient.getInstance().textRenderer.fontHeight + 5
			else titleTopPadding
		}
	final override var parent: Screen? = null

	init {
		zOffset = 0
	}

	override fun init() {
		super.init()
		lastDrawable.clear()
		titleLabel.setPosition(titleLeftPadding, titleTopPadding)
		this.addDrawableChild(titleLabel)
	}

	override fun shouldPause(): Boolean {
		return this.pauseScreen
	}

	protected open fun drawBackgroundColor(matrices: MatrixStack) {
		drawRect(matrices, 0, 0, this.width, this.height, backgroundColor)
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		matrices.translate(0.0, 0.0, zOffset.toDouble())
		this.drawBackgroundColor(matrices)
		super.render(matrices, mouseX, mouseY, delta)
		for (drawable in lastDrawable) {
			drawable.render(matrices, mouseX, mouseY, delta)
		}
	}

	override fun shouldCloseOnEsc(): Boolean {
		return shouldCloseOnEsc
	}


	fun <T> addElement(drawableElement: T): T where  T : Element, T : Drawable, T : Selectable {
		return addDrawableChild(drawableElement)
	}

	fun <T> addLastDrawableElement(drawableElement: T): T where  T : Element, T : Drawable, T : Selectable {
		lastDrawable.add(drawableElement as Drawable)
		return addSelectableChild(drawableElement)
	}

	fun removeElement(element: Element) {
		this.remove(element)
	}

	override fun onScreenClose() {

	}

	override fun onClose() {
		this.onScreenClose()
		openScreen(parent)
	}
}