# TODO

## Tooltip Popup 布局污染修复（已完成）

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

## 配置 GUI 重建清单（configwrapper）—— 主体已完成，余 1 项

> 来源：`6771fcea`（迁移前整理）删除的 15 个 `ui/configwrapper/*.kt` 与所依赖的 23 个 `ui/preset/*.kt`，
> 按实际调用统计得出。基础设施（Screen / Popup(`LocalPopupHost`) / 滚动 / 文本输入）由 compose-minecraft 提供，不在此列。
> 相关旧源码可随时取回：`git show 6771fcea^:<path>`。

### 1. 包装器框架（15 个，已重建）

> 落地提交：`9bad2f4f feat(config): 重建配置 GUI 包装器框架与各类型 wrapper`（2026-09-20，20 文件 / +2600 行）。
> 基础设施（Screen / Popup(`LocalPopupHost`) / 滚动 / 文本输入）由 compose-minecraft 提供；配置行 UI 由 `ui/configwrapper/` 承载。

- [x] `ConfigRowWrapper` 行骨架：配置名 + 控件 + 重置按钮 + tooltip + 悬停高亮（旧 `configwrapper/Base.kt`）
- [x] `ConfigManagerWrapper` / `ConfigGroupWrapper` / `ConfigUIWrapper`：页面骨架、分组、按类型分发
      — `ConfigManagerWrapper` 自带搜索栏（旧 `SearchPanel` 语义）与分组导航；
      `ConfigUIWrapper` + `UIWrappers` 注册表按谓词分发整行，节点也可用 `uiWrapper` 指定专用实现
- [x] `PrimitiveConfigWrapper`：Bool / Int / Long / Float / Double（含 range → 进度型控件）
- [x] `StringConfigWrapper`（含多行可扩展编辑器：编辑弹窗内 `TextFieldLineLimits.MultiLine(6..12)`）
- [x] `EnumConfigWrapper`（选项 > 10 个自动挂搜索框）
- [x] `DurationConfigWrapper`
- [x] `VectorConfigWrapper`：2i / 3i / 2f / 3f / 2d / 3d
- [x] `ColorConfigWrapper`
- [ ] `ColorSchemeConfigWrapper` —— 「一整套配色槽位」的编辑入口。**通用包装器仍未实现**：
      `UIWrappers`（`ConfigUIWrapper.kt`）没有注册 `ColorScheme` 值类型，消费方要显示一份配色只能自己写 `uiWrapper`。
      用户可见的那份需求已由主题组覆盖（2026-09-26 `f099e188`）：`IGConfig.Gui.Theme` 的 `mode` 行 +
      `mod/config/ThemeConfig.kt` 的 `CustomColorSchemeEditor` → `ui/sokitsu/theme/ColorSchemeEditor.kt`
      （`ColorSchemeEditorDialog`，槽位按钮组 + 亮暗标记 + 以默认浅/深色为底）；配色整份存在 `custom_scheme`
      配置项里并以 `.uiWrapper { }` 整行不出
- [x] `KeybindConfigWrapper`
- [x] `ListConfigWrapper` / `MapConfigWrapper`（含编辑弹窗、拖拽排序与 `DragHandle` / `EditDialogContentList`）
- [x] ~~`CacheConfigWrapper`（缓存清理 + 用量显示）~~ —— **不再重建**（2026-09-26 结案）：它服务的两张 GPU 图集
      （`item_texture_atlas_size` / `texture_atlas_size`）属旧 Skia 离屏渲染（`ui/util/render/`，见 §C），
      `ItemIcon` 改原生 `Modifier.minecraftItem` 后已无对应实现，配置项没有回归落点；
      对应文案（§D 的 `gui.cache.*`）同批删除
- [x] 旧版没有的新件：`ConfigElementEditor`（编辑弹窗内按值的运行时类型取控件，不带行骨架）/
      `ConfigRowTheme`（配置行的组件 token 声明）/ `ConfigState`（`Config` → Compose `State`）/
      `BezierCurveConfigWrapper`（自定义缓动曲线）/ `EasingConfigWrapper`（缓动预设行：选中「自定义」
      即弹曲线编辑器、悬停控件显示当前自定义曲线预览，见「缓动 easing 配置化」节）/
      `mod/config/ThemeConfig.kt`（`ThemeMode` / `resolveThemeScheme` / `CustomColorSchemeEditor`）/
      配置页入口见 `mod/IbukiGourdModScreen.kt` 的 `openIbukiGourdModScreen(parent)`
      （Fabric ModMenu 与 NeoForge 配置按钮共用），配置页只是它的一页

### 2. 组件清单（均已落地）

**图标与按钮**

