@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.fixed
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.width
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.context.extension.batchRenderTexture
import moe.forpleuvoir.ibukigourd.gui.render.context.extension.renderBox
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
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
import moe.forpleuvoir.ibukigourd.gui.widget.PressableElement
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PRESS_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import moe.forpleuvoir.ibukigourd.render.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.render.translate
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.ibukigourd.util.Tick
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.pick
import org.jetbrains.annotations.Contract
import org.joml.Vector3fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class ButtonWidget(
    override var onPress: () -> Unit = { },
    override var onRelease: () -> Unit = { },
    var color: () -> ARGBColor = { COLOR },
    var theme: ButtonTheme = TEXTURE,
    modifier: Modifier = Modifier
) : PressableElement(modifier) {

    init {
        padding = PADDING
    }

    var pressOffset: Vector3fc = PRESS_OFFSET

    fun longPress(time: Tick, action: () -> Unit) {
        longPressTime = time
        longPress = action
    }

    fun press(action: () -> Unit) {
        onPress = {
            action()
        }
    }

    fun release(action: () -> Unit) {
        onRelease = {
            action()
        }
    }

    override fun onRender(renderContext: RenderContext) {
        val offset = pressOffset
        renderContext.scissorOffset(offset) {
            useMatrixStack {
                matrixStack.translate(offset)
                tryRender {
                    renderBackground(this)
                }
                super.onRender(this)
                tryRender {
                    renderOverlay(this)
                }
            }
        }
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderContext {
            batchRenderTexture {
                drawTexture(transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed), color())
            }
        }

    }

}

/**
 * 在当前容器中添加一个[ButtonWidget]
 * @receiver ElementContainer
 * @param onPress () -> Unit
 * @param onRelease () -> Unit
 * @param color () -> ARGBColor
 * @param pressOffset Float
 * @param theme ButtonTheme
 * @param modifier Modifier
 * @param scope ButtonWidget.() -> Unit
 * @return ButtonWidget
 */
@OptIn(ExperimentalContracts::class)
@Contract("_ ->this")
fun ElementContainer.button(
    onPress: () -> Unit = { },
    onRelease: () -> Unit = { },
    color: () -> ARGBColor = { COLOR },
    theme: ButtonTheme = TEXTURE,
    modifier: Modifier = Modifier,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(Button(onPress, onRelease, color, theme, modifier, scope))
}

/**
 * [ButtonWidget]构造函数
 * @param onPress () -> Unit
 * @param onRelease () -> Unit
 * @param color () -> ARGBColor
 * @param pressOffset Float
 * @param theme ButtonTheme
 * @param modifier Modifier
 * @param scope ButtonWidget.() -> Unit
 * @return ButtonWidget
 */
@OptIn(ExperimentalContracts::class)
@Contract("_ ->this")
fun Button(
    onPress: () -> Unit = { },
    onRelease: () -> Unit = { },
    color: () -> ARGBColor = { COLOR },
    theme: ButtonTheme = TEXTURE,
    modifier: Modifier = Modifier,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return ButtonWidget(onPress, onRelease, color, theme, modifier).apply(scope)
}

@Contract("_ ->this")
fun ElementContainer.checkBox(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    modifier: Modifier = Modifier.width(12.fixed).height(12.fixed),
): ButtonWidget = addElement(CheckBox(statusDelegate, onChanged, color, modifier))

@Contract("_ ->this")
fun CheckBox(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    modifier: Modifier = Modifier.width(12.fixed).height(12.fixed),
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({ status = !status;onChanged(status) }, { }, color, TEXTURE, modifier) {
        override fun onRenderBackground(renderContext: RenderContext) {
            status.pick(
                status(CHECK_BOX_TRUE_DISABLED, CHECK_BOX_TRUE_IDLE, CHECK_BOX_TRUE_HOVERED, CHECK_BOX_TRUE_PRESSED),
                status(CHECK_BOX_FALSE_DISABLED, CHECK_BOX_FALSE_IDLE, CHECK_BOX_FALSE_HOVERED, CHECK_BOX_FALSE_PRESSED)
            ).let { widgetTexture ->
                renderContext.tryRender {
                    batchRenderTexture {
                        ninePatchTexture(transform, widgetTexture, color())
                    }
                }
            }
        }
    }.apply {
        pressOffset = Vector3f()
    }
}

/**
 * 在当前容器中添加一个锁样式的[CheckBox]
 * @receiver ElementContainer
 * @param statusDelegate DelegatedValue<Boolean>
 * @param onChanged (Boolean) -> Unit
 * @param color () -> ARGBColor
 * @param modifier Modifier
 * @return ButtonWidget
 */
@Contract("_ ->this")
fun ElementContainer.lockBox(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    modifier: Modifier = Modifier.width(10.fixed).height(10.fixed),
): ButtonWidget = addElement(LockBox(statusDelegate, onChanged, color, modifier))

/**
 * 锁样式的 [CheckBox]
 * @param statusDelegate DelegatedValue<Boolean>
 * @param onChanged (Boolean) -> Unit
 * @param color () -> ARGBColor
 * @param modifier Modifier
 * @return ButtonWidget
 */
