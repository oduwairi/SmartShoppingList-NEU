package com.iriawud.smartshoppinglist.ui.inventory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iriawud.smartshoppinglist.network.InventoryItem
import com.iriawud.smartshoppinglist.network.PredefinedItem
import com.iriawud.smartshoppinglist.network.RetrofitInstance
import com.iriawud.smartshoppinglist.ui.CategoryRepository
import com.iriawud.smartshoppinglist.ui.ItemViewModel
import com.iriawud.smartshoppinglist.ui.SingleLiveEvent
import com.iriawud.smartshoppinglist.ui.shopping.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel : ViewModel(), ItemViewModel {

    private val _items = MutableLiveData<MutableList<Item>>()
    val items: LiveData<MutableList<Item>> get() = _items

    private val _predefinedItems = MutableLiveData<List<PredefinedItem>>()
    val predefinedItems: LiveData<List<PredefinedItem>> get() = _predefinedItems

    private val _dueItems = SingleLiveEvent<Item>()
    val dueItems: LiveData<Item> get() = _dueItems

    private val _categories = MutableLiveData<Map<Int, String>>()
    val categories: LiveData<Map<Int, String>> get() = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val processedItems = mutableSetOf<String>() // Track processed items by name or ID

    init {
        initializeData()
    }

    fun initializeData() {
        viewModelScope.launch {
            _items.value = mutableListOf() // Initialize with an empty list
            fetchCategories() // Fetch categories first
            fetchInventoryItems() // Fetch shopping items only after categories are fetched
            fetchPredefinedItems()
        }
    }

    private suspend fun fetchInventoryItems() {
        try {
            // Indicate loading state
            _isLoading.postValue(true)

            // Fetch inventory items using Retrofit's suspend function
            val response = RetrofitInstance.api.getInventoryItems()

            if (response.isSuccessful) {
                // Map category names from the categories map
                val categoryMap = _categories.value ?: emptyMap()

                // Transform InventoryItem objects into Item objects
                val fetchedItems = response.body()?.map { inventoryItem ->
                    val createdAt =
                        MathUtils.convertToSimpleDateFormat(inventoryItem.stocked_at)
                    val categoryName =
                        categoryMap[inventoryItem.category_id] ?: "Uncategorized"
                    val frequency = MathUtils.calculateFrequencyFromRestockDate(
                        inventoryItem.stocked_at ?: Item.getCurrentTimestamp(),
                        inventoryItem.restock_date
                    )

                    // Map InventoryItem to Item
                    Item(
                        id = inventoryItem.item_id,
                        name = inventoryItem.item_name,
                        quantity = inventoryItem.quantity_stocked.toString() + " " + inventoryItem.quantity_unit,
                        price = inventoryItem.price?.let { "$it ${inventoryItem.currency}" } ?: "Not set",
                        priority = inventoryItem.priority,
                        category = categoryName,
                        imageUrl = inventoryItem.image_url ?: inventoryItem.item_name.lowercase(),
                        frequency = frequency,
                        createdAt = createdAt
                    )
                } ?: emptyList()

                // Update LiveData with the fetched items
                withContext(Dispatchers.Main) {
                    _items.value = fetchedItems.toMutableList()

                    // Process due items
                    fetchedItems.forEach { inventoryItem ->
                        if (inventoryItem.amountLeftPercent == 0 && !processedItems.contains(inventoryItem.name)) {
                            processedItems.add(inventoryItem.name) // Mark item as processed
                            _dueItems.value = inventoryItem
                        }
                    }
                }
            } else {
                // Handle unsuccessful response
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch items: ${response.message()}"
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues)
            withContext(Dispatchers.Main) {
                _error.value = "Error: ${e.message}"
            }
        } finally {
            // Reset loading state
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchCategories() {
        try {
            // Indicate loading state
            _isLoading.postValue(true)

            // Fetch categories using Retrofit's suspend function
            val response = RetrofitInstance.api.getCategories()

            if (response.isSuccessful) {
                val categoryList = response.body() ?: emptyList()
                val categoryMap = categoryList.associateBy(
                    keySelector = { it.category_id },
                    valueTransform = { it.category_name }
                )

                // Update LiveData with the fetched categories
                withContext(Dispatchers.Main) {
                    _categories.value = categoryMap
                }

                // Save categories to repository
                CategoryRepository.setCategories(categoryList)
            } else {
                // Handle unsuccessful response
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch categories: ${response.message()}"
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network errors)
            withContext(Dispatchers.Main) {
                _error.value = "Error fetching categories: ${e.message}"
            }
        } finally {
            // Reset loading state
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchPredefinedItems() {
        try {
            // Indicate loading state
            _isLoading.postValue(true)

            // Fetch predefined items using Retrofit's suspend function
            val response = RetrofitInstance.api.getPredefinedItems()

            if (response.isSuccessful) {
                val predefinedItems = response.body() ?: emptyList()

                // Update LiveData with the fetched predefined items
                withContext(Dispatchers.Main) {
                    _predefinedItems.value = predefinedItems
                }
            } else {
                // Handle unsuccessful response
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch predefined items: ${response.message()}"
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues)
            withContext(Dispatchers.Main) {
                _error.value = "Error fetching predefined items: ${e.message}"
            }
        } finally {
            // Reset loading state
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }


    override fun addItem(item: Item) {
        viewModelScope.launch {
            try {
                // Indicate loading state
                _isLoading.postValue(true)

                // Map category name to ID
                val categoryMap = _categories.value ?: emptyMap()
                val categoryId = categoryMap.keys.firstOrNull { categoryMap[it] == item.category }
                if (categoryId == null) {
                    Log.w("InventoryViewModel", "Category '${item.category}' not found in categories map.")
                }

                // Parse item fields
                val quantityParts = item.quantity.split(" ")
                val quantityStocked = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val quantityUnit = quantityParts.getOrNull(1)?.trim() ?: "pcs"

                val priceParts = item.price.split(" ")
                val priceValue = priceParts.getOrNull(0)?.toDoubleOrNull()
                val currency = priceParts.getOrNull(1)?.trim() ?: "USD"

                val restockDate = MathUtils.calculateRestockDate(item.createdAt, item.frequency)

                // Create the InventoryItem object
                val inventoryItem = InventoryItem(
                    inventory_id = 1, // Adjust inventory_id logic as needed
                    item_id = item.id,
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

                // Make the API call using Retrofit's suspend function
                val response = RetrofitInstance.api.addInventoryItem(inventoryItem)

                if (response.isSuccessful) {
                    // Check if the item already exists locally
                    val existingItemIndex = _items.value?.indexOfFirst { it.name.lowercase().trim() == item.name.lowercase().trim() }

                    if (existingItemIndex != null && existingItemIndex != -1) {
                        // Update the existing item
                        val updatedList = _items.value?.toMutableList()
                        updatedList?.set(existingItemIndex, item)
                        _items.postValue(updatedList)
                    } else {
                        // Add the item locally if it's new
                        addItemLocally(item)
                    }
                } else {
                    // Handle unsuccessful response
                    _error.postValue("Failed to add item: ${response.message()}")
                    Log.e("InventoryViewModel", "Error adding item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., network issues)
                _error.postValue("Error: ${e.message}")
                Log.e("InventoryViewModel", "API call failed: ${e.message}", e)
            } finally {
                // Reset loading state
                _isLoading.postValue(false)
            }
        }
    }

    override fun updateItem(item: Item) {
        // Stub implementation, skipping for now
    }

    // Add item locally for immediate UI update
    private fun addItemLocally(item: Item) {
        _items.value?.let {
            it.add(item)
            _items.value = it // Trigger LiveData update
        }
    }
}
