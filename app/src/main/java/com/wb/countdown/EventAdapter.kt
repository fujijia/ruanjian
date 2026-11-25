package com.wb.countdown

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val onEdit: (CountdownEvent) -> Unit,
    private val onDelete: (CountdownEvent) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<CountdownEvent> = emptyList()

    fun submitList(list: List<CountdownEvent>) {
        events = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position], onEdit, onDelete)
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiView: TextView = itemView.findViewById(R.id.itemEmoji)
        private val titleView: TextView = itemView.findViewById(R.id.itemTitle)
        private val dateView: TextView = itemView.findViewById(R.id.itemDate)
        private val daysView: TextView = itemView.findViewById(R.id.itemDays)
        private val unitView: TextView = itemView.findViewById(R.id.itemUnit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.itemDelete)

        fun bind(
            event: CountdownEvent,
            onEdit: (CountdownEvent) -> Unit,
            onDelete: (CountdownEvent) -> Unit
        ) {
            val days = DateUtils.daysLeft(event.targetDate)
            emojiView.text = event.emoji
            titleView.text = event.title
            dateView.text = "${DateUtils.formatChinese(event.targetDate)} · ${DateUtils.weekday(event.targetDate)}"

            if (days > 0) {
                daysView.text = days.toString()
                unitView.text = "天后"
            } else if (days == 0) {
                daysView.text = "今天"
                unitView.text = ""
            } else {
                daysView.text = (-days).toString()
                unitView.text = "天前"
            }

            itemView.setBackgroundResource(CARD_BACKGROUNDS[event.colorIndex % CARD_BACKGROUNDS.size])
            itemView.setOnClickListener { onEdit(event) }
            deleteButton.setOnClickListener { onDelete(event) }
        }

        companion object {
            private val CARD_BACKGROUNDS = intArrayOf(
                R.drawable.card_pink,
                R.drawable.card_purple,
                R.drawable.card_cyan,
                R.drawable.card_orange
            )
        }
    }
}