- [x] `Icon` 组件 + 图标集（**像素图标**：38 个手绘 `.aseprite` 于 `texture/sokitsu/icon/`，图集 id `icon`）
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
- [x] `TabStrip`（页签条 + 面板）
      — `ui/sokitsu/TabStrip.kt` / `TabStripTheme.kt`：按 `TabStripPlacement`（Top / Bottom）选素材
      （素材只在贴面板的那一侧开口）；另有 `TabStripTab`；点页签原地不动，只有外部改选中才复位滚动窗口
- [x] `TabRow`（等宽页签行 + 选中指示器）
      — `ui/sokitsu/TabRow.kt` / `TabRowTheme.kt`（`676d1133`）：标签经 `Tab` 放进 `tabs` 槽，
      每格宽度由页签行统一测量（`SubcomposeLayout`）、标签内容由 `Tab` 自己承担；
      `indicator` 可替换（缺省整格宽的 Primary 指示器）；配置页的分页导航即用它
      （`ConfigManagerWrapper`，`4dfd7a13` 改用页签导航）
- [x] `TableLayout` + `LazyTableLayout`（表格）
      — `ui/sokitsu/TableLayout.kt`（`TableLayoutDefaults` 同文件内）：`TableLayoutScope` 的 `row` / `spanItem` DSL、
      `TableCellScope` 单元格对齐，表头与数据列尺寸对齐；`LazyTableLayout` 为惰性行版本
- [x] 自动隐藏滚动条（旧 `AutoHideScrollbar`）
      — `ui/sokitsu/Scroller.kt` / `ScrollerTheme.kt`（meta 键 `scroller`）：
        `VerticalScroller` / `HorizontalScroller` 常规版 + `VerticalFlatScroller` /
        `HorizontalFlatScroller` flat 细条版；`autoHide=true` 时无滚动空间**完全不组合**；
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
- [x] 删除确认语义封装
      — `ui/editdialog/RemoveConfirmButton.kt`：`RemoveButton`（`delete` 图标 + 气泡提示，点击直接执行）
      + `RemoveConfirmButton`（点击弹 `SimpleAlertDialog` 确认，确认按钮用主题 `error` / `onError` 配色，
      标题取 `IGLang.Misc.removeConfirm`，另有补充正文槽位）
      — `quickAction` 在**点击时**求值，默认读全局 `isQuickAction`
      （`InputHandler.wasKeyPressed(IGConfig.Gui.quickActionKeyCode)`，配置项缺省左 Shift）→ 按住跳过确认
