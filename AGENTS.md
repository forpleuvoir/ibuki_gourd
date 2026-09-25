# AGENTS.md

> 本文件为 AI 编程助手（Cursor / Codex / Junie / Jules / Claude 等）提供 IbukiGourd 项目的上下文与协作规范。
> 修改代码前请先阅读本文件。若项目结构发生重大变化，请同步更新。

## 项目概述

**IbukiGourd**（`ibukigourd`）是一个用 **Kotlin** 编写的 Minecraft **多加载器（Fabric + NeoForge）模组库**，本身不提供玩法内容，而是为其它 MOD 提供**前置功能**：

- **配置管理**（客户端 / 服务端，含序列化、自动扫描、保存/加载）
- **配置 GUI**（基于自研 **compose-minecraft**，渲染直连原版 `GuiGraphics`，迁移进行中）
- **指令 DSL**（Brigadier 包装）
- **事件总线**
- **输入系统**（键位 / 鼠标）
- **文本 DSL**（含内联样式文本解析）
- **渲染、任务调度、i18n 等通用工具**

核心抽象（`ConfigManager` / `Config` / `Event` / `EventFactory` / `Initializable` 等）来自**同级依赖库 `moe.forpleuvoir.nebula`**（`nebula-common` / `nebula-config` / `nebula-event`），IbukiGourd 将其适配到 Minecraft 环境。修改这些类型时注意其基类不在本仓库内。

## 技术栈与版本

| 项 | 值 |
|---|---|
| 语言 | Kotlin `2.4.0`（主），少量 Java（mixin） |
| JVM | Java `25` |
| Minecraft | `26.2` |
| Fabric | Loader `0.19.3` / API `0.157.0+26.2` / Fabric Kotlin `1.13.12+kotlin.2.4.0` / Loom `1.17.19` |
| NeoForge | `26.2.0.59` / moddev `2.0.143` / Kotlin for Forge `6.3.0` |
| Mixin | `0.8.5` + MixinExtras `0.5.3` |
| UI | compose-minecraft `0.1.0`（`moe.forpleuvoir:compose_minecraft-{common,fabric,neoforge}-26.2`，自研 Compose，内嵌 androidx compose runtime/ui/foundation/animation，渲染直连 MC `GuiGraphics`，无 material3/skia；旧 Compose Desktop 离屏渲染已移除，新 UI 体系为 `ui/sokitsu`，见下） |
| 依赖库 | `nebula` `0.4.0` |
| 构建工具 | Gradle（Kotlin DSL），版本目录 `gradle/libs.versions.toml` |

版本号集中在 `gradle.properties`（`group` / `mod_id` / `mod_name` / `version` 等）与 `gradle/libs.versions.toml`。`version` 当前为 `0.11.1+alpha`。

## 仓库结构

多加载器标准三段式：**common / fabric / neoforge**。

