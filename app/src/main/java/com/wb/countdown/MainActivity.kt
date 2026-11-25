package com.wb.countdown

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var repo: EventRepository
    private lateinit var adapter: EventAdapter
    private lateinit var rootView: View
    private lateinit var emptyView: TextView

    private val addEventLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) loadEvents()
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    repo.setBackgroundUri(uri.toString())
                    applyBackground()
                    CountdownWidgetProvider.refreshAll(this)
                    Toast.makeText(this, getString(R.string.toast_bg_updated), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.toast_bg_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.mainRoot)
        emptyView = findViewById(R.id.emptyView)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        repo = EventRepository(this)
        adapter = EventAdapter(
            onEdit = { event ->
                val intent = Intent(this, AddEventActivity::class.java).apply {
                    putExtra("event_id", event.id)
                }
                addEventLauncher.launch(intent)
            },
            onDelete = { event ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(getString(R.string.dialog_delete_msg, event.title))
                    .setPositiveButton(R.string.delete) { _, _ ->
                        repo.deleteEvent(event.id)
                        loadEvents()
                        CountdownWidgetProvider.refreshAll(this)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
        }

        applyBackground()
        loadEvents()
        CountdownWidgetProvider.refreshAll(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_bg -> {
                pickImageLauncher.launch(arrayOf("image/*"))
                true
            }
            R.id.action_clear_bg -> {
                repo.setBackgroundUri(null)
                applyBackground()
                CountdownWidgetProvider.refreshAll(this)
                Toast.makeText(this, R.string.toast_bg_cleared, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applyBackground() {
        val uriString = repo.getBackgroundUri()
        if (uriString.isNullOrEmpty()) {
            rootView.setBackgroundResource(R.drawable.bg_gradient)
            return
        }
        val bitmap = ImageUtils.decodeScaled(this, Uri.parse(uriString), 1600)
        if (bitmap != null) {
            rootView.background = BitmapDrawable(resources, bitmap)
        } else {
            rootView.setBackgroundResource(R.drawable.bg_gradient)
        }
    }

    private fun loadEvents() {
        val events = repo.getEvents().sortedBy { DateUtils.daysLeft(it.targetDate) }
        adapter.submitList(events)
        emptyView.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }
}
