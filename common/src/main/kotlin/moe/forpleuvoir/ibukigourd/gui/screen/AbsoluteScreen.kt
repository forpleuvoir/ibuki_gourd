package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.measureCompletion
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.Screen.defaultBgBlurRadius
import moe.forpleuvoir.ibukigourd.util.mc

fun interface AbsoluteScreenScope : ScreenScope, AbsoluteLayoutScope

fun AbsoluteScreen(
    modifier: Modifier = Modifier,
    content: AbsoluteScreenScope.() -> Unit
): IGScreenImpl {
    return object : IGScreenImpl(), AbsoluteLayout {
        override var compose: () -> Unit = { AbsoluteScreenScope { this }.content() }
    }.apply {
        Modifier
            .bgBlurRadius(defaultBgBlurRadius)
            .name("AbsoluteScreen")
            .measureCompletion {
                onMeasureCompletion()
                transform.set(mc.window.guiScaledWidth.toFloat(), mc.window.guiScaledHeight.toFloat())
            }
            .then(modifier).foldInApply()
    }
}
