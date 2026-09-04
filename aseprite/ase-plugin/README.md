# sokitsu-ase-plugin

Aseprite 扩展：为 `.ase` 的图层附加 **Sokitsu 纹理元数据**，供运行时（`common` 直读 `.ase` 构建图集）消费。

## 架构

- **一个 `.ase` = 一个 `SokitsuTexture`**，文件内图层 = 一个 `TextureLayer`，atlas 图集由多个 `.ase` 缝合（运行时）
- `.ase` 内的帧 = 动画帧（图层配置全局，动画只换位图）
- 组件状态 = 多个 `.ase`（状态 → 纹理 id 的映射由组件层决定）
- 运行时不再有烘焙 JSON/PNG 中间产物，源文件即资源；资源包覆盖 `.ase` 即换肤

## 安装

```bash
cd tools/ase-plugin
zip sokitsu.aseprite-extension package.json sokitsu.lua
```

双击生成的 `.aseprite-extension` 安装，或 `Edit > Preferences > Extensions > Add Extension`。

**要求 Aseprite 1.3 或更高**（`object.properties` / Dialog 动态显隐均需 1.3+）。

## 用法

右键图层（图层组与 tilemap 被跳过），菜单中紧邻内置「图层属性」出现：

- **Sokitsu 纹理设置...** — 设置面板
- **导入 Sokitsu 调色板** — 把参考**灰阶**写入当前精灵前 11 个 palette 槽（`Color{gray}` 灰度定义，亮度 255→5 每档 −25，按亮度排序；作画统一取色，避免跨文件明暗偏差）
- **Sokitsu 保存诊断** — 调试：对活动图层执行 全量写入 → 读回 并复制到剪贴板
- **清除 Sokitsu 设置** — 移除元数据

多选图层时批量写入（合并为一次 undo）；保存后自动回读校验。

### 快捷键

插件命令与 Aseprite 原生命令一样可在 **`Edit > Keyboard Shortcuts`** 中绑定按键：

1. 打开 Keyboard Shortcuts，搜索框输入 `Sokitsu`
2. 点选 **「Sokitsu 纹理设置...」**（命令 id `SokitsuLayerSettings`）
3. 按你想要的组合键（如 `S` 或 `Alt+S`）完成绑定

绑好后工作流：选中图层 → 按快捷键 → 面板直接编辑当前图层（单选始终锁定 `activeLayer`）。

### 面板字段

| 字段 | 说明 |
|---|---|
| 作为 Sokitsu 图层导出 | 关闭则不参与图集（未配置过的图层默认导出） |
| 着色：模式 | `Mask` / `Tint`（默认） / `HueShift` → `TextureTintMode`，下拉旁有随选提示 |
| 着色：同时替换 Alpha | 勾选则 α 替换为主题色档的 α（默认保留纹理 α） |
| 着色：色彩层级 | 未绑定 / `Outline` / `Shadow` / `Dark` / `Base` / `Highlight` → `ColorLevel` |
| 填充：模式 | `stretch` / `ninepatch` / `tile` → `TextureFill`；按模式**动态显隐**下方区块 |
| 平铺缩放 | 仅 `tile` |
| 九宫格 Slice | 下拉列出当前文件全部 Slice，默认选中 `<图层名>:sokitsu`（命名约定，非强制）；「导入所选 Slice」推导 border |
| 左/上/右/下 + 向外 | 数值框填绝对值，「向外」勾选则存负数（向外渲染） |
| 禁用切片 | 9 个复选框按 3×3（编号 0–8 对应九宫格空间位置） |

### 着色语义（colorLevel = 主题色 T，纹理像素色 = C，默认模式 Tint）

- **Mask**：完全替换为 T（C 只当剪影/遮罩，alpha 生效）
- **Tint**：`HSV(T.H, T.S, C.V)` — 主题给色相饱和，纹理只贡献明度结构（**纹理画灰阶**）
- **HueShift**：`HSL(T.H, C.S, C.L)` — 只把色相转到主题，纹理自身饱和/亮度保留（**纹理画彩色**）

三种模式 α 默认取 C.a；勾选「同时替换 Alpha」则取 T.a（实心主题覆盖等场景）。

## 存储

extension properties，pluginKey `forpleuvoir/sokitsu`（必须与 package.json 的 `publisher`/`name` 一致）。
值以强类型二进制存于 0x2020 properties 段（bool / int32 / float / string / vector），非 JSON。

## 已知限制

- 无法向内置「图层属性」面板注入控件，只能以独立对话框呈现；命令挂 `layer_popup_properties` 组
- 混合模式仅支持 Normal（其他烘焙/运行时降级并告警）
- 面板动态显隐依赖 Dialog `modify{visible}` + `onchange`（1.3+）

## 参考

- 解析与解码：`ase/` 模块（Ase.kt / AseParser.kt / AseRenderer.kt）
- 纹理定义：`common/.../ui/sokitsu/texture/SokitsuTexture.kt`
- 格式规范：<https://github.com/aseprite/aseprite/blob/main/docs/ase-file-specs.md>
