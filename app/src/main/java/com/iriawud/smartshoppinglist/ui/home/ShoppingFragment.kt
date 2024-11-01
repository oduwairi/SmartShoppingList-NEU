package com.iriawud.smartshoppinglist.ui.home

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding


class ShoppingFragment : Fragment() {
    private lateinit var adapter: ShoppingAdapter
    private lateinit var viewModel: ShoppingViewModel

    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!
    private var isExpanded: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
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
            val newItemQuantity = binding.quantityEditText.text.toString().trim()
            val newItemQuantityUnit = binding.quantityUnitEditText.text.toString().trim()
            val newItemCost = binding.costEditText.text.toString().trim()
            val newItemCostUnit = binding.costUnitEditText.text.toString().trim()
            val newItemPriority = binding.prioritySlider.value.toInt()

            if (newItemName.isNotBlank()) {
                // Create a new ShoppingItem with the mapped imageUrl
                val newItem = ShoppingItem(
                    name = newItemName,
                    quantity = newItemQuantity + " " + newItemQuantityUnit,
                    category = "Uncategorized",
                    price = newItemCost + " " + newItemCostUnit,
                    priority = newItemPriority,
                    imageUrl = newItemName.lowercase()
                )

                // Add the new item to the ViewModel and reset fields
                viewModel.addItem(newItem)
                binding.editTextNewItem.text.clear()
                binding.quantityEditText.text.clear()
                binding.quantityUnitEditText.text.clear()
                binding.costEditText.text.clear()
                binding.costUnitEditText.text.clear()
                binding.prioritySlider.value = 5f

                //reset expanded view
                if (isExpanded) {
                    toggleExpandableDetailsCard()
                }
            }
        }

        binding.buttonItemDetails.setOnClickListener {
            toggleExpandableDetailsCard()
        }

    }

    private fun toggleExpandableDetailsCard() {
        val currentHeight = binding.expandableCardInputs.height
        val newHeight: Int
        if (!isExpanded) {
            // Assuming dpToPx is an extension function on Context
            newHeight = dpToPx(500f) // Change 200f to your desired expanded height in dp
        } else {
            newHeight = dpToPx(50f)  // Minimal height when collapsed
        }

        val valueAnimator = ValueAnimator.ofInt(currentHeight, newHeight)
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = binding.expandableCardInputs.layoutParams
            layoutParams.height = animatedValue
            binding.expandableCardInputs.layoutParams = layoutParams
        }
        valueAnimator.duration = 300 // Duration of the transition in milliseconds
        valueAnimator.start()

        isExpanded = !isExpanded
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * (requireContext().resources.displayMetrics.densityDpi.toFloat() / 160f)).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleItemDeleted(item: ShoppingItem) {
        viewModel.deleteItem(item)
    }
}

