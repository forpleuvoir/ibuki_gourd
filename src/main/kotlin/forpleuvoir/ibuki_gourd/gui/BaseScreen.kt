package forpleuvoir.ibuki_gourd.gui

import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
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

 * 文件名 BaseScreen

 * 创建时间 2021/12/13 16:47

 * @author forpleuvoir

 */
abstract class BaseScreen(title: Text) : Screen(title), IBaseScreen {

	companion object {
		@JvmStatic
		fun openScreen(screen: Screen) {
			MinecraftClient.getInstance().setScreen(screen)
		}
	}

	val mc: MinecraftClient = MinecraftClient.getInstance()
	var backgroundColor: Int = BLACK.intValue(0.5f)
	var titlePadding = 20f
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

	private fun drawTitle(matrices: MatrixStack?) {
		this.textRenderer.drawWithShadow(matrices, title, titlePadding, titlePadding, WHITE.intValue)
	}

	override fun onScreenClose() {

	}

	@Suppress("unchecked_cast")
	override fun onClose() {
		this.onScreenClose()
		mc.setScreen(parent)
	}
}