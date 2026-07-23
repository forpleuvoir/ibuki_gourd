# AGENTS.md

> 本文件为 AI 编程助手（Cursor / Codex / Junie / Jules / Claude 等）提供 IbukiGourd 项目的上下文与协作规范。
> 修改代码前请先阅读本文件。若项目结构发生重大变化，请同步更新。

## 项目概述

**IbukiGourd**（`ibukigourd`）是一个用 **Kotlin** 编写的 Minecraft **多加载器（Fabric + NeoForge）模组库**，本身不提供玩法内容，而是为其它 MOD 提供**前置功能**：

- **配置管理**（客户端 / 服务端，含序列化、自动扫描、保存/加载）
- **配置 GUI**（基于 **JetBrains Compose Multiplatform + Material3 + Skia**，非原生 `GuiGraphics`）
- **指令 DSL**（Brigadier 包装）
- **事件总线**
- **输入系统**（键位 / 鼠标）
- **文本 DSL**（含内联样式文本解析）
- **渲染、任务调度、i18n 等通用工具**

核心抽象（`ConfigManager` / `Config` / `Event` / `EventFactory` / `Initializable` 等）来自**同级依赖库 `moe.forpleuvoir.nebula`**（`nebula-common` / `nebula-config` / `nebula-event`），IbukiGourd 将其适配到 Minecraft 环境。修改这些类型时注意其基类不在本仓库内。

## 技术栈与版本

| 项 | 值 |
|---|---|
| 语言 | Kotlin `2.4.0`（主），少量 Java（mixin） |
| JVM | Java `25` |
| Minecraft | `26.1.2` |
| Fabric | Loader `0.19.2` / API `0.146.1+26.1.2` / Fabric Kotlin `1.13.12+kotlin.2.4.0` / Loom `1.16-SNAPSHOT` |
| NeoForge | `26.1.2.22-beta` / moddev `2.0.141` / Kotlin for Forge `6.3.0` |
| Mixin | `0.8.5` + MixinExtras `0.5.3` |
| UI | Compose `1.11.0` + Material3 `1.11.0-alpha07` + MaterialKolor `4.1.1`（Desktop，渲染桥接到 MC，排除 `material-desktop`） |
| 依赖库 | `nebula` `0.3.14` |
| 构建工具 | Gradle（Kotlin DSL），版本目录 `gradle/libs.versions.toml` |

版本号集中在 `gradle.properties`（`group` / `mod_id` / `mod_name` / `version` 等）与 `gradle/libs.versions.toml`。`version` 当前为 `0.11.1+alpha`。

## 仓库结构

多加载器标准三段式：**common / fabric / neoforge**。

```
ibuki_gourd/
├── common/                 # 共享逻辑（绝大部分 Kotlin 代码）
│   ├── src/main/kotlin/moe/forpleuvoir/ibukigourd/   # 见下“源码包结构”
│   ├── src/main/java/.../mixin/                       # mixin（含 client 子包）
│   ├── src/main/resources/
│   │   ├── META-INF/services/                         # ServiceLoader 注册（ModInitialization 等）
│   │   ├── META-INF/accesstransformer.cfg             # NeoForge AT
│   │   ├── ibukigourd.classtweaker                    # Fabric access widener
│   │   └── assets/ibukigourd/{lang,shaders,texture}   # 资源（lang/shader/贴图）
│   ├── src/devOnly/...                                # 仅开发环境运行的测试代码（不发布）
│   └── build.gradle.kts                               # id("multiloader-common") + neoforgedModDev
├── fabric/                # Fabric 加载器实现
│   ├── src/main/kotlin/.../FabricIbukiGourd(.kt)      # ModInitializer 入口
│   ├── src/main/kotlin/.../FabricIbukiGourdClient     # client 入口
│   ├── src/main/kotlin/.../platform/FabricPlatformHelper
│   ├── src/main/kotlin/.../compat/ModMenuImpl         # ModMenu 集成
│   ├── src/main/resources/fabric.mod.json             # 入口 + custom.ibukigourd.package 元数据
│   ├── src/main/resources/ibukigourd.fabric.mixins.json
│   └── build.gradle.kts                               # id("multiloader-loader") + fabricLoom
├── neoforge/              # NeoForge 加载器实现
│   ├── src/main/kotlin/.../NeoforgeIbukiGourd(.kt)    # @Mod 入口（class）
│   ├── src/main/kotlin/.../NeoforgeIbukiGourdClient
│   ├── src/main/kotlin/.../platform/NeoforgePlatformHelper
│   └── build.gradle.kts                               # multiloader-loader + neoforgedModDev
├── buildSrc/
│   ├── build.gradle.kts                               # 预编译 Groovy 插件 + Kotlin/Compose Gradle 插件
│   └── src/main/groovy/
│       ├── multiloader-common.gradle                  # 公共：kotlin/compose 插件、Java25、processResources 模板、publishing
│       └── multiloader-loader.gradle                  # 加载器侧
├── gradle/libs.versions.toml                          # 版本目录
├── gradle.properties                                  # mod 元数据占位符
├── settings.gradle.kts                               # include("common","fabric","neoforge")
├── build.gradle.kts                                  # 顶层：info.toml 生成 + 发布/构建任务
├── ibukigourd.info.toml                               # 给 shields.io 用的版本信息（构建生成，勿手改）
├── README.md / README-ENG.md                          # 使用说明（含接入示例）
├── CHANGE_LOG.md / TODO.md / NOTICE.md / DOC.md       # 变更/计划/许可/进度
└── .gitignore
```

