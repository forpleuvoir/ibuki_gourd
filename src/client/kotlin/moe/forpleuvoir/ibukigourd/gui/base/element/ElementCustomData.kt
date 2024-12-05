package moe.forpleuvoir.ibukigourd.gui.base.element

object ElementCustomData {

    const val NAME = "name"

    val IGElement.name: String
        get() = customData[NAME] as? String ?: this::class.simpleName ?: "UNKNOWN_ELEMENT"

}