- [x] 可伸缩编辑弹窗（旧 `FlexibleDialog`、`EditDialog*`）
      — `ui/sokitsu/FlexibleDialog.kt`：面板宽度由内容决定（`usePlatformDefaultWidth = false`，
      `minWidth` / `maxWidth` 缺省不约束），正文区 `weight(1f, fill = false)` 便于放滚动列表，
      面板精灵 / 配色 / 进出场动画沿用 `alert_dialog` 主题槽位
      — `ui/editdialog/EditDialog.kt`：以可编辑副本承载内容，确认时把副本快照交回
      `onConfirm: (List<E>) -> Boolean`（返回 false 保持打开），取消 / 遮罩关闭直接丢弃副本
      — `ui/editdialog/EditDialogContent.kt`：`EditDialogContent`（表头 + 正文 + 角标按钮槽位）与
      `EditDialogContentHeader`（可选首 / 尾列 + 内容列 + 分割线）
      — `ui/editdialog/EditDialogContentList.kt`：可拖拽排序的编辑行列表（三列对齐：拖拽手柄 / 内容 / 删除），
      条目 key 由 `KeyedListState` 单调分配；`ListConfigWrapper` / `MapConfigWrapper` 即其消费者
      — `ui/editdialog/EditDialogContentCards.kt`（`7f38d198`）：卡片网格容器（头部 = 拖拽手柄 + 尾部操作组，
      可指定列数），装"整块编辑器"这类非表格内容；`BezierCurveConfigWrapper` 即其消费者
      — 注：以上骨架与 `DragHandle` 都在 **`ui.editdialog/`**（`5ca8dec5` 从 `ui/sokitsu/` 拆出），不在 sokitsu 包内
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
- [x] 文本右键上下文菜单的 Sokitsu 呈现层
      — `ui/sokitsu/menu/SokitsuContextMenu.kt`：`SokitsuContextMenuRepresentation` 由
      `SokitsuTheme` 经 `LocalContextMenuRepresentation` 提供，文本框右键即走此呈现层
      （旧 `ui/platformcontext/Material3TextContextMenu.kt` 不采用）

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
- [x] `KeySetter`（按键捕获、组合键显示；旧 558 行）
      — `ui/keybind/KeySetter.kt`（**非 sokitsu 包**，用 sokitsu 组件拼装）：`KeyCodeSetButton`（单键）/ `KeybindSetButton`（组合键，
      松开全部按键才写入，`Esc`、`Ctrl + Backspace` 清空）/ `KeybindSettingSetButton` +
      `KeybindSettingColumn`（穿透 / 严格开关、环境与触发模式 `Selector`、长按阈值与重复间隔
      `IntField` 按触发模式显隐）/ `Keybind.hoverText()`（按键组合 + 冲突列表）
      — 捕获状态机在 `ui/util/InputState.kt`：`rememberKeyCapture` 全程由键盘 / 鼠标**事件回调**驱动
      （不逐帧轮询、不在组合期或协程里写状态）；`rememberPressedKeys` 维护按下顺序
      — 捕获期间按键 / 鼠标事件在回调里直接 `cancel`：mixin 在事件之后检查 `isCancelled()`，
      能连**按住的 GLFW_REPEAT** 一起吞掉 —— `InputHandler.onKeyPress` 对重复触发直接放行，
      靠"注册通配 `Keybind`"只能吞首次按下，会漏成重复输入
      — 捕获期间气泡用 `tooltip(pinned = inputting)` 钉住：`basicTooltip` / `tooltip` 新增 `pinned` 参数
      （跳过悬停延迟直接展示、忽略移出、弹层 `dismissOnClickOutside = false`），
      否则点击按钮本身就算"点弹层外部"会被关掉，看不到实时累计的按键
      — 捕获的按下集合必须用 `LinkedHashSet`（`MutableList.add` 恒为 true，按住重复会重复入列，
      释放一次删不净 → 永不提交、隐藏动作键卡在按下态）；`PressedKeysState` 同理
      — tooltip 内容居中靠 `BubblePanel` 的 `contentAlignment = Center`：面板有 `minSize`，
      内容窄时会被 `TopStart` 贴在左侧，此时单独给文本 `textAlign` 是无效的（Text 节点宽度即文字宽度）
      — 冲突高亮读 `InputHandler.detectKeyConflicts` + `keybindVersion`，用主题 `error` / `onError` 配色
      — 新增 `key_environment` 三档文案（8 个语言文件）；旧 `KeybindAssistChip`（Material3 Chip）不采用
      — 与旧版差别：旧版用 `withFrameNanos` 逐帧轮询 `InputHandler.pressedKeys`，本版改为事件驱动

**列表**

- [x] 列表/映射条目编辑弹窗内容（旧 `EditDialogContent*`）
      — `ui/editdialog/EditDialogContent.kt`：`EditDialogContent`（默认表头 + 正文槽位
      `(LazyListState) -> Unit` + 右下角浮动按钮槽位）与 `EditDialogContentHeader`
      （可选首 / 尾列 + 内容列 + 分割线）；表头由默认槽位渲染，内容区不要再写一次
- [x] 浮动添加按钮 + 随滚动显隐（旧 FAB / `fabVisibilityAnimation` / `rememberFabVisibilityByScroll`）
      — 普通 `Button` + 定位修饰器拼出，不新增 FAB 组件；显隐逻辑在 `ui/util/FabVisibility.kt`：
      `rememberFabScrollVisibility`（`LazyListState` / `LazyGridState` / `LazyStaggeredGridState` /
      `ScrollState` / 项目 `ScrollerAdapter` 五版）返回 `FabScrollVisibility`（自身即
      `NestedScrollConnection`），由 `Modifier.fabScrollVisibility` 挂在滚动容器**祖先**上：
      **方向判定**（向下累计超 `hideDistance` 收起、向上滚动立即恢复、回到起始端恒可见、≤2px 抖动忽略）
      在嵌套滚动回调 `onPreScroll` 里推进 —— 回调发生在滚动事件派发期，既不在组合期也不在帧派发器上
      — 位移取「索引 / 偏移」单调标记差值，不用"可见项平均尺寸"（后者随可见集抖动，会误判方向）
      — ⚠️ **滚动不得触发组合期或协程里的快照写入**：两种写法在本平台都会与测量 / 快照锁撞成
      **永久死等**（卡死 + CPU 空闲 + 完全无响应，暂停栈停在 `System.nanoTime`）；
      只在组合期**读**滚动状态是安全的（本仓 `Scroller` / `Selector` 即此）
      — `Modifier.fabVisibilityAnimation`（图层透明度 / 位移 / 缩放）保留给外部低频显隐；
      组件侧由 `EditDialogContent` 用 `AnimatedVisibility`（淡入 / 上移 / 缩放，隐藏后移出组合）
      内置到浮动按钮槽位；强制隐藏默认读隐藏动作键 `IGConfig.Gui.hideActionKeyCode`（缺省左 Alt）

