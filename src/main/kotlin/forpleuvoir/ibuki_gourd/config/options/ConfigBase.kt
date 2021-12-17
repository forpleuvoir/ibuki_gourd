package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.IConfigNotifiable
import forpleuvoir.ibuki_gourd.config.IConfigResettable
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigBase

 * 创建时间 2021/12/9 17:26

 * @author forpleuvoir

 */
abstract class ConfigBase : IConfigBase, IConfigResettable, IConfigNotifiable {

	companion object {
		val log = IbukiGourdLogger.getLogger(this::class.java)
	}

	private var callback: ((IConfigBase) -> Unit)? = null

	override val displayName: Text
		get() = name.tText()

	override val displayRemark: Text
		get() = remark.tText()

	override fun equals(other: Any?): Boolean {
		return if (other is ConfigBase)
			(this.name == other.name && this.isValueEquals(other))
		else false
	}

	override fun isValueEquals(other: IConfigBase): Boolean {
		if (this.type == other.type) {
			if (this is IConfigBaseValue<*> && other is IConfigBaseValue<*>) {
				return this.isEquals(other.getValue())
			}
		}
		return false
	}

	override fun setOnValueChangedCallback(callback: ((IConfigBase) -> Unit)) {
		this.callback = callback
	}

	override fun onValueChange() {
		if (callback != null) {
			callback?.invoke(this)
		}
	}

	override fun hashCode(): Int {
		var result = callback?.hashCode() ?: 0
		result = 31 * result + displayName.hashCode()
		result = 31 * result + displayRemark.hashCode()
		return result
	}

}

