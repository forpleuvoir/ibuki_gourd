# IBUKI GOURD

[English](/README-eng.md)

<img src = "doc/logo.png" width ="256" alt="icon">

`IbukiGourd` 是一个主要由`kotlin`编写的`Minecraft Fabric MOD`,主要为其他MOD提供前置功能

如:`配置管理` `配置GUI` `指令DSL` `GUI DSL`

依赖于:

- [Fabric API](https://github.com/FabricMC/fabric)
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin/)

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
    implementation("moe.forpleuvoir:ibukigourd:$version")
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

1. 在`fabric.mod.json`中添加

    ```json
    {
      "custom": {
        "ibukigourd": {
          "package": [
            "your.code.pack"
          ]
        }
      }
    }
    ```

2. 在配置管理器上添加注解`@ModConfig("config_Key")`

    ```kotlin
    @ModConfig("config_Key")
    object YourModConfigs : ClientModConfigManager(yourModMeta,"key")
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

预期效果

```kotlin   
BoxScreen {
   Row(
      modifier = Modifier,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
   ) {
      Button {
            click = {
               Toast.showToast(text = "hello minecraft")
            }
         TextLabel("hello minecraft")
         Icon(IconTextures.LOCK)
      }
    }
}.open()//打开屏幕
```

## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的
> IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com)
为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200"/>](https://www.jetbrains.com)