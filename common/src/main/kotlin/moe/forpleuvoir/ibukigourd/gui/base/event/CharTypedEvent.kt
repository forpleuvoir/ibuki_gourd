package moe.forpleuvoir.ibukigourd.gui.base.event

data class CharTypedEvent(
    val codepoint: Int,
    val modifiers: Int
) : GUIEvent() {
    val codepointAsString by lazy { Character.toString(codepoint) }
}
