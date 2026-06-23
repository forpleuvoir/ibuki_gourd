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