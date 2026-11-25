package com.wb.countdown

import java.util.UUID

/**
 * 一个倒计时事件。
 * targetDate 使用 yyyy-MM-dd 格式存储，便于跨版本持久化与比较。
 */
data class CountdownEvent(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var targetDate: String = "",
    var emoji: String = "✨",
    var colorIndex: Int = 0
)
