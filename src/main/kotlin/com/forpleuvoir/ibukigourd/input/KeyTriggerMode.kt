package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.util.text.Text
import com.forpleuvoir.ibukigourd.util.text.translatable

enum class KeyTriggerMode(val key: String) {

	//按键按下时触发一次
	OnPress("on_press"),

	//按键长按时触发一次
	OnLongPress("on_long_press"),

	//按键释放时触发一次
	OnRelease("on_release"),

	//按键按住时重复触发
	OnPressed("on_pressed"),

	//按键长按时重复触发
	OnLongPressed("on_long_pressed"),

	//按键按下和释放时都会触发一次
	BOTH("both");

	val displayName: Text
		get() = translatable("ibuki_gourd.key_bind.trigger_mode.${key}")

	val description: Text
		get() = translatable("ibuki_gourd.key_bind.trigger_mode.${key}.description")


	companion object {
		fun fromKey(key: String): KeyTriggerMode {
			KeyTriggerMode.values().forEach {
				if (it.key == key) return it
			}
			return OnRelease
		}

	}

}