> `runs/`、`build/`、`modJar/`、`.gradle/`、`.idea/`、`.kotlin/` 等已在 `.gitignore` 中，勿提交。
> `src/test/**` 默认被忽略；开发期测试代码放在 `src/devOnly/`。

## 源码包结构（`common/.../moe/forpleuvoir/ibukigourd/`）

| 包 | 职责 |
|---|---|
| `(根)` | `IbukiGourd`（common 入口 `object`，`MOD_ID="ibukigourd"`）、`IbukiGourdClient`（client 入口） |
| `api` | 框架 SPI：`Tickable`、`ClientResourceReloaderListener` |
| `command` / `command.dsl` | Brigadier 包装；DSL：`ArgumentScope`、`RequiredArgumentScope`、`createCommand`、`registerCommand`、`@CommandDslMark` |
| `config` / `config.item` | 配置框架：`ModConfigManager`、`ModConfigHandler`、`ClientModConfigManager/Handler`、`ServerModConfigManager/Handler`；`ConfigKeyBind` / `ConfigVector` / `ConfigPairList` 等 |
| `event` / `event.events.{client,server}` | 事件（基于 nebula `EventFactory`）：`ClientLifecycleEvent`、`ClientTickEvent`、`ServerLifecycleEvent`、`ServerCommandRegistrationEvent`、`MouseEvent`、`KeyboardEvent` 等；`CancellableContext` |
| `input` | `Keybind` / `KeybindSetting` / `KeyCode` / `KeyTriggerTiming` / `KeyEnvironment` / `Keyboard` / `MouseButton` / `MouseCursor` / `InputHandler` |
| `lang` | `IGLang`（i18n 键命名空间中心）、`MiscLang` / `ColorLang` / `ConfigWrapperLang` / `InputLang`、`TranslationRecorder` |
| `mod` / `mod.config` / `mod.ui` | IbukiGourd 自身的配置与屏幕：`IGConfig`、`ModScreen`、`IbukiGourdScreen` |
| `platform` / `platform.services` | 多加载器抽象：`Services`（`java.util.ServiceLoader` 加载 `PlatformHelper` + 全部 `ModInitialization`）、`PlatformHelper`、`INITS` / `PLATFORM` |
| `render` | `BaseExtension` 等渲染辅助 |
| `task` | 调度：`TickTask` / `TickTaskScheduler` / `ClientTickTaskScheduler` / `TaskExecutor` / `SimpleTaskExecutor` |
| `text` / `text.style` / `text.inlinestyletext{,.modifier}` | 文本 DSL：`TextDSL` / `StyleDSL` / `HoverEventDsl` / `InlineStyleTextParser` + `ColorModifier` / `ClickEventModifier` / `HoverEventModifier` / `DecorationModifier` 等 |
| `ui` | **Compose 配置 GUI**（见下） |
| `util` / `util.math{,.bezier}` | `ModLogger` + `logger()` 扩展；向量扩展（`Vector2f/2d/2i/3f/3d/3i`）；`Easing` / `Bezier` / 缓动；`PackScanner`、`PageHelper`、`FixedSizeQueue`、`LateInitValue`、`NebulaOps`、`SimpleResourceReloaderListener` |

### `ui` 子结构（Compose GUI）

