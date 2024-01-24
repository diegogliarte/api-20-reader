package com.example.api20_detector

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AnalysisHistoryManager(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "analysis_history")

    private object PreferencesKeys {
        val HISTORY = stringPreferencesKey("history")
    }

    suspend fun saveAnalysisHistory(title: String, notes: String, code: String) {
        val historyData = "$title|$notes|$code"
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[PreferencesKeys.HISTORY] ?: ""
            preferences[PreferencesKeys.HISTORY] = currentHistory + historyData + ";;"
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
