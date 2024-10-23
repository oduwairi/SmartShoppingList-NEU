package com.iriawud.smartshoppinglist.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding

class ShoppingFragment : Fragment() {
    private lateinit var adapter: ShoppingAdapter
    private lateinit var viewModel: ShoppingViewModel

    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ShoppingViewModel::class.java]

        adapter = ShoppingAdapter(mutableListOf(), this::handleItemDeleted)
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner, { updatedList ->
            adapter.updateItems(updatedList)
        })

        binding.buttonAddItem.setOnClickListener {
            val newItemName = binding.editTextNewItem.text.toString()
            val numberPicker = binding.numberPickerQuantity
            val quantity = numberPicker.value  // Get the current value from the NumberPicker
            numberPicker.minValue = 1  // Minimum quantity
            numberPicker.maxValue = 100  // Maximum quantity
            numberPicker.value = 1  // Default quantity
            if (newItemName.isNotBlank()) {
                val newItem = ShoppingItem(
                    itemName = newItemName,
                    quantity = quantity.toString() + " pieces",  // Convert the integer quantity to String
                    itemCategory = "Uncategorized",
                    price = 0.0,
                    imageUrl = null
                )
                viewModel.addItem(newItem)
                binding.editTextNewItem.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleItemDeleted(item: ShoppingItem) {
        viewModel.deleteItem(item)
    }
}

