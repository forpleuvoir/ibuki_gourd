v1.0.0-alpha
  - 版本号由 `0.11.1+alpha` 升至 `1.0.0-alpha`：**UI 渲染栈整体替换**，属破坏性更新，升级前请读末尾的「破坏性变更」
  - 本条目覆盖自 `v0.11.0+alpha`（`26.1.2`）之后的全部改动，共 100+ 次提交
  - `Minecraft`版本更新至`26.2`（Fabric API `0.157.0+26.2` / Loader `0.19.3` / Loom `1.17.19`；NeoForge `26.2.0.59` / moddev `2.0.143`；ModMenu `20.0.1`）
  - 工具链更新至`Kotlin` `2.4.0` / `JVM` `25`；依赖库`nebula`更新至`0.4.0`
  - **【UI 重写】**渲染改用自研`compose-minecraft` `0.1.0`：内嵌 androidx Compose，绘制直连原版`GuiGraphics`；移除`Compose Multiplatform`桌面离屏渲染与`Skia` / `Material3` / `MaterialKolor` / `backdrop`依赖
  - **【UI 重写】**新增像素风组件库`ui/sokitsu`：`Button` / `ColorButton` / `FlatButton`(+`IconButton` / `TextButton`) / `Icon`+`Icons`(38 个`.aseprite`图标) / `Surface` / `Switch` / `Slider`(+`NumberSlider`) / `TextField`(+`NumberField`) / `Text` / `Divider` / `ProgressBar` / `RadioButton`(+`RadioButtonGroup`) / `Scroller`(常规 + flat 细条，`autoHide` / `autoFade`) / `TabStrip` / `TabRow` / `TableLayout`(+`LazyTableLayout`) / `AlertDialog`(+`SimpleAlertDialog`) / `FlexibleDialog` / `DropdownMenu` / `Tooltip` / `Toast` / `UiState`
  - **【UI 重写】**新增主题体系：`SokitsuTheme`入口、语义槽位`ColorScheme`+`ColorSchemeToken`与「调用点 > 组件 token 表 > 主题槽位」回退链、资源包主题 meta`SokitsuThemeMeta`、配置驱动的运行时覆盖`SokitsuThemeOverride`、`ThemeType`+`systemTheme()`系统主题探测
  - **【UI 重写】**新增`.aseprite`运行时程序化图集（`SokitsuAseLoader`→`SokitsuStitcher`→`SokitsuAtlasManager`，随资源重载刷新）；新增`aseprite`子模块（ASE 解析库，独立发布）；`reorderable`改为源码内嵌
  - **【UI 重写】**新增屏幕基座`SokitsuScreen`（`ComposeScreen`薄包装 + 主题 + 按窗口分辨率分档缩放`SokitsuScreenScale` / `SokitsuScreenDefaults`）与模组屏幕 API`ui/ModScreen.kt`（顶栏 + 页签条 + `ModScreenTab` / `ModScreenState`）
  - **【UI 重写】**新增组件：`Selector`(单/多选 + 搜索)、`ColorPicker`(HSV/RGB 逐通道条 + 数值框 + 复制粘贴 + 透明棋盘)、`KeySetter`系列、`BezierCurveEditor`+`BezierCurvePlot`、`ItemIcon`(改走原生`Modifier.minecraftItem`，不再离屏烘焙)、`SokitsuContextMenu`(文本框右键，由`SokitsuTheme`注入)
  - **【UI 重写】**新增编辑浮层骨架`ui/editdialog`：`EditDialog` / `EditDialogContent` / `EditDialogContentList`(拖拽排序) / `EditDialogContentCards` / `DragHandle` / `RemoveButton`+`RemoveConfirmButton`
  - **【UI 重写】**新增全局覆盖层`ui/overlay`：`OverlayService`(按 key 注册常驻内容 + `present`门控 + 帧回调)、`OverlayHost`(自持常驻场景，`GuiRendererOverlayMixin`每帧驱动，与屏幕同档缩放，空闲帧零开销)、`OverlayContainer`；`Toast`改为覆盖层的注册条目，`Tooltip`不再产生布局节点
  - **【配置】**配置 GUI 重建：`ConfigManagerWrapper`(页签导航 + 跨分组搜索 + 面包屑)、`ConfigGroupWrapper`、行骨架`ConfigRowWrapper`(名称 / 控件 / 重置 / tooltip / 悬停高亮)、各类型 wrapper
  - **【配置】**新增对外扩展点：`UIWrappers`(按节点类型或值类型注册整行呈现)与单节点`uiWrapper { }`(空实现 = 该行不出行)
  - **【配置】**新增配置项构造器：`configEnum(name, default, codec)`(自定义枚举`Codec`)、`configVector2i/2f/2d`与`configVector3i/3f/3d`、`configStringPair` / `configPair` / `configPairList`、`configKeyCode`、`configKeybind` / `configToggleKeybind`
  - **【配置】**屏幕与对话框配置组重做：进出场时长 / 偏移 / 淡入淡出、停画世界、解除界面内 60 帧上限`unlimit_framerate`、进出场缓动`easing`与自定义曲线`easing_custom`(配曲线编辑器)、`pause_game`(作为`SokitsuScreen.pauseGame`缺省值)
  - **【配置】**主题配置组回归：`mode`(跟随系统 / 深色 / 浅色 / 自定义)+`custom_scheme`+配色方案编辑器(槽位按钮组 + 亮暗标记)
  - **【配置】**`configToggleKeybind`切换时默认弹一条「所属组 → 配置名 : 开/关」的`Toast`(tag 取配置路径，连按只刷新同一条)
  - **【事件 / 输入】**键盘与鼠标事件支持取消(含按住重复一并吞掉)，新增`KeyEnvironment`(环境限定)与`KeyTriggerTiming`(按下 / 按住 / 长按 / 长按持续 / 松开 / 按下与松开)
  - **【事件 / 输入】**`Keybind` / `KeybindSetting`支持穿透、严格、环境、长按阈值与重复间隔；`InputHandler`提供冲突检测与可注销句柄；键位捕获改为事件驱动
  - **【文本 / i18n】**文本 DSL 与内联样式解析、文本排版工具(`wrapToLines` / `wrapToTextLines` / 尺寸测量)、新增`ThemeLang`；语言文件键整理
  - **【工具】**新增`ColorConvert` / `ColorContrast`(颜色互转与对比色)、`util.codec`(`Codec.identifier`工厂化 + `Codec.ibukigourdIdentifier` + Compose 尺寸编解码`dp` / `size` / `dpSize` / `intSize` / `padding` / `offset`)、`util.math.easing`(`Easing` / `CubicBezier` / `EasingCurve` / `EasingPreset`)
  - **【文档】**`README`(中 / 英)重写为「特性表 + 依赖 + 三分钟上手 + 手册索引」；新增`doc/manual/`开发者手册（12 章：接入 / 配置 / 配置界面 / 指令 / 事件 / 输入 / 文本 / 屏幕与组件 / 覆盖层与提示 / 任务与工具 / 多加载器 / 常见问题）
  - **【工程】**开发期测试屏扩充到 35 个（`SokitsuTestScreen`主菜单 + `igtest`指令）
  - **【破坏性变更】**旧 UI API 全部移除：`openComposeScreen` / `openComposePopupScreen` / `IbukiGourdTheme` / 旧`ComposeScreen` / `SkiaContext` / 旧抽屉式`ModScreen`；改用`SokitsuScreen.open { }`+`SokitsuTheme`+`ui/ModScreen.kt`
  - **【破坏性变更】**`MutableText.withColor` / `withShadowColor`的 Compose 颜色重载移除；`Style.color`扩展改名`Style.nebulaColor`
  - **【破坏性变更】**Cache 配置组（旧`Skia`图集缓存）与`ConfigWrapperLang.cacheUsed` / `cacheLimit` / `cacheMb`移除
  - **【破坏性变更】**依赖坐标随 MC 版本变为`moe.forpleuvoir:ibukigourd-<platform>-26.2`；新版 Loom 无 remap 环节，Fabric 侧用普通`implementation`引入（不再有`modImplementation`）
  - **【破坏性变更】**配置项构造器是`context(ConfigGroup)`函数，调用方模块若报 context 相关编译错误需补上`-Xcontext-parameters`

