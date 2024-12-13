package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.gui.configwrapper.ConfigsWrapper
import moe.forpleuvoir.ibukigourd.gui.screen.TabScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.gui.widget.SearchBar
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig
import moe.forpleuvoir.ibukigourd.mod.gui.IGConfig
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.collection.notifiableList

fun testScreen7() = TabScreen(
    header = {
        TextLabel("这是顶部测试", modifier = Modifier.align(Alignment.Right))
        TextLabel("这是顶部测试2", modifier = Modifier.align(Alignment.Right))
    },
    modifier = Modifier.onClose {
        TestConfig.asyncSave()
        IGConfig.asyncSave()
    },
    tabColor = stateOf(Color(0xffffccf0)),
    inactiveColor = stateOf(Color(0xffb3f2ff))
) {
    tab3()
    tab1()
    tab2()
}

fun TabScope.tab3() = Tab("配置管理器测试") {
    ConfigManagerWrapper(IGConfig)
}

fun TabScope.tab1() = Tab("测试用配置设置") {
    Row {
        val list = notifiableList(TestConfig.configs())
        SearchBar(
            textConsumer = { str ->
                list.disableNotify {
                    list.clear()
                    list.addAll(TestConfig.configs().filter { it.matched(str.toRegex()) })
                }
                list.onChange(list)
            },
            hintText = stateOf("搜索.."),
            modifier = Modifier.fill(),
            textEditorModifier = { Modifier.weight(1) }
        )
        ConfigsWrapper(list, modifier = Modifier.fill()).apply {
            list.subscribe {
                this.recompose()
            }
        }
    }
}

fun TabScope.tab2() = Tab("第二页,选择框颜色设置") {
    val color = mutableStateOf(GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR).apply {
        subscribe {
            GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR = Color(it.argb)
        }
    }
    ColorPicker(color)
}