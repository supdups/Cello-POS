package com.example.myapplication.ui.items

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Product
import com.example.myapplication.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewProductForm(
    val name: String = "",
    val price: String = ""
)

class ItemsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _form = MutableStateFlow(NewProductForm())
    val form: StateFlow<NewProductForm> = _form

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Which product (if any) is currently being edited inline.
    private val _editingId = MutableStateFlow<Long?>(null)
    val editingId: StateFlow<Long?> = _editingId

    fun onNameChange(value: String) = _form.update { it.copy(name = value) }
    fun onPriceChange(value: String) = _form.update { it.copy(price = value) }

    // Create
    fun addProduct() {
        val current = _form.value
        val price = current.price.toDoubleOrNull()
        if (current.name.isBlank() || price == null || price <= 0) {
            _error.value = "Enter a name and a valid price"
            return
        }
        viewModelScope.launch {
            repository.addProduct(current.name.trim(), price)
            _form.value = NewProductForm()
            _error.value = null
        }
    }

    // Update — start editing
    fun startEditing(product: Product) {
        _editingId.value = product.id
        _form.value = NewProductForm(name = product.name, price = product.price.toString())
    }

    fun cancelEditing() {
        _editingId.value = null
        _form.value = NewProductForm()
    }

    fun saveEdit(product: Product) {
        val current = _form.value
        val price = current.price.toDoubleOrNull()
        if (current.name.isBlank() || price == null || price <= 0) {
            _error.value = "Enter a name and a valid price"
            return
        }
        viewModelScope.launch {
            repository.updateProduct(product.copy(name = current.name.trim(), price = price))
            _editingId.value = null
            _form.value = NewProductForm()
            _error.value = null
        }
    }

    // Delete
    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).productDao()
            @Suppress("UNCHECKED_CAST")
            return ItemsViewModel(ProductRepository(dao)) as T
        }
    }
}
