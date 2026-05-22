package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

object IGLang {

    private fun lang(key: String, vararg args: Any): MutableText {
        return Translatable(IbukiGourd.MOD_ID + ".${key}", args = args)
    }

    //------------ Misc ------------\\

    val reset get() = lang("misc.reset")

    val confirm get() = lang("misc.confirm")

    val cancel get() = lang("misc.cancel")

    val switchOn get() = lang("misc.switch.on")

    val switchOff get() = lang("misc.switch.off")

    fun switch(switch: Boolean) = if (switch) switchOn else switchOff

    fun coloredSwitch(switch: Boolean, onColor: Color = Colors.LIMEGREEN, offColor: Color = Colors.RED) =
        if (switch) switchOn.withColor(onColor) else switchOff.withColor(offColor)

    val unsupported get() = lang("misc.unsupported")

    val notSpecified get() = lang("misc.not_specified")

    val hasNothing get() = lang("misc.has_nothing")

    fun dialogReturnTip(keyCode: KeyCode) = lang("misc.dialog_return_tip", keyCode.keyNameText)

    val setting get() = lang("misc.setting")

    val search get() = lang("misc.search")

    val moveUp get() = lang("misc.move_up")

    val moveDown get() = lang("misc.move_down")

    val moveLeft get() = lang("misc.move_left")

    val moveRight get() = lang("misc.move_right")

    val add get() = lang("misc.add")

    val remove get() = lang("misc.remove")

    fun removeConfirm(content: Any) = lang("misc.remove_confirm", content)

    val edit get() = lang("misc.edit")

    val copy get() = lang("misc.copy")

    val paste get() = lang("misc.paste")

    val content get() = lang("misc.content")

    //------------ Color ------------\\

    val red get() = lang("color.red")

    val green get() = lang("color.green")

    val blue get() = lang("color.blue")

    val alpha get() = lang("color.alpha")

    val hue get() = lang("color.hue")

    val saturation get() = lang("color.saturation")

    val brightness get() = lang("color.brightness")

    fun clickCopyColor(color: Color) = lang("color.click_copy_color", color.hexStr)

    fun copyColorSuccess(color: Color) = lang("color.copy_success", color.hexStr)

    //------------ ConfigWrapper ------------\\

    fun <T : Comparable<*>> notInRange(value: T, minValue: T, maxValue: T) = lang("config_wrapper.not_in_range", value, minValue, maxValue)

    val pairFirst get() = lang("config_wrapper.pair.first")

    val pairSecond get() = lang("config_wrapper.pair.second")

    val mapKey get() = lang("config_wrapper.map.key")

    val mapValue get() = lang("config_wrapper.map.value")

    fun keyExists(key: Any) = lang("config_wrapper.key_exists", key)

    val pressToSetting get() = lang("config_wrapper.press_to_setting")

    val releaseToSaveSetting get() = lang("config_wrapper.release_to_save_setting")

    val keybindConflict get() = lang("config_wrapper.keybind_conflict")

    val move get() = lang("config_wrapper.move")

    fun listConfigWrapperText(count: Int) = lang("config_wrapper.list.text", count)

    fun mapConfigWrapperText(count: Int) = lang("config_wrapper.map.text", count)

    //------------ Input ------------\\

    val nextAction get() = lang("input.next_action")

    val exactMatch get() = lang("input.exact_match")

    val longPressTime get() = lang("input.long_press_time")

    val repeatTriggerInterval get() = lang("input.repeat_trigger_interval")

    val environment get() = lang("input.environment")

    val triggerMode get() = lang("input.trigger_mode")

}