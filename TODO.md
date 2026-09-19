# TODO

## 4. Tooltip Popup 布局污染修复

- **位置**: `Tooltip.kt` — `Modifier.tooltip()`
- **问题**: `Modifier.tooltip()` 中 `Popup()` 作为子 Composable 被发射到父布局（Row/Column）的组合作用域中，虽然 Popup 渲染在独立层且尺寸为 0，但仍在组合树中参与 Row 的 `Arrangement.SpaceBetween` 等布局算法的 child 计数，导致布局异常。
- **已探索的方案**:
  - **方案 1 — 用 Dialog 替代 Popup**（已否决）: Compose Desktop 的 `Dialog` 创建独立 OS 窗口（GLFW/AWT），完全绕过自定义 Skia→Minecraft 渲染管线，不可用。
  - **方案 2 — Portal 模式（CompositionLocal + Root 级渲染，同 Scene）**: `DefaultComposeSceneHost` 中通过 `CompositionLocal` 提供 `PopupHostState`，`Modifier.tooltip()` 注册 entry，root 级渲染 Popup。Popup 与 content 同级，脱离 Row/Column 布局树。
- **选定方案 — Toast-like Overlay（独立 ComposeScene，跨 Scene 状态共享）**:
  - 模仿 `OverlayHost` 的独立 `ComposeScene` + singleton 模式
  - 新建 `TipHost` singleton 存储 tip entries
  - 新建 `TipContainer` composable，在 `OverlayHost` 的 scene 树中渲染
  - `Modifier.tooltip()` 通过 `TipHost` 提交 entry 而不发射 Popup
  - 跨 Scene 状态同步：Compose `SnapshotState` 是 JVM 全局的，`active`、`anchorBounds` 等 State 可跨 Scene 读写
  - 点击 dismiss：`OverlayHost.render()` 仅转发 MOVE 事件，不处理 click；tooltip 通过 `collectIsHoveredAsState` 的 hover 事件 dismiss，`dismissOnClickOutside = false`
- **进度**: 已完成 ✅ — 方案 2 Portal 模式 + 主题注入
  （最终落地：场景根 `LocalPopupHost` + `PopupHostOverlay`，**未**另开独立场景；
  上面"方案 1 已否决"的理由——Compose Desktop 的 Dialog 是独立 OS 窗口——已随 UI 迁移失效，
  现在平台的 `Dialog` 是场景内图层，可用）

---

## 配置 GUI 重建清单（configwrapper）

> 来源：`6771fcea`（迁移前整理）删除的 15 个 `ui/configwrapper/*.kt` 与所依赖的 23 个 `ui/preset/*.kt`，
> 按实际调用统计得出。基础设施（Screen / Popup(`LocalPopupHost`) / 滚动 / 文本输入）由 compose-minecraft 提供，不在此列。
> 相关旧源码可随时取回：`git show 6771fcea^:<path>`。

### 1. 包装器框架（15 个，需重建）

- [ ] `ConfigRowWrapper` 行骨架：配置名 + 控件 + 重置按钮 + tooltip + 悬停高亮（旧 `configwrapper/Base.kt`）
- [ ] `ConfigManagerWrapper` / `ConfigGroupWrapper` / `ConfigUIWrapper`：页面骨架、分组、按类型分发
- [ ] `PrimitiveConfigWrapper`：Bool / Int / Long / Float / Double（含 range → 进度型控件）
- [ ] `StringConfigWrapper`（含多行可扩展编辑器）
- [ ] `EnumConfigWrapper`
- [ ] `DurationConfigWrapper`
- [ ] `VectorConfigWrapper`：2i / 3i / 2f / 3f / 2d / 3d
- [ ] `ColorConfigWrapper` / `ColorSchemeConfigWrapper`
- [ ] `KeybindConfigWrapper`
- [ ] `ListConfigWrapper` / `MapConfigWrapper`（含编辑弹窗、拖拽排序）
- [ ] `CacheConfigWrapper`（缓存清理 + 用量显示）

### 2. 待实现组件

**图标与按钮**

- [x] `Icon` 组件 + 图标集（**像素图标**：36 个手绘 `.aseprite` 于 `texture/sokitsu/icon/`，图集 id `icon`）
      — 代码 `ui/sokitsu/Icon.kt`：`object Icons` 属性常量（`Icons.Add` etc.，含 `all` / `byId`）+ `Icon(icon, scale, size?, tint)`；
      尺寸 = 素材尺寸 × `scale`（缺省 `pixelScale`）；配套图标总览屏 `IconTestScreen`
