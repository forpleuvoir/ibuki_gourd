package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.BufferRenderer
import java.util.function.Consumer

object ScreenManager : Tickable {

    var current: Screen? = null
        private set

    fun setScreen(screen: Screen?) {
        current = screen
        BufferRenderer.reset()
        current?.let {
            mc.mouse.unlockCursor()
            KeyBinding.unpressAll()
            InputHandler.unpressAll()
            it.init.invoke()
            mc.skipGameRender = false
            mc.updateWindowTitle()
            return
        }
        mc.soundManager.resumeAll()
        mc.mouse.lockCursor()
        mc.updateWindowTitle()
    }

    fun open(screenScope: Screen.() -> Unit): Screen {
        return screen {
            screenScope(this)
            this.parentScreen = current
            setScreen(this)
        }
    }

    fun open(screen: Screen): Screen {
        screen.parentScreen = current
        setScreen(screen)
        return screen
    }


    @JvmStatic
    fun hasScreen(): Boolean = current != null

    @JvmStatic
    fun hasScreen(action: Consumer<Screen>) {
        current?.let { action.accept(it) }
    }

    override fun tick() {
        current?.tick()
    }

}