- `ui/configwrapper/` —— 配置项包装器：`ConfigManagerWrapper`、`GroupConfigsWrapper`、`ConfigsWrapper`，及按类型：`StringConfigWrapper`、`ColorConfigWrapper`、`EnumConfigWrapper`、`ListConfigWrapper`、`MapConfigWrapper`、`KeybindConfigWrapper`、`DurationConfigWrapper`、`ColorSchemeConfigWrapper`、`PrimitiveConfigWrapper`、`ConfigGroupWrapper`、`ReorderableItemList`
- `ui/preset/`（含 `preset/modifier`）—— 预置组件：`Text`、`Tip`/`TipBox`、`SearchBar`、`ColorButton`、`ColorPicker`、`NumberField`、`NumberSlider`、`Selector`、`KeySetter`、`ItemIcon`、`Texture`、`BlitTexture`；modifier：`Tooltip`、`Backgourd`
- `ui/toast/` —— `Toast`、`ToastHandler`、`ToastContainer`、`ToastStrategy`、`ToastAnimation`
- `ui/overlay/` —— `OverlayHost`、`OverlayService`、`OverlayContainer`
- `ui/icon/`（`default/`、`filled/`）—— `Icons` 对象 + 大量 Material 风格图标
- `ui/scene/`（含 `internal/`）—— Compose 嵌入 MC：`ComposeSceneHost`、`DefaultComposeSceneHost`、`ComposeSceneFactory`，及内部 bridge/lifecycle/renderer
- `ui/skia/`（含 `internal/`）—— `SkiaContext`、`SkiaSurface`、内部 GPU frame/texture 管理
- `ui/platformcontext/` —— `MinecraftPlatformContext`、`IbukiGourdTheme`、剪贴板、文本右键菜单、窗口信息、composition-local providers
- `ui/util/render/` —— `SkiaItemRenderHelper`、`OffscreenRenderTarget`

## 关键入口点

- **common 入口**：`IbukiGourd.init()` —— 遍历 `INITS`（平台 `ModInitialization`，经 ServiceLoader 收集）+ 本地 `inits`（如 `ServerModConfigHandler`）调用 `init()`。
- **client 入口**：`IbukiGourdClient.init()` —— 注册 `ClientModConfigHandler`、`IGConfig`，并在 `ClientLifecycleEvent.Starting` 时初始化 `SkiaContext` / `ComposeSceneWarmup` / `OverlayHost`。
- **fabric**：`FabricIbukiGourd : ModInitializer`（委托 `IbukiGourd.init()`），`FabricIbukiGourdClient`，`compat/ModMenuImpl`。入口在 `fabric.mod.json`。
- **neoforge**：`@Mod(IbukiGourd.MOD_ID) class NeoforgeIbukiGourd`，在 `FMLCommonSetupEvent` 调 `IbukiGourd.init()`。
- **平台抽象**：`Services` 通过 `ServiceLoader` 解析 `PlatformHelper`（fabric/neoforge 各自实现）与所有 `ModInitialization`。`PlatformHelper.getIGModClasses()` 反射扫描各 MOD 元数据中的 `package` 键（fabric 为 `custom.ibukigourd.package`，neoforge 为 `modproperties.$modId.package`），加载其 KClass（跳过 `.mixin` 包）——**这是 IbukiGourd 发现消费方 MOD 中被注解的配置/屏幕类的机制**。

## 构建与常用任务

> Windows 默认 shell 为 `cmd.exe`，请使用 `gradlew.bat`（PowerShell 下用 `.\gradlew`）。

```bash
# 构建 fabric + neoforge 的 jar，输出到 modJar/<mc>/<version>/
gradlew.bat buildAllModJar

# 发布（需仓库凭据）
gradlew.bat publishModToSnapshotsRepository   # 快照仓库 maven.forpleuvoir.moe/snapshots
gradlew.bat publishModToReleasesRepository    # 发布仓库 maven.forpleuvoir.moe/releases
gradlew.bat publishModToLocalRepository       # Maven Local

# 单模块构建/测试
gradlew.bat :fabric:build
gradlew.bat :neoforge:build
gradlew.bat :common:test
```

- 开发期运行配置由 `fabric`/`neoforge` 的 `build.gradle.kts` 中的 `runs{}` 定义（client/server/data），运行目录为 `runs/`。
- client dev 运行可由环境变量 `mcName` / `mcUUID` 指定测试账号。
- `ibukigourd.info.toml` 由根 `build.gradle.kts` 自动生成，**勿手动编辑**。

## 编码规范

- **包名根**：`moe.forpleuvoir.ibukigourd.*`；同级库 `moe.forpleuvoir.nebula.*`。新增代码务必放进正确的子包，不要创建新顶层包除非确有必要。
- **Kotlin 风格**：
  - 用 `object` 单例做门面/Handler（如 `IbukiGourd`、`ClientModConfigHandler`、`Services`、`IGLang`、`Icons`）。
  - 广泛使用 **DSL**：`@DslMarker`（`CommandDslMark` / `TextDslMark`）、scope 类（`ArgumentScope` / `RequiredArgumentScope`）、顶层入口函数（`registerCommand`、`IbukiGourdScreen()`）。
  - 大量使用 **context receivers / parameters**：`context(CommandDispatcher<S>)`、`context(ModConfigManager)` 等（编译开关 `-Xcontext-parameters` 已在 `multiloader-common.gradle` 启用）。
  - 用扩展函数补充能力（`logger()`、向量运算、`translateText`）。