**其它**

- [x] `SokitsuScreen` + `SokitsuScreenScale`（屏幕基座与分辨率自适应）
      — `ui/sokitsu/SokitsuScreen.kt`：`ComposeScreen` 的薄包装，只补主题与缩放两件事；
      `SokitsuScreenScale.kt` 把「Compose 密度 + pixelScale」打包成一档，按窗口分辨率选择后下发，
      只改其一会让组件外框与框内素材对不上（九宫格边框按 pixelScale 折算）
- [x] `ItemIcon`（MC 物品渲染为图标）
      — `ui/item/ItemIcon.kt`（**非 sokitsu 包**）：物品本体走 `Modifier.minecraftItem`（原生物品模型，
      与 Compose 内容共享绘制顺序 / 裁剪，可参与 Popup / Dialog 图层），悬停 tooltip 走
      `Modifier.minecraftTooltip`（`TooltipLines.fromItem`），悬停放大走 `graphicsLayer`
      （尺寸变化由绘制节点画布矩阵承担），数量用 `BasicText` 叠在右下角；
      布局尺寸即绘制尺寸（默认 48dp，取 16 的整数倍），另有 `color` / `seed`；**不再走离屏烘焙与逐帧原生推流**
- [x] `Keyed` / `rememberKeyedList`（列表 key 稳定工具）
      — `ui/util/Keyed.kt`：key 跟**数据**走而非位置，重排时条目组合状态不丢；
      消费者：`ListConfigWrapper` / `MapConfigWrapper`，以及 `EditDialogContentList`（`KeyedListState`）

### 3. 已有可直接复用

`Button` / `ColorButton` / `FlatButton`(+`IconButton` / `TextButton`) / `Icon` + `Icons`（38 个像素图标）/
`Switch` / `Slider` + `NumberSlider` / `NumberField` / `TextField` / `Text` / `Surface` /
`Tooltip` + `BasicTooltip` / `RadioButton`(+`RadioButtonGroup`) / `AlertDialog`(+`SimpleAlertDialog`) /
`FlexibleDialog` / `EditDialog`(+`EditDialogContent` / `EditDialogContentHeader` / `EditDialogContentList`) /
`RemoveButton` / `RemoveConfirmButton` / `DragHandle` / `KeySetter`（`KeyCodeSetButton` / `KeybindSetButton` /
`KeybindSettingSetButton` / `KeybindSettingColumn`） /
`ColorPicker`(+`ColorPickButton`) / `Toast`（全局常驻） / `Divider` / `ProgressBar` /
`TabStrip` / `TableLayout` + `LazyTableLayout` / `SokitsuScreen` + `SokitsuScreenScale` /
`DropdownMenu`(+`DropdownMenuItem`) / `SokitsuContextMenu`（文本框右键，theme 级
`LocalContextMenuRepresentation`） / `Keyed` + `rememberKeyedList`（列表 key 稳定） /
`Selector`（单/多选 + 搜索，单/多选共用展开体） /
`VerticalScroller` + `HorizontalScroller`（常规）与 `VerticalFlatScroller` + `HorizontalFlatScroller`（flat 细条，
支持 `autoHide` / `autoFade`，滚动源经 `ScrollerAdapter` 解耦）/
`BezierCurveEditor` + `BezierCurvePlot`（曲线编辑与预览）/ `TabRow`（等宽页签行 + 指示器）/
`ColorSchemeEditorDialog` + `SokitsuThemeOverride`（配色编辑与配置驱动的主题覆盖）/
`UiState`（交互状态）/
主题与 `SokitsuThemeMeta` 体系。

另有一个**源码内嵌的独立模块**可用（见 `NOTICE.md`）：

- `reorderable/` — Reorderable v3.0.0（Apache-2.0）的拖拽排序实现，覆盖 Lazy 列表 / Lazy 网格 /
  交错网格与非 Lazy 列表四套 API，包名保持上游 `sh.calvin.reorderable`；
  `common` 已以 `compileOnly` 接入，fabric / neoforge 侧随 mod jar 内嵌。

### 4. 设计点（已拍板）

- [x] **图标方案**：已定为 **`.aseprite` 像素图标**（`texture/sokitsu/icon/`，独立图集 `icon`）+ `Icons` 属性常量集；
      旧版的 Material Symbols 矢量图标（32 个 `ImageVector`）不采用
- [x] **菜单皮肤**：已定为**复用 tooltip 的气泡体素材**（`ui/tooltip/bubble`，meta
      `dropdown_menu.panel_sprite`），配色与 `TooltipTokens` 同族 —— 不需要另画菜单素材
      — `Dialog` 一项已解决：`AlertDialog` 复用 `ui/surface/float_panel`（凸起带阴影的浮层面板）；
        `Chip` 不做（胶囊圆角与像素风圆角体系冲突，且其过滤/多选语义本项目用不到）
