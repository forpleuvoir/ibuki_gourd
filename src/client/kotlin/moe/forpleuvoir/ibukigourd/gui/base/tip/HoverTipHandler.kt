package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext

object HoverTipHandler : Tickable {

    private var currentTip: HoverTip? = null

    fun setCurrentTip(parent: () -> Transform, tip: HoverTip) {
        if (this.currentTip == tip) return
        currentTip = tip
        with(tip) {
            init(parent)
            show()
        }
    }

    fun hideCurrentTip() {
        currentTip = null
    }

    @JvmStatic
    fun render(drawContext: IGDrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        currentTip?.render(drawContext, mouseX, mouseY, delta)
    }

    override fun onTick() {
        currentTip?.onTick()
    }

}