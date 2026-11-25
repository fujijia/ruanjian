package com.wb.countdown

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 使用 SharedPreferences + JSON 持久化事件列表与背景图 Uri，不依赖第三方库。 */
class EventRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("countdown_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EVENTS = "events"
        private const val KEY_BG_URI = "background_uri"
    }

    fun getEvents(): List<CountdownEvent> {
        val json = prefs.getString(KEY_EVENTS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            val result = mutableListOf<CountdownEvent>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(
                    CountdownEvent(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        targetDate = o.optString("targetDate", ""),
                        emoji = o.optString("emoji", "✨"),
                        colorIndex = o.optInt("colorIndex", 0)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveEvents(events: List<CountdownEvent>) {
        val arr = JSONArray()
        events.forEach { e ->
            val o = JSONObject()
            o.put("id", e.id)
            o.put("title", e.title)
            o.put("targetDate", e.targetDate)
            o.put("emoji", e.emoji)
            o.put("colorIndex", e.colorIndex)
            arr.put(o)
        }
        prefs.edit().putString(KEY_EVENTS, arr.toString()).apply()
    }

    fun addEvent(event: CountdownEvent) {
        val list = getEvents().toMutableList()
        list.add(event)
        saveEvents(list)
    }

    fun updateEvent(event: CountdownEvent) {
        val list = getEvents().map { if (it.id == event.id) event else it }
        saveEvents(list)
    }

    fun deleteEvent(id: String) {
        saveEvents(getEvents().filterNot { it.id == id })
    }

    fun getBackgroundUri(): String? = prefs.getString(KEY_BG_URI, null)

    fun setBackgroundUri(uri: String?) {
        prefs.edit().putString(KEY_BG_URI, uri).apply()
    }
}
