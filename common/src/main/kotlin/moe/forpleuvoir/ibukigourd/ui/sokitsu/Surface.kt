package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.contentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents

/**
 * 表面容器：Sokitsu 里所有"带背景的容器"的公共底座，语义对齐 Material3 的 `Surface`。
 *
 * 职责（与 M3 同构）：
 * 1. **画背景**：按 [sprite]（九宫格精灵）+ [tone] 绘制容器背景；[tone] 未指定时按
 *    [SurfaceTokens.Container] 解析（调用点 > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone]
 *    作用域 > 组件 token > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme]）；
 * 2. **下发内容色**：经 [ProvideContentColorTextStyle] 把内容色（默认取容器色板的**配对内容色**，
 *    见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone.contentColor]）与 [textStyle]
 *    提供给子树，内部的 Text / Icon 自动取到，无需逐个传色；
 * 3. **承载交互**（可点击重载）：[clickable] + 点击音效，指示（[indication]）绘制在背景之上。
 *
 * 本组件**不**负责：最小尺寸、内边距、语义角色——这些由具体组件决定。
 * M3 的 Button 正是把容器渲染整块委托给 Surface、自己只留这些"按钮语义"的东西，
 * [Button] 同样如此。
 *
 * [sprite] 为 null 时背景**退化为色板 base 的纯色填充**（即 M3 `Surface` 铺 `color` 的默认行为），
 * 见 [SurfaceDefaults.sprite]：图集里暂无通用面板素材，素材补齐后无需改动调用点即可换成精灵。
 *
 * @param tone 容器背景色板，未指定按 [SurfaceTokens.Container] 解析
 * @param contentColor 内容色，未指定取 [tone] 解析后色板的配对内容色
 * @param sprite 背景精灵；null = 用色板 base 纯色填充
 * @param textStyle 下发给子树的文本样式（默认沿用当前 [LocalTextStyle]，即不修改）
 * @param contentAlignment 内容的对齐方式，默认左上（M3 的 Surface 亦为左上，
 *   需要居中的组件如 [Button] 显式传 [Alignment.Center]）
 * @param content 容器内容
 */
@Composable
fun Surface(
    modifier: Modifier = Modifier,
    tone: ColorTone = ColorTone.Unspecified,
    contentColor: Color = Color.Unspecified,
    sprite: SokitsuSprite? = SurfaceDefaults.sprite,
    textStyle: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    SurfaceContainer(
        modifier = modifier,
        interactiveModifier = Modifier,
        tone = tone,
        contentColor = contentColor,
        sprite = sprite,
        textStyle = textStyle,
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * 可点击的 [Surface]：在容器渲染之上附加交互（[clickable] + 点击音效）。
 *
 * 与 M3 的 `Surface(onClick = ...)` 同构——交互由 Surface 承载，
 * 具体组件（[Button]）只负责把状态解析出的色板/精灵/内容色交进来。
 *
 * 修饰器顺序为 `外部 modifier → 背景精灵 → clickable`：
 * 精灵作背景画在最底、点击指示（[indication]）画在其上，
 * 与 [Button] 原先的 `.sokitsuSprite(...).clickable(...)` 顺序一致。
 *
 * @param pressSound 点击音效，null = 静音；默认 [SurfaceDefaults.LocalPressSound]，
 *   组件可用自己的默认值覆盖（如 [ButtonDefaults.LocalPressSound]）
 * @param interactionSource 交互源，不传则内部新建；组件需要收集 pressed/hovered/focused
 *   时应当传入自己持有的实例（[Button] 正是如此）
 * @param indication 点击指示，默认 [LocalIndication]
 */
@Composable
fun Surface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ColorTone = ColorTone.Unspecified,
    contentColor: Color = Color.Unspecified,
    sprite: SokitsuSprite? = SurfaceDefaults.sprite,
    textStyle: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment = Alignment.TopStart,
    pressSound: SoundInstance? = SurfaceDefaults.LocalPressSound.current,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = LocalIndication.current,
    content: @Composable () -> Unit,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    SurfaceContainer(
        modifier = modifier,
        interactiveModifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClick = {
                pressSound?.let { sound -> mc.soundManager.play(sound) }
                onClick()
            },
        ),
        tone = tone,
        contentColor = contentColor,
        sprite = sprite,
        textStyle = textStyle,
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * [Surface] 两个重载的公共实现：解析配色 → 下发内容色 → 画背景 → 叠加交互修饰器。
 *
 * [interactiveModifier] 单独传入而非并入 [modifier]，是为了保证绘制顺序：
 * `外部 modifier` → `背景精灵` → `交互`，使背景处于最底层、点击指示在其之上。
 */
@Composable
private fun SurfaceContainer(
    modifier: Modifier,
    interactiveModifier: Modifier,
    tone: ColorTone,
    contentColor: Color,
    sprite: SokitsuSprite?,
    textStyle: TextStyle,
    contentAlignment: Alignment,
    content: @Composable () -> Unit,
) {
    val resolvedTone = tone.resolve(SurfaceTokens.Container)
    val resolvedContent = contentColor.takeOrElse { resolvedTone.contentColor() }

    ProvideContentColorTextStyle(
        contentColor = resolvedContent,
        textStyle = textStyle,
    ) {
        Box(
            modifier = modifier
                .then(surfaceBackgroundModifier(sprite, resolvedTone))
                .then(interactiveModifier),
            contentAlignment = contentAlignment,
        ) {
            content()
        }
    }
}

/**
 * 容器背景：有精灵时画九宫格精灵，否则**退化为色板 base 的纯色填充**。
 *
 * 纯色填充是 Material3 `Surface` 的默认行为（它铺的是 `color`）；本项目是像素风精灵体系，
 * 理想形态是铺九宫格面板精灵，但图集里暂无通用面板素材（见 [SurfaceDefaults.sprite]），
 * 故暂时用纯色兜底——素材补齐后所有调用点无需改动即可自动换成精灵。
 */
@Composable
private fun surfaceBackgroundModifier(sprite: SokitsuSprite?, tone: ColorTone): Modifier =
    if (sprite != null) Modifier.sokitsuSprite(sprite, tone)
    else Modifier.background(tone.base)

object SurfaceDefaults {

    /**
     * 默认背景精灵：**null** —— 图集里目前只有 button / switch 两套素材，没有通用面板精灵，
     * 因此默认走"色板 base 纯色填充"（M3 `Surface` 的默认行为）；
     * 需要九宫格背景时由调用方显式传入（如 [Button] 传入自己的四态按钮精灵）。
     * 待补一张 `ui/surface` 面板素材后，把默认值改为该精灵即可，调用点无需改动。
     */
    val sprite: SokitsuSprite? = null

    /**
     * 点击音效，与 [ButtonDefaults.LocalPressSound]、
     * [SwitchDefaults.LocalPressSound] 同款（默认 UI 按钮音，传 null 可静音）。
     */
    val LocalPressSound = compositionLocalOf<SoundInstance?> {
        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f)
    }
}
