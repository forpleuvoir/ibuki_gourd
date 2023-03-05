package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeObject
import com.forpleuvoir.nebula.serialization.json.toJsonStr

class KeyBindSettingImpl(
	override var environment: KeyEnvironment = KeyEnvironment.InGame,
	override var nextAction: NextAction = NextAction.Cancel,
	override var exactMatch: Boolean = true,
	override var triggerMode: KeyTriggerMode = KeyTriggerMode.OnRelease,
	triggerPeriod: Long = 5,
	longPressTime: Long = 20,
) : KeyBindSetting {

	private val log = logger()

	override var triggerPeriod: Long = triggerPeriod.coerceAtLeast(0)
		set(value) {
			field = value.coerceAtLeast(0)
		}

	override var longPressTime: Long = longPressTime.coerceAtLeast(0)
		set(value) {
			field = value.coerceAtLeast(0)
		}

	override fun serialization(): SerializeElement {
		return serializeObject {
			"key_environment" - environment.key
			"next_action" - nextAction.name
			"exact_match" - exactMatch
			"trigger_mode" - triggerMode.key
			"trigger_period" - triggerPeriod
			"long_press_time" - longPressTime
		}
	}

	override fun deserialization(serializeElement: SerializeElement) {
		try {
			val obj = serializeElement.asObject
			environment = environment.fromKey(obj["key_environment"]!!.asString)
			nextAction = NextAction.valueOf(obj["next_action"]!!.asString)
			exactMatch = obj["exact_match"]!!.asBoolean
			triggerMode = KeyTriggerMode.fromKey(obj["trigger_mode"]!!.asString)
			triggerPeriod = obj["trigger_period"]!!.asLong
			longPressTime = obj["long_press_time"]!!.asLong
		} catch (e: Exception) {
			log.error(e)
		}
	}

	override fun matched(regex: Regex): Boolean {
		return regex.run {
			containsMatchIn(serialization().toJsonStr())
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as KeyBindSetting

		if (environment != other.environment) return false
		if (nextAction != other.nextAction) return false
		if (exactMatch != other.exactMatch) return false
		if (triggerMode != other.triggerMode) return false
		if (triggerPeriod != other.triggerPeriod) return false
		if (longPressTime != other.longPressTime) return false

		return true
	}

	override fun hashCode(): Int {
		var result = environment.hashCode()
		result = 31 * result + nextAction.hashCode()
		result = 31 * result + exactMatch.hashCode()
		result = 31 * result + triggerMode.hashCode()
		result = 31 * result + triggerPeriod.hashCode()
		result = 31 * result + longPressTime.hashCode()
		return result
	}

	override fun copyOf(target: KeyBindSetting): Boolean {
		var valueChange = false
		if (this.environment != target.environment) {
			this.environment = target.environment
			valueChange = true
		}
		if (this.nextAction != target.nextAction) {
			this.nextAction = target.nextAction
			valueChange = true
		}
		if (this.exactMatch != target.exactMatch) {
			this.exactMatch = target.exactMatch
			valueChange = true
		}
		if (this.triggerMode != target.triggerMode) {
			this.triggerMode = target.triggerMode
			valueChange = true
		}
		if (this.triggerPeriod != target.triggerPeriod) {
			this.triggerPeriod = target.triggerPeriod
			valueChange = true
		}
		if (this.longPressTime != target.longPressTime) {
			this.longPressTime = target.longPressTime
			valueChange = true
		}
		return valueChange
	}
}