package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.api.Tickable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

open class IGScreen(title: Text) : Screen(title) {
    override fun tick() {
        children().forEach {
            if (it is Tickable) {
                it.tick()
            }
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }

}