- [x] `FlatButton` 底座 + `IconButton` + `TextButton`
      — 落地于 `FlatButton.kt` / `FlatButtonTheme.kt` / `IconButton.kt` / `TextButton.kt`；
      `ui/flat_button/` 素材只画了 `pressed`(α200) / `focused`(α127) → **其余状态不渲染背景**（不是纯色填充）
      — 注：**浮动按钮仍需要**，但用普通 `Button` + 定位修饰器拼出，不新增 FAB 组件

**容器与装饰**

- [x] `Divider`（分割线）
      — `ui/sokitsu/Divider.kt` / `DividerTheme.kt`（meta 键 `divider`）；**纯色矩形**，不依赖素材；
      颜色走 token `DividerTokens.Line`（= `outline`），默认厚 1dp
- [x] `ProgressBar`（进度条，缓存加载用）
      — `ui/sokitsu/ProgressBar.kt` / `ProgressBarTheme.kt`（meta 键 `progress_bar`）；
      轨道 + 填充两个**纯色矩形**自绘，不依赖素材；默认高 4dp；**不做动画**（进度值由业务驱动）
- [x] 自动隐藏滚动条（旧 `AutoHideScrollbar`）
      — `ui/sokitsu/Scroller.kt` / `ScrollerTheme.kt`（meta 键 `scroller`）：
        `VerticalScroller` / `HorizontalScroller` 常规版 + `VerticalOverlayScroller` /
        `HorizontalOverlayScroller` 叠加版；`autoHide=true` 时无滚动空间**完全不组合**；
        `autoFade=true` 时非活动（未滚动 / 未悬浮）一段时间后动画降可见度到 30%；
        滚动源经 `ScrollerAdapter` 解耦（`ScrollState` 直读 / `LazyListState` 估计法）
- [x] `Toast`（操作反馈）
      — 落地于 `ui/sokitsu/toast/`：`Toast` / `ToastStrategy` / `ToastAnimation` / `ToastTheme` /
      `ToastHandler` / `ToastContainer` / `ToastHost`；**全局常驻**（自持 Compose 场景与渲染器，
      经 `GuiRendererToastMixin` 每帧驱动），HUD / 原版界面 / Compose 屏幕之上都可见；
      主题 meta 键 `toast`，面板复用气泡素材，配置在 `IGConfig.Gui.Toast`

**弹窗与菜单**

- [x] `AlertDialog` + `SimpleAlertDialog`
      — `ui/sokitsu/AlertDialog.kt` / `SimpleAlertDialog.kt`；主题 meta 键 `alert_dialog`，
      出入场动画由平台 `Dialog` 的图层快照重放承担（调用方用 `if (show)` 控制组合即可）
- [ ] 删除确认一类语义封装（旧 `RemoveConfirmButton`）
- [ ] 可伸缩编辑弹窗（旧 `FlexibleDialog`、`EditDialog*`）
- [x] `DropdownMenu` + 菜单项（分开式）
      — `ui/sokitsu/menu/DropdownMenu.kt` / `DropdownMenuTheme.kt`（meta 键 `dropdown_menu`）：
      核心 API 走**无状态**（`expanded` / `onDismissRequest` / `anchorBounds`），另有把展开标志与
      锚点打包的 `DropdownMenuState` + `rememberDropdownMenuState` 便利重载；
      锚点由 `Modifier.dropdownMenuAnchor` 捕获（`(Rect) -> Unit` 与 state 两个版本），不新增布局节点；
      自带定位器
      — 面板用**共享的** `BubblePanel`（九宫格气泡体 + 陷边箭头，与 tooltip 同源）；
        **与锚点中心对齐**、下方不足翻上方；`max_height` 超出时面板内滚动
      — **容器只管弹出**：染色 / 内边距 / 最大高度等均为可覆盖的默认值；内容放什么由调用方决定；
        面板宽高与各项宽度**都由内容撑开**（不填充、不拉齐各项）
      — `DropdownMenuItem` 是**可选**的默认条目：高度取 `heightIn(min)`（内容更高就撑开）、
        内边距 / 图标间距 / 图标倍率 / 悬停色均为可覆盖的默认值（组件常量，**不进主题 meta**）
- [ ] 文本右键上下文菜单（旧 `Material3TextContextMenu`）

**输入与选择**

