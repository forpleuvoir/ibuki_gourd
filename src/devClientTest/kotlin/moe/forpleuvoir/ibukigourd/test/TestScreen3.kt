package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.DropDownMenu
import moe.forpleuvoir.ibukigourd.gui.widget.Proxy
import moe.forpleuvoir.ibukigourd.gui.widget.Spinner
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.LockButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.maxWidth
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.*
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen3() = RowScreen(
    modifier = Modifier.renderOverlay { ctx, _, _, _ ->
        this as IGScreenImpl<*>
        val lines = listOf(
            Literal(focusedWidget.toString()),
            Literal(hoveredWidget.toString())
        )
        ctx.batchRenderText {
            pushTextLines(
                lines, transform.asWorldBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top
            )
        }
    }
) {
    val selectText = stateOf("本居小铃")
    val listString = listOf("东风谷早苗", "博丽灵梦", "雾雨魔理沙", "伊吹萃香")
    Column {
        DropDownMenu {
            TextField(selectText)
            DropDownContent {
                RowListWrapped(
                    modifier = Modifier.disableRenderBackground().padding(0f),
                    horizontalAlignment = Alignment.Left,
                ) {
                    val proxy: State<RowListScope.() -> IGWidget> = stateOf {
                        Button {
                            Icon(IconTextures.CLOSE)
                            TextField("关闭")
                        }
                    }
                    Proxy(proxy)
                    val state = stateOf(false)
                    state.subscribe { s ->
                        proxy.setValue {
                            if (s) {
                                Button {
                                    Icon(IconTextures.LOCK)
                                    TextField("锁定")
                                }
                            } else {
                                Button {
                                    Icon(IconTextures.CLOSE)
                                    TextField("关闭")
                                }
                            }
                        }
                    }
                    Button {
                        TextField("切换")
                        press {
                            state.toggle()
                        }
                    }
                    listString.forEach { str ->
                        FlatButton(
                            modifier = Modifier.width(listString.maxWidth(textRenderer) + 2f),
                            hoveredColor = Colors.CYAN.opacity(.35f),
                            horizontalArrangement = Arrangement.Left
                        ) {
                            press {
                                this@DropDownMenu.toggle()
                                selectText.setValue(str)
                            }
                            TextField(str)
                        }
                    }
                    repeat(23) {
                        TextField("aa$it", modifier = Modifier.width(40f).renderHoveredOutlineBox(Colors.BANANA_YELLOW)) {
                            setting {
                                horizontalAlignment = Alignment.Left
                            }
                        }
                    }
                }
            }
        }
        FlatButton(hoveredColor = Colors.AQUA.opacity(.25f)) {
            TextField(selectText)
        }
        val status = stateOf(true)
        val map = mapOf(
            "东风谷早苗" to IconTextures.CLOSE,
            "博丽灵梦" to IconTextures.DELETE,
            "雾雨魔理沙" to IconTextures.FILTER,
            "伊吹萃香" to IconTextures.SAVE
        )
        SwitchButton(status)
        LockButton(status)
        val wrapper: WidgetContainerScope.(String) -> IGWidget = { str: String ->
            Column(
                horizontalArrangement = Arrangement.spacedBy(2f)
            ) {
                Icon(map[str]!!, color = Colors.BLACK, modifier = Modifier.size(8f, 8f))
                TextField(str)
            }
        }
        Spinner(
            options = listString,
            selectedWrapper = {
                wrapper(this, it)
            },
            optionWrapper = {
                wrapper(this, it)
            }
        )
        Spinner(listString)
    }

    Button(
        modifier = Modifier.maxWidth(80f).maxHeight(80f)
    ) {
        val text = stateOf("测试文本:")
        var count = 1
        press {
            text + "\n测试宽度$count"
            count++
        }
        TextField(
            str = text,
            style = style(color = Colors.COFFEE),
            modifier = Modifier
                .renderOverlay { ctx, _, _, _ ->
                    if (wasMouseOver)
                        ctx.batchRenderBox {
                            pushBoxOutline(contentBox(true), Colors.ROSE)
                            pushBoxOutline(transform.asWorldBox, Colors.MEDIUM_TEAL)
                        }
                }
                .padding(4f)
        )
    }

    val list = listOf(
        Arrangement.SpaceBetween,
        Arrangement.SpaceAround,
        Arrangement.SpaceEvenly,
    )
    val lv = listOf(
        Arrangement.Top,
        Arrangement.Center,
        Arrangement.Bottom
    )
    val lh = listOf(
        Arrangement.Left,
        Arrangement.Center,
        Arrangement.Right
    )
    Column(Modifier.height(200f)) {
        Row(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lh).forEach { arrangement ->
                ColumnTest(arrangement)
            }
        }
        Column(
            modifier = Modifier.weight(1).fill()
        ) {
            (list + lv).forEach { arrangement ->
                RowTest(arrangement)
            }
        }
    }

}

private fun RowScope.ColumnTest(arrangement: Arrangement.Horizontal) = Column(
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { ctx, _, _, _ ->
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        },
    horizontalArrangement = arrangement
) {
    Button { TextField("按钮1") }
    Button { TextField("按钮2") }
    Button { TextField("按钮3") }
}

private fun ColumnScope.RowTest(arrangement: Arrangement.Vertical) = Row(
    verticalArrangement = arrangement,
    modifier = Modifier
        .padding(3f)
        .fill()
        .weight(1)
        .renderBackground { ctx, _, _, _ ->
            ctx.batchRenderBox {
                if (wasMouseOver) {
                    pushBoxOutline(transform, Colors.AQUA)
                }
                pushBox(transform, Colors.AQUA.alpha(.25f))
            }
        }
) {
    Button { TextField("按钮1") }
    Button { TextField("按钮2") }
    Button { TextField("按钮3") }
}