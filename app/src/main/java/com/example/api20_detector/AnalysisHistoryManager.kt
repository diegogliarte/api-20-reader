package com.example.api20_detector

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AnalysisHistoryManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val historyItemDao = db.historyItemDao()

    private object PreferencesKeys {
        val HISTORY = stringPreferencesKey("history")
    }

    suspend fun saveAnalysisHistory(historyUUID: String, title: String, notes: String, code: String) {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val formattedDateTime = formatter.format(calendar.time)
        val newHistoryItem = HistoryItem(historyUUID, title, notes, code, formattedDateTime)

        historyItemDao.insertHistoryItem(newHistoryItem)
    }

    suspend fun removeAnalysisHistory(itemToRemove: HistoryItem) {
        historyItemDao.deleteHistoryItem(itemToRemove)
    }

    fun getAnalysisHistory(): LiveData<List<HistoryItem>> {
        return historyItemDao.getAllHistoryItems()
    }

    companion object {
        @Volatile
        private var INSTANCE: AnalysisHistoryManager? = null

        fun getInstance(context: Context): AnalysisHistoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalysisHistoryManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
