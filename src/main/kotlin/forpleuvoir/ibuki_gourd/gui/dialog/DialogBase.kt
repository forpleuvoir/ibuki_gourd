package forpleuvoir.ibuki_gourd.gui.dialog

import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.toast.Toast
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.dialog

 * 文件名 DialogBase

 * 创建时间 2021/12/23 16:25

 * @author forpleuvoir

 */
open class DialogBase<D : DialogBase<D>>(protected var dialogWidth: Int, protected var dialogHeight: Int, title: Text, parent: Screen?) :
	ScreenBase(title) {

	val x: Int get() = width / 2 - dialogWidth / 2
	val y: Int get() = height / 2 - dialogHeight / 2

	private var onCloseCallback: ((D) -> Unit)? = null

	fun setOnCloseCallback(onCloseCallback: (D) -> Unit) {
		this.onCloseCallback = onCloseCallback
	}

	init {
		this.parent = parent
	}

	protected val margin: Int get() = 5

	override var titleLeftPadding: Int
		get() = super.titleLeftPadding + this.x
		set(value) {
			value + this.x
		}

	override var titleTopPadding: Int
		get() = super.titleTopPadding + this.y
		set(value) {
			value + this.y
		}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		parent?.render(matrices, mouseX, mouseY, delta)
		renderBackground(matrices)
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun drawBackgroundColor(matrices: MatrixStack) {
		//RenderUtil.drawOutlinedBox(x, y, dialogWidth, dialogHeight, Color4i.BLACK.apply { alpha = 180 }, Color4f.WHITE)
	}

	override fun renderBackground(matrices: MatrixStack, vOffset: Int) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader)
		RenderSystem.setShaderTexture(0, Toast.TEXTURE)
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.enableDepthTest()
		matrices.translate(0.0,0.0,10.0)
		//top left
		DrawableHelper.drawTexture(matrices,  x, y, 4, 4, 0f, 0f, 4, 4, 256, 256)
		//top center
		DrawableHelper.drawTexture(matrices, x + 4, y, this.dialogWidth - 8, 4, 4f, 0f, 151, 4, 256, 256)
		//top right
		DrawableHelper.drawTexture(matrices, x + this.dialogWidth - 4, y, 4, 4, 156f, 0f, 4, 4, 256, 256)
		//center left
		DrawableHelper.drawTexture(matrices, x, y + 4, 4, this.dialogHeight - 8, 0f, 4f, 4, 23, 256, 256)
		//center
		DrawableHelper.drawTexture(matrices, x + 4, y + 4, this.dialogWidth - 8, this.dialogHeight - 8, 4f, 4f, 151, 23, 256, 256)
		//center right
		DrawableHelper.drawTexture(matrices, x + this.dialogWidth - 4, y + 4, 4, this.dialogHeight - 8, 156f, 4f, 4, 23, 256, 256)
		//bottom left
		DrawableHelper.drawTexture(matrices, x, this.y + this.dialogHeight - 4, 4, 4, 0f, 28f, 4, 4, 256, 256)
		//bottom center
		DrawableHelper.drawTexture(matrices, x + 4, this.y + this.dialogHeight - 4, this.dialogWidth - 8, 4, 4f, 28f, 4, 4, 256, 256)
		//bottom right
		DrawableHelper.drawTexture(matrices, x + this.dialogWidth - 4, this.y + this.dialogHeight - 4, 4, 4, 156f, 28f, 4, 4, 256, 256)
	}

	override fun resize(client: MinecraftClient?, width: Int, height: Int) {
		parent?.resize(client, width, height)
		super.resize(client, width, height)
	}

	@Suppress("unchecked_cast")
	override fun onScreenClose() {
		onCloseCallback?.invoke(this as D)
	}


}