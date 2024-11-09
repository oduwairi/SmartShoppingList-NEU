package com.iriawud.smartshoppinglist.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.databinding.FragmentInventoryBinding
import com.iriawud.smartshoppinglist.ui.CategorySelectionDialog
import com.iriawud.smartshoppinglist.ui.ShoppingUtils

class InventoryFragment : Fragment() {
    private lateinit var adapter: InventoryAdapter
    private lateinit var viewModel: InventoryViewModel

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private var isInputBarExpanded: Boolean = false
    private var isBottomMenuExpanded: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]


        adapter = InventoryAdapter(mutableListOf())
        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.inventoryRecyclerView.adapter = adapter

        //observe the items in the view model and show empty state view if the list is empty
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            ShoppingUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.inventoryRecyclerView,
                isEmptyCheck = { items.isEmpty() })
        }

        // Fetch inventory items on fragment load
        viewModel.fetchInventoryItems()

        //setup dropdown menus with default values
        ShoppingUtils.setupDropdownMenus(
            context = requireContext(),
            quantityUnitSpinner = binding.quantityUnitSpinner,
            prioritySpinner = binding.prioritySpinner,
            prioritySlider = binding.prioritySlider,
            frequencyUnitSpinner = null
        )

        //set on click listener for "add" button to create a ShoppingItem class
        binding.buttonAddItem.setOnClickListener {
            ShoppingUtils.addItem(
                binding.editTextNewItemInventory.text.toString().trim(),
                binding.quantityEditText.text.toString().trim(),
                binding.quantityUnitSpinner.selectedItem.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
                binding.currentCategoryText.text.toString().trim(),
                null,
                null,
                binding.remaningQuantitySlider.value.toInt(),
                viewModel,
                listOf(
                    binding.editTextNewItemInventory,
                    binding.quantityEditText,
                    binding.costEditText,
                    binding.costUnitEditText
                ),
                binding.prioritySlider,
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for "item details" button to toggle expandable card
        binding.buttonItemDetails.setOnClickListener {
            isInputBarExpanded = ShoppingUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for menu button to toggle the menu
        binding.buttonMenuExpandInventory.setOnClickListener {
            isBottomMenuExpanded = ShoppingUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpandInventory,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }

        //set on click listener for category card to open category selection dialog
        binding.setCategoryCard.setOnClickListener {
            val dialog = CategorySelectionDialog { selectedCategory ->
                binding.currentCategoryText.text = selectedCategory
            }
            dialog.show(childFragmentManager, "CategorySelectionDialog")
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}