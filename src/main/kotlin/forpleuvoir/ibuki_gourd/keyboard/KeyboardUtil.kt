package forpleuvoir.ibuki_gourd.keyboard

import net.minecraft.client.util.InputUtil
import java.util.*
import kotlin.collections.HashSet


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.keyboard

 * 文件名 KeyboardUtil

 * 创建时间 2021/12/15 22:20

 * @author forpleuvoir

 */
object KeyboardUtil {

	private val keys: HashSet<Int> by lazy {
		HashSet()
	}

	private val onPressCallback: LinkedList<KeyBind> = LinkedList()

	fun wasPressed(vararg keys: Int): Boolean {
		var returnValue = true
		keys.forEach {
			if (!KeyboardUtil.keys.contains(it)) returnValue = false
		}
		return returnValue
	}

	inline fun wasPressed(vararg keys: Int, callback: () -> Unit) {
		if (wasPressed(*keys)) {
			callback.invoke()
		}
	}

	fun wasPressed(vararg keys: InputUtil.Key): Boolean {
		var returnValue = true
		keys.forEach {
			if (!KeyboardUtil.keys.contains(it.code)) returnValue = false
		}
		return returnValue
	}

	inline fun wasPressed(vararg keys: InputUtil.Key, callback: () -> Unit) {
		if (wasPressed(*keys)) {
			callback.invoke()
		}
	}

	fun setOnPressCallback(vararg keys: Int, callback: () -> Unit) {
		val keyBind = KeyBind(*keys, callback = callback)
		onPressCallback.addLast(keyBind)
	}

	private fun callbackHandler() {
		onPressCallback.forEach {
			it.callbackHandler(this.keys)
		}
	}

	private fun onPressChanged(){
		callbackHandler()
	}

	fun setPressed(key: Int) {
		if (keys.add(key)) {
			onPressChanged()
		}

	}

	fun setRelease(key: Int) {
		keys.remove(key)
	}
}