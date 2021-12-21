package forpleuvoir.ibuki_gourd.gui

import forpleuvoir.ibuki_gourd.utils.color.Color4f.Companion.BLACK
import forpleuvoir.ibuki_gourd.utils.color.Color4f.Companion.WHITE
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui

 * 文件名 ScreenBase

 * 创建时间 2021/12/13 16:47

 * @author forpleuvoir

 */
abstract class ScreenBase(title: Text) : Screen(title), IScreenBase {
	var shouldCloseOnEsc: Boolean = true

	companion object {
		@JvmStatic
		fun openScreen(screen: Screen?) {
			MinecraftClient.getInstance().setScreen(screen)
		}
	}

	val mc: MinecraftClient = MinecraftClient.getInstance()
	var backgroundColor: Int = BLACK.rgba(0.5f)
	var titleLeftPadding = 15f
	var titleTopPadding = 10f
	override var parent: Screen? = null

	override fun isPauseScreen(): Boolean {
		return false
	}

	private fun drawBackgroundColor(matrices: MatrixStack?) {
		DrawableHelper.fill(matrices, 0, 0, this.width, this.height, backgroundColor)
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		this.drawBackgroundColor(matrices)
		super.render(matrices, mouseX, mouseY, delta)
		this.drawTitle(matrices)
	}

	override fun shouldCloseOnEsc(): Boolean {
		return shouldCloseOnEsc
	}

	private fun drawTitle(matrices: MatrixStack?) {
		this.textRenderer.drawWithShadow(matrices, title, titleLeftPadding, titleTopPadding, WHITE.rgba)
	}

	override fun onScreenClose() {

	}

	@Suppress("unchecked_cast")
	override fun onClose() {
		this.onScreenClose()
		mc.setScreen(parent)
	}
}