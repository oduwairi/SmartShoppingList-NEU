package com.iriawud.smartshoppinglist.ui.shopping

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iriawud.smartshoppinglist.network.PredefinedItem
import com.iriawud.smartshoppinglist.network.RecommendationItem
import com.iriawud.smartshoppinglist.network.RetrofitInstance
import com.iriawud.smartshoppinglist.network.ShoppingListItem
import com.iriawud.smartshoppinglist.ui.CategoryRepository
import com.iriawud.smartshoppinglist.ui.ItemViewModel
import com.iriawud.smartshoppinglist.ui.inventory.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShoppingViewModel : ViewModel(), ItemViewModel {

    private val _items = MutableLiveData<MutableList<Item>>()
    val items: LiveData<MutableList<Item>> get() = _items

    private val _categories = MutableLiveData<Map<Int, String>>()
    val categories: LiveData<Map<Int, String>> get() = _categories

    private val _predefinedItems = MutableLiveData<List<PredefinedItem>>()
    val predefinedItems: LiveData<List<PredefinedItem>> get() = _predefinedItems

    private val _recommendations = MutableLiveData<List<RecommendationItem>>()
    val recommendations: LiveData<List<RecommendationItem>> get() = _recommendations


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun initializeData() {
        viewModelScope.launch {
            _items.value = mutableListOf() // Initialize with an empty list
            fetchCategories() // Wait for categories to be fetched
            fetchShoppingItems() // Wait for shopping items to be fetched
            fetchPredefinedItems() // Wait for predefined items to be fetched
            fetchRecommendations() // Fetch recommendations
        }
    }

    private suspend fun fetchShoppingItems() {
        try {
            // Indicate loading state
            _isLoading.postValue(true)

            // Fetch shopping items using Retrofit's suspend function
            val response = RetrofitInstance.api.getShoppingItems()

            if (response.isSuccessful) {
                // Map categories to their names
                val categoryMap = _categories.value ?: emptyMap()

                // Transform the fetched data into your desired model
                val fetchedItems = response.body()?.map { shoppingListItem ->
                    val createdAt =
                        MathUtils.convertToSimpleDateFormat(shoppingListItem.added_at)
                    val categoryName =
                        categoryMap[shoppingListItem.category_id] ?: "Uncategorized"

                    // Map ShoppingListItem to your Item data model
                    Item(
                        id = shoppingListItem.item_id,
                        name = shoppingListItem.item_name,
                        quantity = "${if (shoppingListItem.quantity % 1 == 0.0) shoppingListItem.quantity.toInt() else shoppingListItem.quantity} ${shoppingListItem.quantity_unit}",
                        price = shoppingListItem.price?.let { "$it ${shoppingListItem.currency}" } ?: "Not set",
                        priority = shoppingListItem.priority,
                        category = categoryName,
                        imageUrl = shoppingListItem.image_url
                            ?: shoppingListItem.item_name.lowercase(),
                        frequency = if (shoppingListItem.frequency_value != null && shoppingListItem.frequency_unit != null) {
                            "${shoppingListItem.frequency_value} per ${shoppingListItem.frequency_unit}"
                        } else {
                            "Not set"
                        },
                        createdAt = createdAt
                    )
                } ?: emptyList()

                // Update LiveData on the main thread
                withContext(Dispatchers.Main) {
                    _items.value = fetchedItems.toMutableList()
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
            _isLoading.postValue(true) // Indicate loading state

            // Make the API call (this runs on Dispatchers.IO by default)
            val response = RetrofitInstance.api.getCategories()

            if (response.isSuccessful) {
                val categoryList = response.body() ?: emptyList()
                val categoryMap = categoryList.associateBy(
                    keySelector = { it.category_id },
                    valueTransform = { it.category_name }
                )

                // Update LiveData on the main thread
                withContext(Dispatchers.Main) {
                    _categories.value = categoryMap
                }

                // Save categories in the repository
                CategoryRepository.setCategories(categoryList)
            } else {
                // Handle error response
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch categories: ${response.message()}"
                }
            }
        } catch (e: Exception) {
            // Handle network or API call exceptions
            withContext(Dispatchers.Main) {
                _error.value = "Error fetching categories: ${e.message}"
            }
        } finally {
            // Ensure loading indicator is reset
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
                // Extract the predefined items from the response
                val predefinedItems = response.body() ?: emptyList()

                // Update LiveData on the main thread
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

    private suspend fun fetchRecommendations() {
        try {
            _isLoading.postValue(true)

            val userId = 1 // Replace with the actual user ID
            val response = RetrofitInstance.api.getRecommendationItems(userId)

            if (response.isSuccessful) {
                val fetchedRecommendations = response.body() ?: emptyList()

                // Update LiveData with fetched recommendations
                withContext(Dispatchers.Main) {
                    _recommendations.value = fetchedRecommendations
                }
            } else {
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch recommendations: ${response.message()}"
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _error.value = "Error fetching recommendations: ${e.message}"
            }
        } finally {
            _isLoading.postValue(false)
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
                    Log.w("ShoppingViewModel", "Category '${item.category}' not found in categories map.")
                }

                // Parse item fields
                val quantityParts = item.quantity.split(" ")
                val quantityValue = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val quantityUnit = quantityParts.getOrNull(1)?.trim() ?: "pcs"

                val priceParts = item.price.split(" ")
                val priceValue = priceParts.getOrNull(0)?.toDoubleOrNull()
                val currency = priceParts.getOrNull(1)?.trim() ?: "USD"

                val frequencyParts = item.frequency.split(" ")
                val frequencyValue = frequencyParts.getOrNull(0)?.toIntOrNull() // e.g., "1"
                val frequencyUnit = frequencyParts.getOrNull(2)?.trim() // e.g., "week"

                // Create the ShoppingListItem object
                val shoppingListItem = ShoppingListItem(
                    item_id = item.id,
                    list_id = 1,
                    item_name = item.name,
                    quantity = quantityValue,
                    quantity_unit = quantityUnit,
                    price = priceValue,
                    currency = currency,
                    image_url = item.imageUrl,
                    priority = item.priority,
                    frequency_value = frequencyValue,
                    frequency_unit = frequencyUnit,
                    category_id = categoryId ?: 1,
                    added_at = item.createdAt
                )

                // Make the API call using Retrofit's suspend function
                val response = RetrofitInstance.api.addShoppingItem(shoppingListItem)

                if (response.isSuccessful) {
                    // Check if the item already exists locally
                    val existingItemIndex = _items.value?.indexOfFirst { it.name.lowercase().trim() == item.name.lowercase().trim() }

                    if (existingItemIndex != null && existingItemIndex != -1) {
                        // Update the existing item
                        val updatedList = _items.value?.toMutableList()
                        updatedList?.set(existingItemIndex, item)
                        _items.postValue(updatedList!!)
                    } else {
                        // Add the item locally if it's new
                        addItemLocally(item)
                    }
                } else {
                    // Handle unsuccessful response
                    _error.postValue("Failed to add item: ${response.message()}")
                    Log.e("ShoppingViewModel", "Error adding item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., network issues)
                _error.postValue("Error: ${e.message}")
                Log.e("ShoppingViewModel", "API call failed: ${e.message}", e)
            } finally {
                // Reset loading state
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteRecommendationItem(recommendation: RecommendationItem) {
        viewModelScope.launch {
            try {
                // Delete the recommendation item from the backend
                val response = RetrofitInstance.api.deleteRecommendationItem(recommendation.item_id!!)
                if (response.isSuccessful) {
                    // Remove the recommendation item from the local list
                    _recommendations.value = _recommendations.value?.filter { it.item_id != recommendation.item_id }
                } else {
                    Log.e("ShoppingViewModel", "Failed to delete recommendation item: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ShoppingViewModel", "Error deleting recommendation item: ${e.message}", e)
            }
        }
    }


    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                // Indicate loading state
                _isLoading.postValue(true)

                val itemId = item.id
                if (itemId == null) {
                    // Handle null item ID
                    _error.postValue("Item ID is null, cannot delete item.")
                    return@launch
                }

                // Make the API call using Retrofit's suspend function
                val response = RetrofitInstance.api.deleteShoppingItem(itemId)

                if (response.isSuccessful) {
                    // Remove the item locally for immediate UI update
                    deleteItemLocally(item)
                } else {
                    // Handle unsuccessful response
                    _error.postValue("Failed to delete item: ${response.message()}")
                    Log.e("ShoppingViewModel", "Error deleting item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., network issues)
                _error.postValue("Error: ${e.message}")
                Log.e("ShoppingViewModel", "API call failed: ${e.message}", e)
            } finally {
                // Reset loading state
                _isLoading.postValue(false)
            }
        }
    }

    override fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                // Indicate loading state
                _isLoading.postValue(true)

                // Map the `Item` to `ShoppingListItem` for the API call
                val categoryMap = _categories.value ?: emptyMap()
                val categoryId = categoryMap.keys.firstOrNull { categoryMap[it] == item.category } ?: 1

                val quantityParts = item.quantity.split(" ")
                val quantityValue = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val quantityUnit = quantityParts.getOrNull(1)?.trim() ?: "pcs"

                val priceParts = item.price.split(" ")
                val priceValue = priceParts.getOrNull(0)?.toDoubleOrNull()
                val currency = priceParts.getOrNull(1)?.trim() ?: "USD"

                val frequencyParts = item.frequency.split(" ")
                val frequencyValue = frequencyParts.getOrNull(0)?.toIntOrNull()
                val frequencyUnit = frequencyParts.getOrNull(2)?.trim()

                val shoppingListItem = ShoppingListItem(
                    item_id = item.id,
                    list_id = 1, // Assuming list_id is always 1 for now
                    item_name = item.name,
                    quantity = quantityValue,
                    quantity_unit = quantityUnit,
                    price = priceValue,
                    currency = currency,
                    image_url = item.imageUrl,
                    priority = item.priority,
                    frequency_value = frequencyValue,
                    frequency_unit = frequencyUnit,
                    category_id = categoryId,
                    added_at = item.createdAt
                )

                // Make the API call to update the item
                val response = RetrofitInstance.api.updateShoppingItem(item.id!!, shoppingListItem)

                if (response.isSuccessful) {
                    // Update the item locally
                    updateItemLocally(item)
                } else {
                    // Handle unsuccessful response
                    _error.postValue("Failed to update item: ${response.message()}")
                    Log.e("ShoppingViewModel", "Error updating item: HTTP ${response.code()}, ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exceptions
                _error.postValue("Error: ${e.message}")
                Log.e("ShoppingViewModel", "API call failed: ${e.message}", e)
            } finally {
                // Reset loading state
                _isLoading.postValue(false)
            }
        }
    }


    private fun addItemLocally(item: Item) {
        _items.value?.let {
            it.add(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun deleteItemLocally(item: Item) {
        _items.value?.let {
            it.remove(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun updateItemLocally(updatedItem: Item) {
        _items.value = _items.value?.map {
            if (it.id == updatedItem.id) updatedItem else it
        }?.toMutableList() // Convert the result back to MutableList
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            try {
                // Indicate loading state
                _isLoading.postValue(true)

                // Get all item IDs to delete from the database
                val itemIds = _items.value?.mapNotNull { it.id } ?: emptyList()

                // Make API calls to delete each item
                itemIds.forEach { itemId ->
                    try {
                        val response = RetrofitInstance.api.deleteShoppingItem(itemId)
                        if (!response.isSuccessful) {
                            Log.e("ShoppingViewModel", "Failed to delete item with ID: $itemId")
                        }
                    } catch (e: Exception) {
                        Log.e("ShoppingViewModel", "Error deleting item with ID: $itemId", e)
                    }
                }

                // Clear local list
                _items.value?.clear()
                _items.postValue(_items.value) // Notify observers of the change
            } catch (e: Exception) {
                // Handle exceptions (e.g., network issues)
                _error.postValue("Error deleting all items: ${e.message}")
                Log.e("ShoppingViewModel", "Error deleting all items: ${e.message}", e)
            } finally {
                // Reset loading state
                _isLoading.postValue(false)
            }
        }
    }

}
