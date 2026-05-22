# Compose UI 适配计划

## 目录

```
common/.../ui/
  ├── compose/               ← 复制/参考 Compose UI 源码（Apache 2.0）
  │   ├── constraints/
  │   ├── geometry/
  │   ├── unit/
  │   ├── modifier/
  │   ├── layout/
  │   └── foundation/
  ├── node/                  ← 你自己的节点系统（MIT）
  ├── applier/               ← Compose Runtime 连接代码（MIT）
  └── rendering/             ← Minecraft 渲染桥接（MIT）
```

---

## 第 1 批：基础值类型（无渲染依赖，直接复制/参考）

- [ ] **Constraints** — `compose.ui:ui:unit/Constraints.kt`
  - `Constraints(minWidth, maxWidth, minHeight, maxHeight)`
  - 工厂方法：`Constraints.fixed(w, h)`、`Constraints.fit()` 等
- [ ] **IntOffset** — `compose.ui:ui:unit/IntOffset.kt`
  - `IntOffset(x, y)`、`Offset(x, y)`
- [ ] **IntSize** — `compose.ui:ui:unit/IntSize.kt`
  - `IntSize(width, height)`、`Size(width, height)`
- [ ] **Dp / Density** — `compose.ui:ui:unit/Dp.kt`
  - `Dp` 值类型、`Density` 接口（`toPx()`、`toDp()`）
- [ ] **Alignment** — `compose.ui:ui:unit/Alignment.kt`
  - `Alignment.TopStart`、`Center`、`BottomEnd`、`CenterStart` 等
  - 对齐算法的数学公式
- [ ] **Arrangement** — `compose.ui:ui:layout/Arrangement.kt`
  - `Arrangement.Start`、`Center`、`SpaceBetween`、`SpaceEvenly` 等
- [ ] **Offset / Size 几何类型** — `compose.ui:ui:geometry/`
  - `Offset(x, y)`、`Size(w, h)`、操作符重载

> **标注**：以上文件头部保留 Apache 2.0 版权声明，代码可以不做任何修改。

---

## 第 2 批：Modifier 系统（核心架构）

- [ ] **Modifier 接口** — `compose.ui:ui:Modifier.kt`
  - `Modifier` 接口、`then()` 链式组合、`CombinedModifier`、`Modifier.Element`
- [ ] **Modifier.Node 基类** — `compose.ui:ui:modifier/ModifierNode.kt`
  - `Modifier.Node`、`ModifierNodeOwnerScope`
- [ ] **LayoutModifierNode** — 实现 `Modifier.size()`、`Modifier.padding()` 等布局装饰
  - 参考 `compose.ui:ui:modifier/layout/LayoutModifierNode.kt`
- [ ] **DrawModifierNode** — 实现 `Modifier.background()`、`Modifier.border()` 等绘制装饰
  - 参考 `compose.ui:ui:modifier/draw/DrawModifierNode.kt`

> **标注**：Modifier 系统和 Modifier.Node 机制是纯 Kotlin 抽象，可以直接复制。DrawModifierNode 需要改渲染实现。

---

## 第 3 批：布局系统

- [ ] **MeasurePolicy** — `compose.ui:ui:layout/MeasurePolicy.kt`
  - `MeasurePolicy.measure(Measurable, Constraints): MeasureResult`
  - `IntrinsicMeasurable` + 内在测量接口
- [ ] **Layout composable** — `compose.ui:ui:layout/Layout.kt`
  - `@Composable fun Layout(content, measurePolicy)`
  - 核心：`ComposeNode<LayoutNode>` + 测量+放置流程
- [ ] **Row 布局** — `compose.foundation:foundation:layout/ColumnRow.kt`
  - `Row` 的水平排列算法（测量子节点 → 按 Arrangement 分配位置）
- [ ] **Column 布局** — `compose.foundation:foundation:layout/ColumnRow.kt`
  - `Column` 的垂直排列算法
