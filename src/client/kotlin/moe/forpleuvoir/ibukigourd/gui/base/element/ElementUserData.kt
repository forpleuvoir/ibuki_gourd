package moe.forpleuvoir.ibukigourd.gui.base.element

object ElementUserData {

    private const val NAME_KEY = "#name"

    fun IGElement.setName(name: String) {
        userData[NAME_KEY] = name
    }

    val IGElement.name: String
        get() = userData[NAME_KEY] as? String ?: this::class.simpleName ?: "UNKNOWN_ELEMENT"

}