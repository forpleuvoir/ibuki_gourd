package forpleuvoir.ibuki_gourd.gui.dialog

import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.function.Function


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.dialog

 * 文件名 DialogConfirm

 * 创建时间 2021/12/24 12:03

 * @author forpleuvoir

 */
open class DialogConfirm(
	dialogWidth: Int,
	dialogHeight: Int,
	title: Text,
	parent: Screen?,
	protected open var confirmCallback: Function<DialogConfirm, Boolean> = Function { true },
	protected open var cancelCallback: Function<DialogConfirm, Boolean> = Function { true }
) : DialogBase<DialogConfirm>(dialogWidth, dialogHeight, title, parent) {

	private val buttonMargin: Int
		get() = 10

	override val paddingBottom: Int
		get() = super.paddingBottom + buttonHeight + buttonMargin

	override fun init() {
		super.init()
		initButton()
	}

	private lateinit var confirm: Button

	private lateinit var cancel: Button

	protected open val buttonWidth: Int
		get() = this.dialogWidth / 2 - this.buttonMargin * 2

	protected open val buttonHeight: Int
		get() = 20

	private fun initButton() {
		this.confirm = Button(
			this.x + this.dialogWidth / 2 + this.buttonMargin,
			this.y + this.dialogHeight - this.buttonHeight - this.buttonMargin,
			buttonWidth,
			buttonHeight,
			IbukiGourdLang.Confirm.tText()
		) {
			if (confirmCallback.apply(this)) {
				this.onClose()
				return@Button
			}
		}
		this.cancel = Button(
			this.x + this.dialogWidth / 2 - buttonWidth - this.buttonMargin,
			this.y + this.dialogHeight - this.buttonHeight - this.buttonMargin,
			buttonWidth,
			buttonHeight,
			IbukiGourdLang.Cancel.tText()
		) {
			if (cancelCallback.apply(this)) {
				this.onClose()
				return@Button
			}
		}
		this.addDrawableChild(confirm)
		this.addDrawableChild(cancel)
	}

}