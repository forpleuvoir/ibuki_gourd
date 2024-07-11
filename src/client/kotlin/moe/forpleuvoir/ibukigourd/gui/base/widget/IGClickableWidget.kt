//package moe.forpleuvoir.ibukigourd.gui.base.widget
//
//import moe.forpleuvoir.ibukigourd.gui.base.Padding
//import moe.forpleuvoir.ibukigourd.input.Mouse
//import moe.forpleuvoir.ibukigourd.util.soundManager
//import moe.forpleuvoir.nebula.common.ifc
//import net.minecraft.client.sound.PositionedSoundInstance
//import net.minecraft.client.sound.SoundManager
//import net.minecraft.sound.SoundEvents
//import java.util.function.Consumer
//
//abstract class IGClickableWidget(
//    x: Int,
//    y: Int,
//    width: Int,
//    height: Int,
//    padding: Padding = Padding(0),
//) : IGParentWidget(x, y, width, height, padding) {
//
//    override fun forEachElement(consumer: Consumer<IGWidget>) {
//        consumer.accept(this)
//    }
//
//    open fun onClick(mouseX: Double, mouseY: Double) {}
//
//    open fun onRelease(mouseX: Double, mouseY: Double) {}
//
//    protected open fun onDrag(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double) {}
//
//    var clickSound: PositionedSoundInstance? = PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)
//
//    var releaseSound: PositionedSoundInstance? = null
//
//    protected fun playClickSound(soundManager: SoundManager) {
//        clickSound?.let { soundManager.play(it) }
//    }
//
//    protected fun playReleaseSound(soundManager: SoundManager) {
//        releaseSound?.let { soundManager.play(it) }
//    }
//
//    protected open fun isValidClickButton(button: Int): Boolean = button == Mouse.LEFT.code
//
//    protected open fun clicked(mouseX: Double, mouseY: Double): Boolean {
//        return transform.isMouseOvered(mouseX, mouseY)
//    }
//
//    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        if (this.active && this.visible) {
//            super.mouseClicked(mouseX, mouseY, button).ifc { return true }
//            if (isValidClickButton(button)) {
//                if (this.clicked(mouseX, mouseY)) {
//                    this.playClickSound(soundManager)
//                    this.onClick(mouseX, mouseY)
//                    return true
//                }
//            }
//        }
//        return false
//    }
//
//    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        if (this.isValidClickButton(button)) {
//            this.onRelease(mouseX, mouseY)
//            this.playReleaseSound(soundManager)
//            return true
//        } else {
//            return false
//        }
//    }
//
//    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
//        if (this.isValidClickButton(button)) {
//            this.onDrag(mouseX, mouseY, deltaX, deltaY)
//            return true
//        } else {
//            return false
//        }
//    }
//
//}