package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.MutableComponent
import java.net.URI

object ClickEventModifier : TextModifier {

    private val pattern = """c:(open_url|open_file|run_command|suggest_command|change_page|copy_to_clipboard)=>(.+)""".toRegex()

    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        if (exp == "c:none" || exp == "c:null") return current.style { clickEvent(null) }
        val (action, value) = pattern.matchEntire(exp)?.destructured ?: return null
        return when (action) {
            "open_url"          -> ClickEvent.OpenUrl(URI.create(value))
            "open_file"         -> ClickEvent.OpenFile(value)
            "run_command"       -> ClickEvent.RunCommand(value)
            "suggest_command"   -> ClickEvent.SuggestCommand(value)
            "change_page"       -> ClickEvent.ChangePage(value.toInt())
            "copy_to_clipboard" -> ClickEvent.CopyToClipboard(value)
            else                -> null
        }?.let {
            current.style { clickEvent(it) }
        }
    }

}