package com.example.myapplication.data.repository

import com.example.myapplication.data.local.Order
import com.example.myapplication.data.local.OrderDao
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for Order data.
 * ViewModels talk to this class only — never directly to Room or a network API.
 */
class OrderRepository(
    private val orderDao: OrderDao
) {
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun saveOrderLocally(order: Order): Long {
        return orderDao.insert(order)
    }

    suspend fun getUnsyncedOrders(): List<Order> {
        return orderDao.getUnsyncedOrders()
    }

    suspend fun markSynced(order: Order) {
        orderDao.update(order.copy(synced = true))
    }
}
