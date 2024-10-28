package com.iriawud.smartshoppinglist.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
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

        // Setup swipe functionality using the ShoppingCardSwiper class
        val swipeHandler = ShoppingCardSwiper(
            context = requireContext(),
            adapter = adapter,
            onItemDeleted = { item ->
                handleItemDeleted(item)
            },
            onItemDone = { item ->
                viewModel.markItemAsDone(item)
            }
        )

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.shoppingRecyclerView)

        binding.buttonAddItem.setOnClickListener {
            val newItemName = binding.editTextNewItem.text.toString().trim()
            if (newItemName.isNotBlank()) {
                // Map common item names to drawable resource names
                val imageUrl = when (newItemName.lowercase()) {
                    "apples" -> "apples" // Refers to `R.drawable.apple`
                    "milk" -> "milk"   // Refers to `R.drawable.milk`
                    "eggs" -> "eggs" // Refers to `R.drawable.banana`
                    else -> null // Use a default or fallback image if no match is found
                }

                // Create a new ShoppingItem with the mapped imageUrl
                val newItem = ShoppingItem(
                    itemName = newItemName,
                    quantity = "1 piece",  // Convert the integer quantity to String
                    itemCategory = "Uncategorized",
                    price = 0.0,
                    imageUrl = imageUrl
                )

                // Add the new item to the ViewModel
                viewModel.addItem(newItem)
                binding.editTextNewItem.text.clear()
            }
        }

        setupBottomButtonListeners()
    }

    private fun setupBottomButtonListeners() {
        binding.cardDeleteAll.setOnClickListener {
            viewModel.deleteAllItems()
        }

        binding.cardDone.setOnClickListener {
            viewModel.deleteAllItems()
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

