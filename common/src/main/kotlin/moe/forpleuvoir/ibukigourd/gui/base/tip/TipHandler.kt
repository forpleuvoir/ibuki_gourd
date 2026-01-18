package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.render.runWithZOffset
import kotlin.time.Duration
import kotlin.time.TimeSource

object TipHandler : Tickable {

    private val tips: MutableList<Tip> = ArrayList(5)

    private val popList: MutableMap<Tip, TimeSource.Monotonic.ValueTimeMark> = mutableMapOf()

    fun pushTip(parent: () -> Transform, tip: Tip): Tip {
        return if (tip in tips) tip
        else tip.apply {
            tips += this
            init(parent)
            show()
        }
    }

    fun pushTip(duration: Duration, parent: () -> Transform, tip: Tip): Tip {
        return pushTip(parent, tip).apply {
            if (tip in tips) {
                popList[tip] = TimeSource.Monotonic.markNow() + duration
            }
        }
    }

    fun cancelPop(tip: Tip?) {
        if (tip == null) return
        if (tip in popList.keys) {
            popList.remove(tip)
        }
    }

    fun popTip(tip: Tip?) {
        if (tip == null) return
        if (tip in tips) {
            popList[tip] = TimeSource.Monotonic.markNow()
        }
    }

    private fun handlerRemovedTip() {
        tips.removeIf { tip ->
            if (tip in popList.keys) {
                if (popList[tip]!!.elapsedNow() >= tip.setting.hideDelay) {
                    popList.remove(tip)
                    true
                } else false
            } else false
        }
    }

    @JvmStatic
    fun render(guiGraphics: IGGuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        handlerRemovedTip()
        runWithZOffset(IGScreen.currentScreenZOffset + IGScreen.POPUP_Z_OFFSET) {
            tips.forEach {
                it.render(guiGraphics, mouseX, mouseY, delta)
            }
        }
    }

    override fun onTick() {
        tips.forEach {
            it.onTick()
        }
    }

}