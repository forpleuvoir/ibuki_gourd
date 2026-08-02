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
- **GUI**

![ibukigourd](https://img.shields.io/modrinth/v/ibukigourd?label=Modrinth&color=8647B3)

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

Configuration is powered by nebula. For client-side configurations, the class should
extend `ClientModConfigManager(modId, name)`. Config items are declared via nebula's
extension functions (`configString` / `configBoolean` / `configInt` / `configLong` /
`configFloat` / `configDouble` / `configEnum` / `configColor` / `configDuration` /
`configList` / `configMap`, etc.) with property delegation, and can be nested in `ConfigGroup`s.

**Example:**

```kotlin
object YourModConfigs : ClientModConfigManager("your_mod_id", "config") {

    init {
        addConfig(Gui)
    }

    // Config group
    object Gui : ConfigGroup("gui") {
        var stringConfig by configString("config_key_1", "defaultValue")

        var intConfig by configInt("config_key_2", 100, 0, 1000)

        var booleanConfig by configBoolean("config_key_3", true)
    }

}
```

Register the manager with the config handler and its lifecycle (load/save) is managed
automatically:

```kotlin
ClientModConfigHandler.register(YourModConfigs)
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
// Initialize and load in one step, falling back to a forced save if loading fails
YourModConfigs.startup()
```

#### Server-side Configuration

For server-side configurations, the class should extend `ServerModConfigManager(modId, name)`
and be registered with the server handler:

```kotlin
object YourServerConfigs : ServerModConfigManager("your_mod_id", "config") {
    // Config items are the same as for client configurations
}

ServerModConfigHandler.register(YourServerConfigs)
```

#### Configuration Screen

To render the configuration screen, use the config screen manager wrapper
`ConfigManagerWrapper(configManager: ConfigManager, modifier: Modifier = Modifier)`:

```kotlin
// Example
openComposeScreen {
    IbukiGourdTheme {
        ConfigManagerWrapper(YourModConfigs)
    }
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

### GUI

The GUI is built with JetBrains Compose Multiplatform + Material3 and rendered into the
Minecraft screen through Skia, instead of the native `GuiGraphics`. Screen content is an
ordinary `@Composable` composition, giving you access to the full Compose toolset
(layout, animation, Material3 themes, state management, and more).

Open a Compose screen:

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

Open a popup screen:

```kotlin
openComposePopupScreen {
    Card(Modifier.padding(24.dp)) {
        Text("Dialog")
    }
}
```

Common entry points:

- `openComposeScreen(content)`: opens a full-screen Compose screen, configurable via
  `pauseGame` / `renderParent` / `parentScreen` / `shouldRenderLevel` / `entryAnimation`.
- `openComposePopupScreen(content)`: opens a Compose popup screen.
- `ComposeScreen(...)` / `Screen.open()`: construct and open a screen instance directly.
- `IbukiGourdTheme`: provides theming (light/dark, Material3 ColorScheme).
- `ConfigManagerWrapper(configManager)`: renders a config manager as a configuration UI.
- Built-in components: `ItemIcon` / `ItemIconVanilla` (item icons), `BlitTexture` (textures),
  `Text`, `TipBox`, `SearchBar`, `ColorButton` / `ColorPicker`, `NumberField` / `NumberSlider`,
  `Selector`, `KeySetter`, and more.

The Compose-based GUI enables intuitive layout and interaction design within Minecraft.
