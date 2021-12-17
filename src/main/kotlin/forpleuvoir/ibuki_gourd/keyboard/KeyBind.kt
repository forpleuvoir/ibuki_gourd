package forpleuvoir.ibuki_gourd.keyboard

import forpleuvoir.ibuki_gourd.event.util.KeyEnvironment
import net.minecraft.client.MinecraftClient


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.keyboard

 * 文件名 KeyBind

 * 创建时间 2021/12/17 15:03

 * @author forpleuvoir

 */
class KeyBind(vararg key: Int, val keyEnvironment: KeyEnvironment, private val callback: () -> Unit) {
	private val keys: Set<Int> = key.toSet()

	fun callbackHandler(key: Set<Int>) {
		if (key.containsAll(keys)) {
			val keyEnv = if (MinecraftClient.getInstance().currentScreen == null) KeyEnvironment.IN_GAME else KeyEnvironment.IN_SCREEN
			if (keyEnv == this.keyEnvironment || this.keyEnvironment == KeyEnvironment.ALL)
				callback.invoke()
		}
	}
}