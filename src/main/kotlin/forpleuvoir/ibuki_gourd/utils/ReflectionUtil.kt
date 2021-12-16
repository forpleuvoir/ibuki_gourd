package forpleuvoir.ibuki_gourd.utils


import java.lang.reflect.Field

/**
 * @author forpleuvoir
 *
 * #project_name dhwuia
 *
 * #package forpleuvoir.dhwu1a.core.util
 *
 * #class_name ReflectionUtil
 *
 * #create_time 2021/4/20 5:16
 */
object ReflectionUtil {

	fun Class<*>.isImplemented(target: Class<*>): Boolean {
		return listOf(*this.interfaces).stream().anyMatch { aClass: Class<*> -> aClass == target }
	}

	@JvmStatic
	fun Class<*>.isExtended(target: Class<*>): Boolean {
		return this.getSuperClass().stream().anyMatch { aClass: Class<*> -> aClass == target }
	}

	/**
	 * 获取所有父类
	 *
	 * @param clazz [Class]
	 * @return [List]
	 */
	fun Class<*>.getSuperClass(): List<Class<*>> {
		val clazzs: MutableList<Class<*>> = ArrayList()
		var suCl = this.superclass
		clazzs.add(this.superclass)
		while (suCl != null) {
			clazzs.add(suCl)
			suCl = suCl.superclass
		}
		return clazzs
	}

	/**
	 * 设置对象对应字段的值
	 *
	 * @param fieldName 字段名
	 * @param object    对象
	 * @param value     值
	 */
	fun Any.setFieldValue(fieldName: String, value: Any?) {
		try {
			val field = this.getDeclaredField(fieldName)!!
			field.isAccessible = true
			field[this] = value
		} catch (e: NullPointerException) {

		} catch (e: IllegalAccessException) {

		}
	}

	/**
	 * 获取父类中对应名称的属性
	 * @param object    : 子类对象
	 * @param fieldName : 父类中属性名
	 * @return 父类中的属性
	 */
	fun Any.getDeclaredField(fieldName: String): Field? {
		var field: Field
		var clazz: Class<*> = this.javaClass
		while (clazz != Any::class.java) {
			try {
				field = clazz.getDeclaredField(fieldName)
				return field
			} catch (ignored: Exception) {
			}
			clazz = clazz.superclass
		}
		return null
	}
}