package com.example.api20_detector.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.api20_detector.AnalysisHistoryManager
import com.example.api20_detector.HistoryAdapter
import com.example.api20_detector.HistoryItem
import com.example.api20_detector.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var analysisHistoryManager: AnalysisHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        analysisHistoryManager = AnalysisHistoryManager.getInstance(this)
        setupRecyclerView()
        loadHistory()
    }

    private fun updateRecyclerViewWithHistoryItems(historyItems: List<HistoryItem>) {
        // Define the adapter with the current history items and the deletion callback
        viewAdapter = HistoryAdapter(historyItems) { historyItem ->
            showDeletionConfirmationDialog(historyItem)
        }

        // Set the adapter to the RecyclerView
        recyclerView.adapter = viewAdapter
    }

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        // Initially, set an empty list or load the history right away
        loadHistory()
    }

    private fun loadHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            val history = analysisHistoryManager.getAnalysisHistory()
            val historyItems = parseHistory(history)
            withContext(Dispatchers.Main) {
                updateRecyclerViewWithHistoryItems(historyItems)
            }
        }
    }


    private fun showDeletionConfirmationDialog(historyItem: HistoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete History")
            .setMessage("Are you sure you want to delete this history item?")
            .setPositiveButton("Delete") { dialog, which ->
                // Delete the item and reload history
                CoroutineScope(Dispatchers.IO).launch {
                    analysisHistoryManager.removeAnalysisHistory(historyItem)
                    loadHistory() // Make sure this method correctly updates the adapter on the UI thread
                }
            }
            .setNegativeButton("Cancel", null) // Dismiss dialog without action
            .show()
    }

    private fun parseHistory(historyString: String): List<HistoryItem> {
        return historyString.split(";;")
            .filter { it.isNotEmpty() }
            .map { historyItem ->
                val parts = historyItem.split("|")
                HistoryItem(parts[0], parts[1], parts[2], parts[3], parts[4])
            }
    }
}