- [x] `Selector` / `EnumSelector`（下拉、可搜索选择）
      — `ui/selector/`（`Selector` / `SelectorSelection` / `SelectorTrigger` / `SelectorExpanded` /
        `SelectorExpandStyle`）：**单选 + 多选共用同一展开体**；
        `searchFilter` 非 null 时弹窗载体内置搜索栏（放大镜图标 + 过滤）；
        展开载体由 `SelectorExpandStyle` 分发（下拉菜单 / Dialog 弹窗，搜索时一律弹窗）；
        弹窗内 `LazyColumn` + 自动隐藏滚动条（overlay 同行占位）
      — `EnumSelector` 未单独封装：枚举场景直接把枚举值列表传给 `Selector`
- [x] `ColorPicker`
      — `ui/colorpicker/`（`ColorPicker` / `ColorChannelSlider` / `Checkerboard` / `ColorPickButton`）：
      HSV / RGB 页签 + 通道条（含色相条）+ alpha 条 + 透明棋盘 + 色值复制 / 粘贴（带 toast 反馈）
      — 交互模型定为**逐通道条 + 数值框**（非渐变面板拖拽）；`ColorPickButton` 是"底色 = 当前颜色、
        点击弹 `AlertDialog` 编辑副本"的入口组件
- [ ] hex 文本输入框（当前色值只有只读显示 + 整串复制 / 粘贴，没有反向输入）
- [ ] `KeySetter`（按键捕获、组合键显示；旧 558 行）
- [ ] 多行可扩展文本编辑器（旧 `ExpandableStringContentEditor`）

**列表**

- [ ] 可拖拽排序列表 + `DragHandle`（旧 `ReorderableItemList`）
      — **基础设施已就位**：Reorderable v3.0.0 源码内嵌为独立模块 `reorderable/`（包名保持
        `sh.calvin.reorderable`，Apache-2.0，见 `NOTICE.md`），已接入 common / fabric / neoforge
        并通过编译与打包验证；待做的是本项目的列表组件与 `DragHandle` 封装
        （旧版走 Lazy 路线：`rememberReorderableLazyListState` + `ReorderableItem`；
        本仓列表目前是 `verticalScroll` + Column，可改用 `ReorderableList` 那套非 Lazy API）
- [ ] 删除确认按钮（旧 `RemoveConfirmButton`）
- [ ] 列表/映射条目编辑弹窗内容（旧 `EditDialogContent*`）
- [ ] 浮动添加按钮 + 随滚动显隐（旧 FAB / `fabVisibilityAnimation` / `rememberFabVisibilityByScroll`）
      — 用普通 `Button` + 定位修饰器拼出，不新增 FAB 组件

**其它**

- [ ] `ItemIcon`（MC 物品渲染为图标）
- [ ] `SearchBar` / `SearchPanel`（配置搜索 + 匹配高亮）
- [ ] `Keyed` / `rememberKeyedList`（列表 key 稳定工具）

### 3. 已有可直接复用

`Button` / `ColorButton` / `FlatButton`(+`IconButton` / `TextButton`) / `Icon` + `Icons`（36 个像素图标）/
`Switch` / `Slider` + `NumberSlider` / `NumberField` / `TextField` / `Text` / `Surface` /
`Tooltip` + `BasicTooltip` / `RadioButton`(+`RadioButtonGroup`) / `AlertDialog`(+`SimpleAlertDialog`) /
`ColorPicker`(+`ColorPickButton`) / `Toast`（全局常驻） / `Divider` / `ProgressBar` /
`DropdownMenu`(+`DropdownMenuItem`) / `Selector`（单/多选 + 搜索，单/多选共用展开体） /
`VerticalScroller` + `HorizontalScroller`（常规）与 `VerticalOverlayScroller` + `HorizontalOverlayScroller`（叠加，
支持 `autoHide` / `autoFade`，滚动源经 `ScrollerAdapter` 解耦）/ 主题与 `SokitsuThemeMeta` 体系。

另有一个**源码内嵌的独立模块**可用（见 `NOTICE.md`）：

- `reorderable/` — Reorderable v3.0.0（Apache-2.0）的拖拽排序实现，覆盖 Lazy 列表 / Lazy 网格 /
  交错网格与非 Lazy 列表四套 API，包名保持上游 `sh.calvin.reorderable`；
  `common` 已以 `compileOnly` 接入，fabric / neoforge 侧随 mod jar 内嵌。

### 4. 待拍板的设计点

- [x] **图标方案**：已定为 **`.aseprite` 像素图标**（`texture/sokitsu/icon/`，独立图集 `icon`）+ `Icons` 属性常量集；
      旧版的 Material Symbols 矢量图标（32 个 `ImageVector`）不采用
