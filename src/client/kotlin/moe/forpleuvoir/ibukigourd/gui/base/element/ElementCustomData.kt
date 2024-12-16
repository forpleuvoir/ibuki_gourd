package moe.forpleuvoir.ibukigourd.gui.base.element

object ElementCustomData {

    private const val NAME_KEY = "name"

    fun IGElement.setName(name: String) {
        customData[NAME_KEY] = name
    }

    val IGElement.name: String
        get() = customData[NAME_KEY] as? String ?: this::class.simpleName ?: "UNKNOWN_ELEMENT"

}