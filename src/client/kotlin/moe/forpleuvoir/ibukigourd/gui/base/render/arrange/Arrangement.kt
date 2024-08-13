package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

sealed interface Arrangement {

    data object SpaceBetween : Arrangement

    data object SpaceAround : Arrangement

    data object SpaceEvenly : Arrangement

    data object Start : Arrangement

    data object Center : Arrangement

    data object End : Arrangement

}

