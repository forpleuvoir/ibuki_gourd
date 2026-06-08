package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 按键绑定设置
 *
 * @param env                按键环境
 * @param trigger            触发时机
 * @param passthrough        是否允许事件继续传递
 * @param strict             是否严格匹配按键输入
 * @param longPressThreshold 长按判定阈值（客户端游戏刻）
 * @param repeatInterval     重复触发间隔（客户端游戏刻）
 */
data class KeybindSetting(
    val env: KeyEnvironment = KeyEnvironment.InGame,
    val trigger: KeyTriggerTiming = KeyTriggerTiming.Press,
    val passthrough: Boolean = false,
    val strict: Boolean = true,
    val longPressThreshold: Int = 20,
    val repeatInterval: Int = 5
) : Matchable<Regex> {

    companion object : Codec<KeybindSetting> by Codec.create<KeybindSetting>()
        .field<KeyEnvironment>("env").getter(KeybindSetting::env).default(KeyEnvironment.InGame).codec(KeyEnvironment)
        .field<KeyTriggerTiming>("trigger").getter(KeybindSetting::trigger).default(KeyTriggerTiming.Press).codec(KeyTriggerTiming)
        .field<Boolean>("passthrough").getter(KeybindSetting::passthrough).default(false).codec(Codec.boolean)
        .field<Boolean>("strict").getter(KeybindSetting::strict).default(true).codec(Codec.boolean)
        .field<Int>("longPressThreshold").getter(KeybindSetting::longPressThreshold).default(20).codec(Codec.int)
        .field<Int>("repeatInterval").getter(KeybindSetting::repeatInterval).default(5).codec(Codec.int)
        .build(::KeybindSetting)

    override fun matched(target: Regex): Boolean =
        target.containsMatchIn(env.key) ||
                target.containsMatchIn(trigger.key) ||
                target.containsMatchIn(passthrough.toString()) ||
                target.containsMatchIn(strict.toString()) ||
                target.containsMatchIn(longPressThreshold.toString()) ||
                target.containsMatchIn(repeatInterval.toString())

}

