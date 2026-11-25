package com.wb.countdown

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

/** 桌面小组件：根据桌面上的实际尺寸自动切换 1 / 3 / 5 行布局。 */
class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { updateOne(context, appWidgetManager, it) }
        WidgetRefreshScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateOne(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) refreshAll(context)
    }

    companion object {
        const val ACTION_REFRESH = "com.wb.countdown.REFRESH"

        private val TITLE_IDS = intArrayOf(
            R.id.widget_title_1, R.id.widget_title_2, R.id.widget_title_3,
            R.id.widget_title_4, R.id.widget_title_5
        )
        private val DAYS_IDS = intArrayOf(
            R.id.widget_days_1, R.id.widget_days_2, R.id.widget_days_3,
            R.id.widget_days_4, R.id.widget_days_5
        )
        private val SUB_IDS = intArrayOf(
            R.id.widget_sub_1, R.id.widget_sub_2, R.id.widget_sub_3,
            R.id.widget_sub_4, R.id.widget_sub_5
        )
        private val ROW_IDS = intArrayOf(
            R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3,
            R.id.widget_row_4, R.id.widget_row_5
        )

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CountdownWidgetProvider::class.java)
            )
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            try {
                manager.updateAppWidget(appWidgetId, buildViews(context, appWidgetId))
            } catch (e: Exception) {
                // 小组件尺寸变化期间系统可能短暂返回无效 options，下一轮刷新会纠正。
            }
        }

        private fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
            val options = managerOptions(context, appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 150)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

            val (layoutId, maxRows) = when {
                minWidth >= 320 && minHeight >= 280 -> R.layout.widget_large to 5
                minWidth >= 180 && minHeight >= 150 -> R.layout.widget_medium to 3
                else -> R.layout.widget_small to 1
            }

            val views = RemoteViews(context.packageName, layoutId)

            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)

            val repo = EventRepository(context)
            val events = repo.getEvents().sortedBy { DateUtils.daysLeft(it.targetDate) }

            views.setViewVisibility(R.id.widget_empty, if (events.isEmpty()) View.VISIBLE else View.GONE)

            for (i in TITLE_IDS.indices) {
                if (i < maxRows && i < events.size) {
                    val event = events[i]
                    val days = DateUtils.daysLeft(event.targetDate)
                    views.setViewVisibility(ROW_IDS[i], View.VISIBLE)
                    views.setTextViewText(TITLE_IDS[i], "${event.emoji} ${event.title}")
                    views.setTextViewText(
                        DAYS_IDS[i],
                        when {
                            days > 0 -> days.toString()
                            days == 0 -> "今天"
                            else -> (-days).toString()
                        }
                    )
                    views.setTextViewText(
                        SUB_IDS[i],
                        when {
                            days > 0 -> "天后 · ${DateUtils.formatChinese(event.targetDate)}"
                            days == 0 -> "就是今天！"
                            else -> "天前 · ${DateUtils.formatChinese(event.targetDate)}"
                        }
                    )
                } else {
                    views.setViewVisibility(ROW_IDS[i], View.GONE)
                }
            }

            val background = repo.getBackgroundUri()?.let { uri ->
                ImageUtils.decodeScaled(context, Uri.parse(uri), 512)
            }
            if (background != null) {
                views.setImageViewBitmap(R.id.widget_bg, background)
            } else {
                views.setImageViewResource(R.id.widget_bg, R.drawable.widget_gradient)
            }

            return views
        }

        private fun managerOptions(context: Context, appWidgetId: Int): Bundle {
            return try {
                AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId) ?: Bundle()
            } catch (e: Exception) {
                Bundle()
            }
        }
    }
}
