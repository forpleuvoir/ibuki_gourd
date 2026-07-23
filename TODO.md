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
