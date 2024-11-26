package com.iriawud.smartshoppinglist.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding
import com.iriawud.smartshoppinglist.ui.CategorySelectionDialog
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.inventory.InventoryViewModel


class ShoppingFragment : Fragment() {
    //setup adapter and view models
    private lateinit var adapter: ShoppingAdapter
    private lateinit var shoppingViewModel: ShoppingViewModel
    private lateinit var inventoryViewModel: InventoryViewModel

    //setup binding and other variables
    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!
    private var isInputBarExpanded: Boolean = false
    private var isBottomMenuExpanded: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shoppingViewModel = ViewModelProvider(this)[ShoppingViewModel::class.java]
        inventoryViewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]

        adapter = ShoppingAdapter(mutableListOf(), shoppingViewModel)
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter

        shoppingViewModel.initializeData()

        shoppingViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            GuiUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.shoppingRecyclerView,
                isEmptyCheck = { items.isEmpty() })
        }

        // Observe predefined items and update the AutoCompleteTextView
        shoppingViewModel.predefinedItems.observe(viewLifecycleOwner) { predefinedItems ->
            GuiUtils.setupAutoCompleteTextView(
                context = requireContext(),
                autoCompleteTextView = binding.editTextNewItem,
                quantityEditText = binding.quantityEditText,
                quantityUnitSpinner = binding.quantityUnitSpinner,
                costEditText = binding.costEditText,
                costUnitEditText = binding.costUnitEditText,
                prioritySlider = binding.prioritySlider,
                currentCategoryText = binding.currentCategoryText,
                currentCategoryIcon = binding.setCategoryIcon,
                predefinedItems = predefinedItems,
                frequencyEditText = binding.frequencyEditText,
                frequencyUnitSpinner = binding.frequencyUnitSpinner
            )
        }

        inventoryViewModel.dueItems.observe(viewLifecycleOwner) { shoppingItem ->
            shoppingViewModel.addItem(shoppingItem)
        }


        // Setup swipe functionality using the ShoppingCardSwiper class
        val swipeHandler = ShoppingCardSwiper(
            context = requireContext(),
            adapter = adapter,
            onItemDeleted = { item ->
                shoppingViewModel.deleteItem(item)
            },
            onItemDone = { item ->
                shoppingViewModel.deleteItem(item)
                inventoryViewModel.addItem(item)
            }
        )

        //setup dropdown menus with default values
        GuiUtils.setupDropdownMenus(
            context = requireContext(),
            quantityUnitSpinner = binding.quantityUnitSpinner,
            prioritySpinner = binding.prioritySpinner,
            prioritySlider = binding.prioritySlider,
            frequencyUnitSpinner = binding.frequencyUnitSpinner
        )

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.shoppingRecyclerView)

        //set on click listener for "add" button to create a Item class
        binding.buttonAddItem.setOnClickListener {
            GuiUtils.addItem(
                binding.editTextNewItem.text.toString().trim(),
                binding.quantityEditText.text.toString().trim(),
                binding.quantityUnitSpinner.selectedItem.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
                binding.currentCategoryText.text.toString().trim(),
                binding.frequencyEditText.text.toString().trim(),
                binding.frequencyUnitSpinner.selectedItem.toString().trim(),
                null,
                shoppingViewModel,
                listOf(
                    binding.editTextNewItem,
                    binding.quantityEditText,
                    binding.costEditText,
                    binding.costUnitEditText,
                    binding.frequencyEditText,
                ),
                binding.prioritySlider,
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for "item details" button to toggle the expandable card
        binding.buttonItemDetails.setOnClickListener {
            isInputBarExpanded = GuiUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for "menu" button to toggle the expandable menu buttons
        binding.buttonMenuExpand.setOnClickListener {
            isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
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

