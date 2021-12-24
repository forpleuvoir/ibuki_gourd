package forpleuvoir.ibuki_gourd.keyboard

import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import java.util.*


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


	fun setOnPressCallback(vararg keys: Int, keyEnvironment: KeyEnvironment = KeyEnvironment.IN_GAME, callback: () -> Unit) {
		val keyBind = KeyBind(*keys, keyEnvironment = keyEnvironment, callback = callback)
		onPressCallback.addLast(keyBind)
	}

	fun registerKeyBind(key: KeyBind) {
		this.onPressCallback.addLast(key)
	}

	fun registerKeyBind(key: List<KeyBind>) {
		this.onPressCallback.addAll(key)
	}

	private fun callbackHandler():Boolean {
		onPressCallback.forEach {
			if(it.callbackHandler(this.keys)) return true
		}
		return false
	}

	private fun onPressChanged():Boolean {
		return callbackHandler()
	}

	@JvmStatic
	fun setPressed(key: Int): Boolean {
		if (key != GLFW.GLFW_KEY_UNKNOWN)
			if (keys.add(key)) {
				if(onPressChanged()) return true
			}
		return false
	}

	@JvmStatic
	fun setRelease(key: Int) {
		if (key != GLFW.GLFW_KEY_UNKNOWN)
			keys.remove(key)
	}


}