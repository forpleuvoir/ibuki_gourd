package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.layout.Row
import moe.forpleuvoir.ibukigourd.gui.base.layout.draggableList
import moe.forpleuvoir.ibukigourd.gui.base.layout.listLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.row
import moe.forpleuvoir.ibukigourd.gui.tip.hoverTip
import moe.forpleuvoir.ibukigourd.gui.widget.button.*
import moe.forpleuvoir.ibukigourd.gui.widget.doubleScroller
import moe.forpleuvoir.ibukigourd.gui.widget.drop.dropMenu
import moe.forpleuvoir.ibukigourd.gui.widget.drop.dropSelector
import moe.forpleuvoir.ibukigourd.gui.widget.drop.itemList
import moe.forpleuvoir.ibukigourd.gui.widget.drop.items
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.intScroller
import moe.forpleuvoir.ibukigourd.gui.widget.tabs.tab
import moe.forpleuvoir.ibukigourd.gui.widget.tabs.tabs
import moe.forpleuvoir.ibukigourd.gui.widget.text.*
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.*
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.literal
import moe.forpleuvoir.ibukigourd.util.text.withColor
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.notifiableList
import net.minecraft.client.util.math.MatrixStack

var FRT = 0.0
var FPS = 0L

val list = notifiableList("黑丝", "白丝", "黑色裤袜", "白色裤袜", "日富美的裤袜", "普娜拉的裤袜")

fun testScreen(index: Int) {
    when (index) {
        1 -> testScreen1
        2 -> testScreen2
        3 -> testScreen3
        else -> testScreen1
    }.let {
        FRT = 0.0
        FPS = 0L
        ScreenManager.open(it)
    }
}

val testScreen3: Screen
    get() = screen {
        tabs(
            250f,
            150f,
            Padding(4),
            null,
            2f,
            backgroundColor = { Color(0xFFFFCCF0u) },
            inactiveColor = { Color(0xFFB3F2FFu) },
            direction = Direction.Top
        ) {
            val content = listOf(
                Row {
//                   textBox()
                },


                )
            repeat(content.size) { index ->
                var color = Colors.BLACK
                tab(index == 3,
                    tab = Row {
                        if (index == 3) icon(IconTextures.SAVE, scale = 0.8f, shaderColor = { color })
                        textField({ literal("选项卡$index").style { it.withColor(color) } })
                    }, content = content[index]
                ) {
                    onEnter = {
                        color = Colors.WHITE
                    }
                    onExit = {
                        color = Colors.BLACK
                    }
                }
            }
        }
    }


