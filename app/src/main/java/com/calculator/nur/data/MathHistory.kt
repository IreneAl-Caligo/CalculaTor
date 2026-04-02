package com.calculator.nur.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "math_history")
data class MathHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
