package com.example.api20_detector

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "date") val date: String
)

@Dao
interface HistoryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(historyItem: HistoryItem)

    @Query("SELECT * FROM history_items")
    fun getAllHistoryItems(): LiveData<List<HistoryItem>>

    @Delete
    suspend fun deleteHistoryItem(historyItem: HistoryItem)

    @Query(
        "SELECT * FROM history_items " +
                "WHERE title LIKE :query OR " +
                "notes LIKE :query OR " +
                "code LIKE :query"
    )
    fun searchHistoryItems(query: String): LiveData<List<HistoryItem>>

}

