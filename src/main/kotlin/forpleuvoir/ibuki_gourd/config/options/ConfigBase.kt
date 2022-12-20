package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.common.mText
import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import net.minecraft.text.MutableText


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigBase

 * 创建时间 2021/12/9 17:26

 * @author forpleuvoir

 */
abstract class ConfigBase : IConfigBase {

	val log = IbukiGourdLogger.getLogger(this::class.java)

	protected var onValueChange: ((IConfigBase) -> Unit)? = null
	private var callback: ((IConfigBase) -> Unit)? = null

	override val displayName: MutableText
		get() = name.tText().mText

	override val displayRemark: MutableText
		get() = remark.tText().mText

	override fun matched(regex: Regex): Boolean {
		return regex.run {
			containsMatchIn(displayName.string)
					|| containsMatchIn(displayRemark.string)
					|| containsMatchIn(name)
					|| containsMatchIn(remark)
		}
	}


	override fun equals(other: Any?): Boolean {
		return if (other is IConfigBase)
			(this.name == other.name && this.isValueEquals(other))
		else false
	}

	override fun isValueEquals(other: IConfigBase): Boolean {
		if (this.type.name == other.type.name) {
			if (this is IConfigBaseValue<*> && other is IConfigBaseValue<*>) {
				return this.isEquals(other.getValue())
			}
		}
		return false
	}

	override fun setOnValueChangedCallback(callback: ((IConfigBase) -> Unit)) {
		this.callback = callback
	}

	override fun setOnValueChanged(callback: (IConfigBase) -> Unit) {
		this.onValueChange = callback
	}

	override fun onValueChange() {
		callback?.invoke(this)
		onValueChange?.invoke(this)
	}

	override fun hashCode(): Int {
		var result = callback?.hashCode() ?: 0
		result = 31 * result + displayName.hashCode()
		result = 31 * result + displayRemark.hashCode()
		return result
	}

}

