package com.iriawud.smartshoppinglist.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


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

data class Category(
    val category_id: Int,
    val category_name: String,
    val category_color: String,
    val category_image_url: String
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
}