- [x] **菜单皮肤**：已定为**复用 tooltip 的气泡体素材**（`ui/tooltip/bubble`，meta
      `dropdown_menu.panel_sprite`），配色与 `TooltipTokens` 同族 —— 不需要另画菜单素材
      — `Dialog` 一项已解决：`AlertDialog` 复用 `ui/surface/float_panel`（凸起带阴影的浮层面板）；
        `Chip` 不做（胶囊圆角与像素风圆角体系冲突，且其过滤/多选语义本项目用不到）
- [x] **ColorPicker 交互模型**：已定为**逐通道条 + 数值框**（HSV / RGB 页签切换），不用渐变面板拖拽

### 5. 建议实现顺序（每层都能先在测试屏里验）

> **原则：先补齐基础组件，高层级（配置 GUI 包装器框架）暂不碰**，等基础组件收敛后再开工。

- **已完成**：`FlatButton` 底座 + `IconButton` / `TextButton`、`Icon` + `Icons`（36 个）、
  `AlertDialog` + `SimpleAlertDialog`、`ColorPicker`（含 `ColorPickButton`）、`Toast`、
  `Divider`、`ProgressBar`、`DropdownMenu`（分开式）、`Selector`（单/多选 + 搜索）、
  滚动条（常规 + overlay，`autoHide` / `autoFade`）
- **下一步（基础组件，按依赖从底到顶）**：
  1. `Keyed` / `rememberKeyedList`（列表 key 稳定工具，最底层）
  2. 可拖拽排序列表 + `DragHandle`（拖拽库已就位，只需写组件）
  3. `KeySetter`（按键捕获，配置 GUI 与 keybind 编辑的公共依赖）
  4. 文本右键上下文菜单、删除确认语义封装
  5. `ItemIcon`、多行可扩展编辑器、hex 文本输入框
- **押后（高层级）**：配置 GUI 包装器框架（`ConfigRowWrapper` → 各类型 wrapper →
  `ConfigManagerWrapper`）、`SearchBar`（配置搜索）、`CacheConfigWrapper` 等页面级内容

---

## 屏幕 / 对话框进出场动画配置（compose-minecraft 绑定）

> 落地于 `IGConfig.Gui.Screen` / `IGConfig.Gui.Dialog`，绑定 compose-minecraft 的
> `ScreenAnimationDefaults` / `ComposeScreenDefaults` / `DialogAnimationDefaults` /
> `DialogComposeScreenDefaults`；配置改动对**之后新建**的屏幕 / 对话框生效。

- [x] 屏幕：`fade_in_duration` / `fade_in_offset` / `fade` / `animation_enabled`、`disable_world_render`
- [x] 对话框：`scrim_color` / `fade_in_duration` / `initial_scale`、`disable_world_render`
- [ ] **缓动 easing 占位**：上游 `ScreenAnimationDefaults.easing` / `DialogAnimationDefaults.easing`
      是任意 `Easing` 对象，nebula 现有配置类型（primitive / duration / color / enum / list / map /
      自定义 codec）都不承载「曲线函数」，需要专门的配置项。候选：
  - 预设枚举（`configEnum` → `LinearEasing` / `FastOutSlowInEasing` / `LinearOutSlowInEasing` /
    `FastOutLinearInEasing`），改动最小，GUI 也好做控件；
  - 三次贝塞尔四点（`x1,y1,x2,y2` → `CubicBezierEasing`），表达力最强，需自定义 codec + `x∈[0,1]` 校验；
  - 任意 `Easing` lambda 不可序列化，只能代码注入，不进配置。
- [x] **不做全局配置**（属具体屏幕的构造参数，上游也没有对应默认值对象）：`pauseGame` /
      `closeOnEsc` / `density` / `exitParentOnOpen` / `renderParentScreen`

---

## 出处审计（2026-09-19，agent 整理；未删改上面任何条目）

> 基准：`6771fcea^` 的 `ui/` 文件树（`git ls-tree -r 6771fcea^ --name-only`）+ 迁移删除清单 `build/cw_del.log`。
> 取回任一文件：`git show 6771fcea^:common/src/main/kotlin/moe/forpleuvoir/ibukigourd/ui/<path>`。
> 行数为 `git show … | Measure-Object -Line` 实测值。

### A. 有出处（旧文件可对照）

