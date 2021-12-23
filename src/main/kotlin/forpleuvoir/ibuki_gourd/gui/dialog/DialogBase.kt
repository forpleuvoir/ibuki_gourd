package forpleuvoir.ibuki_gourd.gui.dialog

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
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
open class DialogBase<D : DialogBase<D>>(private var dialogWidth: Int, private var dialogHeight: Int, title: Text, parent: Screen) :
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

	override var titleLeftPadding: Float
		get() = super.titleLeftPadding + this.x
		set(value) {
			value + this.x
		}

	override var titleTopPadding: Float
		get() = super.titleTopPadding + this.y
		set(value) {
			value + this.y
		}

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		parent?.render(matrices, mouseX, mouseY, delta)
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun drawBackgroundColor(matrices: MatrixStack) {
		RenderUtil.drawOutlinedBox(x, y, dialogWidth, dialogHeight, Color4i.BLACK.apply { alpha = 180 }, Color4f.WHITE)
	}

	override fun renderBackground(matrices: MatrixStack?, vOffset: Int) {
		if (client!!.world != null) {
			this.fillGradient(matrices, x, y, dialogWidth, dialogHeight, -1072689136, -804253680)
		} else {
			renderBackgroundTexture(vOffset)
		}
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