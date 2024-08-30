package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonScope
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowList
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListScope
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.*
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick

class DropDownMenuScope(private val owner: IGButtonWidget, private val state: State<Boolean>) : ButtonScope {

    override fun owner(): IGButtonWidget = owner

    internal var items: RowListScope.() -> Unit = {}

    fun items(items: RowListScope.() -> Unit) {
        this.items = items
    }

    fun toggle() {
        state.setValue(!state.getValue())
    }

}

fun WidgetContainerScope.DropDownMenu(
    scope: DropDownMenuScope.() -> Unit,
) {
    val expandState = stateOf(false)
    //上面的空余空间,下面的空余空间
    var space = 0f to 0f
    //最大空间的位置 false :up true: down
    var maxSpaceDir = false
    var parentBox = Box.Unspecified
    var onPlaced = {}
    var placedPosition = Vector2f()
    Button(
        modifier = Modifier
            .padding(4f)
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
            },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val dropDownMenuScope = DropDownMenuScope(this.owner(), expandState).apply(scope)
        val scrollState = ScrollState()
        val icon = stateOf(WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN)

        expandState.subscribe {
            icon.setValue(it.pick(WidgetTextures.DROP_DOWN_MENU_ARROW_UP, WidgetTextures.DROP_DOWN_MENU_ARROW_DOWN))
        }

        press {
            expandState.toggle()
        }

        Box(Modifier
            .width(3f)
            .height(10f)
            .margin(horizontal = 2f)
            .renderHoveredOutlineBox(Colors.AQUA)
            .renderBackground { context, _, _, _ ->
                context.batchRenderTextureColored {
                    pushWidgetTexture(transform, WidgetTextures.DROP_DOWN_MENU_SEPARATOR_VERTICAL)
                }
            })

        Icon(icon)

        Absolute(
            Modifier
                .active(expandState)
                .visible(expandState)
                .layer(GuiLayer.Pop)
        ) {
            Column(
                Modifier
                    .padding(3f)
                    .mousePress {
                        onMousePress(it)
                        it.tryUse(!wasMouseOver).onSuccess {
                            expandState.setValue(false)
                            this@Button.owner().playClickSound(soundManager)
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

                    //先尝试放置于父组件中心
                    var x = parentBox.center.x() - owner().transform.halfWidth
                    //将位置限制在可防止范围内
                    x = x.coerceIn(0f..(mc.window.scaledWidth.toFloat() - owner().transform.width))

                    //------------ 计算可放置的Y位置 ------------\\

                    //尝试放在下面
                    val (topSpace, bottomSpace) = space
                    //对比原来的大小是否能放下
                    val greaterThanBottomSpace = owner().transform.height > bottomSpace
                    if (!greaterThanBottomSpace) {
                        //能放下
                        owner().apply {
                            measure(Constraints.of(maxHeight = bottomSpace))
                            //放置于父组件下面
                            placedPosition = Vector2f(x, parentBox.bottom)
                        }
                        return@place
                    }
                    //放不下,检查最大空间的位置
                    if (maxSpaceDir) {
                        //在下面时,强制放在下面
                        owner().apply {
                            measure(Constraints.of(maxHeight = bottomSpace))
                            //放置于父组件下面
                            placedPosition = Vector2f(x, parentBox.bottom)
                        }
                        return@place
                    } else {
                        //在上面时,强制放在上面
                        owner().apply {
                            measure(Constraints.of(maxHeight = topSpace))
                            //放置于父组件上面
                            placedPosition = Vector2f(x, parentBox.top - owner().transform.height)
                        }
                    }
                }
                RowList(
                    scrollState = scrollState,
                    horizontalAlignment = Alignment.Left
                ) {
                    dropDownMenuScope.items(this)
                }
                Scroller(
                    scrollState = scrollState,
                    modifier = Modifier
                )
            }
        }
    }

}