```
ibuki_gourd/
├── common/                 # 共享逻辑（绝大部分 Kotlin 代码）
│   ├── src/main/kotlin/moe/forpleuvoir/ibukigourd/   # 见下“源码包结构”
│   ├── src/main/java/.../mixin/                       # mixin（含 client 子包）
│   ├── src/main/resources/
│   │   ├── META-INF/accesstransformer.cfg             # NeoForge AT
│   │   ├── ibukigourd.classtweaker                    # Fabric access widener
│   │   ├── icon.png                                   # mod 图标
│   │   └── assets/ibukigourd/{lang,shaders,texture,sokitsu_atlas}  # 文案 / 着色器 / ASE 素材 / 图集定义
│   ├── src/devOnly/                                   # 仅开发环境运行的测试代码（不发布）
│   │   ├── kotlin/.../test/                           # 测试屏（SokitsuTestScreen 主菜单及分屏）、测试配置、测试指令
│   │   ├── resources/META-INF/services/               # 开发期 ModInitialization 注册
│   │   └── lang/                                      # 测试配置文案
│   └── build.gradle.kts                               # id("multiloader-common") + neoforgedModDev
├── fabric/                # Fabric 加载器实现
│   ├── src/main/kotlin/.../FabricIbukiGourd(.kt)      # ModInitializer 入口
│   ├── src/main/kotlin/.../FabricIbukiGourdClient     # client 入口
│   ├── src/main/kotlin/.../platform/FabricPlatformHelper
│   ├── src/main/kotlin/.../compat/ModMenuImpl         # ModMenu 集成（接 ibukiGourdModScreen）
│   ├── src/main/kotlin/.../fabricevent/ReloadListenerRegistry  # 资源重载监听注册
│   ├── src/main/resources/META-INF/services/          # PlatformHelper 注册
│   ├── src/main/resources/fabric.mod.json             # 入口 + custom.ibukigourd.package 元数据
│   ├── src/main/resources/ibukigourd.fabric.mixins.json
│   └── build.gradle.kts                               # id("multiloader-loader") + fabricLoom
├── neoforge/              # NeoForge 加载器实现
│   ├── src/main/kotlin/.../NeoforgeIbukigourd(.kt)    # @Mod 入口（class）
│   ├── src/main/kotlin/.../NeoforgeIbukigourdClient
│   ├── src/main/kotlin/.../platform/NeoforgePlatformHelper
│   ├── src/main/kotlin/.../neoforgeevent/{CommandRegistry,ReloadListenerRegistry}.kt
│   ├── src/main/resources/META-INF/{neoforge.mods.toml,services/}
│   ├── src/main/resources/ibukigourd.neoforge.mixins.json
│   └── build.gradle.kts                               # multiloader-loader + neoforgedModDev
├── aseprite/              # ASE（Aseprite）文件解析库（纯 Kotlin JVM 模块，零第三方依赖，独立发布）
│   ├── src/main/kotlin/moe/forpleuvoir/ibukigourd/asetools/   # Ase / AseParser / AseRenderer
│   └── src/test/                                      # 单元测试（仓内唯一纳入版本管理的测试源集）
├── reorderable/           # Reorderable v3.0.0 源码内嵌（Apache-2.0），包名保持 sh.calvin.reorderable
│   └── src/main/kotlin/sh/calvin/reorderable/         # Lazy 列表 / 网格 / 交错网格 / 非 Lazy 列表四套 API
├── buildSrc/
│   ├── build.gradle.kts                               # 预编译 Groovy 插件 + Kotlin/Compose Gradle 插件
│   └── src/main/groovy/
│       ├── multiloader-common.gradle                  # 公共：kotlin/compose 插件、Java25、processResources 模板、publishing
│       └── multiloader-loader.gradle                  # 加载器侧
├── doc/                   # 文档用图（logo、截图）
├── resources/             # 素材源文件（.aseprite / .blend，不参与构建）
├── gradle/libs.versions.toml                          # 版本目录
├── gradle.properties                                  # mod 元数据占位符
├── settings.gradle.kts                               # include("aseprite","reorderable","common","fabric","neoforge")
├── build.gradle.kts                                  # 顶层：info.toml 生成 + 发布/构建任务
├── ibukigourd.info.toml                               # 给 shields.io 用的版本信息（构建生成，勿手改）
├── README.md / README-ENG.md                          # 使用说明（含接入示例）
├── CHANGE_LOG.md / TODO.md / NOTICE.md / AGENTS.md    # 变更/计划/许可/协作规范
└── .gitignore
```

> `runs/`、`build/`、`modJar/`、`.gradle/`、`.idea/`、`.kotlin/` 等已在 `.gitignore` 中，勿提交。
> `src/test/**` 默认被忽略；开发期测试代码放在 `src/devOnly/`（`aseprite/src/test/` 除外，该模块测试纳入版本管理）。

## 源码包结构（`common/.../moe/forpleuvoir/ibukigourd/`）

