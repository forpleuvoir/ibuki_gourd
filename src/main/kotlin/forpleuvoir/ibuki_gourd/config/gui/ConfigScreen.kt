package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.gui.ScreenBase
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.utils.color.Color4f
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text


/**
 * 配置界面

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 ConfigScreen

 * 创建时间 2021/12/21 15:29

 * @author forpleuvoir

 */
class ConfigScreen(
	private val configTitle: Text,
	private val itemHeight: Int = 24,
	private val pageSize: Int = 10,
	private val configGroup: IConfigGroup
) : ScreenBase("${configTitle.string} => ${configGroup.displayKey.string}".text()) {

	private val topPadding = titleTopPadding.toInt() + MinecraftClient.getInstance().textRenderer.fontHeight + 5

	private val margin: Int = 2

	private var buttonPosY = topPadding + margin

	private var searchBar: TextFieldWidget? = null
	private val searchBarHeight: Int = 15
	private var listWidget: WidgetListConfig? = null
	private var matrices: MatrixStack? = null

	init {
		configGroup.changeCurrent(configGroup)
	}

	override fun init() {
		super.init()
		initConfigGroupButton()
		initSearchBar()
		initListWidget()
	}

	override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		this.matrices = matrices
		super.render(matrices, mouseX, mouseY, delta)
	}

	private fun drawCenterMessage(message: Text) {
		matrices?.let {
			textRenderer.drawWithShadow(
				it,
				message,
				(this.width / 2 - textRenderer.getWidth(message) / 2).toFloat(),
				titleTopPadding,
				Color4f.WHITE.rgba
			)
		}
	}

	private fun initListWidget() {
		searchBar?.let {
			listWidget = WidgetListConfig(configGroup.option(), this, 0, it.y + searchBarHeight + margin, pageSize, itemHeight, this.width)
			listWidget?.setFilter { configEntry ->
				configEntry.config.displayName.string.contains(it.text)
						|| configEntry.config.displayRemark.string.contains(it.text)
						|| configEntry.config.name.contains(it.text)
						|| configEntry.config.remark.contains(it.text)
			}
			listWidget?.setHoverCallback { entry -> drawCenterMessage(entry.config.displayRemark) }
			this.addDrawableChild(listWidget)
		}
	}

	private fun initSearchBar() {
		searchBar = TextFieldWidget(textRenderer, 15, buttonPosY + margin + 20, this.width - 15 * 2, searchBarHeight, LiteralText.EMPTY).apply {
			text = ""
			setMaxLength(65535)
		}
		this.addDrawableChild(searchBar)
	}

	private fun initConfigGroupButton() {
		var posX = 15
		configGroup.all().forEach {
			val button = Button(posX, buttonPosY, it.displayKey) { _ ->
				openScreen(ConfigScreen(configTitle, itemHeight, pageSize, it))
			}.apply {
				setHoverCallback { _ ->
					drawCenterMessage(it.displayRemark)
				}
				active = it != configGroup
				posX += width + margin
				if (x + width > this@ConfigScreen.width - 15) buttonPosY += height + margin
			}
			this.addDrawableChild(button)
		}
	}
}