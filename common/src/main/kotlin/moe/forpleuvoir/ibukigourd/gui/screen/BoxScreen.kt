package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.Screen.defaultBgBlurRadius

fun interface BoxScreenScope : ScreenScope, BoxLayoutScope

fun BoxScreen(
    modifier: Modifier = Modifier,
    content: BoxScreenScope.() -> Unit
): IGScreenImpl = object : IGScreenImpl(), BoxLayout {
    override var compose: () -> Unit = { BoxScreenScope { this }.content() }
}.apply {
    Modifier
        .bgBlurRadius(defaultBgBlurRadius)
        .name("BoxScreen")
        .then(modifier).foldInApply()
}

