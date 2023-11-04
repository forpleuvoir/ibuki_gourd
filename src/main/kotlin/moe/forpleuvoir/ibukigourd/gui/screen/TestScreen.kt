package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.list
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.gui.widget.text.textInput
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.ibukigourd.render.helper.*
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.mouseX
import moe.forpleuvoir.ibukigourd.util.mouseY
import moe.forpleuvoir.ibukigourd.util.text.literal
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack

var FRAME_TIME = 0.0

fun testScreen() {
    ScreenManager.open {
        renderBackground = {
            renderRect(it.matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
        }
        padding(4)

        val input = textInput {
            margin(bottom = 5f)
            hintText = literal("测试测试")
        }

        list(0f, 120f, showScroller = false) {
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
        scroller(200f, 10f, { 0f }, { 0f }, { 1f }, Arrangement.Horizontal)
        list(240f, 0f, Arrangement.Horizontal, showScroller = true, showBackground = true) {
            for (i in 0..10) {
                button {
                    textField("水平按钮$i")
                    margin(left = 5f, right = 5f)
                }
            }
        }

        textField(
            { literal("firstIndex:${input.firstCharacterIndex}\nstart:${input.selectionStart},end:${input.selectionEnd} \nsuggest:${input.suggestion?.invoke(input.text)}") },
            color = Colors.WHITE
        ).apply {
            fixed = true
            transform.x = 5f
            transform.y = 5f

        }
        renderOverlay = {
            val rect = contentRect(false)

            textRenderer.batchRenderText(it.matrixStack.peek().positionMatrix) {
                renderAlignmentText(
                    literal("x:%.2f, y:%.2f".format(mouseX, mouseY)),
                    rect,
                    align = PlanarAlignment::BottomLeft,
                    false,
                    color = Colors.WHITE
                )
                renderAlignmentText(
                    "%.2fms".format(FRAME_TIME / 1000000),
                    rect,
                    align = PlanarAlignment::TopRight,
                    false,
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