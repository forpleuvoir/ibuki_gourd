package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import moe.forpleuvoir.ibukigourd.util.logger
import java.util.concurrent.TimeUnit

/**
 * 探测操作系统当前的主题偏好：true = 深色，false = 浅色。
 *
 * 各平台经子进程查询系统配置：
 * - Windows：注册表 `HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`
 *   的 `AppsUseLightTheme`（`REG_DWORD`，0 = 应用深色，1 = 应用浅色）；
 * - macOS：全局偏好 `AppleInterfaceStyle`（`Dark` = 深色）；
 * - Linux：GNOME `color-scheme`（`prefer-dark` = 深色）。
 *
 * 结果缓存 [REFRESH_INTERVAL_MS] 毫秒；查询失败或不支持的平台返回浅色。
 * 每次实际探测写一条 info 日志，便于核对读取到的主题与系统是否一致。
 */
fun systemDarkTheme(): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastProbeMillis < REFRESH_INTERVAL_MS) return cachedDarkTheme
    synchronized(probeLock) {
        if (System.currentTimeMillis() - lastProbeMillis < REFRESH_INTERVAL_MS) return cachedDarkTheme
        cachedDarkTheme = probeOsDarkTheme()
        lastProbeMillis = System.currentTimeMillis()
        logger.info("System theme probe: dark=$cachedDarkTheme (os=$osName)")
        return cachedDarkTheme
    }
}

private const val REFRESH_INTERVAL_MS = 30_000L

private val logger = logger("SokitsuSystemTheme")

private val probeLock = Any()

@Volatile
private var cachedDarkTheme: Boolean = false

/** 0 而非 Long.MIN_VALUE：`now - Long.MIN_VALUE` 会溢出变负，导致首次新鲜度判断永远为"新鲜"、探测永不执行。 */
@Volatile
private var lastProbeMillis: Long = 0L

private val osName: String = System.getProperty("os.name").lowercase()

private val isWindows: Boolean get() = osName.contains("windows")

private val isMac: Boolean get() = osName.contains("mac")

private val isLinux: Boolean get() =
    osName.contains("linux") || osName.contains("nix") || osName.contains("nux")

private fun probeOsDarkTheme(): Boolean = when {
    isWindows -> exec("reg", "query", WINDOWS_PERSONALIZE_KEY, "/v", "AppsUseLightTheme")
        ?.let(::parseWindowsAppsUseLightTheme)
        ?: false

    isMac     -> exec("defaults", "read", "-g", "AppleInterfaceStyle")
        ?.let { it.trim().equals("Dark", ignoreCase = true) }
        ?: false

    isLinux   -> exec("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
        ?.let { it.contains("prefer-dark") }
        ?: false

    else      -> false
}

private const val WINDOWS_PERSONALIZE_KEY =
    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"

/**
 * 解析 `reg query` 输出：`AppsUseLightTheme    REG_DWORD    0x1`。
 * 0x0 = 应用深色（true）；解析不出按浅色处理（null）。
 */
private fun parseWindowsAppsUseLightTheme(output: String): Boolean? {
    val line = output.lineSequence().firstOrNull { it.contains("AppsUseLightTheme", ignoreCase = true) } ?: return null
    val hex = line.substringAfterLast("0x", "").trim().takeWhile { it.isDigit() }
    return hex.toIntOrNull(16)?.let { it == 0 }
}

/** 执行子进程并返回 stdout；超时（5s）或异常返回 null。 */
private fun exec(vararg command: String): String? = runCatching<String?> {
    val process = ProcessBuilder(*command).start()
    val output = process.inputStream.bufferedReader().readText()
    if (process.waitFor(5L, TimeUnit.SECONDS)) output else {
        process.destroyForcibly()
        null
    }
}.getOrNull()