val testScreen2: Screen
    get() = screen {
        padding(10)
        val dl1 = draggableList(height = 120f) {
            spacing = 4f
            repeat(10) {
                row(80f) {
                    button { textField("测试一下$it") }
                }
            }
        }
        val dl2 = draggableList(width = 240f, arrangement = Arrangement.Horizontal) {
            repeat(10) {
                row(80f) {
                    button { textField("测试一下$it") }
                }
            }
        }
        renderBackground = {
            renderRect(it.matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
            renderOutline(it.matrixStack, contentRect(false), Colors.RED.opacity(0.5f))
            val rect = contentRect(true)
            textRenderer.batchRender {
                renderStringLines(
                    it.matrixStack,
                    "1.state: ${dl1.state.name} ,counter : ${dl1.draggingCounter}\n2.state: ${dl2.state.name} ,counter : ${dl2.draggingCounter}",
                    rect,
                    align = PlanarAlignment::TopLeft,
                    color = Colors.WHITE
                )
                renderAlignmentText(
                    it.matrixStack,
                    literal("x:%.2f, y:%.2f".format(mousePosition.x, mousePosition.y)),
                    rect,
                    align = PlanarAlignment::BottomLeft,
                    color = Colors.WHITE
                )
                renderStringLines(
                    it.matrixStack,
                    "frt:%.2fms\nfps:${FPS}".format(FRT / 1000000),
                    rect,
                    align = PlanarAlignment::TopRight,
                    color = Colors.WHITE,
                )
            }
        }
    }

val testScreen1: Screen
    get() = screen {
        renderBackground = {
            renderRect(it.matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
            renderRect(it.matrixStack, rect(vertex(6f, this.transform.halfHeight, 0f), 60f, 20f), Colors.RED.opacity(0.5f))
        }
        padding(4)
        spacing = 5f
        row {
            spacing = 5f
            val text = textInput(150f) {
                hintText = literal("测试测试")
            }
            dropSelector(options = list, onSelectionChange = {
                println("选择了$it")
                text.text = it
            }, scrollable = true)
            dropMenu {
                textField("下拉菜单")
                items {
                    repeat(20) {
                        flatButton(height = 16f) {
                            textField("下拉菜单选项$it")
                        }
                    }
                }
            }
        }
        row {
            spacing = 5f
            val statusDelegate = delegate(true)
            var status by statusDelegate
            var currentText = "啊啊"
            textInput(width = 150f) {
                text = currentText
                hintText = literal("只能输入浮点")
                onTextChanged = {
                    currentText = it
                }
            }
            button {
                textField(
                    { if (status) literal("开").style { it.withColor(Colors.GREEN) } else literal("关").style { it.withColor(Colors.RED) } },
                    width = 40f,
                    alignment = PlanarAlignment::Center,
                )
                release {
                    status = !status
                }
            }
            lockBox(statusDelegate)
            checkBox(statusDelegate)
            switchButton(statusDelegate)
        }
        row {
            spacing = 5f
            val intDelegate = delegate(0)
            val intValue by intDelegate
            intInput(intDelegate) { hintText = literal("只能输入整数") }
            button {
                textField({ intValue.toString() }, width = 40f)
            }
            dropMenu {
                textField("下拉菜单")
                itemList(maxHeight = 160f) {
                    repeat(20) {
                        flatButton(height = 16f) {
                            textField("下拉菜单选项$it")
                        }
                    }
                }
            }
        }
        row {
            spacing = 5f
            val floatDelegate = delegate(0f)
            val floatValue by floatDelegate
            floatInput(floatDelegate) { hintText = literal("只能输入浮点数") }
            doubleInput { hintText = literal("只能输入双精度浮点数") }
            button {
                textField({ floatValue.toString() }, width = 40f)
            }
        }
        var input: TextBoxWidget
        row {
            spacing = 5f
            input = textBox(120f, 120f, padding = Margin(5)) {
                hintText = literal("多行文本输入框测试")
                transform.z = 10f
            }
            listLayout(height = 120f) {
                spacing = 3f
                repeat(50) { i ->
                    if (i % 2 == 0)
                        button {
                            textField({ "按钮$i" })
                        }
                    else
                        button { textField("按钮$i 啊", rightToLeft = true) }
                }
            }
            intScroller(0, -10..10, {}, length = 100f)
        }
        doubleScroller(0.0, -20.0..20.0, { }, length = 100f, arrangement = Arrangement.Horizontal)
        listLayout(240f, null, Arrangement.Horizontal, showScroller = false, showBackground = true) {
            spacing = 5f
            repeat(10) { i ->
                button {
                    spacing = 4f
                    if (i % 2 == 0)
                        icon(IconTextures.RIGHT, shaderColor = { Colors.BLACK })
                    textField("水平按钮$i")
                    if (i == 5) {
                        hoverTip {
                            textField(literal("我是提示!$i").style {
                                it.withColor(Colors.RED)
                            })
                        }
                    }
                }
            }
        }

        renderOverlay = {
            val rect = contentRect(false)

            textRenderer.batchRender {
                renderAlignmentText(
                    it.matrixStack,
                    literal("x:%.2f, y:%.2f".format(mousePosition.x, mousePosition.y)),
                    rect,
                    align = PlanarAlignment::BottomLeft,
                    color = Colors.WHITE
                )
                renderStringLines(
                    it.matrixStack,
                    "frt:%.2fms\nfps:${FPS}".format(FRT / 1000000),
                    rect,
                    align = PlanarAlignment::TopRight,
                    color = Colors.WHITE
                )
            }

//			renderCrossHairs(matrixStack, rect.center.x, rect.center.y)
            renderOutline(it.matrixStack, rect, Colors.RED.opacity(0.5f))
//			renderCrossHairs(matrixStack, mouseX, mouseY)
        }
    }


fun renderCrossHairs(matrixStack: MatrixStack, x: Number, y: Number) {
    renderLine(
        matrixStack, 2f,
        colorVertex(0, y, 0, Colors.RED),
        colorVertex(mc.window.scaledWidth, y, 0, Colors.RED),
        normal = Vector3f(1f, 0f, 0f)
    )
    renderLine(
        matrixStack, 2f,
        colorVertex(x.toDouble() + 1, 0, 0, color = Colors.LIME),
        colorVertex(x.toDouble() + 1, mc.window.scaledHeight, 0, Colors.LIME),
        normal = Vector3f(0f, 1f, 0f)
    )
}

fun colorTest(matrixStack: MatrixStack) {
    renderHueGradientRect(matrixStack, rect(3f, 3f, 0f, 160, 5f), 240, hueRange = 0f..360f)
    renderHueGradientRect(matrixStack, rect(164f, 3f, 0f, 160, 5f), 240, reverse = true, hueRange = 0f..360f)

    renderHueGradientRect(matrixStack, rect(3, 109f, 0f, 5f, 120f), 240, arrangement = Arrangement.Vertical, hueRange = 0f..360f)
    renderHueGradientRect(matrixStack, rect(9, 109f, 0f, 5f, 120f), 240, arrangement = Arrangement.Vertical, reverse = true, hueRange = 0f..360f)


    renderSaturationGradientRect(matrixStack, rect(3f, 9f, 0f, 160, 5f), saturationRange = 0f..1f, hue = 210f, value = 1f)
    renderSaturationGradientRect(matrixStack, rect(164f, 9f, 0f, 160, 5f), reverse = true, saturationRange = 0f..1f, hue = 210f, value = 1f)

    renderSaturationGradientRect(matrixStack, rect(15, 109f, 0f, 5, 120), arrangement = Arrangement.Vertical, saturationRange = 0f..1f, hue = 210f, value = 1f)
    renderSaturationGradientRect(
        matrixStack,
        rect(21, 109f, 0f, 5, 120),
        arrangement = Arrangement.Vertical,
        reverse = true,
        saturationRange = 0f..1f,
        hue = 210f,
        value = 1f
    )


    renderValueGradientRect(matrixStack, rect(3f, 15f, 0f, 160, 5f), valueRange = 0f..1f, hue = 210f, saturation = 1f)
    renderValueGradientRect(matrixStack, rect(164f, 15f, 0f, 160, 5f), reverse = true, valueRange = 0f..1f, hue = 210f, saturation = 1f)

    renderValueGradientRect(matrixStack, rect(27, 109, 0f, 5, 120), arrangement = Arrangement.Vertical, valueRange = 0f..1f, hue = 210f, saturation = 1f)
    renderValueGradientRect(
        matrixStack,
        rect(33, 109, 0f, 5, 120),
        arrangement = Arrangement.Vertical,
        reverse = true,
        valueRange = 0f..1f,
        hue = 210f,
        saturation = 1f
    )


    renderAlphaGradientRect(matrixStack, rect(3f, 21f, 0f, 160, 5f), alphaRange = 0f..1f, color = Color(0, 128, 255))
    renderAlphaGradientRect(matrixStack, rect(164f, 21f, 0f, 160, 5f), reverse = true, alphaRange = 0f..1f, color = Color(0, 128, 255))

    renderAlphaGradientRect(matrixStack, rect(39, 109, 0f, 5, 120), arrangement = Arrangement.Vertical, alphaRange = 0f..1f, color = Color(0, 128, 255))
    renderAlphaGradientRect(
        matrixStack,
        rect(45, 109, 0f, 5, 120),
        arrangement = Arrangement.Vertical,
        reverse = true,
        alphaRange = 0f..1f,
        color = Color(0, 128, 255)
    )


    renderSVGradientRect(matrixStack, rect(3f, 27f, 0f, 120f, 80f), hue = 210f)
    renderSVGradientRect(matrixStack, rect(124f, 27f, 0f, 120f, 80f), hue = 210f, reverse = true)

    renderSVGradientRect(matrixStack, rect(51, 109, 0f, 80, 120), arrangement = Arrangement.Vertical, hue = 210f)
    renderSVGradientRect(matrixStack, rect(132, 109, 0f, 80, 120), arrangement = Arrangement.Vertical, hue = 210f, reverse = true)
}