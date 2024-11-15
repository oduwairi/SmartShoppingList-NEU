package com.iriawud.smartshoppinglist.ui.shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iriawud.smartshoppinglist.ui.ItemViewModel
import android.util.Log
import com.iriawud.smartshoppinglist.network.Category
import com.iriawud.smartshoppinglist.network.InventoryItem
import com.iriawud.smartshoppinglist.network.RetrofitInstance
import com.iriawud.smartshoppinglist.network.ShoppingListItem
import com.iriawud.smartshoppinglist.ui.inventory.MathUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShoppingViewModel : ViewModel(), ItemViewModel {

    private val _items = MutableLiveData<MutableList<ShoppingItem>>()
    val items: LiveData<MutableList<ShoppingItem>> get() = _items

    private val _categories = MutableLiveData<Map<Int, String>>()
    val categories: LiveData<Map<Int, String>> get() = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun initializeData() {
        _items.value = mutableListOf() // Initialize with an empty list
        fetchCategories() // Fetch categories first
        fetchShoppingItems() // Fetch shopping items only after categories are fetched
    }

    // Fetch inventory items from backend
    private fun fetchShoppingItems() {
        _isLoading.value = true
        RetrofitInstance.api.getShoppingItems().enqueue(object : Callback<List<ShoppingListItem>> {
            override fun onResponse(
                call: Call<List<ShoppingListItem>>,
                response: Response<List<ShoppingListItem>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val categoryMap = _categories.value ?: emptyMap()
                    val fetchedItems = response.body()?.map { shoppingListItem ->
                        val createdAt = MathUtils.convertToSimpleDateFormat(shoppingListItem.added_at)
                        val categoryName = categoryMap[shoppingListItem .category_id] ?: "Uncategorized"
                        // Map ShoppingListItem to ShoppingItem
                        ShoppingItem(
                            id = shoppingListItem .item_id,
                            name = shoppingListItem .item_name,
                            quantity = "${if (shoppingListItem.quantity % 1 == 0.0) shoppingListItem.quantity.toInt() else shoppingListItem.quantity} ${shoppingListItem.quantity_unit}",
                            price = shoppingListItem .price.toString() + " " + shoppingListItem .currency,
                            priority =shoppingListItem .priority,
                            category = categoryName,
                            imageUrl = shoppingListItem .image_url ?: shoppingListItem .item_name.lowercase(),
                            frequency = if (shoppingListItem.frequency_value != null && shoppingListItem.frequency_unit != null) {
                                "${shoppingListItem.frequency_value} per ${shoppingListItem.frequency_unit}"
                            } else {
                                "Not set"
                            } ,
                            createdAt = createdAt
                        )
                    } ?: emptyList()
                    _items.value = fetchedItems.toMutableList()
                } else {
                    _error.value = "Failed to fetch items: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<ShoppingListItem>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }


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
                    Log.e("ShoppingViewModel", "Error fetching categories: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error fetching categories: ${t.message}"
                Log.e("ShoppingViewModel", "API call failed: ${t.message}")
            }
        })
    }

    // Add a new shopping item to the backend
    override fun addItem(item: ShoppingItem) {
        _isLoading.value = true

        val categoryMap = _categories.value ?: emptyMap()
        val categoryId = categoryMap.keys.firstOrNull { categoryMap[it] == item.category }
        if (categoryId == null) {
            Log.w("ShoppingViewModel", "Category '${item.category}' not found in categories map.")
        }

        val quantityParts = item.quantity.split(" ")
        val quantityValue = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val quantityUnit = quantityParts.getOrNull(1)?.trim() ?: "pcs"

        val priceParts = item.price.split(" ")
        val priceValue = priceParts.getOrNull(0)?.toDoubleOrNull()
        val currency = priceParts.getOrNull(1)?.trim() ?: "USD"

        val frequencyParts = item.frequency.split(" ")
        val frequencyValue = frequencyParts.getOrNull(0)?.toIntOrNull()  //1 ETC..
        val frequencyUnit = frequencyParts.getOrNull(2)?.trim() // WEEK, MONTH ETC...

        val shoppingListItem = ShoppingListItem(
            item_id = null, // Auto-generated by backend
            list_id = 1,
            item_name = item.name,
            quantity = quantityValue,
            quantity_unit = quantityUnit,
            price = priceValue,
            currency = currency,
            image_url = item.imageUrl,
            priority = item.priority,
            frequency_value =  frequencyValue,
            frequency_unit = frequencyUnit,
            category_id = categoryId ?: 1,
            added_at = item.createdAt
        )

        RetrofitInstance.api.addShoppingItem(shoppingListItem).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    addItemLocally(item)
                } else {
                    _error.value = "Failed to add item: ${response.message()}"
                    Log.e("ShoppingViewModel", "Error adding item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                Log.e("ShoppingViewModel", "API call failed: ${t.message}", t)
            }
        })
    }

    fun deleteItem(item: ShoppingItem) {
        _isLoading.value = true

        val itemId = item.id
        if (itemId == null) {
            _error.value = "Item ID is null, cannot delete item."
            return
        }
        RetrofitInstance.api.deleteShoppingItem(itemId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    // Remove the item locally for immediate UI update
                    deleteItemLocally(item)
                } else {
                    _error.value = "Failed to delete item: ${response.message()}"
                    Log.e("ShoppingViewModel", "Error deleting item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                Log.e("ShoppingViewModel", "API call failed: ${t.message}", t)
            }
        })
    }

    private fun addItemLocally(item: ShoppingItem) {
        _items.value?.let {
            it.add(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun deleteItemLocally(item: ShoppingItem) {
        _items.value?.let {
            it.remove(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun deleteAllItems() {
        _items.value?.clear()
        _items.postValue(_items.value)  // Notify observers of the change
    }
}