v0.11.0+alpha
  - `Minecraft`版本更新至`26.1.2`
  - 集成`Compose Multiplatform`桌面渲染引擎，迁移至`Material3` + `MaterialKolor`
  - 实现基于`GuiGraphicsExtractor`的新渲染系统及`Skia`离屏物品渲染
  - 重构配置 GUI，新增`Toast`通知、`ColorPicker`、`DurationSlider`、`Selector`、`SearchPanel`等组件
  - 新增`ECS`事件通道系统
  - 重构输入系统、内联样式文本渲染与翻译系统

v0.10.4+beta
  - 修复了与Modern UI 文本渲染器不兼容的问题
  - 修复了内联样式文本解析器无法正常解析`keybindContent`的问题
  - Gui部分小幅修改,使用`Animator`代替了原来的动画计算方式

v0.10.3+beta
 - 修复布局问题
 - 部分API变动

v0.10.2+beta
 - 线性布局新增了`priority`属性, 用于设置测量的优先级
 - 修复了服务端无法加载模组的问题
 - 优化部分组件布局问题

v0.10.1+beta
- `Minecraft`版本更新至`1.21.11`
- 代码结构优化

v0.10.0+beta
 - `Minecraft`版本更新至`1.21.10`,并且支持`Neoforge`
 - 为屏幕添加了淡入动画
 - 渲染部分代码大幅修改
 - 暂时移除了屏幕模糊设置