| 包 | 职责 |
|---|---|
| `(根)` | `IbukiGourd`（common 入口 `object`，`MOD_ID="ibukigourd"`）、`IbukiGourdClient`（client 入口） |
| `api` | 框架 SPI：`Tickable`、`ClientResourceReloaderListener` |
| `command` / `command.dsl` | Brigadier 包装；DSL：`ArgumentScope`、`RequiredArgumentScope`、`createCommand`、`registerCommand`、`@CommandDslMark` |
| `config` / `config.item` | 配置框架：`ModConfigManager`、`ModConfigHandler`、`ClientModConfigManager/Handler`、`ServerModConfigManager/Handler`（另含 `ConfigExtensions`、`LoggerExceptionHandler`）；配置项构造器 `ConfigKeyBind` / `ConfigVector` / `ConfigPairList` / `ConfigEnum` |
| `event` / `event.events.{client,server}` | 事件（基于 nebula `EventFactory`）：`ClientLifecycleEvent`、`ClientTickEvent`、`ServerLifecycleEvent`、`ClientCommandRegistrationEvent`、`ServerCommandRegistrationEvent`、`event.events.client.input` 下的 `MouseEvent` / `KeyboardEvent` 等；`CancellableContext` |
| `input` | `Keybind` / `KeybindSetting` / `KeyCode` / `KeyTriggerTiming` / `KeyEnvironment` / `Keyboard` / `MouseButton` / `MouseCursor` / `InputHandler` |
| `lang` | `IGLang`（i18n 键命名空间中心）、`MiscLang` / `ColorLang` / `ConfigWrapperLang` / `InputLang`、`TranslationRecorder` |
| `mod` / `mod.config` | IbukiGourd 自身内容：配置 `IGConfig`（`Gui` 组下 `Theme` / `Toast` / `Scroller` / `Screen` / `Dialog`，以及 `quickActionKeyCode` / `hideActionKeyCode` / `openScreen`）；自身屏幕 `IbukiGourdModScreen.kt`（`ibukiGourdModScreen` / `openIbukiGourdModScreen`：顶栏 + 彩蛋触发 + 页签条，**配置页是其中一页**；ModMenu / NeoForge 模组列表的"配置"按钮也开它） |
| `platform` / `platform.services` | 多加载器抽象：`Services`（`java.util.ServiceLoader` 加载 `PlatformHelper` + 全部 `ModInitialization`）、`PlatformHelper`、`INITS` / `PLATFORM` |
| `render` / `render.extension{,.state,.texture}` | 渲染辅助：`BaseExtension`、`GuiGraphicsExtractorAccessor`、`IGRenderPipelines`；GuiGraphicsExtractor 扩展（`CircleDrawer`、矩形/文本/blit/misc 扩展、`AnchorPosition`）；渲染状态（`IGBlitRenderState` / `IGTiledBlitRenderState` / `ColoredBoxRenderState` / `GuiTextRenderStateExtensions` / `ItemRenderStateExtension`）；纹理与 UV（`IGTexture`、`TextureInfo`、`TextureUVMapping` / `UVMapping`、九宫格 `Corner`） |
| `task` | 调度：`TickTask` / `TickTaskScheduler` / `ClientTickTaskScheduler` / `TaskExecutor` / `SimpleTaskExecutor` |
| `text` / `text.style` / `text.inlinestyletext{,.modifier}` | 文本 DSL：`TextDSL` / `Texts` / `TextDslMark` / `StyleDSL` / `HoverEventDsl` / `InlineStyleTextParser` + `ColorModifier` / `ClickEventModifier` / `HoverEventModifier` / `DecorationModifier` / `LegacyChatFormattingModifier` / `TextContentModifier` / `TextModifier` 等；另有 `TextSizeSupplier`、客户端文本扩展 |
| `ui.keybind` | 按键绑定编辑（**非 sokitsu 包**，用 sokitsu 组件拼装）：`KeySetter.kt` —— `KeyCodeSetButton`（单键捕获）/ `KeybindSetButton`（组合键，事件驱动捕获、捕获期取消事件吞键（含按住重复）、捕获气泡 `tooltip(pinned = true)` 钉住）/ `KeybindSettingSetButton` + `KeybindSettingColumn`（穿透 / 严格 / 环境 / 触发模式 / 长按阈值 / 重复间隔）/ `Keybind.hoverText()`（冲突提示） |
| `ui.item` | 非主题 UI 原子：`ItemIcon`（MC 物品图标 —— `Modifier.minecraftItem` 原生物品绘制 + `Modifier.minecraftTooltip` 原版 tooltip + `graphicsLayer` 悬停放大 + `BasicText` 数量叠层；布局尺寸即绘制尺寸） |
| `ui`（根） | `ModScreen.kt` —— **公共模组屏幕 API**：`ModScreen`（两个重载：缺省顶栏 = `title` / `icon` / `headerActions` 三个可组合槽位，或整个顶栏由调用方给）+ `ModScreenTab` / `ModScreenState` / `rememberModScreenState` / `ModScreenHeader` / `ModScreenIcon` / `ModScreenDefaults`；页签条走 `TabStrip`（上朝向）铺主题全屏面板素材，不绑定具体模组 |
| `ui.configwrapper` | 配置 GUI（**已重建**，作为模组屏幕 `mod/IbukiGourdModScreen.kt` 的一页）：页面骨架 `ConfigManagerWrapper`（搜索栏 + 分组导航）/ `ConfigGroupWrapper` / 行骨架 `ConfigRowWrapper`（名称 + 控件 + 重置 + tooltip）/ `ConfigUIWrapper` + `UIWrappers` 类型注册表（按谓词分发整行，节点可用 `uiWrapper` 指定专用实现）/ `ConfigElementEditor`（编辑弹窗内按值的运行时类型取控件，不带行骨架）/ `ConfigRowTheme` / `ConfigState`（`Config` → Compose `State`）；各类型 wrapper：`Primitive`(Bool/Int/Long/Float/Double) / `String` / `Enum` / `Duration` / `Vector` / `Color` / `Keybind` / `List` / `Map` / `BezierCurve` |
| `ui.editdialog` | 编辑浮层骨架（**非 sokitsu 包**，用 sokitsu 组件拼装）：`EditDialog`（可编辑副本，确认时把快照交回）/ `EditDialogContent`（表头 + 浮动新增按钮 + 正文，含 `EditDialogContentHeader` / `EditDialogContentDefaults`）/ `EditDialogContentList`（表格式条目容器：可拖拽排序、右侧 flat 细条滚动条、挂钩 `KeyedListState`）/ `EditDialogContentCards`（卡片网格容器：头部 = 拖拽手柄 + 尾部操作组，可指定列数）/ `DragHandle` / `RemoveButton` + `RemoveConfirmButton`（弹确认，按住快速动作键跳过） |
| `ui.colorpicker` | `ColorPicker` / `ColorChannelSlider` / `Checkerboard` / `ColorPickButton`（HSV / RGB 页签 + 通道条 + alpha 条 + 透明棋盘 + 色值复制粘贴） |
| `ui.selector` | `Selector` / `SelectorSelection` / `SelectorTrigger`(+Theme) / `SelectorExpanded` / `SelectorExpandStyle`（单/多选共用展开体；`searchFilter` 非空时展开体内置搜索栏；载体按下拉菜单 / 弹窗分发） |
| `ui.curve` | 缓动曲线编辑器：`BezierCurveEditor` / `BezierCurvePlot`(+Theme)（预设行 + 四点编辑 + 曲线预览，被 `BezierCurveConfigWrapper` 消费） |
| `ui` / `ui.sokitsu{,.theme,.draw,.texture{,.atlas},.menu,.toast,.tooltip}` | 新 UI 体系（Sokitsu，像素风）**组件**：`Button` / `ColorButton` / `FlatButton`(+`IconButton` / `TextButton`) / `Icon`(+`Icons`) / `Surface` / `Switch` / `Slider`(+`NumberSlider`) / `TextField`(+`NumberField`) / `Text` / `Divider` / `ProgressBar` / `RadioButton`(+`RadioButtonGroup`) / `Scroller`（常规 + flat 细条，`autoHide` / `autoFade`；滚动源含 `LazyListState` / `LazyGridState` 适配器）/ `TabStrip` / `TableLayout`(+`LazyTableLayout`：列宽支持权重与上下限、固定表头排在列表之外、`listTrailing` 槽位) / `AlertDialog`(+`SimpleAlertDialog`) / `FlexibleDialog`（`screenPadding` / `maxHeight`）（配 `*Theme` 组件 token 取色，颜色走「调用点传参 > 组件 token 表 > 主题槽位」回退链）；**屏幕**：`SokitsuScreen`（`ComposeScreen` 薄包装，套 `SokitsuTheme` + `SokitsuScreenRoot` 并按窗口分辨率选 `SokitsuScreenScale`，同时下发 `LocalDensity` 与 `pixelScale`）；**交互状态**：`UiState`（`disabled > pressed > (hover ∪ focused) > normal`）；`theme`：`SokitsuTheme` 入口、`ColorScheme` + `ColorSchemeToken`（语义槽位）与 `TokenResolution`（回退链解析）、`ContentColor` / `Typography` / `SokitsuIndicationNodeFactory` / `SokitsuTextSelectionColors`、`ThemeType` + `systemTheme()`、`SokitsuThemeMeta(+Loader)` 资源包主题 meta；`draw`：`sokitsuSprite` 精灵绘制 + `BubblePanel` 气泡体及其渲染插件；`texture.atlas`：运行时程序化图集（`SokitsuAseLoader` → `SokitsuStitcher` → `SokitsuAtlasManager`）；`menu`：`DropdownMenu`(+`DropdownMenuItem`) / `SokitsuContextMenu`；`toast`：全局常驻提示（自持场景，经 `GuiRendererToastMixin` 每帧驱动）；`tooltip`：`Tooltip` / `BasicTooltip`（经 `LocalPopupHost` 注册到场景根，**不产生布局节点**） |
| `util` / `util.math{,.bezier,.easing}` / `util.codec` | `ModLogger` + `logger()` 扩展；向量扩展（`Vector2f/2d/2i/3f/3d/3i`）；`util.math.easing`：`Easing` + 11 条曲线实现（sine/quad/cubic/quart/quint/expo/circ/back/bounce/elastic/linear）、`CubicBezier`、`EasingCurve` + `EasingDirection`（组件 meta 用）、`EasingPreset`（配置用）；`util.math.bezier`：`BezierUtil`；`util.codec`：`IdentifierCodec` / `UnitCodec`；`ColorContrast` / `ColorConvert`、`PackScanner`、`PageHelper`、`FixedSizeQueue`、`LateInitValue`、`NebulaOps`、`SimpleResourceReloaderListener` |

