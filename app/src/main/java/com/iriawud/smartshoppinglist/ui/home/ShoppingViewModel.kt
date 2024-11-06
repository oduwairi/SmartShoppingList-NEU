package com.iriawud.smartshoppinglist.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iriawud.smartshoppinglist.ui.ItemViewModel
import com.iriawud.smartshoppinglist.ui.dashboard.InventoryViewModel

class ShoppingViewModel : ViewModel(), ItemViewModel {
    private val _items = MutableLiveData<MutableList<ShoppingItem>>()
    val items: LiveData<MutableList<ShoppingItem>> get() = _items

    init {
        _items.value = mutableListOf() // Initialize with an empty mutable list
    }

    override fun addItem(item: ShoppingItem) {
        _items.value?.let {
            it.add(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun deleteItem(item: ShoppingItem) {
        _items.value?.let {
            it.remove(item)
            _items.value = it // Trigger LiveData update
        }
    }

    fun deleteAllItems() {
        _items.value?.clear()
        _items.postValue(_items.value)  // Notify observers of the change
    }

    fun markItemAsDone(item: ShoppingItem) {
        _items.value?.let {
            it.remove(item)
            _items.value = it // Trigger LiveData update
        }
    }
}
