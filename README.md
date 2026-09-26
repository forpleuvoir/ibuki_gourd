# IBUKI GOURD

简体中文 | [English](/README-ENG.md)

<img src = "doc/logo.png" width ="256" alt="icon">

[IbukiGourd](https://modrinth.com/mod/ibukigourd) 是一个用 **Kotlin** 编写的 **Minecraft Fabric / NeoForge 前置库**，
本身不提供玩法内容，只为其它 MOD 提供基础设施：

| 能力 | 说明 | 入口 |
|---|---|---|
| **配置管理** | 委托式配置项、分组、序列化、自动加载 / 保存、类型化 GUI 编辑器 | `ClientModConfigManager` / `ClientModConfigHandler` |
| **配置 GUI** | 把配置管理器直接渲染成一个界面（搜索 / 页签导航 / 各类型控件 / 拖拽排序编辑弹窗） | `ConfigManagerWrapper` |
| **UI 组件库** | 像素风 Compose 组件（按钮 / 输入 / 选择器 / 表格 / 页签 / 弹窗 / 颜色选择 / 曲线编辑…） | `ui/sokitsu/**`、`SokitsuScreen` |
| **全局覆盖层** | 画在原版 HUD / 原版界面 / Compose 屏幕之上的常驻内容（Toast 即基于它实现） | `ui/overlay/OverlayService` |
| **指令 DSL** | Brigadier 的 Kotlin DSL 包装 | `CommandDispatcher.registerCommand` |
| **事件总线** | 生命周期 / 每帧 / 键鼠事件（基于 nebula `EventFactory`） | `ClientLifecycleEvent` / `ClientTickEvent` / `KeyboardEvent`… |
| **输入系统** | 组合键注册、触发模式（按下 / 长按 / 重复）、穿透与环境判定、冲突检测 | `Keybind` / `InputHandler` |
| **文本 DSL** | 文本构建（字面量 / 可翻译 / 内联样式解析）+ 排版辅助 | `buildText` / `InlineStyleText` / `Texts` |
| **i18n** | 语言键命名空间与文案录制 | `IGLang` / `TranslationRecorder` |
| **任务调度 / 工具** | 客户端 tick 任务、协程工具、向量 / 颜色 / 编解码 / 数学（贝塞尔 / 缓动） | `task/**`、`util/**` |

界面渲染基于自研的 [compose-minecraft](https://github.com/forpleuvoir/Compose-Minecraft)（内嵌 androidx Compose，
渲染直连原版 `GuiGraphics`，**不使用 Material3 / Skia 离屏渲染**），观感是像素风（整数倍放大 + `.aseprite` 素材）。

![ibukigourd](https://img.shields.io/modrinth/v/ibukigourd?label=Modrinth&color=8647B3)

## 如何使用

### 1. 添加仓库与依赖

```kts
repositories {
    maven("https://maven.forpleuvoir.moe/releases")   // 发布版
    maven("https://maven.forpleuvoir.moe/snapshots")  // 快照版
}

dependencies {
    // fabric 侧
    implementation("moe.forpleuvoir:ibukigourd-fabric-$minecraftVersion:$ibukigourdVersion")
    // neoforge 侧
    implementation("moe.forpleuvoir:ibukigourd-neoforge-$minecraftVersion:$ibukigourdVersion")
    // 纯逻辑（服务端 / 不需要界面时）
    implementation("moe.forpleuvoir:ibukigourd-common-$minecraftVersion:$ibukigourdVersion")
}
```

| 占位符 | 当前值 |
|---|---|
| `$minecraftVersion` | `26.2` |
| `$ibukigourdVersion` | `0.11.1+alpha` |

依赖的库（`nebula`、`compose-minecraft`、`reorderable`、`aseprite`）会随加载器构件内嵌
（fabric 走 `include`、neoforge 走 `jarJar`），通常无需另行声明；运行时还需要对应加载器的
Fabric API + Fabric Language Kotlin，或 Kotlin for Forge。

### 2. 三分钟上手

**配置**：继承 `ClientModConfigManager`，配置项用委托声明，注册到处理器后生命周期自动管理。

```kotlin
object YourModConfigs : ClientModConfigManager("your_mod_id", "config") {

    object Gui : ConfigGroup("gui") {
        var title by configString("title", "Hello")
        var scale by configFloat("scale", 1f, 0.5f, 2f)
        val openKey by configKeybind("open_key", Keyboard.RIGHT_SHIFT)
    }

    init { addConfig(Gui) }
}

// 在你的 mod 入口（或 ModInitialization 服务里）
ClientModConfigHandler.register(YourModConfigs)
```

**打开配置界面**：套一层 `SokitsuScreen`（主题 + 分辨率缩放）即可。

```kotlin
SokitsuScreen.open {
    ConfigManagerWrapper(YourModConfigs)
}
```

**指令**：

```kotlin
dispatcher.registerCommand("yourCommand") {
    literal("hello") {
        executes { source.sendSuccess({ Literal("hi!") }, false); 1 }
    }
}
```

**屏幕 + 提示**：

```kotlin
SokitsuScreen.open {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("hello minecraft")
        Button(onClick = { ToastHandler.showContent { Text("clicked") } }) {
            Text("Click me")
        }
    }
}
```

### 3. 文档

完整的开发者手册在 [`doc/manual/`](doc/manual/README.md)：

| 章节 | 内容 |
|---|---|
| [01 接入与依赖](doc/manual/01-接入与依赖.md) | 坐标、注册方式、元数据、平台抽象 |
| [02 配置系统](doc/manual/02-配置系统.md) | 配置项构造器、分组、观测、生命周期、序列化 |
| [03 配置界面](doc/manual/03-配置界面.md) | `ConfigManagerWrapper`、控件分发与自定义、文案 |
| [04 指令 DSL](doc/manual/04-指令DSL.md) | 注册、参数、建议、权限 |
| [05 事件总线](doc/manual/05-事件总线.md) | 生命周期 / tick / 键鼠事件与取消 |
| [06 输入与快捷键](doc/manual/06-输入与快捷键.md) | `Keybind`、触发模式、冲突检测、UI 编辑器 |
| [07 文本与 i18n](doc/manual/07-文本与i18n.md) | 文本 DSL、内联样式、排版、语言键 |
| [08 屏幕与 UI 组件](doc/manual/08-屏幕与UI组件.md) | `SokitsuScreen`、主题与像素缩放、组件清单 |
| [09 覆盖层与提示](doc/manual/09-覆盖层与提示.md) | 全局覆盖层、Toast、Tooltip |
| [10 任务与工具](doc/manual/10-任务与工具.md) | tick 调度、协程、向量 / 颜色 / 数学工具 |
| [11 多加载器与平台抽象](doc/manual/11-多加载器与平台抽象.md) | `PlatformHelper`、服务注册、跨加载器写法 |
| [12 常见问题](doc/manual/12-常见问题.md) | 踩坑清单与设计约定 |

> 想直接从源码看用法：`common/src/devOnly/` 下有 30+ 个组件测试屏（`SokitsuTestScreen` 主菜单），
> 是仓库里最全的活例子。

## 许可

本项目以 MIT 许可发布，见 [LICENSE](LICENSE)；内嵌的第三方源码见 [NOTICE.md](NOTICE.md)。