### `ui` 包现状（Sokitsu）

旧的 Compose Desktop 离屏渲染 UI（`preset` / `overlay` / `icon` / `scene` / `skia` / `widget` 等子包，以及 `mod/ui/` 屏幕、`mod/waht/` 彩蛋游戏）已随迁移移除，对应的 `ModScreen` / `SkiaContext` 等旧 API 一并删除。注意 `ui/configwrapper/` 现已存在，但它是基于 sokitsu 组件**重建的新实现**，与迁移前的同名包无关。

新 UI 体系 **`ui/sokitsu/`** 已基于 compose-minecraft 落地，屏幕基座是 `SokitsuScreen`（`ComposeScreen` 的薄包装：套 `SokitsuTheme` + `SokitsuScreenRoot`，按窗口分辨率选一档 `SokitsuScreenScale` 后下发 `LocalDensity` 与主题 `pixelScale`）；IbukiGourd 自身屏幕见 `mod/IbukiGourdModScreen.kt`（模组屏幕：`ui/ModScreen.kt` + 顶栏彩蛋触发；配置页 = 主题 + 底色面板 + `ConfigManagerWrapper`，是它的第一页）。

- **像素风渲染**：`LocalSokitsuPixelScale` 整数放大（1 逻辑像素 → N×N 屏幕像素块，缺省 3），素材密度（@1x/@2x）与之正交；`SokitsuScreenScale` 把「Compose 密度 + pixelScale」打包成一档，两者必须同向变化才不会让组件外框与框内素材对不上。
- **主题**：`SokitsuTheme` 入口；色板为 `ColorScheme`，组件不持有具体颜色、只声明语义槽位 `ColorSchemeToken`，由 `TokenResolution` 按「调用点传参 > 组件 token 表 > 主题槽位」解析；亮/暗由 `ThemeType`（Light/Dark/Unknown）表达，`systemTheme()` 子进程探测系统主题（Windows 注册表 / macOS `defaults` / Linux `gsettings`），探测失败返回 `Unknown`、按浅色收敛；`ContentColor` / `Typography` / `SokitsuIndicationNodeFactory` / `SokitsuTextSelectionColors` 分别接入内容色、字体、指示器与文本选区。
- **资源包驱动**：`SokitsuThemeMeta`（亮/暗 section + pixelScale + 组件 uiMeta，缺槽回落内置工厂）与运行时程序化图集（ASE 素材 → `SokitsuStitcher` 拼合），均随资源重载整体刷新。主题 meta 文件为 `assets/ibukigourd/sokitsu_meta.json`，**库内默认不提供**——它只是资源包自定义的入口，缺失时全部走内置默认值。
- **素材着色**（两个**正交**维度，均为逐图层标注）：合成策略 `tint`（`TextureTintMode`：`Mask` 纯色替换 / `Multiply` 槽位色 × 灰度 / `Passthrough` 直出 → 决定渲染管线）× 颜色来源 `level`（`tone` 组件主色 / `shadow` 黑 / `outline` 描边色 / 任意 `ColorScheme` 槽位名 / `none` 白 → 决定顶点色）。
- **图集约定**：定义 `assets/<ns>/sokitsu_atlas/<atlasId>.json`；扫描目录 `texture/sokitsu/<atlasId>/`；纹理 id = 文件路径去掉 `texture/sokitsu/` 前缀与 `.aseprite` 后缀（如 `icon/add`）。现有图集定义 `ui` / `icon`；`SokitsuAtlasManager.DEFAULT_ATLAS_ID` 为 `sokitsu`，库内未提供其定义文件（同主题 meta，留给资源包）。
- **按钮与图标**：`Button` / `ColorButton`（Button 薄包装：底色由调用方给定**具体颜色**而非主题槽位，内容色按底色亮度取纯黑/纯白，自有 `color_button` meta 段）/ `FlatButton`（**无常驻背景**的按钮底座：只在"当前状态有素材"时渲染背景，`normal` / `disabled` 无素材即整块透明（不能复用 `Surface`，其 `sprite=null` 会退化成纯色填充）；内容色按 meta 的 `contentBlend` 逐状态系数向黑白混合、禁用态按 `disabledBlend` 向背景混合；`IconButton` / `TextButton` 基于它，各有独立 meta 段；两者内容都是**可组合槽位**（`@Composable RowScope.() -> Unit`，不是 `String` 参数），文字类型不受限（`String` / `AnnotatedString` / `Component`，i18n 文案传 `MiscLang.xxx` 即可），文字 + 图标混排也直接支持）/ `Icon` + `Icons`（**像素图标集**：38 个 `.aseprite` 于 `texture/sokitsu/icon/`，独立图集 `icon`；`Icons.Add` 式属性常量 + `all` / `byId`，`Icon` 尺寸 = 素材尺寸 × `scale`（缺省 `pixelScale`），`tint` 缺省跟随 `LocalContentColor`）。
- **容器与装饰**：`Surface` / `Divider`（纯色矩形，不依赖素材）/ `ProgressBar`（轨道 + 填充两个纯色矩形，不做动画）/ `Scroller`（`Vertical` / `Horizontal` 常规版 + `*FlatScroller` flat 细条版；`autoHide` 无滚动空间时完全不组合，`autoFade` 非活动时降可见度；滚动源经 `ScrollerAdapter` 解耦，含 `LazyListState` / `LazyGridState` 两种适配器）/ `TabStrip`（页签条 + 面板，按 `TabStripPlacement` 选素材）/ `TabStripTab` / `TableLayout` + `LazyTableLayout`（`TableLayoutScope` 的 `column` / `rows` / `spanItem` DSL：列宽可取固定值或权重并用 min / max 夹住，表头默认居中且可钉在列表之外、`listTrailing` 给列表体右侧挂附加内容）。
- **输入与选择**：`Switch` / `Slider`（「凹槽轨道 + 按进度裁剪的填充」两张精灵叠放，`label` 槽内容画两遍、各裁到进度边界内外以分用两种内容色）+ `NumberSlider`（类型化入口：Int/Long/Float/Double/Duration/Percent）；`TextField`（凹槽背景精灵 + `BasicTextField`，聚焦/错误描边经 outline 层染色）+ `NumberField`（文本 ↔ 数值双向换算与钳制）；`RadioButton`（按 index/count/RTL 解析 left/center/right/single 分段纹理）+ `RadioButtonGroup`（`item { }` DSL 自动编号）。
- **弹窗与列表**：`AlertDialog`(+`SimpleAlertDialog`)（出入场动画由平台 `Dialog` 的图层快照重放承担，调用方用 `if (show)` 控制组合）/ `FlexibleDialog`（宽度由内容决定，`usePlatformDefaultWidth = false`；`screenPadding` 给面板与窗口留边、`maxHeight` 给高度上限）。编辑浮层骨架（`EditDialog` / `EditDialogContent` / `EditDialogContentList` / `EditDialogContentCards` / `DragHandle` / `RemoveButton` / `RemoveConfirmButton`）见 `ui.editdialog`。
- **浮层与提示**：`DropdownMenu`(+`DropdownMenuItem`)（无状态 API + 便利 `DropdownMenuState`，锚点经 `Modifier.dropdownMenuAnchor` 捕获，面板用共享 `BubblePanel`）/ `SokitsuContextMenu`（由 `SokitsuTheme` 注入 `LocalContextMenuRepresentation`）/ `Toast`（全局常驻，自持场景与渲染器，经 `GuiRendererToastMixin` 每帧驱动，HUD / 原版界面 / Compose 屏幕之上都可见）/ `Tooltip` + `BasicTooltip`（经 `LocalPopupHost` 注册到场景根，**不产生布局节点**，`pinned` 时跳过悬停延迟直接展示）/ `UiState`（`disabled > pressed > (hover ∪ focused) > normal`）。
- **devOnly 测试资产**：`SokitsuTestScreen` 主菜单下 30+ 分屏（颜色主题 / 按钮 / 图标 / 滚轮 / 选择器 / 表格 / 页签 / 曲线 / 配置包装器等），另有图集校验屏 `AtlasTestScreen` 与开发指令 `igtest`。

