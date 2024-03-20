# IBUKI GOURD

[简体中文](https://github.com/forpleuvoir/ibuki_gourd/blob/dev/README.md)

<img src = "doc/logo.png" width ="256" alt="icon">

`IbukiGourd` is a `Minecraft Fabric MOD` primarily written in `kotlin`, mainly providing prerequisite features for other MODs.

`Config Manage` `Config GUI` `Command DSL` `GUI DSL`

Dependent:

- [Fabric API](https://github.com/FabricMC/fabric)
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric)

## Usage

### Dependency
Add repositories to your Gradle project

Gradle Groovy:
```
//Snapshot repository
maven {
    name "forpleuvoirSnapshots"
    url "https://maven.forpleuvoir.moe/snapshots"
}
//Releases repository
maven {
    name "forpleuvoirReleases"
    url "https://maven.forpleuvoir.moe/releases"
}
```
Gradle Kotlin:
```
//Snapshot repository
maven {
    name = "forpleuvoirSnapshots"
    url = uri("https://maven.forpleuvoir.moe/snapshots")
}
//Releases repository
maven {
    name = "forpleuvoirReleases"
    url = uri("https://maven.forpleuvoir.moe/releases")
}
```
Add it as a dependency to your Gradle project:
```
dependencies {
    implementation("moe.forpleuvoir:ibukigourd:$version")
}
```

### Config

Client-side Config,need to extends `ClientModConfigManager`

example:
```
object YourModConfigs : ClientModConfigManager(yourModMeta,"key"){

    //Use property delegations
    var stringConfig by ConfigString("config_key_1","defaultValue")

    //Delegates are not used
    val mapConfing = ConfigStringMap("config_key_2",mapOf("k1" to "v1","k2" to "v2"))

    //Add a child container
    object Other : ModConfigContainer("other"){
        ......
    }

}
```
Manually manage the Config Manager
```
//initialize
YourModConfigs.init()
//Load the config from the file
YourModConfigs.load()
//Save the config to a file
YourModConfigs.save()
//Forced save
YourModConfigs.forceSave()
```
Server-side config,need to extends`ServerModConfigManager`
```
//For initialization, you need to pass in the Minecraft Server instance, and the rest is configured with the same client
ServerModConfigManager.init(MinecraftServer)
```
Automatically manage config

 - Add in`fabric.mod.json`
```
"custom": {
  "ibukigourd": {
    "package": [
      "your.code.pack"
    ]
  }
}
```
 - Add annotations on the Config Manager`@ModConfig("config_Key")`
```
@ModConfig("config_Key")
object YourModConfigs : ClientModConfigManager(yourModMeta,"key")
```
### Command DSL(planned, undeveloped)
expect:
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

### GUI DSL(Under development)
expect:
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


## Acknowledgement

>Thanks to [JetBrains](https://www.jetbrains.com) for allocating free open-source licences for IDEs such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai).
 
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200"/>](https://www.jetbrains.com)