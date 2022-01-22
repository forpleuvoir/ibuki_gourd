package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.common.PositionParentWidget
import forpleuvoir.ibuki_gourd.gui.icon.ArrowIcon
import forpleuvoir.ibuki_gourd.utils.color.Color4i
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
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
	private val hoverTextAdapter: Function<String, List<Text>> = Function { emptyList() },
	parent: Screen,
	private val pageSize: Int,
	private val itemHeight: Int,
	x: Int,
	y: Int,
	width: Int,
) : PositionParentWidget(x, y, width, itemHeight * (pageSize + 1)) {

	init {
		this.parent = parent
	}

	val current: E
		get() = entryAdapter.apply(text.text.string)

	var toggleCallback: ((now: E) -> Unit)? = null

	var expand: Boolean = false
		private set(value) {
			field = value
			onExpandChangedCallback?.invoke(field)
		}

	var onExpandChangedCallback: ((Boolean) -> Unit)? = null

	var renderIcon = true

	var zOffset: Double = 2.0

	protected val text: LabelText = LabelText(
		stringAdapter.apply(default).text,
		this.x,
		this.y,
		this.width,
		this.itemHeight,
		LabelText.Align.CENTER_LEFT
	).apply {
		bordColor = Color4i.WHITE
		setPadding(rightPadding = 16)
		setOnPressCallback {
			listWidget.visible = !listWidget.visible
			listWidget.active = !listWidget.active
			expand = !expand
			setInitialFocus(listWidget)
			true
		}
	}

	private val listWidget: WidgetListString = WidgetListString(
		ArrayList<String>().apply { items.forEach { this.add(stringAdapter.apply(it)) } },
		this.parent!!,
		this.x,
		this.y + this.itemHeight,
		pageSize,
		this.itemHeight,
		this.width
	).apply {
		allChildren().forEach {
			it.setHoverTexts(*hoverTextAdapter.apply(it.value).toTypedArray())
		}
		scrollbar.holdVisible = true
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

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if (!visible) return
		hovered = isMouseOver(mouseX.toDouble(), mouseY.toDouble())
		drawableChildren().forEach {
			if (it is WidgetListString) {
				matrices.translate(0.0, 0.0, zOffset)
				it.render(matrices, mouseX, mouseY, delta)
				matrices.translate(0.0, 0.0, -zOffset)
			} else {
				it.render(matrices, mouseX, mouseY, delta)
			}
		}
		if (renderIcon)
			if (expand) {
				ArrowIcon.Down.render(
					matrices,
					this.x + this.width - 15,
					this.y + (this.itemHeight / 2) - (16 / 2),
					16,
					16,
					false
				)
			} else {
				ArrowIcon.Left.render(
					matrices,
					this.x + this.width - 12,
					this.y + (this.itemHeight / 2) - (12 / 2),
					12,
					12,
					false
				)
			}
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
		return listWidget.mouseScrolled(mouseX, mouseY, amount)
	}

	private fun toggle(entry: E) {
		if (stringAdapter.apply(current) != stringAdapter.apply(entry)) {
			this.text.text = stringAdapter.apply(entry).text
			toggleCallback?.invoke(current)
		}
		expand = listWidget.visible
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {
		builder.put(NarrationPart.TITLE, stringAdapter.apply(default))
	}

	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.HOVERED
	}

}