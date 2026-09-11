package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val total: Double,
    val paymentType: String,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
