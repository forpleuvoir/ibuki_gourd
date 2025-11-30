package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.keyPress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.mousePress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderParent
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.closeScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.init
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.PopupScreen.enableReturnHotkey
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.PopupScreen.returnHotkeyKeycode
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screens.Screen

fun PopupScreen(
    modifier: Modifier = Modifier,
    parentScreen: IGScreen? = mc.screen as IGScreen?,
    content: BoxScreenScope.() -> Unit,
) = BoxScreen(
    Modifier
        .name("PopupScreen")
        .renderParent(true)
//        .bgBlurRadius(defaultBgBlurRadius)
        .keyPress {
            onKeyPress(it)
            if (!it.used && enableReturnHotkey) {
                if (InputHandler.wasKeyPressed(returnHotkeyKeycode)) {
                    closeScreen()
                }
            }
        }
        .mousePress {
            onMousePress(it)
            if (!it.used && enableReturnHotkey) {
                if (InputHandler.wasKeyPressed(returnHotkeyKeycode)) {
                    closeScreen()
                }
            }
        }
        .then(modifier)
) {
    owner().parentScreen = parentScreen as Screen?
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
