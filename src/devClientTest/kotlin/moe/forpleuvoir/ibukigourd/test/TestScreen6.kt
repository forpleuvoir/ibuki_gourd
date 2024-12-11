package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigsWrapper
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen

fun testScreen6() = RowScreen(
    modifier = Modifier
        .onClose {
            TestConfig.asyncSave()
        },
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    ConfigsWrapper(TestConfig.configs(), modifier = Modifier.fill().weight(1))
}


