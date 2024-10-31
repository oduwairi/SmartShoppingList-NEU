package com.iriawud.smartshoppinglist.ui.home

data class ShoppingItem(
    val name:String,
    val quantity:String,
    val category: String,
    val price: String,
    val imageUrl: String? = null,
    val unit:String = "pc"
)

