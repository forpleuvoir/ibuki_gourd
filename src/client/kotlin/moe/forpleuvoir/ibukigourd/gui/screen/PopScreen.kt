package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenCustomData.bgBlurRadius
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig.PopupScreen.DEFAULT_BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.render.renderBlur
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screen.Screen

fun PopupScreen(
    modifier: Modifier = Modifier,
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScreenScope.() -> Unit,
) = BoxScreen(
    Modifier
        .name("PopupScreen")
        .bgBlurRadius(DEFAULT_BG_BLUR_RADIUS)
        .renderBackground { context, x, y, d ->
            screen()?.parentScreen?.render(context, 0, 0, d)
            mc.gameRenderer.renderBlur((this as IGScreen).bgBlurRadius, d)
            mc.framebuffer.beginWrite(false)
        }
        .mouseRelease {
            screen()?.parentScreen?.mouseReleased(it.x.toDouble(), it.y.toDouble(), it.button.code)
            onMouseRelease(it)
        }
        .keyRelease {
            screen()?.parentScreen?.keyReleased(it.keyCode.code, it.scanCode, it.modifiers)
            onKeyRelease(it)
        }
        .then(modifier)
) {
    owner().parentScreen = parentScreen as Screen
    owner().screen()?.let { s ->
        s.onResize = { client, width, height ->
            s.parentScreen?.resize(client, width, height)
        }
        s.onFirstInit = { client, width, height ->
            s.parentScreen?.init(client, width, height)
        }
        s.onInit = {
            s.parentScreen?.init()
        }
    }
    //------------ Content ------------\\
    content()
}
