package com.wb.countdown

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import java.util.UUID

class AddEventActivity : AppCompatActivity() {

    private lateinit var repo: EventRepository
    private var editingId: String? = null
    private var selectedDate: String = DateUtils.todayString()
    private var selectedEmoji: String = "✨"
    private var colorIndex: Int = 0

    private lateinit var titleEdit: EditText
    private lateinit var dateButton: Button
    private lateinit var deleteButton: Button
    private val emojiButtons = mutableListOf<TextView>()
    private val colorButtons = mutableListOf<View>()

    companion object {
        private val EMOJIS = arrayOf("✨", "🎉", "🎂", "💕", "🎓", "✈️", "🏠", "🎁", "🌸", "⚽", "🎮", "💍")
        private val COLOR_DRAWABLES = intArrayOf(
            R.drawable.card_pink,
            R.drawable.card_purple,
            R.drawable.card_cyan,
            R.drawable.card_orange
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        repo = EventRepository(this)
        titleEdit = findViewById(R.id.editTitle)
        dateButton = findViewById(R.id.btnPickDate)
        deleteButton = findViewById(R.id.btnDelete)

        editingId = intent.getStringExtra("event_id")
        if (editingId != null) {
            repo.getEvents().firstOrNull { it.id == editingId }?.let { event ->
                titleEdit.setText(event.title)
                selectedDate = event.targetDate
                selectedEmoji = event.emoji
                colorIndex = event.colorIndex
                deleteButton.visibility = View.VISIBLE
            }
        }

        updateDateButton()
        setupEmojiPicker()
        setupColorPicker()

        dateButton.setOnClickListener {
            val parts = selectedDate.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val day = parts.getOrNull(2)?.toIntOrNull() ?: 1
            android.app.DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    updateDateButton()
                },
                year, month, day
            ).show()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        deleteButton.setOnClickListener { confirmDelete() }
    }

    private fun setupEmojiPicker() {
        val group = findViewById<LinearLayout>(R.id.emojiGroup)
        EMOJIS.forEachIndexed { index, emoji ->
            val view = TextView(this).apply {
                text = emoji
                textSize = 24f
                gravity = android.view.Gravity.CENTER
                setPadding(12, 12, 12, 12)
                setBackgroundResource(R.drawable.picker_border)
                alpha = if (emoji == selectedEmoji) 1f else 0.45f
                setOnClickListener {
                    selectedEmoji = emoji
                    emojiButtons.forEachIndexed { i, b -> b.alpha = if (i == index) 1f else 0.45f }
                }
            }
            emojiButtons.add(view)
            group.addView(view)
        }
    }

    private fun setupColorPicker() {
        val group = findViewById<LinearLayout>(R.id.colorGroup)
        COLOR_DRAWABLES.forEachIndexed { index, drawable ->
            val view = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(64, 64).apply { setMargins(0, 0, 16, 0) }
                setBackgroundResource(drawable)
                alpha = if (index == colorIndex) 1f else 0.4f
                setOnClickListener {
                    colorIndex = index
                    colorButtons.forEachIndexed { i, b -> b.alpha = if (i == index) 1f else 0.4f }
                }
            }
            colorButtons.add(view)
            group.addView(view)
        }
    }

    private fun updateDateButton() {
        dateButton.text = "${DateUtils.formatChinese(selectedDate)} （${DateUtils.weekday(selectedDate)}）"
    }

    private fun save() {
        val title = titleEdit.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.toast_title_required, Toast.LENGTH_SHORT).show()
            return
        }
        val event = CountdownEvent(
            id = editingId ?: UUID.randomUUID().toString(),
            title = title,
            targetDate = selectedDate,
            emoji = selectedEmoji,
            colorIndex = colorIndex
        )
        if (editingId == null) repo.addEvent(event) else repo.updateEvent(event)
        CountdownWidgetProvider.refreshAll(this)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun confirmDelete() {
        val id = editingId ?: return
        val title = titleEdit.text.toString().trim()
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_msg, title))
            .setPositiveButton(R.string.delete) { _, _ ->
                repo.deleteEvent(id)
                CountdownWidgetProvider.refreshAll(this)
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
