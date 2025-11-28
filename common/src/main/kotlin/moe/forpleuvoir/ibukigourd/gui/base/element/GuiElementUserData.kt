package moe.forpleuvoir.ibukigourd.gui.base.element

object GuiElementUserData {

    private const val NAME_KEY = "#name"

    fun GuiElement.setName(name: String) {
        userData[NAME_KEY] = name
    }

    val GuiElement.name: String
        get() = userData[NAME_KEY] as? String ?: this::class.simpleName ?: "UNKNOWN_ELEMENT"

}