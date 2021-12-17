package forpleuvoir.ibuki_gourd.keyboard


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.keyboard

 * 文件名 KeyBind

 * 创建时间 2021/12/17 15:03

 * @author forpleuvoir

 */
class KeyBind(vararg key: Int, private val callback: () -> Unit) {
	private val keys: Set<Int> = key.toSet()

	fun callbackHandler(key: Set<Int>) {
		if (key.containsAll(keys)) {
			callback.invoke()
		}
	}
}