- [x] **ColorPicker 交互模型**：已定为**逐通道条 + 数值框**（HSV / RGB 页签切换），不用渐变面板拖拽

### 5. 实现顺序与现状

> 原原则是「先补齐基础组件，配置 GUI 包装器框架暂不碰」。§1 已于 `9bad2f4f` 落地，
> 现在只剩 `ColorSchemeConfigWrapper` 一项（通用包装器，用户可见需求已由主题组覆盖），
> 另见「曲线编辑器 —— 遗留项」。

- **基础组件（已完成）**：`FlatButton` 底座 + `IconButton` / `TextButton`、`Icon` + `Icons`（38 个）、
  `AlertDialog` + `SimpleAlertDialog`、`ColorPicker`（含 `ColorPickButton`）、`Toast`、
  `Divider`、`ProgressBar`、`DropdownMenu`（分开式）、`Selector`（单/多选 + 搜索）、
  滚动条（常规 + overlay，`autoHide` / `autoFade`）、`Keyed` / `rememberKeyedList`、
  文本右键上下文菜单（`SokitsuContextMenu`）、`FlexibleDialog` / `EditDialog`（+`EditDialogContent`）、
  `RemoveButton` / `RemoveConfirmButton`、`ItemIcon`（`ui/item`，原生 modifier 绘制）、
  `KeySetter`（按键绑定捕获与设置）、`TabStrip`、`TableLayout` + `LazyTableLayout`、
  `SokitsuScreen` + `SokitsuScreenScale`、`BezierCurveEditor` + `BezierCurvePlot`
- **配置 GUI 专有件（已完成，随 §1）**：
  - 可拖拽排序列表 + `DragHandle`（旧 `ReorderableItemList`）：`reorderable/` 模块内嵌、
    `Icons.DragHandle` + `ui/editdialog/DragHandle.kt`、`ui/editdialog/EditDialogContentList.kt`
  - `SearchBar` / `SearchPanel`：配置搜索，即 `ConfigManagerWrapper` 的私有 `ConfigSearchField`
    （原先的分组导航现已改为 `TabRow` 页签导航 + 底部搜索栏）
  - 多行可扩展文本编辑器：`StringConfigWrapper` 编辑弹窗内的 `MultiLine(6..12)`，非独立组件（§A）
- **仍未做**：`ColorSchemeConfigWrapper`（§1，通用包装器）；曲线编辑器预设行卡片化
  （见「曲线编辑器 —— 遗留项」）
- **不做**：`CacheConfigWrapper`（§1，其配置项已随旧 Skia 图集消失）；hex 反向输入框（无出处，见 §B）

---

## 屏幕 / 对话框进出场动画配置（compose-minecraft 绑定）

> 落地于 `IGConfig.Gui.Screen` / `IGConfig.Gui.Dialog`，绑定 compose-minecraft 的
> `ScreenAnimationDefaults` / `ComposeScreenDefaults` / `DialogAnimationDefaults` /
> `DialogComposeScreenDefaults`；配置改动对**之后新建**的屏幕 / 对话框生效。

- [x] 屏幕：`fade_in_duration` / `fade_in_offset` / `fade` / `animation_enabled`、`disable_world_render`、
      `unlimit_framerate`（`eb3a879f`：无世界的 Compose 界面不再被原版压到 60 帧；开关经
      `ui/ComposeScreenHelper.kt` 暴露给每帧查询它的 `mixin/client/FramerateLimitTrackerMixin.java`）
- [x] 对话框：`scrim_color` / `fade_in_duration` / `initial_scale`、`disable_world_render`
- [x] **缓动 easing 配置化**（2026-09-20 `8d787350` 落地）：`IGConfig.Gui.Screen` / `Gui.Dialog` **各一份**
      `easing`（`configEnum`，`EasingPreset`：9 个预设含 `Custom`）+ `easing_custom`
      （`ConfigItem("easing_custom", CubicBezier.Standard, ConfigSerde.of(CubicBezier))`，两个控制点，
      **不是** `configVector4`）；`init()` 与 `observe` 里经 `EasingPreset.resolve(custom)` 解析成
      `ComposeEasing` 灌给上游 `ScreenAnimationDefaults.easing` / `DialogAnimationDefaults.easing`。
      详见下节「缓动 easing 配置化」。
- [x] **暂停游戏**：`pause_game`（2026-09-26 恢复）：`SokitsuScreen.create/open` 的 `pauseGame` 缺省值
      直接取该配置项，逐屏可覆盖。上游 `ComposeScreen` 的缺省是常量 `true`、没有全局默认值对象，
      所以本项只作为本仓屏幕入口的缺省值，不写回 CMP。
