package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext

fun interface DrawableModifier : Modifier.Element {
    fun applyModify(drawable: IGDrawable)

    override fun tryApplyModify(target: Any) {
        if (target is IGDrawable) applyModify(target)
    }

}

fun Modifier.renderBackground(action: IGDrawable.(IGDrawContext, Float, Float, Float) -> Unit) = this then DrawableModifier { drawable ->
    drawable.renderBackground = { context, mouseX, mouseY, delta ->
        drawable.action(context, mouseX, mouseY, delta)
    }
}

fun Modifier.render(action: IGDrawable.(IGDrawContext, Float, Float, Float) -> Unit) = this then DrawableModifier { drawable ->
    drawable.render = { context, mouseX, mouseY, delta ->
        drawable.action(context, mouseX, mouseY, delta)
    }
}

fun Modifier.renderOverlay(action: IGDrawable.(IGDrawContext, Float, Float, Float) -> Unit) = this then DrawableModifier { drawable ->
    drawable.renderOverlay = { context, mouseX, mouseY, delta ->
        drawable.action(context, mouseX, mouseY, delta)
    }
}