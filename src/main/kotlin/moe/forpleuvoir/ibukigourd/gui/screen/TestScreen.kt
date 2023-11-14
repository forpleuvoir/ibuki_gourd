package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.layout.list
import moe.forpleuvoir.ibukigourd.gui.base.layout.row
import moe.forpleuvoir.ibukigourd.gui.tip.tip
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.doubleScroller
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.intScroller
import moe.forpleuvoir.ibukigourd.gui.widget.numberScroller
import moe.forpleuvoir.ibukigourd.gui.widget.text.*
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.ibukigourd.render.helper.*
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.text.literal
import moe.forpleuvoir.ibukigourd.util.text.withColor
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack

var FRT = 0.0
var FPS = 0L

fun testScreen() {
    ScreenManager.open {
        renderBackground = {
            renderRect(it.matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
        }
        padding(4)

        textInput(150f, margin = Margin(bottom = 5f)) {
            hintText = literal("测试测试")
        }
        numberTextInput(150f, margin = Margin(bottom = 5f)) {
            hintText = literal("只能输入数字")
        }
        var input: TextBox
        row {
            margin(bottom = 6f)
            input = textBox(120f, 120f, padding = Margin(10f, 10f, 5f, 5f)) {
                hintText = literal("多行文本输入框测试")
                transform.z = 10f
                margin(right = 5f)
            }
            list(null, 120f) {
                for (i in 0..50) {
                    if (i % 2 == 0)
                        button {
                            textField({ "按钮$i" })
                            margin(top = 2, bottom = 2)
                        }
                    else
                        button { textField("按钮$i 啊", rightToLeft = true) }
                }
            }
            intScroller(0, -10..10, {}, length = 100f)
        }
        doubleScroller(0.0, -20.0..20.0, { }, length = 100f, arrangement = Arrangement.Horizontal)
        list(240f, null, Arrangement.Horizontal, showScroller = true, showBackground = true) {
            for (i in 0..10) {
                button {
                    if (i % 2 == 0)
                        icon(IconTextures.RIGHT, Size.create(12f, 12f), Colors.BLACK).margin(bottom = -4f, top = -4f)
                    textField("水平按钮$i").margin(top = 1f)
                    margin(left = 5f, right = 5f)
                    if (i == 5) {
                        tip {
                            textField(literal("我是提示!$i").style {
                                it.withColor(Colors.RED)
                            })
                        }
                    }
                }
            }
        }

        textField(
            { literal("cursor:${input.cursor}\nselection:${input.selectionEnd}\namount:${input.amount}\ntotalHeight:${input.textContentHeight}\nselectedText:${input.selectedText}") },
            color = Colors.WHITE
        ).fixed<TextField>(5f, 5f)

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