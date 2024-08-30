package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.FlatButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.*
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

class DropDownMenuScope(private val owner: IGButtonWidget, private val state: State<Boolean>) : ButtonScope {

    override fun owner(): IGButtonWidget = owner

    internal var dropDownContent: BoxScope.() -> Unit = {}

    fun DropDownContent(dropDownContent: BoxScope.() -> Unit) {
        this.dropDownContent = dropDownContent
    }

    fun toggle() {
        state.setValue(!state.getValue())
    }

}

val DropDownMenuSeparatorColor = Color(0xFFCCCCCC)

fun WidgetContainerScope.DropDownMenu(
    modifier: Modifier = Modifier,
    scope: DropDownMenuScope.() -> Unit
) {
    val expandState = stateOf(false)
    //上面的空余空间,下面的空余空间
    var space = 0f to 0f
    //最大空间的位置 false :up true: down
    var maxSpaceDir = false
    var parentBox = Box.Unspecified
    var onPlaced = {}
    var placedPosition = Vector2f()
    var playSound: () -> Unit = {}
    var dropDownContent: BoxScope.() -> Unit = {}
    Button(
        modifier = Modifier
            .padding(horizontal = 5f, vertical = 4f)
            .placeCompleted {
                maxSpaceDir = transform.worldCenter.y() - (mc.window.scaledHeight / 2f) < 0f
                space = transform.worldTop to mc.window.scaledHeight.toFloat() - transform.worldBottom
                parentBox = transform.asWorldBox
                onPlaced()
            }
            .render { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_BACKGROUND)
                }
            } then modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val dropDownMenuScope = DropDownMenuScope(this.owner(), expandState).apply(scope)
        dropDownContent = dropDownMenuScope.dropDownContent
        val icon = stateOf(WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN)
        playSound = { owner().playClickSound(soundManager) }
        expandState.subscribe {
            icon.setValue(it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN))
        }

        press {
            expandState.toggle()
        }

        Column(
            Modifier.renderHoveredOutlineBox(Colors.AQUA)
        ) {
            ColoredBox(
                DropDownMenuSeparatorColor,
                Modifier
                    .width(1f)
                    .matchSibling()
                    .margin(horizontal = 2.5f)
            )

            Icon(icon, modifier = Modifier.padding(vertical = 2.5f))
        }

    }
    Absolute(
        Modifier
            .active(expandState)
            .visible(expandState)
            .layer(GuiLayer.Pop)
    ) {
        Box(
            Modifier
                .padding(3f)
                .mousePress {
                    onMousePress(it)
                    it.tryUse(!wasMouseOver).onSuccess {
                        expandState.setValue(false)
                        playSound()
                    }
                    it.tryUse()
                }
                .placeCompleted {
                    transform.worldX = placedPosition.x
                    transform.worldY = placedPosition.y
                }
                .renderBackground { context, _, _, _ ->
                    context.batchRenderTextureColored {
                        pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_EXPEND_BACKGROUND)
                    }
                }
        ) {
            //当顶层组件被放置时调用,用于测量展开部分的尺寸,并且计算放置位置
            onPlaced = place@{
                //------------ 计算可放置的X位置 ------------\\


                //------------ 计算可放置的Y位置 ------------\\

                //尝试放在下面
                val (topSpace, bottomSpace) = space
                //对比原来的大小是否能放下
                val greaterThanBottomSpace = owner().transform.height > bottomSpace
                if (!greaterThanBottomSpace) {
                    //能放下
                    owner().apply {
                        measure(Constraints.of(maxHeight = bottomSpace))
                        measureCompleted()
                        //放置于父组件下面
                        //先尝试放置于父组件中心
                        var x = parentBox.center.x() - owner().transform.halfWidth
                        //将位置限制在可防止范围内
                        x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width))

                        placedPosition = Vector2f(x, parentBox.bottom)
                    }
                    return@place
                }
                //放不下,检查最大空间的位置
                if (maxSpaceDir) {
                    //在下面时,强制放在下面
                    owner().apply {
                        measure(Constraints.of(maxHeight = bottomSpace))
                        measureCompleted()
                        //放置于父组件下面
                        //先尝试放置于父组件中心
                        var x = parentBox.center.x() - owner().transform.halfWidth
                        //将位置限制在可防止范围内
                        x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width))

                        placedPosition = Vector2f(x, parentBox.bottom)
                    }
                    return@place
                } else {
                    //在上面时,强制放在上面
                    owner().apply {
                        measure(Constraints.of(maxHeight = topSpace))
                        measureCompleted()
                        //放置于父组件上面
                        //先尝试放置于父组件中心
                        var x = parentBox.center.x() - owner().transform.halfWidth
                        //将位置限制在可防止范围内
                        x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width))

                        placedPosition = Vector2f(x, parentBox.top - owner().transform.height)
                    }
                }
            }
            dropDownContent(this)
        }
    }

}


fun WidgetContainerScope.Spinner(
    options: List<String>,
    initialOption: String = options.first(),
    onChange: (String) -> Unit = {},
    selectedColor: ARGBColor = Colors.BANANA_YELLOW.opacity(.35f),
    modifier: Modifier = Modifier,
) {
    check(initialOption in options) { "initialOption must be in options" }
    val maxWidth = options.maxOf { textRenderer.getWidth(it).toFloat() }
    val selected = stateOf(initialOption)
    selected.subscribe {
        onChange(it)
    }
    DropDownMenu(modifier) {
        Text(selected)
        DropDownContent {
            RowListWrapped(
                modifier = Modifier.padding(0f).disableRenderBackground()
            ) {
                options.forEachIndexed { index, option ->
                    if (index != 0) {
                        ColoredBox(
                            DropDownMenuSeparatorColor,
                            Modifier.height(1f).matchSibling()
                        )
                    }
                    FlatButton(
                        modifier = Modifier.width(maxWidth + 2F),
                        hoveredColor = selectedColor,
                        horizontalArrangement = Arrangement.Left,
                    ) {
                        Text(option)
                        press {
                            selected.setValue(option)
                            this@DropDownMenu.toggle()
                        }
                    }
                }
            }
        }
    }
}