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
                        val createdAt = convertToSimpleDateFormat(inventoryItem.stocked_at)
                        val categoryName = categoryMap[inventoryItem.category_id] ?: "Uncategorized"
                        val frequency = calculateFrequency(
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

        val restockDate = calculateRestockDate(item.createdAt, item.frequency)

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

    private fun calculateFrequency(stockedAt: String, restockDate: String?): String {
        if (restockDate.isNullOrEmpty()) return "Not set"

        try {
            // Convert both dates to simple format
            val simpleStockedAt = convertToSimpleDateFormat(stockedAt)
            val simpleRestockDate = convertToSimpleDateFormat(restockDate)

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val stockedDate = formatter.parse(simpleStockedAt)
            val restockDateParsed = formatter.parse(simpleRestockDate)

            if (stockedDate != null && restockDateParsed != null) {
                val differenceInMillis = restockDateParsed.time - stockedDate.time
                val differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)).toInt()

                // Determine the appropriate period (day, week, or month)
                return when {
                    differenceInDays < 7 -> "$differenceInDays per day"
                    differenceInDays < 30 -> "${differenceInDays / 7} per week"
                    else -> "${differenceInDays / 30} per month"
                }
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error calculating frequency: ${e.message}", e)
        }
        return "Not set"
    }



    private fun calculateRestockDate(stockedAt: String, frequency: String): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val stockedDate = formatter.parse(stockedAt)
            val frequencyRegex = Regex("(\\d+) per (\\w+)")
            val matchResult = frequencyRegex.matchEntire(frequency)

            if (stockedDate != null && matchResult != null) {
                val (amount, unit) = matchResult.destructured
                val daysToAdd = when (unit.lowercase()) {
                    "day", "days" -> amount.toInt()
                    "week", "weeks" -> amount.toInt() * 7
                    "month", "months" -> amount.toInt() * 30
                    else -> throw IllegalArgumentException("Frequency format not recognized")
                }
                val restockDate = Date(stockedDate.time + daysToAdd * 24 * 60 * 60 * 1000L)
                formatter.format(restockDate)
            } else {
                null // Invalid frequency or date
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error calculating restock date: ${e.message}", e)
            null
        }
    }

    private fun convertToSimpleDateFormat(dateString: String?): String {
        if (dateString.isNullOrBlank()) {
            return ShoppingItem.getCurrentTimestamp() // Fallback to current timestamp
        }

        return try {
            // Parse RFC 1123 format
            val rfc1123Formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val date = rfc1123Formatter.parse(dateString)

            // Convert to "yyyy-MM-dd HH:mm:ss" format
            val simpleFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            simpleFormatter.format(date!!)
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error converting date format: ${e.message}", e)
            ShoppingItem.getCurrentTimestamp() // Fallback to current timestamp
        }
    }


}