| 条目 | 出处（`6771fcea^` 路径） | 行数 |
|---|---|---|
| 15 个 config wrapper | `ui/configwrapper/{Base, CacheConfigWrapper, ColorConfigWrapper, ColorSchemeConfigWrapper, ConfigGroupWrapper, ConfigManagerWrapper, ConfigUIWrapper, DurationConfigWrapper, EnumConfigWrapper, KeybindConfigWrapper, ListConfigWrapper, MapConfigWrapper, PrimitiveConfigWrapper, StringConfigWrapper, VectorConfigWrapper}.kt` | 合计 2,786 |
| 删除确认语义封装 | `ui/preset/RemoveButton.kt`（`fun RemoveConfirmButton` :25） | 60 |
| 可伸缩编辑弹窗 | `FlexibleDialog` 在 `ui/preset/SimpleAlertDialog.kt:92` | 187 |
| 列表条目编辑弹窗内容 | `EditDialog*` 在 `ui/configwrapper/{String,Map,List}ConfigWrapper.kt` 里**各一份** | — |
| 文本右键上下文菜单 | `ui/platformcontext/Material3TextContextMenu.kt` | 133 |
| `KeySetter` | `ui/preset/KeySetter.kt`（原文写"旧 558 行"，实测 **526**） | 526 |
| 多行可扩展编辑器 | `ui/configwrapper/StringConfigWrapper.kt:40`（private `ExpandableStringContentEditor`，非独立组件） | 444（含 wrapper 全部） |
| 拖拽排序列表 + `DragHandle` | `DragHandle`：`ui/preset/DragHandle.kt` | 47 |
| 同上（`ReorderableItemList`） | `ui/configwrapper/ReorderableItemList.kt` —— **非迁移所删**，`89b4dc16`（2026-07-07「重构配置列表编辑器」）删；取回用 `git show 89b4dc16^:<path>`（见 `build/cw_del.log:18`） | — |
| 浮动添加按钮 + 随滚动显隐 | `ui/preset/modifier/FabVisibilityAnimation.kt` + `ui/preset/state/FabVisibilityByScroll.kt` | 30 + 128 |
| `ItemIcon` | `ui/preset/ItemIcon.kt` | 249 |
| `SearchBar` | `ui/preset/SearchBar.kt`（`SimpleSearchBar` :25） | 72 |
| `SearchPanel` | `ui/configwrapper/ConfigManagerWrapper.kt:240`（private，非独立组件） | — |
| `Keyed` / `rememberKeyedList` | `ui/util/Keyed.kt`（**已于 2026-09-19 重建**） | 26 |

### B. 无出处（整理时自造的建议，不对应任何旧代码）

- **hex 文本输入框** —— 旧版 `ui/preset/ColorPicker.kt` 只有"显示 + 点击复制"（无粘贴、无手工输入）；
  现版为"显示 + 左键复制 + 右键粘贴"。本条出自 `c8f94468`（2026-09-17）整理时的缺口提议。
- 全表复核后，无出处的**仅此一项**。

### C. 已删但本清单未提 —— 待确认是否已由现有组件 / compose-minecraft 基座覆盖

- `ui/preset/modifier/Backgourd.kt`（47）
- `ui/preset/state/RememberInputState.kt`（66）、`ui/preset/state/RememberTextFieldState.kt`（29）
- `ui/widget/ComposeWidget.kt`（80）
- `ui/util/ComposeScreenHelper.kt`（15）
- ⚠️ `ItemIcon` 的**隐性依赖** `ui/util/render/`（当前仓库零引用、迁移时全删）：
  `SkiaItemRenderHelper.kt`(409)、`ItemRenderAtlas.kt`(390)、`TextureAtlasPainter.kt`(217)、
  `OffscreenRenderTarget.kt`(156)、`AtlasRectAllocator.kt`(133)、`ItemAtlasPainter.kt`(67)，
  合计 **1,372 行** —— 即 `ItemIcon` 一项的真实成本远大于 249 行。

### D. 陈旧待修

- 「文本右键上下文菜单」已于 R31–R33 完成（`ui/sokitsu/menu/SokitsuContextMenu.kt`），此处仍为 `[ ]`。
- 「删除确认」重复列了两次（「弹窗与菜单」与「列表」两节各一条）。
- lang 里 `ibukigourd.config.gui.screen.fade_in_offset` / `fade_in_duration` 的文案仍写「淡入…」，
  现已被 `IGConfig.Gui.Screen` 复用为**进出场**动画的位移与时长；`gui.screen.pause_game`
  已无对应配置项（全局暂停不做配置），文案待与配置 GUI 一起修。
