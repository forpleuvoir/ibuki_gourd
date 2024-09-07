package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Proxy
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.ibukigourd.util.switch

fun testScreen9() = ColumnScreen {
    val proxy: State<ColumnScope.() -> IGWidget> = stateOf {
        Button(
            horizontalArrangement = moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement.spacedBy(
                2f,
                moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment.CenterHorizontally
            )
        ) {
            Icon(moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures.CLOSE)
            TextLabel("关闭")
        }
    }
    Proxy(proxy)
    val state = stateOf(false)
    state.subscribe { s ->
        proxy.setValue {
            if (s) {
                Button(
                    horizontalArrangement = moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement.spacedBy(
                        2f,
                        moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment.CenterHorizontally
                    )
                ) {
                    Icon(moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures.LOCK)
                    TextLabel("锁定")
                }
            } else {
                Button(
                    horizontalArrangement = moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement.spacedBy(
                        2f,
                        moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment.CenterHorizontally
                    )
                ) {
                    Icon(moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures.CLOSE)
                    TextLabel("关闭")
                }
            }
        }
    }
    Button {
        TextLabel("切换")
        press {
            state.switch()
        }
    }

}