package moe.forpleuvoir.ibukigourd.ui.skia.backend

import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface

/**
 * Skia 渲染后端。
 *
 * 负责提供 Compose 场景渲染所需的 GPU 上下文与离屏渲染表面，
 * 将「绑定到 Minecraft 纹理的 Skia 表面」这一能力按图形后端分离：
 *
 * - [OpenGLSkiaRenderBackend]：现有 Ganesh(OpenGL) 实现；
 * - Vulkan：待 skiko 官方支持 Vulkan（Graphite）后实现。
 *
 * 所有 GPU 操作（表面创建、绘制、释放）必须通过 [submit] 在对应的
 * 渲染线程/上下文中执行。
 */
interface SkiaRenderBackend {

    /** 后端名称，用于日志与调试。 */
    val name: String

    /** Skia GPU 上下文（Ganesh [DirectContext]）。 */
    val sharedContext: DirectContext

    /** 初始化后端（创建共享上下文等）。幂等。 */
    fun init()

    /**
     * 在渲染线程/后端上下文中执行 GPU 任务。
     *
     * OpenGL 实现会切换到隐藏共享上下文并自动恢复；
     * 未来 Vulkan 实现可在此处直接执行（Vulkan 无需显式上下文切换）。
     */
    fun submit(task: () -> Unit)

    /**
     * 创建常规离屏渲染表面。
     *
     * 创建一个绑定新 Minecraft [GpuTexture] 的 Skia 表面，
     * 供 [moe.forpleuvoir.ibukigourd.ui.skia.SkiaSurface] 承载 Compose 场景渲染。
     */
    fun createSurfaceTarget(width: Int, height: Int): SkiaRenderTarget

    /**
     * 创建图集渲染表面。
     *
     * 与 [createSurfaceTarget] 相同，但额外提供与表面共享同一 backing
     * 纹理的 GPU [Image]（[SkiaRenderTarget.textureImage]），
     * 供 [moe.forpleuvoir.ibukigourd.ui.util.render.ItemRenderAtlas] 直接绘制纹理子区域。
     */
    fun createAtlasTarget(width: Int, height: Int): SkiaRenderTarget

    /** 释放后端资源（共享上下文等）。 */
    fun dispose()
}

/**
 * 离屏渲染表面（后端无关）。
 *
 * 封装「Minecraft GPU 纹理 + Skia Surface」组合，
 * 是 [moe.forpleuvoir.ibukigourd.ui.skia.SkiaSurface] 与
 * [moe.forpleuvoir.ibukigourd.ui.util.render.ItemRenderAtlas] 的底层资源单元。
 */
interface SkiaRenderTarget : AutoCloseable {

    val width: Int

    val height: Int

    /** Minecraft GPU 纹理（blit 到 GUI 缓冲区的来源）。 */
    val gpuTexture: GpuTexture

    /** GPU 纹理视图（blit 使用）。 */
    val textureView: GpuTextureView

    /** Skia 渲染表面（Compose 场景 / 图集绘制的目标）。 */
    val skiaSurface: Surface

    /**
     * 与表面共享同一 backing 纹理的 GPU [Image]。
     *
     * 仅图集目标（[SkiaRenderBackend.createAtlasTarget]）非 null；
     * 常规表面为 null。
     */
    val textureImage: Image?

    /** 将表面内容提交到 GPU（等效 [Surface.flushAndSubmit]）。 */
    fun flushAndSubmit()

    /** 释放全部 GPU 资源。 */
    override fun close()
}
