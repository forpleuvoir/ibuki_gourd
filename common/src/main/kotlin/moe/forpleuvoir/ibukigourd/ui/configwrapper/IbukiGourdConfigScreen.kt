package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import net.minecraft.client.gui.screens.Screen

/**
 * IbukiGourd 自己的配置页：Fabric 侧 ModMenu 与 NeoForge 侧模组列表的"配置"按钮共用。
 *
 * **不自动打开**：只构造屏幕并交给调用方（ModMenu 的工厂只需返回屏幕，由它自行 `setScreen`）。
 * 内容 = sokitsu 主题 + 铺底色面板 + [ConfigManagerWrapper]（页面骨架自带搜索与分组导航）。
 *
 * @param parent 返回时回到的父屏
 */
fun ibukiGourdConfigScreen(parent: Screen?): ComposeScreen = ComposeScreen(parent = parent) {
    SokitsuTheme {
        Surface(Modifier.fillMaxSize()) {
            ConfigManagerWrapper(IGConfig)
        }
    }
}
