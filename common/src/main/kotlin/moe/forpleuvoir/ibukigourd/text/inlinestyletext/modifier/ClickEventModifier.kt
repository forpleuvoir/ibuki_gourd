package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.MutableComponent
import java.net.URI

object ClickEventModifier : TextModifier {

    private val pattern = """click=>(open_url|open_file|run_command|suggest_command|change_page|copy_to_clipboard)=>.+""".toRegex()

    override fun modifier(exp: String): ((MutableComponent) -> MutableComponent)? {
        if (!exp.matches(pattern)) return null
        val s = exp.split("=>")
        val action = s[1]
        val value = s[2]
        return { text ->
            when (action) {
                "open_url"          -> ClickEvent.OpenUrl(URI.create(value))
                "open_file"         -> ClickEvent.OpenFile(value)
                "run_command"       -> ClickEvent.RunCommand(value)
                "suggest_command"   -> ClickEvent.SuggestCommand(value)
                "change_page"       -> ClickEvent.ChangePage(value.toInt())
                "copy_to_clipboard" -> ClickEvent.CopyToClipboard(value)
                else                -> null
            }?.let { event ->
                text.style { clickEvent(event) }
            } ?: text
        }
    }

}