package moe.forpleuvoir.ibukigourd.gui.base.modifier

interface Modifier {

    companion object : Modifier {

    }


    fun then(other: Modifier): Modifier {
        return if (other === Modifier) this else throw IllegalArgumentException("other must be instance of Modifier")
    }

}