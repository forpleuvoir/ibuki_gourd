package forpleuvoir.ibuki_gourd.gui.dialog

import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.color.IColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.GameRenderer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

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

	protected open val texture = Identifier("textures/gui/advancements/window.png")
	var textureColor: IColor<out Number> = Color4i(33, 33, 33)

	val x: Int get() = width / 2 - dialogWidth / 2
	val y: Int get() = height / 2 - dialogHeight / 2

	private var onCloseCallback: ((D) -> Unit)? = null
	protected open val titleColor: IColor<out Number> get() = Color4i(63, 63, 63)

	private lateinit var closeButton: ButtonIcon

	fun setOnCloseCallback(onCloseCallback: (D) -> Unit) {
		this.onCloseCallback = onCloseCallback
	}

	init {
		this.parent = parent
		titleLabel.apply {
			shadow = false
			textColor = titleColor
		}
	}

	protected val margin: Int get() = 5

	override var titleLeftPadding: Int
		get() = 5 + this.x
		set(value) {
			value + this.x
		}

	override var titleTopPadding: Int
		get() = 5 + this.y
		set(value) {
			value + this.y
		}

	protected open val paddingLeft get() = 8
	protected open val paddingRight get() = 8
	protected open val paddingTop get() = titleTopPadding - this.y + titleLabel.height
	protected open val paddingBottom get() = 8

	val left get() = this.x + this.paddingLeft
	val right get() = this.x + this.dialogWidth - this.paddingRight
	val top get() = this.y + this.paddingTop
	val bottom get() = this.y + this.dialogHeight - this.paddingBottom

	val contentWidth get() = this.right - this.left
	val contentHeight get() = this.bottom - this.top

	override fun init() {
		super.init()
		val size = 12
		closeButton =
			ButtonIcon(this.x + this.dialogWidth - margin - size, this.y + margin, Icon.CLOSE, size, color = Color4f(titleColor)) {
				this.close()
			}
		this.addDrawableChild(closeButton)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (parent is DialogBase<*>) {
			if ((parent as DialogBase<*>).dialogWidth > this.dialogWidth && (parent as DialogBase<*>).dialogWidth > this.dialogWidth)
				parent?.render(context, mouseX, mouseY, delta)
			else {
				(parent as DialogBase<*>).parent?.render(context, mouseX, mouseY, delta)
			}
		} else {
			parent?.render(context, mouseX, mouseY, delta)
		}
		renderBackground(context)
		super.render(context, mouseX, mouseY, delta)
	}

	override fun drawBackgroundColor(context: DrawContext) {

	}

	override fun renderBackground(context: DrawContext) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram)
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f)
		RenderSystem.enableBlend()
		RenderSystem.defaultBlendFunc()
		RenderSystem.enableDepthTest()

		context.fill(
			x + paddingLeft,
			y + paddingTop,
			x + this.dialogWidth - paddingRight,
			y + this.dialogHeight - paddingBottom,
			textureColor.rgba
		)
		//top left
		context.drawTexture(texture, x, y, 4, 4, 0f, 0f, 4, 4, 256, 256)
		//top center
		context.drawTexture(texture, x + 4, y, this.dialogWidth - 8, 4, 4f, 0f, 244, 4, 256, 256)
		//top right
		context.drawTexture(texture, x + this.dialogWidth - 4, y, 4, 4, 248f, 0f, 4, 4, 256, 256)
		//top left 2
		context.drawTexture(texture, x, y + 4, 4, paddingTop - 4, 0f, 4f, 4, 12, 256, 256)
		//top center2
		context.drawTexture(texture, x + 4, y + 4, this.dialogWidth - 8, paddingTop - 4, 4f, 4f, 244, 12, 256, 256)
		//top right
		context.drawTexture(texture, x + this.dialogWidth - 4, y + 4, 4, paddingTop - 4, 248f, 4f, 4, 12, 256, 256)

		//center left
		context.drawTexture(
			texture,
			x,
			y + paddingTop,
			3,
			this.dialogHeight - (paddingTop + paddingBottom),
			0f,
			17f,
			3,
			114,
			256,
			256
		)
		context.drawTexture(
			texture,
			x + 3,
			y + paddingTop,
			paddingLeft - 3 - 1,
			this.dialogHeight - (paddingTop + paddingBottom),
			3f,
			17f,
			5,
			114,
			256,
			256
		)
		context.drawTexture(
			texture,
			x + paddingLeft - 1,
			y + paddingTop,
			1,
			this.dialogHeight - (paddingTop + paddingBottom),
			8f,
			17f,
			1,
			114,
			256,
			256
		)
		//center
		context.drawTexture(
			texture,
			x + paddingLeft - 1,
			y + paddingTop,
			this.dialogWidth - (paddingLeft + paddingRight) + 1,
			1,
			8f,
			17f,
			234,
			1,
			256,
			256
		)
		context.drawTexture(
			texture,
			x + paddingLeft - 1,
			y + this.dialogHeight - paddingBottom - 1,
			this.dialogWidth - (paddingLeft + paddingRight) + 1,
			1,
			8f,
			131f,
			234,
			1,
			256,
			256
		)
		//center right
		context.drawTexture(
			texture,
			x + this.dialogWidth - paddingRight,
			y + paddingTop,
			1,
			this.dialogHeight - (paddingTop + paddingBottom),
			243f,
			17f,
			1,
			114,
			256,
			256
		)
		context.drawTexture(
			texture,
			x + this.dialogWidth - paddingRight + 1,
			y + paddingTop,
			paddingRight - 3 - 1,
			this.dialogHeight - (paddingTop + paddingBottom),
			244f,
			17f,
			5,
			114,
			256,
			256
		)
		context.drawTexture(
			texture,
			x + this.dialogWidth - 3,
			y + paddingTop,
			3,
			this.dialogHeight - (paddingTop + paddingBottom),
			249f,
			17f,
			3,
			114,
			256,
			256
		)

		//bottom left 2
		context.drawTexture(texture, x, this.y + this.dialogHeight - paddingBottom, 4, paddingBottom - 4, 0f, 132f, 4, 4, 256, 256)
		//bottom center 2
		context.drawTexture(
			texture,
			x + 4,
			this.y + this.dialogHeight - paddingBottom,
			this.dialogWidth - 8,
			paddingBottom - 4,
			4f,
			133f,
			244,
			4,
			256,
			256
		)
		//bottom right 2
		context.drawTexture(
			texture,
			x + this.dialogWidth - 4,
			this.y + this.dialogHeight - paddingBottom,
			4,
			paddingBottom - 4,
			248f,
			132f,
			4,
			4,
			256,
			256
		)
		//bottom left
		context.drawTexture(texture, x, this.y + this.dialogHeight - 4, 4, 4, 0f, 136f, 4, 4, 256, 256)
		//bottom center
		context.drawTexture(texture, x + 4, this.y + this.dialogHeight - 4, this.dialogWidth - 8, 4, 4f, 136f, 244, 4, 256, 256)
		//bottom right
		context.drawTexture(texture, x + this.dialogWidth - 4, this.y + this.dialogHeight - 4, 4, 4, 248f, 136f, 4, 4, 256, 256)
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