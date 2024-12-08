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
```
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
```
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
```
dependencies {
    implementation("moe.forpleuvoir:ibukigourd:$version")
}
```

### 配置

客户端配置,需要继承`ClientModConfigManager`

例:
```
object YourModConfigs : ClientModConfigManager(yourModMeta,"key"){

    //使用属性委托
    var stringConfig by ConfigString("config_key_1","defaultValue")

    //不使用委托
    val mapConfing = ConfigStringMap("config_key_2",mapOf("k1" to "v1","k2" to "v2"))

    //添加子容器
    object Other : ModConfigContainer("other"){
        ......
    }

}
```
手动管理配置管理器
```
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
```
//初始化时需要传入MinecraftServer实例,其余同客户端配置
ServerModConfigManager.init(MinecraftServer)
```
自动管理配置
- 在`fabric.mod.json`中添加
```
"custom": {
  "ibukigourd": {
    "package": [
      "your.code.pack"
    ]
  }
}
```
- 在配置管理器上添加注解`@ModConfig("config_Key")`
```
@ModConfig("config_Key")
object YourModConfigs : ClientModConfigManager(yourModMeta,"key")
```
### 指令DSL(计划中,未开发)
计划效果
```
literal("yourCommand"){
    literal("subCommand"){
        suggests {
            //do something
        }
        execute {
            //do something
        }
    }
    argument("argName",ArgumentType){
        execute {
            //do something
        }
    }
}

```

### GUI DSL(开发中)
预期效果
```
screen{
    row{
        button(
            click = {
                //do something
            }
        ){
            text("hello minecraft")
            icon()
        }    
    }
}
```


## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的 IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200"/>](https://www.jetbrains.com)