## 关键入口点

- **common 入口**：`IbukiGourd.init()` —— 遍历 `INITS`（平台 `ModInitialization`，经 ServiceLoader 收集）+ 本地 `inits`（如 `ServerModConfigHandler`）调用 `init()`。
- **client 入口**：`IbukiGourdClient.init()` —— 注册 `ClientModConfigHandler` 与 `IGConfig`；向 `MinecraftRenderPlugins` 注册 `SokitsuSpritePlugin` / `SokitsuBubbleSpritePlugin`（精灵与气泡体绘制）；`SokitsuAtlasManager` / `SokitsuThemeMetaLoader` 作为 `ClientResourceReloaderListener` 随资源重载整体刷新。
- **fabric**：`FabricIbukiGourd : ModInitializer`（委托 `IbukiGourd.init()`），`FabricIbukiGourdClient`，`compat/ModMenuImpl`（模组列表的"配置"按钮接 `ibukiGourdModScreen`），`fabricevent/ReloadListenerRegistry`。入口在 `fabric.mod.json`。
- **neoforge**：`@Mod(IbukiGourd.MOD_ID) class NeoforgeIbukigourd`，在 `FMLCommonSetupEvent` 调 `IbukiGourd.init()`；`NeoforgeIbukigourdClient` 另有 `neoforgeevent/{CommandRegistry,ReloadListenerRegistry}`。
- **平台抽象**：`Services` 通过 `ServiceLoader` 解析 `PlatformHelper`（fabric/neoforge 各自实现）与所有 `ModInitialization`。`PlatformHelper.getIGModClasses()` 反射扫描各 MOD 元数据中的 `package` 键（fabric 为 `custom.ibukigourd.package`，neoforge 为 `modproperties.$modId.package`），加载其 KClass（跳过 `.mixin` 包）——**这是 IbukiGourd 发现消费方 MOD 中被注解的配置/屏幕类的机制**。

