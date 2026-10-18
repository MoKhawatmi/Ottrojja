package com.ottrojja.classes

import com.ottrojja.interfaces.TabItem

enum class QuranListeningMode(override val displayName: String): TabItem {
    مقطع_ايات(displayName = "مقطع آيات"),
    سورة_كاملة(displayName = "سورة كاملة")
}
