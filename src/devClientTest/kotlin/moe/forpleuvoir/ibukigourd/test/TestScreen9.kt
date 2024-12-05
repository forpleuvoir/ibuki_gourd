package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Proxy
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch

fun testScreen9() = ColumnScreen {
    val proxy: MutableState<ColumnScope.() -> IGWidget> = mutableStateOf {
        Button(
            horizontalArrangement = Arrangement.spacedBy(
                2f,
                Alignment.CenterHorizontally
            )
        ) {
            Icon(IconTextures.CLOSE)
            TextLabel("关闭")
        }
    }
    Proxy(proxy)
    val state = mutableStateOf(false)
    state.subscribe { s ->
        proxy.setValue {
            if (s) {
                Button(
                    horizontalArrangement = Arrangement.spacedBy(
                        2f,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Icon(IconTextures.LOCK)
                    TextLabel("锁定")
                    HoverTip {
                        TextLabel("点击复制颜色")
                    }
                }
            } else {
                Button(
                    horizontalArrangement = Arrangement.spacedBy(
                        2f,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Icon(IconTextures.CLOSE)
                    TextLabel("关闭")
                    HoverTip {
                        TextLabel("点击复制颜色")
                    }
                }
            }
        }
    }
    Button {
        TextLabel("切换")
        click {
            state.switch()
        }
    }
    Button {
        TextLabel("占位置的")
    }

}