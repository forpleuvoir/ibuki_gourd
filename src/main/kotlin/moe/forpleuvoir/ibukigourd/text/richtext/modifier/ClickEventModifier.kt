package moe.forpleuvoir.ibukigourd.text.richtext.modifier

import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText

object ClickEventModifier : TextModifier {

    private val pattern = """click=>(run_command|suggest_command|open_url|open_file|change_page|copy_to_clipboard)=>.+""".toRegex()

    override fun modifier(exp: String): ((MutableText) -> MutableText)? {
        if (!exp.matches(pattern)) return null
        val s = exp.split("=>")
        val action = s[1]
        val value = s[2]
        return { text ->
            when (action) {
                "run_command"       -> ClickEvent.Action.RUN_COMMAND
                "suggest_command"   -> ClickEvent.Action.SUGGEST_COMMAND
                "open_url"          -> ClickEvent.Action.OPEN_URL
                "open_file"         -> ClickEvent.Action.OPEN_FILE
                "change_page"       -> ClickEvent.Action.CHANGE_PAGE
                "copy_to_clipboard" -> ClickEvent.Action.COPY_TO_CLIPBOARD
                else                -> null
            }?.let {
                text.style { clickEvent(ClickEvent(it, value)) }
            } ?: text
        }
    }

}