## 构建与常用任务

> Windows 默认 shell 为 `cmd.exe`，请使用 `gradlew.bat`（PowerShell 下用 `.\gradlew`）。
>
> 下面这些 Gradle 命令是给**人**（手工构建 / 发布）用的。**AI 助手一律不得执行**——助手侧的编译、构建与检查只能走 IntelliJ IDEA MCP 的原生能力，见「IntelliJ IDEA MCP 与验证」。

```bash
# 构建 fabric + neoforge 的 jar，输出到 modJar/<mc>/<version>/
gradlew.bat buildAllModJar

# 发布（需仓库凭据）
gradlew.bat publishModToSnapshotsRepository   # 快照仓库 maven.forpleuvoir.moe/snapshots
gradlew.bat publishModToReleasesRepository    # 发布仓库 maven.forpleuvoir.moe/releases
gradlew.bat publishModToLocalRepository       # Maven Local

# 单模块构建/测试
gradlew.bat :fabric:build
gradlew.bat :neoforge:build
gradlew.bat :aseprite:test
```

- 开发期运行配置由 `fabric`/`neoforge` 的 `build.gradle.kts` 中的 `runs{}` 定义：fabric 为 `client` / `server`（client 用 `devOnly` 源集），neoforge 为 `client` / `server` / `data`；运行目录 `runs/client`、`runs/server`。
- client dev 运行可由环境变量 `mcName` / `mcUUID` 指定测试账号。
- `ibukigourd.info.toml` 由根 `build.gradle.kts` 自动生成，**勿手动编辑**。

