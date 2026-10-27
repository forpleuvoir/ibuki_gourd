> Entries for v1.0.0-alpha and earlier are maintained in Chinese only; see `CHANGE_LOG.md`.

v1.1.0-alpha
  - Updated to `Minecraft` `26.3` (Fabric API `0.165.0+26.3` / Loader `0.20.1` / Loom `1.19.3` / Fabric Kotlin `1.14.2+kotlin.2.4.21`; NeoForge `26.3.0.99` / moddev `2.0.148` / neoForm `26.3-1` / Kotlin for Forge `6.4.0`; ModMenu `21.0.0`)
  - Toolchain updated to `Kotlin` `2.4.21` / `Gradle` `9.8.2`; `Mixin` `0.8.7` + MixinExtras `0.5.5`
  - **[UI]** `compose-minecraft` updated to `0.2.0` (coordinates `compose_minecraft-<platform>-26.3`), Compose runtime raised to `1.12.2`
  - **[Rendering]** The render stack follows 26.3 to `renderpearl`: `RenderPipeline` / `BlendFunction` / `ColorTargetState` / `PrimitiveTopology` / `GpuFormat` / `GpuTexture` / `FilterMode` and friends moved to `com.mojang.renderpearl.api.*`; custom pipelines bind `BindGroupLayouts.PROJECTION` + `DYNAMIC_TRANSFORMS`
  - **[Rendering]** `position_hsv_color.fsh` / `sokitsu_tint.fsh` adapted to the 26.3 shader constraints: `layout(location = N)` on `in` / `out`, `GL_ARB_separate_shader_objects` enabled, and the `DynamicTransforms` UBO field order is `ModelViewMat, TextureMat, ColorModulator, ModelOffset`
  - **[Input]** The input backend switched to SDL: `Keyboard` / `MouseButton` key codes now use `InputConstants` SDL scancodes and 1-based mouse buttons (left 1 / middle 2 / right 3), plus `KeyCode.fromKey(InputConstants.Key)` dispatching by type; `MouseCursor` uses `CursorTypes` + `Window.selectCursor`
  - **[Input]** Keybind conflict detection compares sets now (order independent, containment counts as a conflict in non-strict mode) and excludes `Keyboard.UNKNOWN`, so unset bindings no longer report conflicts with each other
  - **[Mixin]** `MouseHandler.onMove` injection gained the `xrel` / `yrel` parameters; the `BlitRenderState.<init>` target of `GuiRendererMixin` / `PictureInPictureRendererMixin` now points at renderpearl's `RenderPipeline`; `KeyboardHandlerMixin` uses `InputConstants.PRESS` / `RELEASE` / `REPEAT`
  - **[Engineering]** Gradle wrapper raised to `9.8.2` (Loom 1.19.3 requires >= 9.8.0); the `maven.forpleuvoir.moe/snapshots` repository got a group filter so its 502s no longer break resolution of `net.fabricmc` dependencies
  - **[Breaking]** Dependency coordinates follow the Minecraft version: `moe.forpleuvoir:ibukigourd-<platform>-26.3`
  - **[Breaking]** Keybind codes are SDL scancodes now: bindings stored by name (`key.keyboard.*`) still load, but bindings that landed on `key.keyboard.unknown` during the mismatch are cleared to "unset" and must be set again

