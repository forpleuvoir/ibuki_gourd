package forpleuvoir.ibuki_gourd.keyboard

import forpleuvoir.ibuki_gourd.mixin.MouseAccessor
import net.minecraft.client.MinecraftClient
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

	private val mc get() = MinecraftClient.getInstance()

	private val keys: HashSet<Int> by lazy {
		HashSet()
	}

	private val keyBinds: LinkedList<KeyBind> = LinkedList()

	@JvmStatic
	fun analogKeyPress(keyCode: Int, action: Int = 1) {
		mc.keyboard.onKey(mc.window.handle, keyCode, 0, action, 0)
	}

	@JvmStatic
	fun analogKeyPress(keyCode: Int) {
		analogKeyPress(keyCode, 1)
	}


	@JvmStatic
	fun analogMousePress(button: Int, action: Int = 1) {
		(mc.mouse as MouseAccessor).invokerOnMouseButton(mc.window.handle, button, action, 0)
	}

	@JvmStatic
	fun analogMousePress(button: Int) {
		analogMousePress(button, 1)
	}

	@JvmStatic
	fun analogMouseScroll(horizontal: Double, vertical: Double) {
		(mc.mouse as MouseAccessor).invokerOnMouseScroll(mc.window.handle, horizontal, vertical)
	}

	fun wasPressed(keyBind: KeyBind) {
		keyBind.callbackHandler(this.keys)
	}

	fun wasPressed(vararg keys: Int): Boolean {
		if (keys.isEmpty()) return false
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
		if (keys.isEmpty()) return false
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


	fun setOnPressCallback(
		vararg keys: Int,
		keyEnvironment: KeyEnvironment = KeyEnvironment.IN_GAME,
		callback: () -> Unit
	) {
		val keyBind = KeyBind(*keys, keyEnvironment = keyEnvironment, callback = callback)
		keyBinds.addLast(keyBind)
	}

	fun registerKeyBind(key: KeyBind) {
		this.keyBinds.addLast(key)
	}

	fun removeKeyBind(key: KeyBind) {
		this.keyBinds.remove(key)
	}

	fun registerKeyBind(key: List<KeyBind>) {
		this.keyBinds.addAll(key)
	}

	private fun callbackHandler(): Boolean {
		keyBinds.forEach {
			if (it.callbackHandler(this.keys)) return true
		}
		return false
	}

	private fun onPressChanged(): Boolean {
		return callbackHandler()
	}

	@JvmStatic
	fun setPressed(key: Int): Boolean {
		if (key != GLFW.GLFW_KEY_UNKNOWN)
			if (keys.add(key)) {
				if (onPressChanged()) return true
			}
		return false
	}

	@JvmStatic
	fun setRelease(key: Int) {
		if (key != GLFW.GLFW_KEY_UNKNOWN)
			keys.remove(key)
	}


}