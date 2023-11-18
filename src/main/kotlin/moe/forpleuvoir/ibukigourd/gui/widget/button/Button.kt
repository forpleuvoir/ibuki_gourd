@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PRESS_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.render.helper.translate
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


open class Button(
    public override var onClick: () -> NextAction = { NextAction.Cancel },
    public override var onRelease: () -> NextAction = { NextAction.Cancel },
    var color: () -> ARGBColor = { COLOR },
    val pressOffset: Float = PRESS_OFFSET,
    var theme: ButtonTheme = TEXTURE,
    width: Float? = null,
    height: Float? = null,
    padding: Margin = PADDING,
    margin: Margin? = null,
) : ClickableElement() {

    override var layout: Layout = LinearLayout({ this }, Arrangement.Horizontal)
        set(value) {
            field = value
            arrange()
        }

    init {
        transform.width = width?.also { transform.fixedWidth = true } ?: 20f
        transform.height = height?.also { transform.fixedHeight = true } ?: 20f
        padding(padding)
        margin?.let(::margin)
    }

    fun click(action: () -> Unit) {
        onClick = {
            action()
            NextAction.Cancel
        }
    }

    override fun onRender(renderContext: RenderContext) {
        val offset = Vector3f(0f, status(pressOffset, 0f, 0f, pressOffset), 0f)
        renderContext.scissorOffset(offset) {
            matrixStack {
                matrixStack.translate(offset)
                renderBackground(this)
                super.onRender(this)
                renderOverlay(this)
            }
        }
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed), color())
    }

}

/**
 * 在当前容器中添加一个[Button]
 * @receiver ElementContainer 容器
 * @param onClick () -> NextAction 点击事件
 * @param onRelease () -> NextAction 松开事件
 * @param color () -> ARGBColor 按钮着色器颜色
 * @param pressOffset Float 按钮按下时的偏移量
 * @param theme ButtonTheme 按钮主题
 * @param width Float? 按钮宽度,null -> auto(20f),!null - value
 * @param height Float? 按钮高度,null -> auto(20f),!null - value
 * @param padding Margin 内部边距
 * @param margin Margin? 外部边距 null -> 无外部边距
 * @param scope Button.() -> Unit 按钮作用域
 * @return Button
 */
@OptIn(ExperimentalContracts::class)
@Contract("_ ->this")
fun ElementContainer.button(
    onClick: () -> NextAction = { NextAction.Cancel },
    onRelease: () -> NextAction = { NextAction.Cancel },
    color: () -> ARGBColor = { COLOR },
    pressOffset: Float = PRESS_OFFSET,
    theme: ButtonTheme = TEXTURE,
    width: Float? = null,
    height: Float? = null,
    padding: Margin = PADDING,
    margin: Margin? = null,
    scope: Button.() -> Unit = {}
): Button {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Button(onClick, onRelease, color, pressOffset, theme, width, height, padding, margin).apply(scope))
}

/**
 * 在当前容器中添加一个没有背景的[Button]
 * @receiver ElementContainer
 * @param onClick () -> NextAction
 * @param onRelease () -> NextAction
 * @param color () -> ARGBColor
 * @param hoverColor () -> ARGBColor
 * @param pressColor () -> ARGBColor
 * @param disableColor () -> ARGBColor
 * @param width Float?
 * @param height Float?
 * @param padding Margin
 * @param margin Margin?
 * @param scope Button.() -> Unit
 * @return Button
 */
@OptIn(ExperimentalContracts::class)
fun ElementContainer.flatButton(
    onClick: () -> NextAction = { NextAction.Cancel },
    onRelease: () -> NextAction = { NextAction.Cancel },
    color: () -> ARGBColor? = { null },
    hoverColor: () -> ARGBColor? = { null },
    pressColor: () -> ARGBColor? = { null },
    disableColor: () -> ARGBColor? = { Colors.BLACK.alpha(75) },
    width: Float? = null,
    height: Float? = null,
    padding: Margin = Margin(2),
    margin: Margin? = null,
    scope: Button.() -> Unit = {}
): Button {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(object : Button(onClick, onRelease, { Color(0x000000) }, 0f, TEXTURE, width, height, padding, margin) {

        override fun onRenderBackground(renderContext: RenderContext) {
            status(disableColor(), color(), hoverColor(), pressColor())?.let {
                renderRect(renderContext.matrixStack, transform, it)
            }
        }

        override fun onRender(renderContext: RenderContext) {
            renderBackground(renderContext)
            super.onRender(renderContext)
            renderOverlay(renderContext)
        }
    }.apply(scope))
}
