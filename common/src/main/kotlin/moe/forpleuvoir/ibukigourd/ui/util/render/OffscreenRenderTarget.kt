package moe.forpleuvoir.ibukigourd.ui.util.render

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.CommandEncoder
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.forpleuvoir.nebula.common.color.Color
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OffscreenRenderTarget(
    label: String,
    val texWidth: Int,
    val texHeight: Int
) : RenderTarget(label, true) {
    init {
        createBuffers(texWidth, texHeight)
    }

    @Suppress("PropertyName")
    internal inline val _label get() = label

    fun requireColorTextureView(): GpuTextureView =
        getColorTextureView() ?: error("$label color texture view is null")

    fun requireColorTexture(): GpuTexture =
        getColorTexture() ?: error("$label color texture is null")

    fun requireDepthTextureView(): GpuTextureView =
        getDepthTextureView() ?: error("$label depth texture view is null")

    fun requireDepthTexture(): GpuTexture =
        getDepthTexture() ?: error("$label depth texture is null")


    fun dispose() {
        destroyBuffers()
    }
}

/**
 * 立即渲染到离屏目标。
 * 调用完成后 texture 即包含渲染结果。
 */
fun OffscreenRenderTarget.renderNow(block: (RenderPass) -> Unit) {
    val encoder = RenderSystem.getDevice().createCommandEncoder()
    encoder.createRenderPass(
        { "Offscreen render: $_label" },
        requireColorTextureView(),
        OptionalInt.of(0),
        getDepthTextureView(),
        OptionalDouble.of(1.0)
    ).use { pass ->
        block(pass)
    }
}

/**
 * 挂起等待 GPU → PBO 拷贝完成，返回映射后的 [ByteBuffer]。
 *
 * ⚠ 必须在渲染线程调用；恢复后仍在渲染线程。
 */

private suspend fun copyTextureToBufferAwait(
    encoder: CommandEncoder,
    texture: GpuTexture,
    pbo: GpuBuffer
): ByteBuffer = suspendCancellableCoroutine { cont ->
    encoder.copyTextureToBuffer(
        texture, pbo, 0L,
        {
            try {
                encoder.mapBuffer(pbo, true, false).use { mapped ->
                    cont.resume(mapped.data())
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        },
        0
    )
}

private fun readPixelsToNativeImage(
    data: ByteBuffer,
    width: Int,
    height: Int,
    pixelSize: Int
): NativeImage {
    val image = NativeImage(width, height, false)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val offset = (x + y * width) * pixelSize
            val abgr = data.getInt(offset)
            image.setPixelABGR(x, height - y - 1, abgr or 0xFF000000.toInt())
        }
    }
    return image
}


fun nativeImageToBufferedImage(native: NativeImage): BufferedImage {
    val w = native.width
    val h = native.height
    val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val pixels = native.pixels
    image.setRGB(0, 0, w, h, pixels, 0, w)
    return image
}

fun NativeImage.toComposeImageBitmap(): ImageBitmap {
    val bytesPrePixel = 4
    val pixels = ByteArray(width * 4 * height * bytesPrePixel)
    var k = 0

    for (y in 0 until height) for (x in 0 until width) {
        val argb = Color.fromARGB(getPixel(x, y))
        pixels[k++] = argb.blue.toByte()
        pixels[k++] = argb.green.toByte()
        pixels[k++] = argb.red.toByte()
        pixels[k++] = argb.alpha.toByte()
    }

    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.UNPREMUL))
    bitmap.installPixels(pixels)
    return bitmap.asComposeImageBitmap()
}


suspend fun OffscreenRenderTarget.renderToBufferedImage(
    block: (RenderPass) -> Unit
): BufferedImage {
    val device = RenderSystem.getDevice()
    val encoder = device.createCommandEncoder()

    // Step 1: 立即渲染
    this.renderNow(block)

    // Step 2: 准备 PBO
    val srcTex = requireColorTexture()
    val pixelSize = srcTex.format.pixelSize()
    val bufSize = texWidth * texHeight * pixelSize

    val pbo = device.createBuffer(
        { "Readback PBO: $_label" },
        9,
        bufSize.toLong()
    )

    // Step 3: 挂起等待 GPU → CPU
    val data: ByteBuffer
    try {
        data = copyTextureToBufferAwait(encoder, srcTex, pbo)
    } catch (e: Exception) {
        pbo.close()
        throw e
    }

    // Step 4: 解码
    val nativeImage = readPixelsToNativeImage(data, texWidth, texHeight, pixelSize)
    val result = nativeImageToBufferedImage(nativeImage)
    nativeImage.close()
    pbo.close()
    return result
}
