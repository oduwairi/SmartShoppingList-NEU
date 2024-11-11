package com.iriawud.smartshoppinglist.ui.inventory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iriawud.smartshoppinglist.network.Category
import com.iriawud.smartshoppinglist.network.InventoryItem
import com.iriawud.smartshoppinglist.network.RetrofitInstance
import com.iriawud.smartshoppinglist.ui.ItemViewModel
import com.iriawud.smartshoppinglist.ui.shopping.ShoppingItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewModel : ViewModel(), ItemViewModel {

    private val _items = MutableLiveData<MutableList<ShoppingItem>>()
    val items: LiveData<MutableList<ShoppingItem>> get() = _items

    private val _categories = MutableLiveData<Map<Int, String>>()
    val categories: LiveData<Map<Int, String>> get() = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        _items.value = mutableListOf() // Initialize with an empty list
        fetchCategories() // Fetch categories at initialization
    }

    // Fetch inventory items from backend
    fun fetchInventoryItems() {
        _isLoading.value = true
        RetrofitInstance.api.getInventoryItems().enqueue(object : Callback<List<InventoryItem>> {
            override fun onResponse(
                call: Call<List<InventoryItem>>,
                response: Response<List<InventoryItem>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val categoryMap = _categories.value ?: emptyMap()
                    val fetchedItems = response.body()?.map { inventoryItem ->
                        val createdAt = MathUtils.convertToSimpleDateFormat(inventoryItem.stocked_at)
                        val categoryName = categoryMap[inventoryItem.category_id] ?: "Uncategorized"
                        val frequency = MathUtils.calculateFrequency(
                            inventoryItem.stocked_at ?: ShoppingItem.getCurrentTimestamp(),
                            inventoryItem.restock_date
                        )
                        // Map InventoryItem to ShoppingItem
                        ShoppingItem(
                            name = inventoryItem.item_name,
                            quantity = inventoryItem.quantity_stocked.toString() + " per " + inventoryItem.quantity_unit,
                            price = inventoryItem.price.toString() + " " + inventoryItem.currency,
                            priority = inventoryItem.priority,
                            category = categoryName,
                            imageUrl = inventoryItem.image_url ?: inventoryItem.item_name.lowercase(),
                            frequency = frequency,
                            createdAt = createdAt
                        )
                    } ?: emptyList()
                    _items.value = fetchedItems.toMutableList()
                } else {
                    _error.value = "Failed to fetch items: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    // Fetch categories from the backend and map them
    private fun fetchCategories() {
        _isLoading.value = true
        RetrofitInstance.api.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val categoryList = response.body() ?: emptyList()
                    val categoryMap = categoryList.associateBy({ it.category_id }, { it.category_name })
                    _categories.value = categoryMap
                } else {
                    _error.value = "Failed to fetch categories: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error fetching categories: ${t.message}"
            }
        })
    }



    // Add a new inventory item to backend
    override fun addItem(item: ShoppingItem) {
        _isLoading.value = true

        val categoryMap = _categories.value ?: emptyMap()
        val categoryId = categoryMap.keys.firstOrNull { categoryMap[it] == item.category }
        if (categoryId == null) {
            Log.w("InventoryViewModel", "Category '${item.category}' not found in categories map.")
        }

        val quantityParts = item.quantity.split(" ")
        val quantityStocked = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val quantityUnit = quantityParts.getOrNull(1)?.trim() ?: "pcs"

        val priceParts = item.price.split(" ")
        val priceValue = priceParts.getOrNull(0)?.toDoubleOrNull()
        val currency = priceParts.getOrNull(1)?.trim() ?: "USD"

        val restockDate = MathUtils.calculateRestockDate(item.createdAt, item.frequency)

        val inventoryItem = InventoryItem(
            inventory_id = 1, // Adjust inventory_id logic as needed
            item_name = item.name,
            quantity_stocked = quantityStocked,
            quantity_unit = quantityUnit,
            price = priceValue,
            currency = currency,
            image_url = item.imageUrl,
            priority = item.priority,
            category_id = categoryId ?: 1,
            stocked_at = item.createdAt,
            restock_date = restockDate
        )

        RetrofitInstance.api.addInventoryItem(inventoryItem).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    addItemLocally(item)
                } else {
                    _error.value = "Failed to add item: ${response.message()}"
                    Log.e("InventoryViewModel", "Error adding item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                Log.e("InventoryViewModel", "API call failed: ${t.message}", t)
            }
        })
    }



    // Add item locally for immediate UI update
    private fun addItemLocally(item: ShoppingItem) {
        _items.value?.let {
            it.add(item)
            _items.value = it // Trigger LiveData update
        }
    }
}
