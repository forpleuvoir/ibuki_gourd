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
- [ ] 自动隐藏滚动条（旧 `AutoHideScrollbar`；现只有 `verticalScroll`，无滚动条）
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
- [ ] 「按钮即首行」形态的下拉（**高度统一**：按钮与菜单项共用同一行高，展开后按钮成为列表首行）
      — 与上面的分开式互补：分开式用于动作菜单，本形态用于要"整体感"的场合；
        关键点是行高由**共享 meta** 统一（不靠两边默认值碰巧相等），并保留首末翻转
- [ ] 文本右键上下文菜单（旧 `Material3TextContextMenu`）

**输入与选择**

- [ ] `Selector` / `EnumSelector`（下拉、可搜索选择）
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
`DropdownMenu`(+`DropdownMenuItem`) / 主题与 `SokitsuThemeMeta` 体系。

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

- **已完成**：`FlatButton` 底座 + `IconButton` / `TextButton`、`Icon` + `Icons`（36 个）、
  `AlertDialog` + `SimpleAlertDialog`、`ColorPicker`（含 `ColorPickButton`）、`Toast`、
  `Divider`、`ProgressBar`、`DropdownMenu`（分开式）
- **下一步**：`Selector` / `KeySetter` → 可拖拽列表（拖拽库已就位，只需写组件）→
  包装器框架（`ConfigRowWrapper` → 各类型 wrapper → `ConfigManagerWrapper`）
