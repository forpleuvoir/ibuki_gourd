package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.api.Matchable
import com.forpleuvoir.nebula.serialization.Deserializable
import com.forpleuvoir.nebula.serialization.Serializable

interface KeyBindSetting : Serializable, Deserializable, Matchable {
	/**
	 * 按键触发环境
	 */
	var environment: KeyEnvironment

	/**
	 * 是否取消之后的操作
	 */
	var nextAction: NextAction

	/**
	 * 只有完全匹配的按键才会触发
	 */
	var exactMatch: Boolean

	/**
	 * 按键触发模式
	 */
	var triggerMode: KeyTriggerMode

	/**
	 * 按下多久触发长按
	 */
	var longPressTime: Long

	/**
	 * 重复触发的按键周期
	 */
	var triggerPeriod: Long

	/**
	 * 从其他按键设置复制
	 * @param target KeyBindSetting
	 * @return Boolean
	 */
	fun copyOf(target: KeyBindSetting): Boolean

}

fun keyBindSetting(
	environment: KeyEnvironment = KeyEnvironment.InGame,
	cancelFurtherProcess: NextAction = NextAction.Cancel,
	ordered: Boolean = true,
	triggerMode: KeyTriggerMode = KeyTriggerMode.OnRelease,
	triggerPeriod: Long = 5,
	longPressTime: Long = 20,
	scope: (KeyBindSetting.() -> Unit)? = null
): KeyBindSetting =
	KeyBindSettingImpl(environment, cancelFurtherProcess, ordered, triggerMode, triggerPeriod, longPressTime).apply { scope?.invoke(this) }
