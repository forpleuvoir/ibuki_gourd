package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Direction.*
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TIP
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TIP_ARROW_BOTTOM
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TIP_ARROW_LEFT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TIP_ARROW_RIGHT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TIP_ARROW_TOP
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.ARROW_OFFSET
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.DELAY
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.MARGIN
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.PADDING
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.util.clamp
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 只有鼠标悬浮在元素上才会显示的Tip
 * @param parent Element
 * @param tipHandler () -> TipHandler
 * @param displayDelay UInt
 * @param padding Margin
 * @param margin Margin
 * @param backgroundColor ARGBColor
 * @param optionalDirections Iterable<Direction>
 * @constructor
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MouseHoverTip(
    /**
     * 父元素
     */
    parent: Element,
    tipHandler: () -> TipHandler = { parent.screen() },
    /**
     * 延迟显示时间
     */
    var displayDelay: UInt = DELAY.toUInt(),
    padding: Margin = PADDING,
    margin: Margin = MARGIN,
    var backgroundColor: ARGBColor = Theme.TIP.BACKGROUND_COLOR,
    /**
     * 可显示的方向,会自动选择一个可用的方向
     */
    var optionalDirections: Iterable<Direction> = Direction.entries
) : Tip({ parent }, tipHandler) {

    init {
        padding(padding)
        margin(margin)
    }

    protected open var direction: Direction = optionalDirections.first()
        set(value) {
            if (value in optionalDirections) {
                field = value
            }
        }

    override fun arrange() {
        super.arrange()
        checkDirection()
        calcPosition()
    }

    /**
     * 父元素最新的状态，如果没有变则不需要更新位置
     */
    protected var latestParent: Rectangle<Vector3<Float>> = rect(vertex(0, 0, 0), 0, 0)

    var keepDisplay: Boolean = false

    open var tickCounter: UInt = 0u
        set(value) {
            field = value
            if (tickCounter >= displayDelay) {
                if (field == displayDelay) {
                    visible = push()
                    if (!visible) field = 0u
                }
                if (!Rectangle.equals(latestParent, transform.parent()!!.asWorldRect)) {
                    checkDirection()
                    calcPosition()
                    latestParent = transform.parent()!!.asWorldRect
                }
            }
        }

    protected open fun checkDirection() {
        val parent = this.transform.parent()!!
        val canPlace: (Direction) -> Boolean = { dir ->
            when (dir) {
                Left   -> parent.worldLeft - (transform.width + margin.left) > 0
                Right  -> parent.worldRight + (transform.width + margin.right) < mc.window.scaledWidth
                Top    -> parent.worldTop - (transform.height + margin.top) > 0
                Bottom -> parent.worldBottom - (transform.height + margin.bottom) < mc.window.scaledHeight
            }
        }
        optionalDirections.forEach {
            if (canPlace(it)) {
                direction = it
                return
            }
        }
    }

    protected open fun calcPosition() {
        val parent = this.transform.parent()!!
        when (direction) {
            Left   -> transform.translateTo(-(transform.width + margin.left), -(transform.halfHeight - parent.halfHeight))
            Right  -> transform.translateTo(parent.width + margin.right, -(transform.halfHeight - parent.halfHeight))
            Top    -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), -(transform.height + margin.top))
            Bottom -> transform.translateTo(-(transform.halfWidth - parent.halfWidth), parent.height + margin.top)
        }
        transform.worldY = transform.worldY.clamp(0f..mc.window.scaledHeight - transform.height)
        transform.worldX = transform.worldX.clamp(0f..mc.window.scaledWidth - transform.width)
    }

    override var visible: Boolean = false

    final override var active: Boolean
        get() = tickCounter >= displayDelay && parent().visible
        @Deprecated("Do not set the active value of Tip")
        set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the active value of Tip")
        }


    override fun tick() {
        if (parent().mouseHover() && parent().visible) {
            tickCounter++
        } else if (!keepDisplay || !parent().visible) {
            visible = !pop()
            if (!visible) {
                tickCounter = 0u
            }
        }
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        val (texture, rect) = when (direction) {
            Left   ->
                TIP_ARROW_LEFT to rect(
                    transform.worldRight - ARROW_OFFSET.left,
                    parent().transform.worldCenter.y - MARGIN.left / 2,
                    transform.worldZ,
                    MARGIN.left, MARGIN.left
                )

            Right  ->
                TIP_ARROW_RIGHT to rect(
                    transform.worldLeft - MARGIN.right + ARROW_OFFSET.right,
                    parent().transform.worldCenter.y - MARGIN.left / 2,
                    transform.worldZ,
                    MARGIN.right, MARGIN.right
                )

            Top    ->
                TIP_ARROW_TOP to rect(
                    parent().transform.worldCenter.x - MARGIN.top / 2,
                    transform.worldBottom - ARROW_OFFSET.top,
                    transform.worldZ,
                    MARGIN.top, MARGIN.top
                )

            Bottom ->
                TIP_ARROW_BOTTOM to rect(
                    parent().transform.worldCenter.x - MARGIN.bottom / 2,
                    transform.worldTop - MARGIN.bottom + ARROW_OFFSET.bottom,
                    transform.worldZ,
                    MARGIN.bottom, MARGIN.bottom
                )
        }
        renderTexture(renderContext.matrixStack, transform, TIP, backgroundColor)
        renderTexture(renderContext.matrixStack, rect, texture, backgroundColor)

    }

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (keyCode == Keyboard.LEFT_CONTROL) {
            keepDisplay = true
        }
        return super.onKeyPress(keyCode)
    }

    override fun onKeyRelease(keyCode: KeyCode): NextAction {
        if (keyCode == Keyboard.LEFT_CONTROL) {
            keepDisplay = false
        }
        return super.onKeyRelease(keyCode)
    }

}

@OptIn(ExperimentalContracts::class)
inline fun Element.tip(
    displayDelay: UInt = DELAY.toUInt(),
    padding: Margin = PADDING,
    margin: Margin = MARGIN,
    color: ARGBColor = Theme.TIP.BACKGROUND_COLOR,
    optionalDirections: Iterable<Direction> = Direction.entries,
    scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    this.tip = MouseHoverTip(this, { this.screen() }, displayDelay, padding, margin, color, optionalDirections).apply(scope)
    return this.tip as MouseHoverTip
}

@OptIn(ExperimentalContracts::class)
inline fun Element.tip(
    scope: MouseHoverTip.() -> Unit
): MouseHoverTip {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    this.tip = MouseHoverTip(this).apply(scope)
    return this.tip as MouseHoverTip
}
