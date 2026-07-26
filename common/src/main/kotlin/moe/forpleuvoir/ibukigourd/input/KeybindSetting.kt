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

    init {
        require(longPressThreshold > 0) { "longPressThreshold must be greater than 0" }
        require(repeatInterval > 0) { "repeatInterval must be greater than 0" }
    }


    companion object : Codec<KeybindSetting> by Codec.create<KeybindSetting>()
        .field(KeybindSetting::env).skipDefault().default(KeyEnvironment.InGame).codec(KeyEnvironment)
        .field(KeybindSetting::trigger).skipDefault().default(KeyTriggerTiming.Press).codec(KeyTriggerTiming)
        .field(KeybindSetting::passthrough).skipDefault().default(false).codec(Codec.boolean)
        .field(KeybindSetting::strict).skipDefault().default(true).codec(Codec.boolean)
        .field(KeybindSetting::longPressThreshold).skipDefault().default(20).codec(Codec.int)
        .field(KeybindSetting::repeatInterval).skipDefault().default(5).codec(Codec.int)
        .build(::KeybindSetting)

    override fun matched(target: Regex): Boolean =
        target.containsMatchIn(env.key) ||
                target.containsMatchIn(trigger.key) ||
                target.containsMatchIn(passthrough.toString()) ||
                target.containsMatchIn(strict.toString()) ||
                target.containsMatchIn(longPressThreshold.toString()) ||
                target.containsMatchIn(repeatInterval.toString())

}

