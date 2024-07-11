package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.text.Literal
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.screen.Screen

abstract class IGScreenImpl : Screen(Literal("ibuki gourd screen")), IGScreen {

    override val transform: Transform = Transform(Vector2f(0f, 0f), this.width.toFloat(), this.height.toFloat(), true).apply {
        subscribeSizeChange { _, (width, height) ->
            this@IGScreenImpl.width = width.toInt()
            this@IGScreenImpl.height = height.toInt()
        }
    }

    var pauseGame: Boolean = false

    private val children: MutableList<IGWidget> = ArrayList()

    override fun tick() {
        super<IGScreen>.tick()
    }

    override fun shouldPause(): Boolean {
        return pauseGame
    }

    override fun renderDarkening(context: DrawContext) {
        renderDarkening(context, transform.worldX.toInt(), transform.worldY.toInt(), width, height)
    }

    override fun getNavigationFocus(): ScreenRect {
        return ScreenRect(transform.worldX.toInt(), transform.worldY.toInt(), this.width, this.height)
    }

    override fun renderInGameBackground(context: DrawContext) {
        context.fillGradient(transform.worldX.toInt(), transform.worldY.toInt(), this.width, this.height, -1072689136, -804253680)

    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        transform.width = width.toFloat()
        transform.height = height.toFloat()
    }

    override fun children(): List<IGElement> {
        return children
    }

    fun <W : IGWidget> addWidget(widget: W): W {
        children.add(widget)
        widget.transform.parent = { this.transform }
        return widget
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context, mouseX, mouseY, delta)

        for (igWidget in children) {
            igWidget.render(context, mouseX, mouseY, delta)
        }
    }

}