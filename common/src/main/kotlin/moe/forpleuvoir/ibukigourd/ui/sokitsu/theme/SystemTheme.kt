package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import moe.forpleuvoir.ibukigourd.util.logger
import java.util.concurrent.TimeUnit

/**
 * 探测操作系统当前的主题偏好，返回 [ThemeType]。
 *
 * 各平台经子进程查询系统配置：
 * - Windows：注册表 `HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`
 *   的 `AppsUseLightTheme`（`REG_DWORD`，1 = 应用浅色，0 = 应用深色）；
 * - macOS：全局偏好 `AppleInterfaceStyle`（键不存在/无输出 = 浅色，`Dark` = 深色）；
 * - Linux：GNOME `color-scheme`（`prefer-dark` = 深色，其余输出按浅色）。
 *
 * 结果缓存 [REFRESH_INTERVAL_MS] 毫秒；查询失败或不支持的平台返回 [ThemeType.Unknown]，
 * 由调用方决定回落策略（Sokitsu 内一律按浅色收敛，见 [ThemeType.isLight]）。
 * 每次实际探测写一条 info 日志，便于核对读取到的主题与系统是否一致。
 */
fun systemTheme(): ThemeType {
    val now = System.currentTimeMillis()
    if (now - lastProbeMillis < REFRESH_INTERVAL_MS) return cachedTheme
    synchronized(probeLock) {
        if (System.currentTimeMillis() - lastProbeMillis < REFRESH_INTERVAL_MS) return cachedTheme
        cachedTheme = probeOsTheme()
        lastProbeMillis = System.currentTimeMillis()
        logger.info("System theme probe: theme=$cachedTheme (os=$osName)")
        return cachedTheme
    }
}

private const val REFRESH_INTERVAL_MS = 30_000L

private val logger = logger("SokitsuSystemTheme")

private val probeLock = Any()

@Volatile
private var cachedTheme: ThemeType = ThemeType.Unknown

/** 0 而非 Long.MIN_VALUE：`now - Long.MIN_VALUE` 会溢出变负，导致首次新鲜度判断永远为"新鲜"、探测永不执行。 */
@Volatile
private var lastProbeMillis: Long = 0L

private val osName: String = System.getProperty("os.name").lowercase()

private val isWindows: Boolean get() = osName.contains("windows")

private val isMac: Boolean get() = osName.contains("mac")

private val isLinux: Boolean get() =
    osName.contains("linux") || osName.contains("nix") || osName.contains("nux")

private fun probeOsTheme(): ThemeType = when {
    isWindows -> exec("reg", "query", WINDOWS_PERSONALIZE_KEY, "/v", "AppsUseLightTheme")
        ?.let(::parseWindowsAppsUseLightTheme)
        ?: ThemeType.Unknown

    isMac     -> exec("defaults", "read", "-g", "AppleInterfaceStyle")
        ?.let { if (it.trim().equals("Dark", ignoreCase = true)) ThemeType.Dark else ThemeType.Light }
        ?: ThemeType.Unknown

    isLinux   -> exec("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
        ?.let { if (it.contains("prefer-dark")) ThemeType.Dark else ThemeType.Light }
        ?: ThemeType.Unknown

    else      -> ThemeType.Unknown
}

private const val WINDOWS_PERSONALIZE_KEY =
    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"

/**
 * 解析 `reg query` 输出：`AppsUseLightTheme    REG_DWORD    0x1`。
 * 0x1 = 浅色 / 0x0 = 深色；解析不出返回 null（上层按 [ThemeType.Unknown] 处理）。
 */
private fun parseWindowsAppsUseLightTheme(output: String): ThemeType? {
    val line = output.lineSequence().firstOrNull { it.contains("AppsUseLightTheme", ignoreCase = true) } ?: return null
    val hex = line.substringAfterLast("0x", "").trim().takeWhile { it.isDigit() }
    return hex.toIntOrNull(16)?.let { if (it != 0) ThemeType.Light else ThemeType.Dark }
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
