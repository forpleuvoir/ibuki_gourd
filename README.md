# IBUKI GOURD

简体中文 | [English](/README-ENG.md)

<img src = "doc/logo.png" width ="256" alt="icon">

[IbukiGourd](https://modrinth.com/mod/ibukigourd) 是一个主要由`kotlin`编写的`Minecraft Fabric&Neoforge MOD`
,主要为其他MOD提供前置功能

如:`配置管理` `配置GUI` `指令DSL` `GUI`

![ibukigourd](https://img.shields.io/modrinth/v/ibukigourd?label=Modrinth&color=8647B3)

依赖于:

- Fabric
    - [Fabric API](https://github.com/FabricMC/fabric)
    - [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin/)

- NeoForge
    - [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)

## 如何使用

### 依赖

添加仓库到你的Gradle项目

Gradle Groovy:

```groovy
//快照仓库
maven {
    name "forpleuvoirSnapshots"
    url "https://maven.forpleuvoir.moe/snapshots"
}
//发布仓库
maven {
    name "forpleuvoirReleases"
    url "https://maven.forpleuvoir.moe/releases"
}
```

Gradle Kotlin:

```kts
//快照仓库
maven {
    name = "forpleuvoirSnapshots"
    url = uri("https://maven.forpleuvoir.moe/snapshots")
}
//发布仓库
maven {
    name = "forpleuvoirReleases"
    url = uri("https://maven.forpleuvoir.moe/releases")
}
```

添加依赖

```kts
dependencies {
    implementation("moe.forpleuvoir:ibukigourd-$platform-$minecraftVersion:$modVersion")
}
```

### 配置

配置由 nebula 提供，客户端配置需要继承 `ClientModConfigManager(modId, name)`。
配置项通过 nebula 的扩展函数（`configString` / `configBoolean` / `configInt` / `configLong` /
`configFloat` / `configDouble` / `configEnum` / `configColor` / `configDuration` / `configList` /
`configMap` 等）以属性委托方式声明，并可以嵌套 `ConfigGroup`。

例:

```kotlin
object YourModConfigs : ClientModConfigManager("your_mod_id", "config") {

    init {
        addConfig(Gui)
    }

    // 配置组
    object Gui : ConfigGroup("gui") {
        var stringConfig by configString("config_key_1", "defaultValue")

        var intConfig by configInt("config_key_2", 100, 0, 1000)

        var booleanConfig by configBoolean("config_key_3", true)
    }

}
```

注册到配置处理器后，生命周期（加载/保存）由处理器自动管理：

```kotlin
ClientModConfigHandler.register(YourModConfigs)
```

手动管理配置管理器：

```kotlin
//初始化
YourModConfigs.init()
//从文件中加载配置
YourModConfigs.load()
//保存配置到文件中
YourModConfigs.save()
//强制保存
YourModConfigs.forceSave()
//一键初始化并加载，加载失败时强制保存
YourModConfigs.startup()
```

服务端配置,需要继承`ServerModConfigManager(modId, name)`,并注册到服务端处理器：

```kotlin
object YourServerConfigs : ServerModConfigManager("your_mod_id", "config") {
    //配置项与客户端配置相同
}

ServerModConfigHandler.register(YourServerConfigs)
```

若要使用配置屏幕,使用配置屏管理器包装器
`ConfigManagerWrapper(configManager: ConfigManager,modifier: Modifier = Modifier)`

```kotlin
//示例
openComposeScreen {
    IbukiGourdTheme {
        ConfigManagerWrapper(YourModConfigs)
    }
}
```

### 指令DSL

根指令应该调用此方法注册

```kotlin
fun <S> CommandDispatcher<S>.registerCommand(
   name: String,
   scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit
)
```

```kotlin
dispatcher.registerCommand("yourCommand") {
    literal("subCommand") {
        suggests {
            //do something
        }
        execute {
            //do something
        }
    }
    argument("argName", ArgumentType) {
        execute {
            //do something
        }
    }
}

```

### GUI

GUI 基于 JetBrains Compose Multiplatform + Material3 构建，通过 Skia 渲染到 Minecraft 屏幕，
不再依赖原生 `GuiGraphics`。屏幕内容即普通 `@Composable` 组合，可以使用 Compose 的全部能力
（布局、动画、Material3 主题、状态管理等）。

打开一个 Compose 屏幕：

```kotlin
openComposeScreen {
    IbukiGourdTheme {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    ToastHandler.showContent { Text("hello minecraft") }
                }
            ) {
                Text("hello minecraft")
            }
        }
    }
}
```

打开弹窗屏幕：

```kotlin
openComposePopupScreen {
    Card(Modifier.padding(24.dp)) {
        Text("Dialog")
    }
}
```

常用入口：

- `openComposeScreen(content)`：打开全屏 Compose 屏幕，可配置 `pauseGame` / `renderParent` / `parentScreen` /
  `shouldRenderLevel` / `entryAnimation`。
- `openComposePopupScreen(content)`：打开 Compose 弹窗屏幕。
- `ComposeScreen(...)` / `Screen.open()`：直接构建并打开屏幕实例。
- `IbukiGourdTheme`：提供主题（亮/暗、Material3 ColorScheme）。
- `ConfigManagerWrapper(configManager)`：将配置管理器渲染为配置界面。
- 预置组件：`ItemIcon` / `ItemIconVanilla`（物品图标）、`BlitTexture`（纹理）、`Text`、`TipBox`、`SearchBar`、
  `ColorButton` / `ColorPicker`、`NumberField` / `NumberSlider`、`Selector`、`KeySetter` 等。
