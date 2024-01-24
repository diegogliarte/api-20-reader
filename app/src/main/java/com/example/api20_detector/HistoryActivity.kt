package com.example.api20_detector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = HistoryAdapter(emptyList())

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun loadHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            val history = analysisHistoryManager.getAnalysisHistory()
            val historyItems = parseHistory(history)
            withContext(Dispatchers.Main) {
                viewAdapter = HistoryAdapter(historyItems)
                recyclerView.adapter = viewAdapter
            }
        }
    }

    private fun parseHistory(historyString: String): List<HistoryItem> {
        return historyString.split(";;")
            .filter { it.isNotEmpty() }
            .map { historyItem ->
                val parts = historyItem.split("|")
                HistoryItem(parts[0], parts[1], parts[2])
            }
    }
}
