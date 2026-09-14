package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order): Unit

    @Query("SELECT * FROM orders WHERE synced = 0")
    suspend fun getUnsyncedOrders(): List<Order>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>
}
