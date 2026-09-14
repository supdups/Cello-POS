package com.example.myapplication.ui.order

data class Product(
    val id: String,
    val name: String,
    val price: Double
)

// TODO: replace with products loaded from Room/a backend once the menu needs to be editable.
val sampleMenu = listOf(
    Product("espresso", "Kopi Susu Aren", 3.00),
    Product("latte", "Latte", 4.50),
    Product("cappuccino", "Crystal Meth", 4.50),
    Product("americano", "Americano", 3.50),
    Product("drink", "ganja", 3.25),
    Product("aren", "ujang", 3.00),
)
