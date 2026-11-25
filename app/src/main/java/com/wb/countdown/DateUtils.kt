package com.wb.countdown

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val PATTERN = "yyyy-MM-dd"

    /** 返回目标日期距离今天还有多少天：正数=还有 N 天，0=今天，负数=已过去 N 天。 */
    fun daysLeft(targetDate: String): Int {
        return try {
            val sdf = SimpleDateFormat(PATTERN, Locale.getDefault())
            val target = sdf.parse(targetDate) ?: return 0
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            Math.round((target.time - today.timeInMillis) / 86400000.0).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /** 2026-12-25 -> 2026年12月25日 */
    fun formatChinese(targetDate: String): String {
        return try {
            val sdf = SimpleDateFormat(PATTERN, Locale.getDefault())
            val date = sdf.parse(targetDate) ?: return targetDate
            SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            targetDate
        }
    }

    /** 星期几，例如 星期五 */
    fun weekday(targetDate: String): String {
        return try {
            val sdf = SimpleDateFormat(PATTERN, Locale.getDefault())
            val date = sdf.parse(targetDate) ?: return ""
            SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun todayString(): String = SimpleDateFormat(PATTERN, Locale.getDefault()).format(Date())
}