@Contract("_ ->this")
fun LockBox(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    onChanged: (Boolean) -> Unit = {},
    color: () -> ARGBColor = { COLOR },
    modifier: Modifier = Modifier.width(10.fixed).height(10.fixed)
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({ status = !status;onChanged(status) }, { }, color, TEXTURE, modifier) {
        override fun onRenderBackground(renderContext: RenderContext) {
            status.pick(
                status(LOCK_TRUE_DISABLED, LOCK_TRUE_IDLE, LOCK_TRUE_HOVERED, LOCK_TRUE_PRESSED),
                status(LOCK_FALSE_DISABLED, LOCK_FALSE_IDLE, LOCK_FALSE_HOVERED, LOCK_FALSE_PRESSED)
            ).let { widgetTexture ->
                renderContext.tryRender {
                    batchRenderTexture {
                        ninePatchTexture(transform, widgetTexture, color())
                    }
                }
            }
        }
    }.apply {
        pressOffset = Vector3f()
    }
}

@Contract("_ ->this")
fun ElementContainer.switchButton(
    statusDelegate: DelegatedValue<Boolean> = delegate(false),
    onChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier.width(32.fixed).height(16.fixed)
): ButtonWidget = addElement(SwitchButton(statusDelegate, onChanged, modifier))

@Contract("_ ->this")
fun SwitchButton(
    statusDelegate: DelegatedValue<Boolean> = DelegatedValue(false),
    onChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier.width(32.fixed).height(16.fixed)
): ButtonWidget {
    var status by statusDelegate
    return object : ButtonWidget({ status = !status;onChanged(status) }, { }, { COLOR }, TEXTURE, modifier) {
        override fun onRenderBackground(renderContext: RenderContext) {
            renderContext.tryRender {
                batchRenderTexture {
                    ninePatchTexture(transform, status.pick(SWITCH_BUTTON_ON_BACKGROUND, SWITCH_BUTTON_OFF_BACKGROUND), color())
                    ninePatchTexture(
                        Box(
                            transform.worldPosition.copy(x = transform.worldX + status.pick(transform.width / 2, 0f)),
                            transform.width / 2, transform.height
                        ),
                        status(theme.disabled, theme.idle, theme.hovered, theme.pressed),
                        color()
                    )
                }
            }
        }
    }.apply {
        pressOffset = Vector3f()
    }
}

/**
 *
 * @receiver ElementContainer
 * @param onPress () -> Unit
 * @param onRelease () -> Unit
 * @param color () -> ARGBColor?
 * @param hoverColor () -> ARGBColor?
 * @param pressColor () -> ARGBColor?
 * @param disableColor () -> ARGBColor?
 * @param modifier Modifier
 * @param scope ButtonWidget.() -> Unit
 * @return ButtonWidget
 */
@OptIn(ExperimentalContracts::class)
fun ElementContainer.flatButton(
    onPress: () -> Unit = { },
    onRelease: () -> Unit = { },
    color: () -> ARGBColor? = { null },
    hoverColor: () -> ARGBColor? = { null },
    pressColor: () -> ARGBColor? = { null },
    disableColor: () -> ARGBColor? = { Colors.BLACK.alpha(75) },
    modifier: Modifier = Modifier,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return addElement(
        FlatButton(
            onPress, onRelease, color, hoverColor, pressColor, disableColor, modifier, scope
        )
    )
}

/**
 *
 * @param onPress () -> Unit
 * @param onRelease () -> Unit
 * @param color () -> ARGBColor?
 * @param hoverColor () -> ARGBColor?
 * @param pressColor () -> ARGBColor?
 * @param disableColor () -> ARGBColor?
 * @param modifier Modifier
 * @param scope ButtonWidget.() -> Unit
 * @return ButtonWidget
 */
@OptIn(ExperimentalContracts::class)
fun FlatButton(
    onPress: () -> Unit = { },
    onRelease: () -> Unit = { },
    color: () -> ARGBColor? = { null },
    hoverColor: () -> ARGBColor? = { null },
    pressColor: () -> ARGBColor? = { null },
    disableColor: () -> ARGBColor? = { Colors.BLACK.alpha(75) },
    modifier: Modifier = Modifier,
    scope: ButtonWidget.() -> Unit = {}
): ButtonWidget {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return object : ButtonWidget(onPress, onRelease, { Color(0x000000) }, TEXTURE, modifier) {

        override fun onRenderBackground(renderContext: RenderContext) {
            renderContext.tryRender {
                status(disableColor(), color(), hoverColor(), pressColor())?.let {
                    renderBox(transform.asWorldBox, it)
                }
            }
        }

        override fun onRender(renderContext: RenderContext) {
            renderContext.tryRender {
                renderBackground(this)
            }
            super.onRender(renderContext)
            renderContext.tryRender {
                renderOverlay(this)
            }
        }
    }.apply {
        pressOffset = Vector3f()
        scope.invoke(this)
    }
}