- [x] **不做全局配置**（属具体屏幕的构造参数，上游也没有对应默认值对象）：
      `closeOnEsc` / `density` / `exitParentOnOpen` / `renderParentScreen`

### 缓动 easing 配置化 —— 事实清单与定案（2026-09-20）

**上游签名（compose-minecraft 0.1.0 源码）**

| 位置 | 字段 |
|---|---|
| `ScreenAnimationDefaults`（`platform/screen/ScreenAnimation.kt:60`） | `durationMillis: Int = 220`、`slideFraction: Float = 0.08f`、`fade: Boolean = true`、`easing: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)` |
| `DialogAnimationDefaults`（`platform/screen/DialogComposeScreen.kt:48` 一带） | 同形状：`durationMillis`、`initialScale`、`scrimColor`、`easing: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)` |
| 消费点 | 屏幕 `tween<Float>(durationMillis, easing)`（`ScreenAnimation.kt:141`）；对话框 `tween<Float>(durationMillis, easing = easing)`（`DialogComposeScreen.kt:232`） |

要点：**一条 easing 同时作用于进场与退场**（上游没有 in / out 两段）；`Easing` 即
`androidx.compose.animation.core.Easing`，`CubicBezierEasing` 在 CMP 内嵌的 animation 库里可用。

**仓库里已有的同类机制**

- `util/math/easing/EasingCurve.kt`：
  - `enum class EasingCurve(mode)`：`linear / sine / quad / cubic / quart / quint / expo / circ /
    back / bounce / elastic` 共 11 条，`val easing: Easing` 映射到本仓 `util/math/easing/*` 的实现，
    自带手写 `Codec`（序列化为小写 `mode`）
  - `enum class EasingDirection(mode)`：`In` / `Out` / `InOut`，同样自带 `Codec`
  - `fun EasingCurve.ease(direction): Ease`；`typealias Ease = (Float) -> Float`
  - 已被 `AlertDialogAnimationMeta` 采用（meta jsonc 写 `"curve": "cubic", "direction": "out"`），
    由 `AlertDialogTheme.toDialogTransition()` 转成平台 `DialogTransition`（`Easing { fraction -> curve(fraction) }`）
- 配置侧现有构造器：`configFloat` / `configInt` / `configDuration` / `configBoolean` / `configColor` /
  `configEnum` / `configKeyCode` + `ConfigVector`(2i/2f/2d/3i/3f/3d) + `ConfigPairList`；**没有 vector4**；
  自定义结构可照 `AlertDialogMeta` 的写法用 `Codec.create<T>().field(...)` 组合
- 消费点现状（`IGConfig.kt`）：`Gui.Screen` = `fade_in_duration` / `fade_in_offset` / `fade` /
  `animation_enabled` / `disable_world_render` / `easing` / `easing_custom`；`Gui.Dialog` = `scrim_color` /
  `fade_in_duration` / `initial_scale` / `disable_world_render` / `easing` / `easing_custom`；
  两处都在 `init()` 与各项 `observe` 里把值灌给上游

**已定案（2026-09-20 实现，`8d787350`）**

走的是 **A + B 并存（即原候选 C）**，但预设集合不是直接复用 `EasingCurve`：

- 新增 `util/math/easing/EasingPreset.kt`：`standard` / `linear` / `ease_in` / `ease_out` / `ease_in_out` /
  `back_out` / `bounce_out` / `elastic_out` / `custom` 共 9 项，自带手写 `Codec`；
  `fun EasingPreset.resolve(custom: CubicBezier): Ease` 负责解析 —— 贝塞尔项复用 `CubicBezier` 常量
  （与曲线编辑器预设行同一套曲线），回弹类走 `EasingCurve`（不是三次贝塞尔，四点表达不了），
  `Custom` 取外部传入的四点。
- 新增 `util/math/easing/CubicBezier.kt`：`Linear` / `Standard` / `EaseIn` / `EaseOut` / `EaseInOut` 常量
  + `toEasing()`，自带 `Codec`（供 `ConfigSerde.of(CubicBezier)` 使用）。
- `IGConfig`：`easing` 用 `configEnum("easing", EasingPreset.Standard, EasingPreset)`；`easing_custom` 用
  `ConfigItem("easing_custom", CubicBezier.Standard, ConfigSerde.of(CubicBezier))` —— 即问题 4 选了
  「自定义 Codec 的结构体」而非两个 `configVector2f`，默认取 `CubicBezier.Standard`。
