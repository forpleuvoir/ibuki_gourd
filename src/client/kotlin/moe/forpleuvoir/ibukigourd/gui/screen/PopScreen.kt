package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderParent
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.PopupScreen.DEFAULT_BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screen.Screen

fun PopupScreen(
    modifier: Modifier = Modifier,
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScreenScope.() -> Unit,
) = BoxScreen(
    Modifier
        .name("PopupScreen")
        .renderParent(true)
        .bgBlurRadius(DEFAULT_BG_BLUR_RADIUS)
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