- [ ] **Box 布局** — `compose.foundation:foundation:layout/Box.kt`
  - `Box` 的叠放布局 + Alignment 对齐
- [ ] **Intrinsic 测量** — 用于 Row/Column 的 `intrinsicMin/MaxWidth/Height`
  - 参考 `compose.ui:ui:layout/IntrinsicMeasurable.kt`

> **标注**：测量和放置的纯算法代码可以直接复制。布局循环中调用 `.place()` 时需要换成更新你节点的 x/y 坐标。

---

## 第 4 批：常用 Modifier 实现

- [ ] **Modifier.size()** — `compose.foundation:foundation:Modifier.size.kt`
  - `width(d)`, `height(d)`, `size(w, h)`, `fillMaxWidth()` 等
- [ ] **Modifier.padding()** — `compose.foundation:foundation:Modifier.padding.kt`
  - `padding(all)`, `padding(start, top, end, bottom)` 等
- [ ] **Modifier.offset()** — `compose.foundation:foundation:Modifier.offset.kt`
  - `offset(x, y)`, `offset { ... }`
- [ ] **Modifier.background()** — 需要自己实现
  - 参考 `compose.foundation:foundation:Modifier.background.kt`
  - 复制接口签名，渲染改调 Minecraft 的 `PoseStack` + 矩形绘制
- [ ] **Modifier.border()** (可选) — 同上，渲染部分需要自己实现
- [ ] **Modifier.clip()** (可选) — 裁剪到形状，渲染较复杂

> **标注**：布局类 Modifier（size/padding/offset）的算法直接复制。绘制类 Modifier（background/border/clip）只复制结构，渲染改为对接 Minecraft。

---

## 第 5 批：节点系统和 Runtime 桥接

- [ ] **GuiNode** — 节点基类
  - `children: MutableList<GuiNode>`
  - `x, y, width, height: Float`
  - `measure(constraints): Unit` / `place(x, y): Unit`
  - `draw(...)` — 抽象方法，子类实现渲染
- [ ] **LayoutNode** — 参考 Compose 的 LayoutNode，整合 modifier 链
  - 需要处理：modifier 折叠、测量缓存、放置缓存
- [ ] **GuiNodeApplier** — 继承 `AbstractApplier<GuiNode>`
  - `insertTopDown(index, instance)`
  - `remove(index, count)`
  - `move(from, to, count)`
  - `onClear()`
- [ ] **setContent** 入口
  - `GuiNode.setContent(scope, composable): NodeComposition`
  - `NodeComposition.dispose()` — 清理 composition
- [ ] **CompositionLocal 支持**
  - `staticCompositionLocalOf` / `compositionLocalOf`
  - `CompositionLocalProvider`

---

## 第 6 批：Minecraft 渲染对接

- [ ] **MinecraftCanvas** (或等效命名)
  - 封装 `PoseStack`、`GuiGraphics`、`VertexConsumer`
  - 提供 `drawRect()`、`drawText()`、`drawTexture()` 等方法
- [ ] **DrawScope 适配**
  - 将 Compose 的 `DrawScope` 概念映射到 Minecraft Canvas
- [ ] **字体/文本布局**
  - 对接 Minecraft 的 `Font` + `FormattedCharSequence`
- [ ] **事件处理**
  - 鼠标点击 → 命中测试 → 找到对应 GuiNode
  - 键盘输入 → 焦点管理
  - 对接 `Screen.mouseClicked()`、`keyPressed()` 等

---

## 许可证注意事项

```markdown
common/.../ui/compose/ 下的所有文件：
  文件头部保留：
  /*
   * Copyright 2020 The Compose Authors (JetBrains / AndroidX)
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * ...
   */

common/.../ui/node/、applier/、rendering/ 下的文件：
  你的原创代码，MIT License
```

在项目根目录 `NOTICE.md` 中声明：
```markdown
This project includes code adapted from JetBrains Compose Multiplatform,
licensed under the Apache License, Version 2.0.
See ui/compose/ for the affected files.
```
