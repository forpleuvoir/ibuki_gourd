# TODO

## 1. 配置界面搜索优化 ✅

- **位置**: `ConfigManagerWrapper.kt` — `SearchPanel` / `ConfigGroupWrapper.kt`
- **需求**: 搜索到单项配置时，也展示父级配置组，但不包括根节点。父级只展示匹配的子项。
- **已完成**

## 2. Toast Content 添加边框 ✅

- **位置**: `ToastContainer.kt` — `ToastContent`
- **需求**: 为 `ToastContent` 的 `Surface` 添加 `BorderStroke` 边框
- **已完成**

## 3. 为 ConfigVector 添加配置包装器 ✅

- **位置**: `ConfigVector.kt` + `VectorConfigWrapper.kt` + `ConfigUIWrapper.kt`
- **已完成**: Vector2i/f/d、Vector3i/f/d 全部支持，含 range 范围显示、`LocalVectorFieldWidthFraction` 可配宽度比
