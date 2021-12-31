package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import java.util.function.Function


/**
 * 下拉菜单

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetDropList

 * 创建时间 2021/12/28 18:58

 * @author forpleuvoir

 */
open class WidgetDropList<E>(
	private val items: Collection<E>,
	private val default: E,
	private val stringAdapter: Function<E, String>,
	private val entryAdapter: Function<String, E>,
	val parent: Screen,
	private val pageSize: Int,
	private val itemHeight: Int,
	x: Int,
	y: Int,
	width: Int,
) : PositionParentWidget(x, y, width, itemHeight * (pageSize + 1)) {

	val current: E
		get() = entryAdapter.apply(text.text.string)

	var toggleCallback: ((now: E) -> Unit)? = null

	protected val text: LabelText = LabelText(
		stringAdapter.apply(default).text,
		this.x,
		this.y,
		this.width,
		this.itemHeight,
		LabelText.Align.CENTER_LEFT
	).apply {
		bordColor = Color4i.WHITE
		setOnPressCallback {
			listWidget.visible = !listWidget.visible
			listWidget.active = !listWidget.active
			setInitialFocus(listWidget)
			true
		}
	}

	private val listWidget: WidgetListString = WidgetListString(
		ArrayList<String>().apply { items.forEach { this.add(stringAdapter.apply(it)) } },
		this.parent,
		this.x,
		this.y + this.itemHeight,
		pageSize,
		this.itemHeight,
		this.width
	).apply {
		renderBord = true
		renderBackground = true
		visible = false
		active = false
		onPressCallback = { entry ->
			this.active = false
			this.visible = false
			toggle(entryAdapter.apply(entry.value))
			true
		}
	}

	init {
		addDrawableChild(text)
		addDrawableChild(listWidget)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		return listWidget.mouseScrolled(mouseX, mouseY, amount)
	}

	private fun toggle(entry: E) {
		if (stringAdapter.apply(current) != stringAdapter.apply(entry)) {
			this.text.text = stringAdapter.apply(entry).text
			toggleCallback?.invoke(current)
		}
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {
		builder.put(NarrationPart.TITLE, stringAdapter.apply(default))
	}

	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.HOVERED
	}

}