## 编码规范

- **包名根**：`moe.forpleuvoir.ibukigourd.*`；同级库 `moe.forpleuvoir.nebula.*`。新增代码务必放进正确的子包，不要创建新顶层包除非确有必要。
- **Kotlin 风格**：
  - 用 `object` 单例做门面/Handler（如 `IbukiGourd`、`ClientModConfigHandler`、`Services`、`IGLang`、`Icons`）。
  - 广泛使用 **DSL**：`@DslMarker`（`CommandDslMark` / `TextDslMark`）、scope 类（`ArgumentScope` / `RequiredArgumentScope`）、顶层入口函数（`registerCommand`、`configKeybind`）。
  - 大量使用 **context receivers / parameters**：`context(CommandDispatcher<S>)`、`context(ModConfigManager)` 等（编译开关 `-Xcontext-parameters` 已在 `multiloader-common.gradle` 启用）。
  - 用扩展函数补充能力（`logger()`、向量运算、`translateText`）。
- **GUI 基于 compose-minecraft**：`@Composable` 函数、`Modifier` 链等 compose API 由 `moe.forpleuvoir.compose_minecraft` 提供（内嵌 androidx compose 类，渲染直连原版 `GuiGraphics`）。旧 Compose Desktop（Material3 / Skia 离屏渲染）代码已清理，新 UI 以平台 `ComposeScreen` / `ComposeScreenDefaults` / `ScreenAnimation` 为基座，本仓再包一层 `ui/sokitsu/SokitsuScreen`（主题 + 分辨率缩放）。
- **异步**：`kotlinx.coroutines`（`runBlocking`、`ioLaunch`、`ioAsync`、`awaitAll`）；计时用 `kotlin.time.Duration`。
- **注释与文档用中文**，与现有代码、commit message、README 主语言保持一致。
- **注释只解释当前代码，不写入对话/元信息**：注释只描述技术事实（方法行为、参数含义、取值/单位、边界与回落策略），且只针对**当下这段代码**做说明。禁止写入对话性/过程性内容——不写"为什么这么改""之前实现有 bug""与其他实现的对比""本实现不做什么/与 X 同款/一致"这类来龙去脉与对比八卦；这类信息属于会话与文档，不属于代码注释。
- **i18n**：新增界面文案走 lang key，统一登记到 `IGLang` 子对象，资源文件在 `assets/ibukigourd/lang/`。
- **翻译用词规范**（zh_CN）：
  - **keybind**（`Keybind` / `keybind` 相关）→ 译为 **"快捷键"**，如"按键绑定冲突"→"快捷键冲突"。
  - **keycode**（单纯的按键码设置，如滚轮倍率触发键 `*_key_code`）→ 译为 **"按键"**，如"滚动倍率按键码Ⅰ"→"滚动倍率按键Ⅰ"。
- **资源模板**：`processResources` 会用 `gradle.properties` / `libs.versions.toml` 的占位符展开 `fabric.mod.json`、`*.mixins.json`、`pack.mcmeta`、`META-INF/{,neoforge.}mods.toml`，**新增这类文件中的版本字段一律用占位符**（如 `${version}`、`${minecraft_version}`）。

## Git 与提交

- 主分支为 `dev`（远程默认分支，`compose-test` 已合入）；**MC 26.2 的移植与 Sokitsu 重写工作在分支 `26.2` 上进行，当前领先 `dev` 80+ 提交**；旧的 `refactor/ui-compose` / `refactor/ui-compose-minecraft` 分支已不存在。开工前先确认当前分支与目标分支。
- commit message 用中文，遵循 Conventional Commits（参考历史：`feat:` / `fix:` / `refactor(ui):` / `docs:` 等）。
- **提交前先把草稿给用户确认**：拟拆分的提交数量与文件范围 + 每个提交的完整 message（含版本号是否变更 / 是否需要打 tag），确认后才 `git commit`；用户说"提交"不等于可以跳过确认。
- 仅在被明确要求时才执行 `git commit` / `git push`；在默认分支上应先开分支。
- 提交前勿带入 `build/`、`runs/`、`modJar/`、`out/`、`net/` 等忽略目录。