- **GUI 用 JetBrains Compose（Desktop / Material3）**：`@Composable` 函数、`Modifier` 链、`MaterialTheme`、`AnimatedContent`、`PermanentNavigationDrawer` 等。**渲染经自定义 Skia 表面**（`ui/skia/`、`ui/scene/`）而非原生 `GuiGraphics`。
- **异步**：`kotlinx.coroutines`（`runBlocking`、`ioLaunch`、`ioAsync`、`awaitAll`）；计时用 `kotlin.time.Duration`。
- **注释与文档用中文**，与现有代码、commit message、README 主语言保持一致。
- **i18n**：新增界面文案走 lang key，统一登记到 `IGLang` 子对象，资源文件在 `assets/ibukigourd/lang/`。
- **翻译用词规范**（zh_CN）：
  - **keybind**（`Keybind` / `keybind` 相关）→ 译为 **"快捷键"**，如"按键绑定冲突"→"快捷键冲突"。
  - **keycode**（单纯的按键码设置，如滚轮倍率触发键 `*_key_code`）→ 译为 **"按键"**，如"滚动倍率按键码Ⅰ"→"滚动倍率按键Ⅰ"。
- **资源模板**：`processResources` 会用 `gradle.properties` / `libs.versions.toml` 的占位符展开 `fabric.mod.json`、`*.mixins.json`、`pack.mcmeta`、`META-INF/{,neoforge.}mods.toml`，**新增这类文件中的版本字段一律用占位符**（如 `${version}`、`${minecraft_version}`）。

## Git 与提交

- 主分支 `dev`；当前开发分支 `compose-test`；当前默认 PR 目标分支为 `compose-test`。
- commit message 用中文，遵循 Conventional Commits（参考历史：`feat:` / `fix:` / `refactor(ui):` / `docs:` 等）。
- 仅在被明确要求时才执行 `git commit` / `git push`；在默认分支上应先开分支。
- 提交前勿带入 `build/`、`runs/`、`modJar/`、`out/`、`net/` 等忽略目录。

## 工作约定（给 AI 助手）

1. **改公共 API 前确认影响面**：`config` / `command.dsl` / `event` / `ui`（尤其 `configwrapper`、`preset`）属于对外 API，消费方 MOD 依赖其签名，破坏性改动需谨慎并更新 `README.md` 示例。
2. **跨加载器改动**：能放 `common` 就放 `common`；平台相关能力通过 `platform/services` 抽象，由 fabric/neoforge 各自实现并通过 `META-INF/services` 注册，勿在 common 里硬编码平台判断。
3. **Mixin**：放 `common/.../mixin`（client 相关放 `mixin/client`），并在对应加载器的 `*.mixins.json` 注册；Fabric access widener 用 `ibukigourd.classtweaker`，NeoForge AT 用 `META-INF/accesstransformer.cfg`。
4. **Compose 依赖**：`common` 以 `compileOnly` 引入 compose/material3/materialKolor；`fabric` 用 `includeInternal`、`neoforge` 用 `jarJarInternal` 打包进 jar（均排除 `material-desktop` 及 kotlin/kotlinx/annotations 传递依赖）。新增 UI 依赖请沿用此模式。
5. **先读后写**：修改文件前先读取确认现状；遵循周边代码的命名、注释密度与惯用法。
6. **构建验证**：完成 Kotlin 改动后，优先用 `gradlew.bat :<module>:build` 或对应编译任务验证；不要声称“已通过测试”除非真的运行过。
7. **`nebula` 基类**：若改动触及 `ConfigManager` / `Event` 等定义，注意其声明在 `nebula` 依赖中，本仓库无法直接修改，只能通过包装/扩展。

## IntelliJ IDEA MCP 与验证

执行编译、构建、代码检查或运行配置前，先检查当前环境是否提供 IntelliJ IDEA / JetBrains MCP，并枚举其实际能力，例如项目模型、Gradle 任务、编译、问题检查和运行配置。

- 不假定 MCP 的固定工具名。
- IDEA MCP 能覆盖目标时优先使用，以复用 IDE 已导入的项目模型与环境。
- 不使用 `ps`、系统进程列表或类似方式探测 IDEA 或 Gradle 导入状态。
- 只有在 IDEA MCP 未提供、明确不可用或不能覆盖目标任务时，才回退到仓库根目录的 Gradle Wrapper。
- 先执行覆盖改动范围的最小检查，再按风险扩大验证。
- 交付时说明实际采用的验证方式；回退到 Wrapper 时简述原因。