- GUI 控件：`ui/curve/BezierCurveEditor.kt` + `BezierCurvePlot.kt`（预设行 + 四点编辑 + 曲线预览）。
  `easing` 行走 `ui/configwrapper/EasingConfigWrapper.kt`（`80f521d9`：一行预设选择器，选中「自定义」即弹曲线编辑器，
  悬停控件显示当前自定义曲线的预览）；`easing_custom` 一整份 `CubicBezier` 不再单独出选项行，
  只作为 `easing` 行的另一份配置项存在。`ui/configwrapper/BezierCurveConfigWrapper.kt` 现在服务于
  **`CubicBezier` 值类型**的默认分发（`ConfigUIWrapper` 注册表命中），编辑体改用 `EditDialogContentCards` 卡片容器
  （`7f38d198`）。
- 原「待拍板」5 问的答案：①「方向」**不进配置**（上游只有一条曲线，出场为进场镜像）；②屏幕与对话框
  **各一份**；③只做枚举不够，**必须支持自定义四点**（平台默认曲线 `(0,0,0.2,1)` 要能在配置里表达）；
  ④自定义 Codec 结构体；⑤`easing` / `easing_custom` 的 label + comment 已落 8 个语言文件
  （`fade_in_*` 文案也已改为「进出场」，见 §D）。

**备查：当时评估过的候选**

- **A. 预设枚举**（复用 `EasingCurve` + `EasingDirection`）：零新增编解码，表达力限于 11×3 组合。
- **B. 三次贝塞尔四点**：表达力最强，需自定义 `Codec` 承载四个 float（无 vector4）。
- **C. A + B 并存**：枚举加 `Custom` 变体，选中才读四点 —— **最终采纳**。
- **D. 任意 lambda**：不可序列化，只能代码注入 —— 未采纳。

### 曲线编辑器 —— 遗留项（2026-09-20 记录）

- [ ] **预设行改卡片式**：`ui/curve/BezierCurveEditor` 的预设行现在是 `Column` 里一列等宽 `TextButton`
      （`BezierCurveEditor.kt` 预设循环，代码里没有 `FlowRow`）；改成卡片式（每个预设一张卡片、内含迷你曲线预览）。
      **用户明确押后**，先记录不做。

---

## 出处审计（2026-09-19，agent 整理；未删改上面任何条目）

> 基准：`6771fcea^` 的 `ui/` 文件树（`git ls-tree -r 6771fcea^ --name-only`）+ 迁移删除清单 `build/cw_del.log`。
> 取回任一文件：`git show 6771fcea^:common/src/main/kotlin/moe/forpleuvoir/ibukigourd/ui/<path>`。
> 行数为 `git show … | Measure-Object -Line` 实测值。

### A. 有出处（旧文件可对照）

| 条目 | 出处（`6771fcea^` 路径） | 行数 |
|---|---|---|
| 15 个 config wrapper（2026-09-20 已重建） | `ui/configwrapper/{Base, CacheConfigWrapper, ColorConfigWrapper, ColorSchemeConfigWrapper, ConfigGroupWrapper, ConfigManagerWrapper, ConfigUIWrapper, DurationConfigWrapper, EnumConfigWrapper, KeybindConfigWrapper, ListConfigWrapper, MapConfigWrapper, PrimitiveConfigWrapper, StringConfigWrapper, VectorConfigWrapper}.kt` | 合计 2,786 |
| ↑ 现状（2026-09-26） | 目录里 18 个文件 = 15 项里的 13 项（`Base.kt` 现名 `ConfigRowWrapper.kt`）+ 新增 5 个（`BezierCurveConfigWrapper` / `EasingConfigWrapper` / `ConfigElementEditor` / `ConfigRowTheme` / `ConfigState`）；`CacheConfigWrapper` **不做**，通用 `ColorSchemeConfigWrapper` 仍缺（见 §1） | — |
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
  **2026-09-20 确认：当前不需要，已从清单移除。**
- 全表复核后，无出处的**仅此一项**。

### C. 已删但本清单未提 —— 待确认是否已由现有组件 / compose-minecraft 基座覆盖

- `ui/preset/modifier/Backgourd.kt`（47）
- `ui/preset/state/RememberInputState.kt`（66）、`ui/preset/state/RememberTextFieldState.kt`（29）
- `ui/widget/ComposeWidget.kt`（80）
- `ui/util/ComposeScreenHelper.kt`（15）—— **同名文件已按新用途重写**（2026-09-26）：现为
  `ui/ComposeScreenHelper.kt`，只回答「当前屏幕是不是 `ComposeScreen`」并给原版补丁提供
  `unlimitFramerate` / `shouldUnlimitFramerate()`，与旧那份 15 行的屏幕辅助无关
