package com.iriawud.smartshoppinglist.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Data classes remain unchanged
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
    val item_id: Int? = null,
    val list_id: Int,
    val item_name: String,
    val quantity: Double,
    val quantity_unit: String?,
    val price: Double?,
    val currency: String?,
    val image_url: String,
    val priority: Int,
    val frequency_value: Int?,
    val frequency_unit: String?,
    val category_id: Int,
    val added_at: String
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

    // GET all inventory items (coroutines)
    @GET("/inventory_items")
    suspend fun getInventoryItems(): Response<List<InventoryItem>>

    // POST a new inventory item
    @POST("/inventory_items")
    suspend fun addInventoryItem(@Body item: InventoryItem): Response<Void>

    // GET all categories
    @GET("/categories")
    suspend fun getCategories(): Response<List<Category>>

    // POST a new category
    @POST("/categories")
    suspend fun addCategory(@Body category: Category): Response<Void>

    // GET all shopping items
    @GET("/shopping_items")
    suspend fun getShoppingItems(): Response<List<ShoppingListItem>>

    // POST a new shopping item
    @POST("/shopping_items")
    suspend fun addShoppingItem(@Body item: ShoppingListItem): Response<Void>

    // DELETE a shopping item by ID
    @DELETE("/shopping_items/{id}")
    suspend fun deleteShoppingItem(@Path("id") itemId: Int): Response<Void>

    // Update an existing shopping item by ID
    @PUT("/shopping_items/{id}")
    suspend fun updateShoppingItem(
        @Path("id") itemId: Int,
        @Body updatedItem: ShoppingListItem
    ): Response<Void>


    // GET all predefined items
    @GET("/predefined_items")
    suspend fun getPredefinedItems(): Response<List<PredefinedItem>>
}
