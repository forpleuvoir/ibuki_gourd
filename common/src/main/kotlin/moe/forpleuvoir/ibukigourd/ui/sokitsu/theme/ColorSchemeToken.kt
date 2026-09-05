package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

/**
 * 系统级颜色 token：指向 [ColorScheme] 上的某个**语义槽位**，而不是具体颜色值。
 *
 * 它是"组件 token 映射表"的右侧取值，位于回退链的倒数第二环：
 * ```
 * 组件部位      ButtonTokens.Container
 *   → 系统 token  ColorSchemeToken.Primary
 *     → 主题槽位   ColorScheme.primary : ColorTone
 * ```
 *
 * 引入这一层的意义：组件不持有"默认色是什么"，只声明"我要哪个语义槽位"。
 * 于是换 [ColorScheme]（明暗主题切换、用户自定义配色）时，
 * 所有组件的默认配色自动跟随，无需逐个组件改默认值。
 *
 * 对应 Material3 的 `ColorSchemeKeyTokens`。
 */
enum class ColorSchemeToken {

    Background,
    OnBackground,

    Surface,
    OnSurface,

    /** 与主 Surface 拉开区分度的容器色，见 [ColorScheme.surfaceVariant]。 */
    SurfaceVariant,
    OnSurfaceVariant,

    Primary,
    OnPrimary,

    /** 主色容器色：主色的浅色调，作"主色家族"的容器底（开关开启态轨道、选中卡片底）。 */
    PrimaryContainer,
    OnPrimaryContainer,

    Secondary,
    OnSecondary,

    Error,
    OnError,
}

/**
 * 把系统 token 解析为当前主题上的具体色板。
 *
 * 这是回退链的**最后一环**，只在"调用点未指定 + 作用域未指定"时才走到，
 * 因此参数固定为已解析的 [ColorScheme]，返回值必定是 [ColorTone.isSpecified] 的色板。
 *
 * 注意返回类型：像素风精灵染色需要完整的色阶家族（outline/dark/base/highlight），
 * 所以这里给的是 [ColorTone] 而非单个 [Color]；内容色场景自行取 [ColorTone.base]。
 */
fun ColorScheme.fromToken(token: ColorSchemeToken): ColorTone = when (token) {
    ColorSchemeToken.Background       -> background
    ColorSchemeToken.OnBackground     -> onBackground
    ColorSchemeToken.Surface          -> surface
    ColorSchemeToken.OnSurface        -> onSurface
    ColorSchemeToken.SurfaceVariant   -> surfaceVariant
    ColorSchemeToken.OnSurfaceVariant -> onSurfaceVariant
    ColorSchemeToken.Primary          -> primary
    ColorSchemeToken.OnPrimary        -> onPrimary
    ColorSchemeToken.PrimaryContainer -> primaryContainer
    ColorSchemeToken.OnPrimaryContainer -> onPrimaryContainer
    ColorSchemeToken.Secondary        -> secondary
    ColorSchemeToken.OnSecondary      -> onSecondary
    ColorSchemeToken.Error            -> error
    ColorSchemeToken.OnError          -> onError
}
