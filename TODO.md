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

- [ ] 分隔线（旧 `HorizontalDivider`）
- [ ] `Chip` / `AssistChip`（颜色预览、时长、键位标签）
- [ ] 进度条（旧 `LinearProgressIndicator`，缓存加载用）
- [ ] 自动隐藏滚动条（旧 `AutoHideScrollbar`；现只有 `verticalScroll`，无滚动条）
- [ ] `Toast`（操作反馈，旧 `ui/toast/` 5 个文件）

**弹窗与菜单**

- [ ] `Dialog` 体系：Alert / 确认（删除确认）/ 可伸缩编辑弹窗（旧 `FlexibleDialog`、`EditDialog*`）
- [ ] `DropdownMenu` + 菜单项（旧 `ExposedDropdownMenuBox`）
- [ ] 文本右键上下文菜单（旧 `Material3TextContextMenu`）

**输入与选择**

- [ ] `Selector` / `EnumSelector`（下拉、可搜索选择）
- [ ] `ColorPicker`（HSV 面板 + 色相条 + alpha + 透明棋盘 + hex 输入/粘贴）
- [ ] `KeySetter`（按键捕获、组合键显示；旧 558 行）
- [ ] 多行可扩展文本编辑器（旧 `ExpandableStringContentEditor`）

**列表**

- [ ] 可拖拽排序列表 + `DragHandle`（旧 `ReorderableItemList`）
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
`Tooltip` + `BasicTooltip` / `RadioButton`(+`RadioButtonGroup`) / 主题与 `SokitsuThemeMeta` 体系。

### 4. 待拍板的设计点

- [x] **图标方案**：已定为 **`.aseprite` 像素图标**（`texture/sokitsu/icon/`，独立图集 `icon`）+ `Icons` 属性常量集；
      旧版的 Material Symbols 矢量图标（32 个 `ImageVector`）不采用
- [ ] **Dialog / Chip / 菜单皮肤**：先用纯色 + 描边，还是先补素材（`ui/dialog/`、`ui/chip/`）？
- [ ] **ColorPicker 交互模型**：渐变面板拖拽（旧版做法）还是像素风调色板格子点选？

### 5. 建议实现顺序（每层都能先在测试屏里验）

- **已完成**：`FlatButton` 底座 + `IconButton` / `TextButton`、`Icon` + `Icons`（36 个）
- **下一步**：分隔线 + `Chip` → `Dialog` / `DropdownMenu` → `Selector` / `KeySetter` →
  `ColorPicker` → 可拖拽列表 → 包装器框架（`ConfigRowWrapper` → 各类型 wrapper → `ConfigManagerWrapper`）
