package com.calculator.nur.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MathHistoryDao {
    @Query("SELECT * FROM math_history ORDER BY id DESC LIMIT 500")
    fun getHistory(): Flow<List<MathHistory>>

    @Insert
    suspend fun insertHistory(history: MathHistory)
    
    @Query("DELETE FROM math_history WHERE id NOT IN (SELECT id FROM math_history ORDER BY id DESC LIMIT 500)")
    suspend fun enforceLimit()
    
    @Query("DELETE FROM math_history")
    suspend fun clearHistory()
}
