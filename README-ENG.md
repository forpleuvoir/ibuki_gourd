# IBUKI GOURD

[简体中文](/README.md) | English

<img src = "doc/logo.png" width ="256" alt="icon">

[IbukiGourd](https://modrinth.com/mod/ibukigourd) is a **Minecraft Fabric / NeoForge library** written in **Kotlin**.
It ships no gameplay content — only infrastructure for other mods:

| Feature | What you get | Entry point |
|---|---|---|
| **Config system** | Delegated config entries, groups, serialization, automatic load/save, typed GUI editors | `ClientModConfigManager` / `ClientModConfigHandler` |
| **Config GUI** | Render a config manager as a screen (search, tab navigation, per-type controls, drag-and-drop edit dialogs) | `ConfigManagerWrapper` |
| **UI toolkit** | Pixel-art Compose components (buttons, inputs, selectors, tables, tabs, dialogs, color picker, curve editor…) | `ui/sokitsu/**`, `SokitsuScreen` |
| **Global overlay** | Persistent content drawn above vanilla HUD / screens / Compose screens (Toast is built on it) | `ui/overlay/OverlayService` |
| **Command DSL** | Kotlin DSL over Brigadier | `CommandDispatcher.registerCommand` |
| **Event bus** | Lifecycle / tick / mouse & keyboard events (backed by nebula `EventFactory`) | `ClientLifecycleEvent` / `ClientTickEvent` / `KeyboardEvent`… |
| **Input system** | Key combos, trigger timings (press / long press / repeat), pass-through & environment rules, conflict detection | `Keybind` / `InputHandler` |
| **Text DSL** | Text building (literal / translatable / inline-style parsing) + layout helpers | `buildText` / `InlineStyleText` / `Texts` |
| **i18n** | Language-key namespaces and recording | `IGLang` / `TranslationRecorder` |
| **Scheduling & utils** | Client tick tasks, coroutine helpers, vectors / colors / codecs / math (bezier, easing) | `task/**`, `util/**` |

Rendering is powered by the in-house [compose-minecraft](https://github.com/forpleuvoir/Compose-Minecraft)
(embedded androidx Compose, drawing straight into vanilla `GuiGraphics`, **no Material3 / Skia offscreen rendering**).
The look is pixel art: integer upscaling plus `.aseprite` assets.

![ibukigourd](https://img.shields.io/modrinth/v/ibukigourd?label=Modrinth&color=8647B3)

## How to Use

### 1. Repository and dependency

```kts
repositories {
    maven("https://maven.forpleuvoir.moe/releases")   // releases
    maven("https://maven.forpleuvoir.moe/snapshots")  // snapshots
}

dependencies {
    // Fabric
    implementation("moe.forpleuvoir:ibukigourd-fabric-$minecraftVersion:$ibukigourdVersion")
    // NeoForge
    implementation("moe.forpleuvoir:ibukigourd-neoforge-$minecraftVersion:$ibukigourdVersion")
    // Logic only (server side / no UI)
    implementation("moe.forpleuvoir:ibukigourd-common-$minecraftVersion:$ibukigourdVersion")
}
```

| Placeholder | Current value |
|---|---|
| `$minecraftVersion` | `26.2` |
| `$ibukigourdVersion` | `0.11.1+alpha` |

Bundled dependencies (`nebula`, `compose-minecraft`, `reorderable`, `aseprite`) travel inside the loader artifacts
(`include` on Fabric, `jarJar` on NeoForge), so you normally do not declare them. At runtime you still need
Fabric API + Fabric Language Kotlin, or Kotlin for Forge.

### 2. Three-minute tour

**Config** — extend `ClientModConfigManager`, declare entries as delegated properties, register the manager
and its lifecycle is handled for you.

```kotlin
object YourModConfigs : ClientModConfigManager("your_mod_id", "config") {

    object Gui : ConfigGroup("gui") {
        var title by configString("title", "Hello")
        var scale by configFloat("scale", 1f, 0.5f, 2f)
        val openKey by configKeybind("open_key", Keyboard.RIGHT_SHIFT)
    }

    init { addConfig(Gui) }
}

// in your mod entry point (or a ModInitialization service)
ClientModConfigHandler.register(YourModConfigs)
```

**Config screen** — wrap it in `SokitsuScreen` (theme + resolution scaling):

```kotlin
SokitsuScreen.open {
    ConfigManagerWrapper(YourModConfigs)
}
```

**Command**:

```kotlin
dispatcher.registerCommand("yourCommand") {
    literal("hello") {
        executes { source.sendSuccess({ Literal("hi!") }, false); 1 }
    }
}
```

**Screen + toast**:

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

### 3. Documentation

The full developer manual lives in [`doc/manual/`](doc/manual/README.md) (Chinese); the chapter list is also
mirrored in [README.md](README.md). For live examples, browse `common/src/devOnly/` — that dev-only source set
contains 30+ component test screens reachable from the `SokitsuTestScreen` main menu.

## License

Released under the MIT license, see [LICENSE](LICENSE); bundled third-party sources are listed in [NOTICE.md](NOTICE.md).
