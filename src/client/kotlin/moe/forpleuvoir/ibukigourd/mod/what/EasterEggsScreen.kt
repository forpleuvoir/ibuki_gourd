package moe.forpleuvoir.ibukigourd.mod.what

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel

fun EasterEggsScreen(modifier: Modifier = Modifier) = ColumnScreen(
    modifier,
    Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    Button {
        TextLabel("Snake")
        click {
            SankeGame().open()
        }
    }

    Button {
        TextLabel("GameOfLife")
        click {
            GameOfLifeScreen().open()
        }
    }

}