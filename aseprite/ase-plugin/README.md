# sokitsu-ase-plugin

Aseprite 扩展：为 `.ase` 的图层附加 **Sokitsu 纹理元数据**，供运行时（`common` 直读 `.ase` 构建图集）消费。

## 架构

- **一个 `.ase` = 一个 `SokitsuTexture`**，文件内图层 = 一个 `TextureLayer`，atlas 图集由多个 `.ase` 缝合（运行时）
- `.ase` 内的帧 = 动画帧（图层配置全局，动画只换位图）
- 组件状态 = 多个 `.ase`（状态 → 纹理 id 的映射由组件层决定）
- 运行时不再有烘焙 JSON/PNG 中间产物，源文件即资源；资源包覆盖 `.ase` 即换肤

## 安装

```bash
cd aseprite/ase-plugin
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
| 着色：模式（`tint`） | **怎么合成**：`Mask`（默认，纯槽位色替换，纹理只当剪影）/ `Multiply`（槽位色 × 纹理灰度，正片叠底）/ `Passthrough`（纹理原样、不参与染色）；旧模式名 `Flat`/`Tint`→Mask、`Hsv`/`Luminance`/`Hsl`→Multiply 读取时自动映射 |
| 着色：同时替换 Alpha | 勾选则 α 替换为主题色档的 α（默认保留纹理 α）。⚠️ 该键当前运行时**未读取**，仅保留标注 |
| 颜色槽位（`level`） | **取哪个颜色**：`tone`（组件主色）/ `outline` / `shadow` / `surface` / `primary` … 任意 ColorScheme 槽位名 / `none`（无颜色，顶点色取白）；留空 = 未标注（运行时按 `tone`） |
| 填充：模式 | `stretch` / `ninepatch` / `tile` → `TextureFill`；按模式**动态显隐**下方区块 |
| 平铺缩放 | 仅 `tile` |
| 九宫格 Slice | 下拉列出当前文件全部 Slice，默认选中 `<图层名>:sokitsu`（命名约定，非强制）；「导入所选 Slice」推导 border |
| 左/上/右/下 + 向外 | 数值框填绝对值，「向外」勾选则存负数（向外渲染） |
| 禁用切片 | 9 个复选框按 3×3（编号 0–8 对应九宫格空间位置） |
| 中心格填充 | 仅 `ninepatch`：`stretch`（中心格拉伸，默认）/ `tile`（中心格按源图平铺） |
| 中心格平铺缩放 | 仅中心格填充 = `tile`：tile 单元倍率，与运行时像素放大倍率相乘（存 `centerScale`，≥1） |

### 九宫格中心格

九宫格的中心格（编号 4）默认拉伸整格源图；改为 `tile` 后按中心格源图 1:1 平铺，
tile 单元 = 中心格源像素 × 运行时像素放大倍率 × `centerScale`，中心区域不足一个单元的部分按源 UV 截断。
八个边角分片始终拉伸。缺 `centerFill` 键的旧文件等价于 `stretch`。

### 着色语义（槽位色 = T，纹理像素色 = C）

两个下拉**正交**：模式（`tint`）决定 T 与 C 怎么合，颜色槽位（`level`）决定 T 取哪个色。

- **Mask**：`rgb = T` —— 完全替换为槽位色（C 只当剪影/遮罩，alpha 生效）；适合描边、纯色块
- **Multiply**：`rgb = T × C` —— 正片叠底：槽位色 × 素材灰度，白 = 槽位色原样、越黑越暗；
  明暗全部由灰阶表达，亮暗主题只换槽位色值
- **Passthrough**：`rgb = C` —— 原样输出，T 被忽略（图标/贴图等；alpha 恒取纹理自身）

颜色槽位（`level` 下拉）= 主题颜色槽位名：`tone`（组件主色）、`outline`、`shadow`、
`surface`、`primary` 等任意 ColorScheme 槽位名；`none` = 无颜色（T 取白，Multiply 下即灰度原样）；
留空 = 未标注，运行时按 `tone` 处理。α 恒取 C.a（纹理自身 alpha）。

> 任意组合都成立，例如「`primary` 色 + Multiply」「`tone` 色 + Mask」；`Passthrough` 下颜色槽位不参与。

## 存储

extension properties，pluginKey `forpleuvoir/sokitsu`（必须与 package.json 的 `publisher`/`name` 一致）。
值以强类型二进制存于 0x2020 properties 段（bool / int32 / float / string / vector），非 JSON。

键：`enabled` / `fill` / `tint` / `tintAlpha` / `level` / `scale`（仅 tile）/
`borderLeft`、`borderTop`、`borderRight`、`borderBottom`、`disableSlice`、`centerFill`、`centerScale`（仅 ninepatch）。

## 已知限制

- 无法向内置「图层属性」面板注入控件，只能以独立对话框呈现；命令挂 `layer_popup_properties` 组
- 混合模式仅支持 Normal（其他烘焙/运行时降级并告警）
- 面板动态显隐依赖 Dialog `modify{visible}` + `onchange`（1.3+）

## 参考

- 解析与解码：`ase/` 模块（Ase.kt / AseParser.kt / AseRenderer.kt）
- 纹理定义：`common/.../ui/sokitsu/texture/SokitsuTexture.kt`
- 格式规范：<https://github.com/aseprite/aseprite/blob/main/docs/ase-file-specs.md>
