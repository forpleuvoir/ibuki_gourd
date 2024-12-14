package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.Corner
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.gui.screen.TabScreen
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color

private val icon = WidgetTexture(Corner(), 0, 0, 32, 32, TextureInfo(32, 32, identifier("icon.png")))

fun IbukiGourdModScreen() = TabScreen(
    header = {
        Column(
            Modifier
                .fill()
                .padding(5f),
            horizontalArrangement = Arrangement.spacedBy(5f, Alignment.Left)
        ) {
            Icon(icon, modifier = Modifier.size(16f, 16f))
            TextLabel(IbukiGourd.MOD_NAME)
        }
    },
    modifier = Modifier.onClose {
        IGConfig.asyncSave()
    },
    tabColor = stateOf(Color(0xffffccf0)),
    inactiveColor = stateOf(Color(0xffb3f2ff))
) {
    Config()
}

private fun TabScope.Config() = Tab(
    IGConfig.translateText.plainText
) {
    ConfigManagerWrapper(IGConfig)
}