package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.EntryListWidget
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Predicate


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

	private var configPredicate: Predicate<ConfigBase> = Predicate { true }

	val hoveredEntry: ConfigBaseEntry?
		get() = this.getHoveredEntry()

	init {
		initConfigs()
		setRenderBackground(false)
		setRenderHorizontalShadows(false)
		setRenderHeader(true, 0)
	}

	fun setDefaultFilter() {
		configPredicate = Predicate { true }
	}

	fun setFilter(predicate: Predicate<ConfigBase>) {
		this.configPredicate = predicate
		initConfigs()
	}

	private fun initConfigs() {
		this.clearEntries()
		configs.stream().filter(configPredicate).forEach {
			this.addEntry(ConfigBaseEntry(this, it))
		}
		setSelected(null)
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		RenderUtil.drawRect(x = (width - rowWidth) / 2, y = top, width = rowWidth, height = height, Color4i(0, 0, 0, 30))
		super.render(matrices, mouseX, mouseY, delta)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		scrollAmount -= amount * itemHeight.toDouble()
		return true
	}

	override fun getMaxScroll(): Int {
		return 0.coerceAtLeast(this.maxPosition - (bottom - top))
	}

	override fun renderList(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float) {
		val i = this.entryCount
		selectedOrNull?.renderSelected()
		for (j in 0 until i) {
			val k = getRowTop(j)
			val l = getRowTop(j) + this.itemHeight
			if (l > top && k < bottom) {
				val m = y + j * itemHeight + headerHeight
				val n = itemHeight
				val entry: ConfigBaseEntry = getEntry(j)
				val o = this.rowWidth
				val p: Int = this.rowLeft
				entry.render(matrices, j, k, p, o - 10, n, mouseX, mouseY, this.hoveredEntry == entry, delta)
			}
		}
	}

	fun tick() {
		this.children().forEach {
			if (selectedOrNull == it) {
				(it.tick())
			}
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		this.children().forEach {
			if (selectedOrNull == it) {
				(it.keyPressed(keyCode, scanCode, modifiers))
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		this.children().forEach {
			if (selectedOrNull == it) {
				(it.keyReleased(keyCode, scanCode, modifiers))
			}
		}
		return super.keyReleased(keyCode, scanCode, modifiers)
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		this.children().forEach {
			if (selectedOrNull == it) {
				it.charTyped(chr, modifiers)
			}
		}
		return super.charTyped(chr, modifiers)
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
		RenderUtil.isMouseHovered(x = (width - rowWidth) / 2, y = top, width = rowWidth, height = height, mouseX, mouseY) {
			this.children().forEach {
				it.mouseClicked(mouseX, mouseY, button)
			}
			setSelected(hoveredEntry)
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}


	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		selectedOrNull?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}


	override fun appendNarrations(builder: NarrationMessageBuilder?) {
		builder?.put(NarrationPart.TITLE, "")
	}

}