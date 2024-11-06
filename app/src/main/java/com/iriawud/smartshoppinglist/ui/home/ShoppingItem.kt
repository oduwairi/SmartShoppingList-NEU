package com.iriawud.smartshoppinglist.ui.home


data class ShoppingItem(
    val name:String,
    val quantity:String = "1 pc",
    val category: String = "Uncategorized",
    val price: String = "1 USD",
    val priority: Int = 5,
    val imageUrl: String = name.lowercase(),
    val frequency: String = "1 per week"
)



