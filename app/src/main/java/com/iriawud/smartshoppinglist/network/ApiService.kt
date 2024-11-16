package com.iriawud.smartshoppinglist.network

import com.iriawud.smartshoppinglist.ui.shopping.ShoppingItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


data class InventoryItem(
    val item_id: Int? = null,
    val inventory_id: Int,
    val item_name: String,
    val quantity_stocked: Double,
    val quantity_unit: String?,
    val price: Double?,
    val currency: String?,
    val image_url: String?,
    val priority: Int,
    val category_id: Int,
    val stocked_at: String?,
    val restock_date: String?
)

data class ShoppingListItem(
    val item_id: Int? = null,              // Primary key (auto-incremented, nullable for POST)
    val list_id: Int,                      // Foreign key referencing the shopping list
    val item_name: String,                 // Name of the item
    val quantity: Double,                  // Quantity of the item
    val quantity_unit: String?,            // Unit of measurement (e.g., kg, pcs)
    val price: Double?,                    // Price of the item (nullable)
    val currency: String?,                 // Currency (e.g., USD, EUR)
    val image_url: String,                 // Image URL
    val priority: Int,                     // Priority level
    val frequency_value: Int?,             // Frequency value
    val frequency_unit: String?,           // Frequency unit (e.g., day, week)
    val category_id: Int,                  // Foreign key referencing the category table
    val added_at: String                 // Timestamp when the item was created
)

data class Category(
    val category_id: Int,
    val category_name: String,
    val category_color: String,
    val category_image_url: String
)

data class PredefinedItem(
    val item_id: Int,
    val category_id: Int,
    val item_name: String,
    val average_quantity: Double,
    val default_quantity_unit: String,
    val average_price: Double,
    val default_currency: String,
    val image_url: String,
    val average_priority: Int,
    val average_consumption_rate: Double,
    val default_consumption_unit: String
)


interface ApiService {
    // GET all inventory items
    @GET("/inventory_items")
    fun getInventoryItems(): Call<List<InventoryItem>>

    // POST a new inventory item
    @POST("/inventory_items")
    fun addInventoryItem(@Body item: InventoryItem): Call<Void>

    @GET("/categories")
    fun getCategories(): Call<List<Category>>

    @POST("/categories")
    fun addCategory(@Body category: Category): Call<Void>

    // GET all shopping items
    @GET("/shopping_items")
    fun getShoppingItems(): Call<List<ShoppingListItem>>

    // POST a new shopping item
    @POST("/shopping_items")
    fun addShoppingItem(@Body item: ShoppingListItem): Call<Void>

    // DELETE a shopping item by ID
    @DELETE("/shopping_items/{id}")
    fun deleteShoppingItem(@Path("id") itemId: Int): Call<Void>

    // GET all predefined items
    @GET("/predefined_items")
    fun getPredefinedItems(): Call<List<PredefinedItem>>
}