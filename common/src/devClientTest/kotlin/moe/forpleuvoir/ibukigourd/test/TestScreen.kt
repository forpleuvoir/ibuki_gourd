package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.modifier.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.screen.BoxScreen
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.ColumnListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.overlayMessage
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration.Companion.seconds

private fun modifier(): Modifier {
    var deltaCount = 0
    var fps = 0
    var renderTime = 0.seconds
    return Modifier.padding(8f)
        .tick {
            this as IGScreenImpl
            onTick()
            deltaCount++
            if (deltaCount % 10 == 0) {
                fps = (1.seconds / this.latestFrameRenderTime).toInt()
                deltaCount = 0
            }
            renderTime = this.latestFrameRenderTime

        }
        .renderOverlay { guiGraphics, mouseX, mouseY, delta ->
            this as IGScreenImpl
            val contentBox = contentBox(true)
            onRenderOverlay(guiGraphics, mouseX, mouseY, delta)
            guiGraphics {
                pushBoxOutline(contentBox, Colors.ROSE)
                val texts = listOf(
                    Literal("Screen renderTime:$renderTime").style { color(Colors.AQUA) },
                    Literal("Screen FPS:$fps").style { color(0x00FF00) },
                    Literal("MouseCursor:${MouseCursor.current.name}")
                )
                pushTextLines(texts, contentBox, Alignment.Left, Arrangement.Top)
                val mouse = listOf(
                    Literal("MouseX:$mouseX").style { color(Colors.RED) },
                    Literal("MouseY:$mouseY").style { color(0x00FF00) },
                )
                pushTextLines(
                    mouse,
                    contentBox.copy(height = contentBox.height + 2f),
                    Alignment.Left, Arrangement.Bottom,
                    defaultColor = Colors.ALIEN_GREEN.opacity(.3f),
                    backgroundColor = Colors.BLACK.opacity(.3f)
                )
            }
        }
}

fun TestScreen() = BoxScreen(modifier()) {
    Column(
        modifier = Modifier
            .padding(20f)
            .align(Alignment.Center)
            .renderOverlay { guiGraphics, _, _, _ ->
                this as ColumnWidget
                guiGraphics.pushBoxOutline(contentBox(true), Colors.BLUE)
            },
        verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
    ) {
        RowListWrapped(
            spacing = 3f,
            modifier = Modifier.weight(2).fill(),
            listModifier = { Modifier.weight(1).fill().renderHoveredOutlineBox(Colors.PARCHMENT) }
        ) {
            Button {
                Text("lock", modifier = Modifier.width(230f))
            }
            Button(Modifier.unlockConstraint()) {
                Text("unlock", modifier = Modifier.width(1666f))
            }
        }
        TestColumn()
//        FloatSlider(
//            mutableStateOf(GuiConfig.Screen::defaultBgBlurRadius),
//            0f..25f,
//            textMapper = { Literal("背景模糊:%.2f".format(it)) },
//            modifier = Modifier.minWidth(120f)
//        )
    }


}

fun ColumnScope.TestColumn() = Row(
    modifier = Modifier.renderOverlay { guiGraphics, _, _, _ ->
        guiGraphics.pushBoxOutline(transform, Colors.AQUA)
    }.weight(5),
    horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
) {
    ColumnListWrapped(
        modifier = Modifier.width(120f),
        spacing = 2f,
        listModifier = { Modifier.weight(1) }
    ) {
        Button {
            Text("lock", modifier = Modifier.height(230f))
        }
        Button(Modifier.unlockConstraint()) {
            Text("unlock", modifier = Modifier.height(1666f))
        }
    }
    val icons = listOf(
        IconTextures.CLOSE,
        IconTextures.SEARCH,
        IconTextures.FILTER,
        IconTextures.MINUS,
    )

    Box(
        modifier = Modifier
            .matchSibling()
            .renderOverlay { guiGraphics, _, _, _ ->
                guiGraphics.pushBoxOutline(transform, Colors.AQUA)
            }
    ) {
        Row {
            icons.forEach {
                Icon(it)
                Box(Modifier.width(5f)) {}
            }
        }
    }
    Box(
        Modifier.size(80f, 80f)
            .renderOverlay { guiGraphics, _, _, _ ->
                if (wasMouseOver) guiGraphics.pushBoxOutline(transform, Colors.AQUA)
            }
    ) {
        Icon(IconTextures.CLOSE, modifier = Modifier.align(Alignment.TopLeft))
        Icon(IconTextures.SEARCH, modifier = Modifier.align(Alignment.TopRight))
        Icon(IconTextures.MINUS, modifier = Modifier.align(Alignment.BottomLeft))
        Icon(IconTextures.LOCK, modifier = Modifier.align(Alignment.BottomRight))
        Icon(IconTextures.FILTER, modifier = Modifier.align(Alignment.Center))
    }

    Scroller(
        scrollState = ScrollState().apply {
            amountStep = 1f
            maxAmount = 10f
            barProportion = 0.1f
            amount = 0f
        },
        modifier = Modifier.maxHeight(180f)
    )
    Button(
        modifier = Modifier.height(40f).renderOverlay { guiGraphics, _, _, _ ->
            if (wasMouseOver) guiGraphics.pushBoxOutline(transform, Colors.AQUA)
        },
        horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
    ) {
        click {
            println("按下了测试按钮")
            mc.overlayMessage("按下了测试按钮")
        }
        longPress(10) {
            println("长按了按钮")
        }
        Icon(IconTextures.CLOSE, modifier = Modifier.align(Alignment.Top).maxWidth(16f))
        Icon(IconTextures.SEARCH, modifier = Modifier.align(Alignment.CenterVertically).maxWidth(16f))
        Icon(IconTextures.MINUS, modifier = Modifier.align(Alignment.Bottom).maxWidth(16f))
        Icon(IconTextures.LOCK, modifier = Modifier.align(Alignment.CenterVertically).maxWidth(16f))
        Icon(IconTextures.FILTER, modifier = Modifier.align(Alignment.Top).maxWidth(16f))
    }

}