# IBUKI GOURD

[简体中文](/README.md) | English

<img src = "doc/logo.png" width ="256" alt="icon">

[IbukiGourd](https://modrinth.com/mod/ibukigourd) is a `Minecraft Fabric&Neoforge MOD` primarily written in `Kotlin`. It
is
designed to provide essential features for
other mods, including:

- **Config Management**
- **Config GUI**
- **Command DSL**
- **GUI DSL**

![Modrinth Version](https://img.shields.io/modrinth/v/ibukigourd?label=Modrinth)

Dependencies:

- Fabric
    - [Fabric API](https://github.com/FabricMC/fabric)
    - [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin/)

- NeoForge
    - [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)

## How to Use

### Dependency

Add repositories to your Gradle project:

**Gradle Groovy:**

```groovy
// Snapshot repository
maven {
    name "forpleuvoirSnapshots"
    url "https://maven.forpleuvoir.moe/snapshots"
}
// Release repository
maven {
    name "forpleuvoirReleases"
    url "https://maven.forpleuvoir.moe/releases"
}
```

**Gradle Kotlin:**

```kts
// Snapshot repository
maven {
    name = "forpleuvoirSnapshots"
    url = uri("https://maven.forpleuvoir.moe/snapshots")
}
// Release repository
maven {
    name = "forpleuvoirReleases"
    url = uri("https://maven.forpleuvoir.moe/releases")
}
```

Add the dependency:

```kts
dependencies {
    implementation("moe.forpleuvoir:ibukigourd-$platform-$minecraftVersion:$modVersion")
}
```

### Configuration

#### Client-side Configuration

For client-side configurations, the class should extend `ClientModConfigManager`.

**Example:**

```kotlin
object YourModConfigs : ClientModConfigManager(
   modMeta = yourModMeta,
   key = "key",
   autoScan = AutoScan.close
) {

    // When autoScan is disabled, manually add configuration objects to the container:
    // addConfig(configEntry)
    // Or use extension methods for configuration items

    // Using property delegation
    var stringConfig by ConfigString("config_key_1", "defaultValue")

    // When autoScan is disabled, the `string` method automatically adds the config item
    // to the container. Generally, extension methods should be defined in the respective
    // configuration class file.
    var stringByExtension by string("config_key_1", "defaultValue")

    // Without delegation
    val mapConfig = ConfigStringMap("config_key_2", mapOf("k1" to "v1", "k2" to "v2"))

    init {
        //If autoScan is enabled, you do not need to manually add configuration items
        addConfig(Other)
    }

    // Add a child container
    object Other : ModConfigContainer("other") {
        //......
    }
}
```

**How to manage the configuration manually:**

```kotlin
// Initialize
YourModConfigs.init()
// Load configuration from file
YourModConfigs.load()
// Save configuration to file
YourModConfigs.save()
// Force save configuration
YourModConfigs.forceSave()
```

#### Server-side Configuration

For server-side configurations, the class should extend `ServerModConfigManager`:

```kotlin
// For initialization, pass in the Minecraft server instance.
// The rest of the operations are the same as for the client configuration.
ServerModConfigManager.init(MinecraftServer)
```

### Automatic Configuration Management

To set up automatic configuration management:

1. Add package information
    - For Fabric, add in `fabric.mod.json`:

        ```json
        {
          "custom": {
            "ibukigourd": {
              "package": "your.code.pack"
            }
          }
        }
        ```
    - For Neoforge, add in `neoforge.mods.toml`:

        ```toml
        [modproperties."$yourModId"]
        package = "your.code.pack"
        ```
2. Add annotation `@ModConfig("config_Key")` to the configuration manager:

    ```kotlin
    @ModConfig("config_Key")
    object YourModConfigs : ClientModConfigManager(yourModMeta,"key")
    ```

3. To use the configuration screen, wrap it with the configuration screen manager wrapper
   `ConfigManagerWrapper(configManager: ConfigManager,modifier: Modifier = Modifier)`:

    ```kotlin
    //Example
    BoxScreen {
        ConfigManagerWrapper(YourModConfigs)
    }
    ```

### Command DSL

Use the following method to register root commands:

```kotlin
fun <S> CommandDispatcher<S>.registerCommand(
   name: String,
   scope: ArgumentScope<S, LiteralArgumentBuilder<S>>.() -> Unit
)
```

**Example:**

```kotlin
dispatcher.registerCommand("yourCommand") {
   literal("subCommand") {
        suggests {
           // Provide suggestions
        }
        execute {
           // Execute the command
        }
    }
   argument("argName", ArgumentType) {
        execute {
           // Execution logic with command arguments
        }
    }
}
```

This example demonstrates how root commands can contain nested subcommands and arguments.

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
}.open()  // Opens the screen
```

<img alt="img.png" src="doc/img.png" width="854"/>

The GUI DSL facilitates intuitive layout and interaction design within Minecraft.
