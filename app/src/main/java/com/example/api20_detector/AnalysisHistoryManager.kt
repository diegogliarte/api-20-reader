package com.example.api20_detector

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AnalysisHistoryManager(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "analysis_history")

    private object PreferencesKeys {
        val HISTORY = stringPreferencesKey("history")
    }

    suspend fun saveAnalysisHistory(title: String, notes: String, code: String) {
        // Format the current date and time for lower API levels
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val formattedDateTime = formatter.format(calendar.time)

        // Construct historyData with date appended
        val historyData = "$title|$notes|$code|$formattedDateTime"

        context.dataStore.edit { preferences ->
            val currentHistory = preferences[PreferencesKeys.HISTORY] ?: ""
            preferences[PreferencesKeys.HISTORY] = currentHistory + historyData + ";;"
        }
    }

    suspend fun removeAnalysisHistory(itemToRemove: HistoryItem) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[PreferencesKeys.HISTORY] ?: ""
            Log.d("AnalysisHistoryManager", currentHistory)
            val updatedHistory = currentHistory.split(";;")
                .filter { it.isNotEmpty() }
                .map { it.split("|") }
                .filterNot { historyItem ->
                            historyItem[0] == itemToRemove.title &&
                            historyItem[1] == itemToRemove.notes &&
                            historyItem[2] == itemToRemove.code &&
                            historyItem[3] == itemToRemove.date
                }.joinToString(";;") { it.joinToString("|") } + ";;"

            preferences[PreferencesKeys.HISTORY] = updatedHistory
        }
    }

    suspend fun getAnalysisHistory(): String {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HISTORY] ?: ""
        }.first()
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
