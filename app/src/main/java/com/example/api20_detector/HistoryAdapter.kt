package com.example.api20_detector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<HistoryItem>, val onDeleteClick: (HistoryItem) -> Unit) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.history_item_title)
        val notesTextView: TextView = view.findViewById(R.id.history_item_notes)
        val codeTextView: TextView = view.findViewById(R.id.history_item_code)
        val dateTextView: TextView = view.findViewById(R.id.history_item_date)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.titleTextView.text = item.title
        holder.notesTextView.text = item.notes
        holder.codeTextView.text = item.code
        holder.dateTextView.text = item.date

        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = historyList.size
}
