package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.process
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.Canvas
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextArea
import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.util.animation.ProgressAnimator
import moe.forpleuvoir.ibukigourd.util.math.bezier.CubicEasing
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds

fun testScreen5() = ColumnScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    var color = Colors.RED

    val animator = ProgressAnimator(1.seconds, onStart = {
        color = Colors.RED
    }, onEnd = {
        color = Colors.LIME
    }, easing = CubicEasing::easeOut)

    Canvas(
        Modifier.size(240f, 10f)
            .process {
                animator.update()
            }
    ) { graphics, f, f1, f2 ->
        graphics {
            pushBox(Box(transform.worldX + (animator.value.getValue() * 240f), transform.worldY, Size(10f, 10f)), color)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5f)) {
        Button {
            Text("Play")
            click {
                animator.play()
            }
        }
        Button {
            Text("Stop")
            click {
                animator.stop()
            }
        }
        Button {
            Text("Replay")
            click {
                animator.replay()
            }
        }
        Button {
            Text("Reset")
            click {
                animator.reset()
            }
        }
        Button {
            Text("Pause")
            click {
                animator.pause()
            }
        }
    }

}


