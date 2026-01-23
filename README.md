# IBUKI GOURD

简体中文 | [English](/README-ENG.md)

<img src = "doc/logo.png" width ="256" alt="icon">

[IbukiGourd](https://modrinth.com/mod/ibukigourd) 是一个主要由`kotlin`编写的`Minecraft Fabric&Neoforge MOD`
,主要为其他MOD提供前置功能

如:`配置管理` `配置GUI` `指令DSL` `GUI DSL`

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

客户端配置,需要继承`ClientModConfigManager`

例:

```kotlin
object YourModConfigs : ClientModConfigManager(modMeta = yourModMeta, key = "key", autoScan = AutoScan.close) {

    //自动扫描默认是关闭的,关闭时需要手动将配置对象添加到容器中
    //addConfig(configEntry)
    //或者使用配置项对应的扩展方法

    //使用属性委托
    var stringConfig by ConfigString("config_key_1", "defaultValue")

    //关闭自动扫描时,string 方法会自动将配置项添加到容器中,一般扩展方法都写在对应配置类的文件内
    var stringConfig by string("config_key_1", "defaultValue")

    //不使用委托
    val mapConfig = ConfigStringMap("config_key_2", mapOf("k1" to "v1", "k2" to "v2"))

    init {
        //如果开启了自动扫描(autoScan),则不需要手动添加配置项
        addConfig(Other)
    }

    //添加子容器
    object Other : ModConfigContainer("other") {
        //......
    }

}
```

手动管理配置管理器

```kotlin
//初始化
YourModConfigs.init()
//从文件中加载配置
YourModConfigs.load()
//保存配置到文件中
YourModConfigs.save()
//强制保存
YourModConfigs.forceSave()
```

服务端配置,需要继承`ServerModConfigManager`

```kotlin
//初始化时需要传入MinecraftServer实例,其余同客户端配置
ServerModConfigManager.init(MinecraftServer)
```

自动管理配置

1. 添加包信息
    - Fabric在`fabric.mod.json`中添加

        ```json
        {
          "custom": {
            "ibukigourd": {
              "package": "your.code.pack"
            }
          }
        }
        ```
    - Neoforge在`neoforge.mods.toml`中添加

        ```toml
            [modproperties."$yourModId"]
            package = "your.code.pack"
        ```

2. 在配置管理器上添加注解`@ModConfig("config_Key")`

    ```kotlin
    @ModConfig("config_Key")
    object YourModConfigs : ClientModConfigManager(yourModMeta,"key")
    ```
3. 若要使用配置屏幕,使用配置屏管理器包装器
   `ConfigManagerWrapper(configManager: ConfigManager,modifier: Modifier = Modifier)`
    ```kotlin
   //示例
    BoxScreen {
        ConfigManagerWrapper(YourModConfigs)
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

### GUI DSL

```kotlin   
BoxScreen {
    Row(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button {
            click {
                Toast.showToast(text = "hello minecraft")
            }
            Text("hello minecraft")
            Icon(IconTextures.LOCK)
        }
    }
}.open()//打开屏幕
```

<img alt="img.png" src="doc/img.png" width="854"/>
