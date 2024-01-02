@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_FALSE_DISABLED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_FALSE_HOVERED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_FALSE_IDLE
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_FALSE_PRESSED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_TRUE_DISABLED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_TRUE_HOVERED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_TRUE_IDLE
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.CHECK_BOX_TRUE_PRESSED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_FALSE_DISABLED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_FALSE_HOVERED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_FALSE_IDLE
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_FALSE_PRESSED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_TRUE_DISABLED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_TRUE_HOVERED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_TRUE_IDLE
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LOCK_TRUE_PRESSED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SWITCH_BUTTON_OFF_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SWITCH_BUTTON_ON_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PRESS_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.render.helper.translate
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.Tick
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.ternary
import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class ButtonWidget(
    public override var onClick: () -> NextAction = { NextAction.Cancel },
    public override var onRelease: () -> NextAction = { NextAction.Cancel },
    var color: () -> ARGBColor = { COLOR },
    val pressOffset: Float = PRESS_OFFSET,
    var theme: ButtonTheme = TEXTURE,
    width: Float? = null,
    height: Float? = null,
    padding: Margin? = PADDING,
    margin: Margin? = null,
) : ClickableElement() {

    override var layout: Layout = LinearLayout({ this }, Arrangement.Horizontal)
        set(value) {
            field = value
            field.spacing = this.spacing
            arrange()
        }

    init {
        transform.width = width?.also { transform.fixedWidth = true } ?: 20f
        transform.height = height?.also { transform.fixedHeight = true } ?: 20f
        padding?.let(::padding)
        margin?.let(::margin)
    }

    fun longClick(time: Tick, action: () -> Unit) {
        longClickTime = time
        longClick = action
    }

    fun click(action: () -> Unit) {
        onClick = {
            action()
            NextAction.Cancel
        }
    }

    fun release(action: () -> Unit) {
        onRelease = {
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
 * 在当前容器中添加一个[ButtonWidget]
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
    padding: Padding = PADDING,
    margin: Margin? = null,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Button(onClick, onRelease, color, pressOffset, theme, width, height, padding, margin, scope))
}

/**
 * @see  ElementContainer.button
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
fun Button(
    onClick: () -> NextAction = { NextAction.Cancel },
    onRelease: () -> NextAction = { NextAction.Cancel },
    color: () -> ARGBColor = { COLOR },
    pressOffset: Float = PRESS_OFFSET,
    theme: ButtonTheme = TEXTURE,
    width: Float? = null,
    height: Float? = null,
    padding: Padding = PADDING,
    margin: Margin? = null,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return ButtonWidget(onClick, onRelease, color, pressOffset, theme, width, height, padding, margin).apply(scope)
}

@Contract("_ ->this")
fun ElementContainer.checkBox(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    width: Float? = 12f,
    height: Float? = 12f,
): ButtonWidget = addElement(CheckBox(statusDelegate, onChanged, color, width, height))

@Contract("_ ->this")
fun CheckBox(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    width: Float? = 12f,
    height: Float? = 12f,
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({
        status = !status
        onChanged(status)
        NextAction.Cancel
    }, { NextAction.Cancel }, color, 0f, TEXTURE, width, height, null, null) {

        override fun onRenderBackground(renderContext: RenderContext) {
            status.ternary(
                status(CHECK_BOX_TRUE_DISABLED, CHECK_BOX_TRUE_IDLE, CHECK_BOX_TRUE_HOVERED, CHECK_BOX_TRUE_PRESSED),
                status(CHECK_BOX_FALSE_DISABLED, CHECK_BOX_FALSE_IDLE, CHECK_BOX_FALSE_HOVERED, CHECK_BOX_FALSE_PRESSED)
            ).let { renderTexture(renderContext.matrixStack, transform, it, this.color()) }
        }
    }
}

/**
 * 锁样式的 [moe.forpleuvoir.ibukigourd.gui.widget.button.checkBox]
 * @receiver ElementContainer
 * @param statusDelegate DelegatedValue<Boolean>
 * @param onChanged (Boolean) -> Unit
 * @param color () -> ARGBColor
 * @param width Float?
 * @param height Float?
 * @return ButtonWidget
 */
@Contract("_ ->this")
fun ElementContainer.lockBox(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    width: Float? = 10f,
    height: Float? = 10f,
): ButtonWidget = addElement(LockBox(statusDelegate, onChanged, color, width, height))

/**
 * 锁样式的 [moe.forpleuvoir.ibukigourd.gui.widget.button.CheckBox]
 * @param statusDelegate DelegatedValue<Boolean>
 * @param onChanged (Boolean) -> Unit
 * @param color () -> ARGBColor
 * @param width Float?
 * @param height Float?
 * @return ButtonWidget
 */
@Contract("_ ->this")
fun LockBox(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    width: Float? = 10f,
    height: Float? = 10f,
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({
        status = !status
        onChanged(status)
        NextAction.Cancel
    }, { NextAction.Cancel }, color, 0f, TEXTURE, width, height, null, null) {

        override fun onRenderBackground(renderContext: RenderContext) {
            status.ternary(
                status(LOCK_TRUE_DISABLED, LOCK_TRUE_IDLE, LOCK_TRUE_HOVERED, LOCK_TRUE_PRESSED),
                status(LOCK_FALSE_DISABLED, LOCK_FALSE_IDLE, LOCK_FALSE_HOVERED, LOCK_FALSE_PRESSED)
            ).let { renderTexture(renderContext.matrixStack, transform, it, this.color()) }
        }
    }
}

@Contract("_ ->this")
fun ElementContainer.switchButton(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    width: Float? = 32f,
    height: Float? = 16f,
): ButtonWidget = addElement(SwitchButton(statusDelegate, onChanged, width, height))

@Contract("_ ->this")
fun SwitchButton(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    width: Float? = 32f,
    height: Float? = 16f,
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({
        status = !status
        onChanged(status)
        NextAction.Cancel
    }, { NextAction.Cancel }, { COLOR }, 0f, TEXTURE, width, height, null, null) {

        override fun onRenderBackground(renderContext: RenderContext) {
            renderTexture(renderContext.matrixStack, transform, status.ternary(SWITCH_BUTTON_ON_BACKGROUND, SWITCH_BUTTON_OFF_BACKGROUND), color())
            renderTexture(
                renderContext.matrixStack, rect(
                    transform.worldPosition.x(transform.worldPosition.x + status.ternary(transform.width / 2, 0f)),
                    transform.width / 2,
                    transform.height
                ), status(theme.disabled, theme.idle, theme.hovered, theme.pressed), color()
            )
        }
    }
}

/**
 * 在当前容器中添加一个没有背景的[ButtonWidget]
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
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(
        FlatButton(
            onClick,
            onRelease,
            color,
            hoverColor,
            pressColor,
            disableColor,
            width,
            height,
            padding,
            margin,
            scope
        )
    )
}

/**
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
fun FlatButton(
    onClick: () -> NextAction = { NextAction.Cancel },
    onRelease: () -> NextAction = { NextAction.Cancel },
    color: () -> ARGBColor? = { null },
    hoverColor: () -> ARGBColor? = { null },
    pressColor: () -> ARGBColor? = { null },
    disableColor: () -> ARGBColor? = { Colors.BLACK.alpha(75) },
    width: Float? = null,
    height: Float? = null,
    padding: Padding? = Padding(2),
    margin: Margin? = null,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : ButtonWidget(onClick, onRelease, { Color(0x000000) }, 0f, TEXTURE, width, height, padding, margin) {

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
    }.apply(scope)
}
