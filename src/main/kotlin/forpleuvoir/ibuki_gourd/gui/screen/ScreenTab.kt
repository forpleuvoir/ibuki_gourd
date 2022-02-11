package forpleuvoir.ibuki_gourd.gui.screen

import forpleuvoir.ibuki_gourd.config.ConfigManager
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.widget.WidgetDropList
import forpleuvoir.ibuki_gourd.utils.clamp
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

	override fun init() {
		super.init()
		initConfigGroupButton()
		initModScreen()
	}

	private fun initModScreen() {
		if (ConfigManager.modScreen.size != 0)
			this.addLastDrawableElement(
				WidgetDropList(
					items = ConfigManager.modScreen.keys,
					default = tabEntry.currentMod.modName,
					stringAdapter = { it },
					entryAdapter = { str -> ConfigManager.modScreen.keys.find { it == str }!! },
					parent = this,
					x = this.width - 10 - 80,
					y = 10,
					width = 80,
					itemHeight = 14,
					pageSize = ConfigManager.modScreen.size.clamp(1, 5).toInt()
				)
			)
	}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!this::matrices.isInitialized || this.matrices != matrices) this.matrices = matrices
		super.render(matrices, mouseX, mouseY, delta)
	}

	protected fun drawTopMessage(message: Text) {
		if (isCurrent(this))
			if (textRenderer.getWidth(message) > (textRenderer.getWidth(message) / 2))
				textRenderer.drawWithShadow(
					matrices,
					message,
					(this.width / 2 - textRenderer.getWidth(message) / 2).toFloat(),
					titleTopPadding.toFloat(),
					Color4f.WHITE.rgba
				)
	}

	private fun initConfigGroupButton() {
		var posX = 15
		tabEntry.all.forEach {
			val button = Button(posX, top, tabEntry.buttonHeight, it.displayKey) { _ ->
				it.buttonPress.invoke(tabEntry)
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