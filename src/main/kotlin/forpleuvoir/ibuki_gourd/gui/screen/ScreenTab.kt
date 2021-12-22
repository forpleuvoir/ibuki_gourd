package forpleuvoir.ibuki_gourd.gui.screen

import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.screen

 * 文件名 ScreenTab

 * 创建时间 2021/12/22 15:12

 * @author forpleuvoir

 */
open class ScreenTab(private val tabEntry: IScreenTabEntry) :
	ScreenBase("${tabEntry.baseTitle.string} ${if (tabEntry.baseTitle.string.isNotEmpty()) "=>" else ""} ${tabEntry.displayKey.string}") {

	protected val margin: Int = 2
	protected var top: Int = topPadding + margin

	private lateinit var matrices: MatrixStack

	init {
		tabEntry.changeCurrent(tabEntry)
	}

	override fun init() {
		super.init()
		initConfigGroupButton()

	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!this::matrices.isInitialized || this.matrices != matrices) this.matrices = matrices
		super.render(matrices, mouseX, mouseY, delta)
	}

	protected fun drawTopMessage(message: Text) {
		matrices.let {
			if (textRenderer.getWidth(message) > (textRenderer.getWidth(message) / 2))
				textRenderer.drawWithShadow(
					it,
					message,
					(this.width / 2 - textRenderer.getWidth(message) / 2).toFloat(),
					titleTopPadding,
					Color4f.WHITE.rgba
				)
		}
	}

	private fun initConfigGroupButton() {
		var posX = 15
		tabEntry.all.forEach {
			val button = Button(posX, top, tabEntry.buttonHeight, it.displayKey) { _ ->
				tabEntry.buttonPress.invoke(it)
			}.apply {
				setHoverCallback { _ ->
					drawTopMessage(it.displayRemark)
				}
				active = it != tabEntry
				posX += width + margin
				if (x + width > this@ScreenTab.width - 15) top += height + margin
			}
			this.addDrawableChild(button)
		}
	}

}