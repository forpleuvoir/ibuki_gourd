package moe.forpleuvoir.ibukigourd.mod

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable

object IGLang {

    private fun lang(key: String, vararg args: Any): Text {
        return Translatable(IbukiGourd.MOD_ID + ".${key}", args = args)
    }

    //------------ Misc ------------\\

    val reset = lang("misc.reset")

    val confirm = lang("misc.confirm")

    val cancel = lang("misc.cancel")

    val switchOn = lang("misc.switch.on")

    val switchOff = lang("misc.switch.off")

    val unsupported = lang("misc.unsupported")

    val notSpecified = lang("misc.not_specified")

    val hasNothing = lang("misc.has_nothing")

    val clickBlankBack = lang("misc.click_blank_back")

    val setting = lang("misc.setting")

    val search = lang("misc.search")

    //------------ Color ------------\\

    val red = lang("color.red")

    val green = lang("color.green")

    val blue = lang("color.blue")

    val alpha = lang("color.alpha")

    val hue = lang("color.hue")

    val saturation = lang("color.saturation")

    val brightness = lang("color.brightness")

    fun clickCopyColor(color: String) = lang("color.click_copy_color", color)

    //------------ ConfigWrapper ------------\\

    fun listConfigWrapperText(count: Int) = lang("config_wrapper.list.text", count)

    fun mapConfigWrapperText(count: Int) = lang("config_wrapper.map.text", count)

    //------------ Input ------------\\

    val nextAction = lang("input.next_action")

    val exactMatch = lang("input.exact_match")

    val longPressTime = lang("input.long_press_time")

    val repeatTriggerInterval = lang("input.repeat_trigger_interval")

    val environment = lang("input.environment")

    val triggerMode = lang("input.trigger_mode")

}
