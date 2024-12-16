package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable

object IGLang {

    private fun lang(key: String, vararg args: Any): Text {
        return Translatable(IbukiGourd.MOD_ID + ".${key}", args = args)
    }

    //------------ Misc ------------\\

    val reset get() = lang("misc.reset")

    val confirm get() = lang("misc.confirm")

    val cancel get() = lang("misc.cancel")

    val switchOn get() = lang("misc.switch.on")

    val switchOff get() = lang("misc.switch.off")

    val unsupported get() = lang("misc.unsupported")

    val notSpecified get() = lang("misc.not_specified")

    val hasNothing get() = lang("misc.has_nothing")

    val clickBlankBack get() = lang("misc.click_blank_back")

    val setting get() = lang("misc.setting")

    val search get() = lang("misc.search")

    val moveUp get() = lang("misc.move_up")

    val moveDown get() = lang("misc.move_down")

    val moveLeft get() = lang("misc.move_left")

    val moveRight get() = lang("misc.move_right")

    val add get() = lang("misc.add")

    val remove get() = lang("misc.remove")

    val edit get() = lang("misc.edit")

    val copy get() = lang("misc.copy")

    val paste get() = lang("misc.paste")

    //------------ Color ------------\\

    val red get() = lang("color.red")

    val green get() = lang("color.green")

    val blue get() = lang("color.blue")

    val alpha get() = lang("color.alpha")

    val hue get() = lang("color.hue")

    val saturation get() = lang("color.saturation")

    val brightness get() = lang("color.brightness")

    fun clickCopyColor(color: String) = lang("color.click_copy_color", color)

    //------------ ConfigWrapper ------------\\

    val pressToSetting get() = lang("config_wrapper.press_to_setting")

    val releaseToSaveSetting get() = lang("config_wrapper.release_to_save_setting")

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