## 工作约定（给 AI 助手）

1. **改公共 API 前确认影响面**：`config` / `command.dsl` / `event` / `render` 属于对外 API，消费方 MOD 依赖其签名，破坏性改动需谨慎并更新 `README.md` 示例（README 目前尚未随 UI 迁移重写）。旧 UI 相关 API（旧 `ui` 包、旧抽屉式 `ModScreen` 等）已随迁移删除；新 `ui/sokitsu`（含 `ui.configwrapper` 配置 GUI）已落地，`SokitsuScreen` / `ui/ModScreen.kt` 的模组屏幕 / `Modifier.tooltip` / `Toast` 等已成对外面，改动同样需谨慎。
2. **跨加载器改动**：能放 `common` 就放 `common`；平台相关能力通过 `platform/services` 抽象，由 fabric/neoforge 各自实现并通过 `META-INF/services` 注册，勿在 common 里硬编码平台判断。
3. **Mixin**：放 `common/.../mixin`（client 相关放 `mixin/client`），并在对应加载器的 `*.mixins.json` 注册；Fabric access widener 用 `ibukigourd.classtweaker`，NeoForge AT 用 `META-INF/accesstransformer.cfg`。
4. **compose-minecraft 依赖**：`common` 用 **`compileOnly(libs.composeMinecraft.common)`**（只编译、不传递，打包由加载器侧负责）；`fabric` 用 `api(libs.composeMinecraft.fabric)` + `include(...)`；`neoforge` 用 `api(libs.composeMinecraft.neoforge)` + `jarJar(...)`。构件 pom 已排除 kotlin/kotlinx/annotations 传递依赖，所以同时把 `nebula` / `aseprite` / `reorderable` 各自 `api + include`/`jarJar`。neoforge 另有 `bundledApi` 配置：`api` 已 `extendsFrom(bundledApi)`，并在别处解析其完整传递依赖树后逐个提升为 `jarJar` 直接依赖（等价于 Loom 的 `jarJarInternal`）。构件从 `mavenLocal()` 解析（neoforge 保留 compose 旧坐标重定向处理），调试本地版本时在 `~/.m2/repository/moe/forpleuvoir/` 下确认其版本。新增 UI 依赖请沿用此模式。
   **改完 CMP（`Compose-Minecraft`）源码后必须提醒用户手动发布/推送一次** —— 未发布时本仓仍会解析到 mavenLocal 里的旧构件，改了也不生效；发布动作由用户执行，助手只负责提醒。
5. **先读后写**：修改文件前先读取确认现状；遵循周边代码的命名、注释密度与惯用法。
6. **构建验证**：完成 Kotlin 改动后，只能用 IntelliJ IDEA MCP 的原生构建/检查能力验证（见下节「IntelliJ IDEA MCP 与验证」）；**不得在终端或 shell 里跑 Gradle**。不要声称“已通过测试”除非真的在 IDEA 里跑过。
7. **`nebula` 基类**：若改动触及 `ConfigManager` / `Event` 等定义，注意其声明在 `nebula` 依赖中，本仓库无法直接修改，只能通过包装/扩展。

## IntelliJ IDEA MCP 与验证

**硬性约束：编译、构建、代码检查、运行配置一律只能通过 IntelliJ IDEA MCP 的原生能力完成。**

- 允许：IDE 侧原生的项目模型 / 编译 / 构建 / 检查 / 运行配置工具（如 `build_project` 及 `filesToRebuild`、`get_file_problems`、`lint_files`、`get_project_modules`、`get_run_configurations`、`execute_run_configuration`）。
- 禁止：任何**脱离 IDEA** 的 Gradle 调用 —— 终端 / shell 里跑 `gradlew`、`gradlew.bat`、Wrapper、用 IDE 终端工具包一层命令、另起构建进程等，全部不允许。**即使 IDEA 不可用也不允许降级去跑 Gradle。**
- 不假定 MCP 的固定工具名；内置 `mcp__idea*` 工具缺失时用 `idea-mcp` CLI（见 skill `idea-mcp`），它同样是 IDEA MCP 通道。
- 不使用 `ps`、系统进程列表或类似方式探测 IDEA 或 Gradle 导入状态。
- 端点/会话失效（如 `404 Streamable HTTP session not found`）时，先在 IDE MCP 通道内恢复（`idea-mcp fresh` 重建会话）后重试；仍不可用就**停下来报告"当前无法验证"**，不得改用 Gradle。
- 先执行覆盖改动范围的最小检查（如单文件 `build_project` + `filesToRebuild`、`get_file_problems`），再按风险扩大验证（整项目 `build_project`）。
- 交付时说明实际采用的验证方式与结果（工具名 + 结论）；**禁止声称"构建通过"而实际没在 IDEA 里跑过**。
