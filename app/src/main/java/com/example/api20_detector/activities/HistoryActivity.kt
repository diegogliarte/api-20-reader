package com.example.api20_detector.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
    private lateinit var analysisHistoryManager: AnalysisHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        analysisHistoryManager = AnalysisHistoryManager.getInstance(this)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }

        analysisHistoryManager.getAnalysisHistory().observe(this, Observer { historyItems ->
            recyclerView.adapter = HistoryAdapter(historyItems) { historyItem ->
                showDeletionConfirmationDialog(historyItem)
            }
        })
    }

    private fun showDeletionConfirmationDialog(historyItem: HistoryItem) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_history_dialog_title))
            .setMessage(getString(R.string.delete_history_dialog_message))
            .setPositiveButton(getString(R.string.delete_history_dialog_positive)) { dialog, which ->
                // Delete the item and reload history
                CoroutineScope(Dispatchers.Main).launch {
                    analysisHistoryManager.removeAnalysisHistory(historyItem)
                }
            }
            .setNegativeButton(getString(R.string.delete_history_dialog_negative), null) // Dismiss dialog without action
            .show()
    }
}
