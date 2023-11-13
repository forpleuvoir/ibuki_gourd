@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PRESS_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.render.helper.translate
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


open class Button(
    public override var onClick: () -> NextAction = { NextAction.Continue },
    public override var onRelease: () -> NextAction = { NextAction.Continue },
    var color: () -> ARGBColor = { COLOR },
    val pressOffset: Float = PRESS_OFFSET,
    var theme: ButtonTheme = TEXTURE,
    width: Float? = null,
    height: Float? = null,
    padding: Margin = PADDING,
    margin: Margin? = null,
) : ClickableElement() {
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
    onClick: () -> NextAction = { NextAction.Continue },
    onRelease: () -> NextAction = { NextAction.Continue },
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
