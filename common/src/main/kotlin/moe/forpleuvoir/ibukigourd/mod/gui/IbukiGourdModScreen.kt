package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.keyPress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.gui.screen.TabScreen
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.mod.what.EasterEggsScreen
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.FixedSizeQueue
import moe.forpleuvoir.ibukigourd.util.resourceLocation
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color

private val icon = WidgetTexture(Corner(), 0, 0, 32, 32, TextureInfo(32, 32, resourceLocation("ibukigourd.png")))

private val what = arrayOf(
    Keyboard.KP_1,
    Keyboard.KP_1,
    Keyboard.KP_4,
    Keyboard.KP_5,
    Keyboard.KP_1,
    Keyboard.KP_4,
    Keyboard.KP_1,
    Keyboard.KP_9,
    Keyboard.KP_1,
    Keyboard.KP_9,
    Keyboard.KP_8,
    Keyboard.KP_1,
    Keyboard.KP_0
)

fun IbukiGourdModScreen() = TabScreen(
    header = {
        Row(
            Modifier
                .fill()
                .padding(5f),
            horizontalArrangement = Arrangement.spacedBy(5f, Alignment.Left)
        ) {
            Icon(icon, modifier = Modifier.size(16f, 16f))
            Text(Literal(IbukiGourd.MOD_NAME).style {
                bold()
                color("#FF994dbf")
            })
        }
    },
    modifier = Modifier.onClose {
        IGConfig.asyncSave()
    }.keyPress {
        @Suppress("UNCHECKED_CAST")
        val input = userData["#ig_???"] as? FixedSizeQueue<KeyCode> ?: FixedSizeQueue(13)
        userData["#ig_???"] = input
        input.add(it.keyCode)
        if (input.asArray().contentEquals(what)) {
            EasterEggsScreen().open()
        }
        onKeyPress(it)
    },
    tabColor = stateOf(Color(0xffffccf0)),
    inactiveColor = stateOf(Color(0xffb3f2ff))
) {
    Config()
}

private fun TabScope.Config() = Tab(
    IGConfig.translateText.plainText,
) {
    ConfigManagerWrapper(IGConfig)
}