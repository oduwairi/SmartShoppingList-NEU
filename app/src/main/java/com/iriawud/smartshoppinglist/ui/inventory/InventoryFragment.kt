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
import com.iriawud.smartshoppinglist.ui.GuiUtils

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
            GuiUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.inventoryRecyclerView,
                isEmptyCheck = { items.isEmpty() })
        }

        // Observe predefined items and update the AutoCompleteTextView
        viewModel.predefinedItems.observe(viewLifecycleOwner) { predefinedItems ->
            GuiUtils.setupAutoCompleteTextView(
                context = requireContext(),
                autoCompleteTextView = binding.editTextNewItemInventory,
                quantityEditText = binding.quantityEditText,
                quantityUnitSpinner = binding.quantityUnitSpinner,
                costEditText = binding.costEditText,
                costUnitEditText = binding.costUnitEditText,
                prioritySlider = binding.prioritySlider,
                currentCategoryText = binding.currentCategoryText,
                currentCategoryIcon = binding.setCategoryIcon,
                predefinedItems = predefinedItems
            )
        }

        viewModel.initializeData()

        //setup dropdown menus with default values
        GuiUtils.setupDropdownMenus(
            context = requireContext(),
            quantityUnitSpinner = binding.quantityUnitSpinner,
            prioritySpinner = binding.prioritySpinner,
            prioritySlider = binding.prioritySlider,
            frequencyUnitSpinner = null
        )

        //set on click listener for "add" button to create a ShoppingItem class
        binding.buttonAddItem.setOnClickListener {
            GuiUtils.addItem(
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
            isInputBarExpanded = GuiUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for menu button to toggle the menu
        binding.buttonMenuExpandInventory.setOnClickListener {
            isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpandInventory,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }

        // Set on click listener for category card to open category selection dialog
        binding.setCategoryCard.setOnClickListener {
            val dialog = CategorySelectionDialog { selectedCategory ->
                binding.currentCategoryText.text = selectedCategory

                // Dynamically set the category icon using GuiUtils
                val drawableResId = GuiUtils.getDrawableResId(requireContext(), selectedCategory)
                if (drawableResId != 0) {
                    binding.setCategoryIcon.setImageResource(drawableResId)
                } else {
                    binding.setCategoryIcon.setImageResource(R.drawable.uncategorized) // Fallback icon
                }
            }
            dialog.show(childFragmentManager, "CategorySelectionDialog")
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}