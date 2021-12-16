package forpleuvoir.ibuki_gourd.mod.keyboard

import net.minecraft.client.util.InputUtil


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.keyboard

 * 文件名 KeyboardUtil

 * 创建时间 2021/12/15 22:20

 * @author forpleuvoir

 */
object KeyboardUtil {

	private val keys: HashSet<Int> by lazy {
		HashSet()
	}

	fun wasPressed(vararg keys: Int): Boolean {
		var returnValue = true
		keys.forEach {
			if (!this.keys.contains(it)) returnValue = false
		}
		return returnValue
	}

	inline fun wasPressed(vararg keys: Int, callback: () -> Unit) {
		if (wasPressed(*keys)) {
			callback.invoke()
		}
	}

	fun wasPressed(vararg keys: InputUtil.Key): Boolean {
		var returnValue = false
		keys.forEach {
			if (!this.keys.contains(it.code)) returnValue = false
		}
		return returnValue
	}

	inline fun wasPressed(vararg keys: InputUtil.Key, callback: () -> Unit) {
		if (wasPressed(*keys)) {
			callback.invoke()
		}
	}


	fun setPressed(key: Int) {
		keys.add(key)
	}

	fun setRelease(key: Int) {
		keys.remove(key)
	}
}