//package moe.forpleuvoir.ibukigourd.gui.base.screen
//
//import moe.forpleuvoir.ibukigourd.gui.base.Transform
//import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
//import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
//import moe.forpleuvoir.ibukigourd.render.math.Vector2i
//import moe.forpleuvoir.ibukigourd.text.Literal
//import net.minecraft.client.MinecraftClient
//import net.minecraft.client.gui.DrawContext
//import net.minecraft.client.gui.Element
//import net.minecraft.client.gui.ScreenRect
//import net.minecraft.client.gui.screen.Screen
//
//open class IGScreen : Screen(Literal("ibuki gourd screen")), ElementContainer {
//
//    val transform: Transform = Transform(Vector2i(), this.width, this.height, true).apply {
//        subscribeSizeChange { _, (width, height) ->
//            this@IGScreen.width = width
//            this@IGScreen.height = height
//        }
//    }
//
//    var pauseGame: Boolean = false
//
//    private val children: MutableList<IGWidget> = ArrayList()
//
//    override fun tick() {
//        super<ElementContainer>.tick()
//    }
//
//    override fun shouldPause(): Boolean {
//        return pauseGame
//    }
//
//    override fun renderDarkening(context: DrawContext) {
//        renderDarkening(context, transform.x, transform.y, width, height)
//    }
//
//    override fun getNavigationFocus(): ScreenRect {
//        return ScreenRect(transform.x, transform.y, this.width, this.height)
//    }
//
//    override fun renderInGameBackground(context: DrawContext) {
//        context.fillGradient(transform.x, transform.y, this.width, this.height, -1072689136, -804253680)
//    }
//
//    override fun resize(client: MinecraftClient, width: Int, height: Int) {
//        super.resize(client, width, height)
//        transform.width = width
//        transform.height = height
//    }
//
//    override fun children(): MutableList<out Element> {
//        return children
//    }
//
//    fun <W : IGWidget> addWidget(widget: W): W {
//        children.add(widget)
//        widget.transform.parent = { this.transform }
//        return widget
//    }
//
//    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
//        this.renderBackground(context, mouseX, mouseY, delta)
//
//        for (igWidget in children) {
//            igWidget.render(context, mouseX, mouseY, delta)
//        }
//    }
//
//}