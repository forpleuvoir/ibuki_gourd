package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigContainerWrapper
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen

fun testScreen6() = RowScreen(
    modifier = Modifier,
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    owner().screen()?.onClose = {
        TestConfig.asyncSave()
    }
    ConfigContainerWrapper(TestConfig, modifier = Modifier.fill(), listModifier = { Modifier.fill() })
}


