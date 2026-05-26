package moe.forpleuvoir.ibukigourd.ui.util

import moe.forpleuvoir.nebula.common.color.Color
import androidx.compose.ui.graphics.Color as ComposeColor

inline val Color.toComposeColor: ComposeColor get() = ComposeColor(this.red, this.green, this.blue, this.alpha)

inline val ComposeColor.toNebulaColor: Color get() = Color.fromARGB(this.red, this.green, this.blue, this.alpha)