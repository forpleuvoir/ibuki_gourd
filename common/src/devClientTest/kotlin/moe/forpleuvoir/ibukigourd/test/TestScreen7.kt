package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.base.widget.executeRecompose
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigsWrapper
import moe.forpleuvoir.ibukigourd.gui.modifier.*
import moe.forpleuvoir.ibukigourd.gui.screen.TabScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.gui.widget.SearchBar
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.collection.notifiableList

fun testScreen7() = TabScreen(
    header = {
        Text("这是顶部测试", modifier = Modifier.align(Alignment.Right))
        Text("这是顶部测试2", modifier = Modifier.align(Alignment.Right))
    },
    modifier = Modifier.onClose {
        TestConfig.asyncSave()
        IGConfig.asyncSave()
    }.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MouseCursor()
        MousePosition()
    },
    tabColor = stateOf(Color.ofRGB(0XFFCCF0)),
    inactiveColor = stateOf(Color.ofRGB(0XB3F2FF))
) {
    tab3()
    tab1()
    tab2()
}

fun TabScope.tab3() = Tab("配置管理器测试") {
    ConfigManagerWrapper(TestConfig)
}

fun TabScope.tab1() = Tab("测试用配置设置", true) {
    Column {
        val list = notifiableList(TestConfig.configs())
        SearchBar(
            textConsumer = { str ->
                list.disableNotify {
                    list.clear()
                    list.addAll(TestConfig.configs().filter { it.matched(str.toRegex()) })
                }
                list.onChange(list)
            },
            hintText = stateOf(IGLang.search.plainText),
            modifier = Modifier.fill(),
            textEditorModifier = { Modifier.weight(1) }
        )
        ConfigsWrapper(list, modifier = Modifier.fill()).apply {
            list.subscribe {
                executeRecompose()
            }
        }
    }
}

fun TabScope.tab2() = Tab("第二页,选择框颜色设置") {
    val color = mutableStateOf(GuiConfig.Screen.widgetTestOutlineColor).apply {
        subscribe {
            GuiConfig.Screen.widgetTestOutlineColor = Color.ofARGB(it.argb)
        }
    }
    ColorPicker(color)
}