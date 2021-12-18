package forpleuvoir.ibuki_gourd.config.options.gui

import com.mojang.blaze3d.systems.RenderSystem
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.EntryListWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack


/**
 * 配置列表组件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetListConfigBase

 * 创建时间 2021/12/17 23:34

 * @author forpleuvoir

 */
class WidgetListConfigBase(private val configs: List<ConfigBase>, width: Int, height: Int, top: Int, bottom: Int, itemHeight: Int) :
	EntryListWidget<ConfigBaseEntry>(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight) {

	val hoveredEntry: ConfigBaseEntry?
		get() = this.getHoveredEntry()

	init {
		this.clearEntries()
		configs.forEach {
			this.addEntry(ConfigBaseEntry(this, it))
		}
		setRenderBackground(false)
		setRenderHorizontalShadows(true)
		setRenderHeader(true, 0)
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawRect(x = (width - rowWidth) / 2 + 2, y = top, width = rowWidth, height = height, Color4i(0, 0, 0, 30))
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun renderList(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float) {
		val i = this.entryCount
		val tessellator = Tessellator.getInstance()
		val bufferBuilder = tessellator.buffer
		for (j in 0 until i) {
			val k = getRowTop(j)
			val l = getRowTop(j) + this.itemHeight
			if (l >= top && k <= bottom) {
				val m = y + j * itemHeight + headerHeight
				val n = itemHeight
				val entry: ConfigBaseEntry = getEntry(j)
				val o = this.rowWidth
				var p: Int
				if (isSelectedEntry(j)) {
					p = left + width / 2 - o / 2
					val q = left + width / 2 + o / 2
					RenderSystem.disableTexture()
					RenderSystem.setShader { GameRenderer.getPositionShader() }
					val f = if (this.isFocused) 1.0f else 0.5f
					RenderSystem.setShaderColor(f, f, f, 1.0f)
					bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION)
					bufferBuilder.vertex(p.toDouble(), (m + n + 2).toDouble(), 0.0).next()
					bufferBuilder.vertex(q.toDouble(), (m + n + 2).toDouble(), 0.0).next()
					bufferBuilder.vertex(q.toDouble(), (m - 2).toDouble(), 0.0).next()
					bufferBuilder.vertex(p.toDouble(), (m - 2).toDouble(), 0.0).next()
					tessellator.draw()
					RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f)
					bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION)
					bufferBuilder.vertex((p + 1).toDouble(), (m + n + 1).toDouble(), 0.0).next()
					bufferBuilder.vertex((q - 1).toDouble(), (m + n + 1).toDouble(), 0.0).next()
					bufferBuilder.vertex((q - 1).toDouble(), (m - 1).toDouble(), 0.0).next()
					bufferBuilder.vertex((p + 1).toDouble(), (m - 1).toDouble(), 0.0).next()
					tessellator.draw()
					RenderSystem.enableTexture()
				}
				p = this.rowLeft
				entry.render(matrices, j, k, p, o, n, mouseX, mouseY, this.hoveredEntry == entry, delta)
			}
		}
	}


	override fun getRowWidth(): Int {
		return (this.width * 1).toInt()
	}

	override fun getRowLeft(): Int {
		return left + width / 2 - this.rowWidth / 2
	}

	override fun getScrollbarPositionX(): Int {
		return width - 10
	}

	override fun getRowTop(index: Int): Int {
		return top - scrollAmount.toInt() + index * itemHeight
	}


	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		this.children().forEach {
			it.mouseClicked(mouseX, mouseY, button)
		}
		return true
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		this.children().forEach {
			it.mouseDragged(mouseX, mouseY, button,deltaX, deltaY)
		}
		return true
	}


	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		builder?.put(NarrationPart.TITLE, "")
	}

}