- ⚠️ `ItemIcon` 的**隐性依赖** `ui/util/render/`（当前仓库零引用、迁移时全删）：
  `SkiaItemRenderHelper.kt`(409)、`ItemRenderAtlas.kt`(390)、`TextureAtlasPainter.kt`(217)、
  `OffscreenRenderTarget.kt`(156)、`AtlasRectAllocator.kt`(133)、`ItemAtlasPainter.kt`(67)，
  合计 **1,372 行** —— 即 `ItemIcon` 一项的真实成本远大于 249 行。

> 2026-09-22 复核：`ItemIcon` 已按原生 `Modifier.minecraftItem` / `Modifier.minecraftTooltip` 重做
> （见 §2），不再需要 `ui/util/render/` 那 1,372 行；其余条目由 compose-minecraft 基座覆盖。
>
> 2026-09-26 复核：`ui/overlay` **已按新基座重建**（与 26.1.2 同 API 的常驻覆盖层）——
> `OverlayService`（`register(key, present, content)` / `registerFrameListener` → `OverlayHandle`）、
> `OverlayHost`（自持 `MinecraftComposeScene`，场景根复用 `SokitsuScreenRoot` —— 覆盖层与屏幕同档缩放，
> 经 `mixin/client/GuiRendererOverlayMixin.java` 挂在 `GuiRenderer.render` TAIL 每帧驱动，
> 只转发鼠标 MOVE、`present` 全 false 时整帧跳过）、
> `OverlayContainer`（按注册顺序铺满窗口组合条目）。
> `Toast` 不再是自持场景的独立宿主：它经 `ui/sokitsu/toast/ToastOverlay.install()` 注册成覆盖层的
> 一个条目（`present = { !ToastHandler.isIdle }`）并用 `registerFrameListener` 推进 `ToastHandler.tick`，
> 提示自己的配色在条目内套 `SokitsuTheme`。工具提示（tooltip）仍在场景内（`LocalPopupHost`），不经覆盖层。

### D. 语言文件文案（2026-09-26 复核）

已解决的历史项：

- ✅ `gui.screen.fade_in_offset` / `fade_in_duration` 文案已改为「进出场」（8 个语言文件），
  并新增 `easing` / `easing_custom` 文案；
- ✅ `gui.screen.pause_game` 的标题 + 注释一度随「全局暂停不做配置」删除，2026-09-26 随配置项
  `pause_game`（`SokitsuScreen.pauseGame` 的缺省值）恢复，8 个语言文件的文案沿用 26.1.2 的原文；
- ✅ **12 个孤儿键（12 键 × 8 文件 = 96 条）已删除**：
  `ibukigourd.config.gui.cache.*`（组标题 + 注释 + 2 项 + 2 注释，共 6 键）随 §1 的 `CacheConfigWrapper`
  结案一并删除；`ibukigourd.config.gui.theme.{light_mode, color_scheme_seeds, color_scheme_seed}`
  （各含 `.comment`，共 6 键）是旧主题组的残留 —— 主题组虽已回归，但配置项换成了
  `mode` + `custom_scheme`，这 6 键在 Kotlin 侧零引用。
- ✅ `ibukigourd.config.open_screen*`（2 键）**不再算孤儿**：`IGConfig` 重新用
  `configKeybind("open_screen", …)` 接 `openIbukiGourdModScreen()`，文案已复活。

**待补的翻译缺口**（不是孤儿键，是没译）：

| 语言 | 缺的键 | 键数 |
|---|---|---|
| `fr_fr` | `gui.toast.duration`(+comment) / `gui.toast.max_visible`(+comment) / `color.paste_success` / `color.paste_failed` / 整段 selector 文案 | 14 |
| `ja_jp` / `ko_kr` / `ru_ru` / `zh_hk` / `zh_tw` | 整段 selector 文案（`#selector` 分节标记 + `selector.auto_max_items` / `selector.expand_style` 系列） | 各 8 |

删完后 `en_us` / `zh_cn` 各 194 键，其余 6 个语言各 180 ~ 186 键。
（`"#xxx"` 是语言文件里的**分节标记**约定，如 `#color` / `#config` / `#input`；六个语言缺 `#selector`
是整段 selector 文案都没译的结果，不是异常键。）

另：`common/src/devOnly/lang/config.json` 不是资源包文案，而是 `igtest` 指令经 `TranslationRecorder.dump`
生成的 dump（`TestCommand.kt`），内容仍停在旧主题组（含上述 6 个已删键、也没有 `mode` / `custom_scheme`）——
它靠游戏内重新 dump 刷新，不手工维护。

- 2026-09-20 复核已解决：「文本右键上下文菜单」已勾选（`ui/sokitsu/menu/SokitsuContextMenu.kt`）；
  「删除确认」重复条目已去重（保留「弹窗与菜单」一条）。
