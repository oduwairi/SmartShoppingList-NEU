package com.iriawud.smartshoppinglist.ui.home

data class ShoppingItem(
    val itemName:String,
    val quantity:String,
    val itemCategory: String,
    val price: Double,
    val imageUrl: String? = null
)

