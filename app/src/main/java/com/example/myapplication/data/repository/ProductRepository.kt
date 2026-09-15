package com.example.myapplication.data.repository

import com.example.myapplication.data.local.Product
import com.example.myapplication.data.local.ProductDao
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun addProduct(name: String, price: Double) {
        productDao.insert(Product(name = name, price = price))
    }

    suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }
}
