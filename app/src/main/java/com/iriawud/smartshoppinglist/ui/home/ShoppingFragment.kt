package com.iriawud.smartshoppinglist.ui.home

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
import com.iriawud.smartshoppinglist.ui.ShoppingUtils
import com.iriawud.smartshoppinglist.ui.dashboard.InventoryViewModel


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

        adapter = ShoppingAdapter(mutableListOf<ShoppingItem>())
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter

        shoppingViewModel.items.observe(viewLifecycleOwner, { updatedList ->
            adapter.updateItems(updatedList)
        })

        shoppingViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            ShoppingUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.shoppingRecyclerView,
                isEmptyCheck = { items.isEmpty() })
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
        ShoppingUtils.setupDropdownMenus(
            context = requireContext(),
            quantityUnitSpinner = binding.quantityUnitSpinner,
            prioritySpinner = binding.prioritySpinner,
            prioritySlider = binding.prioritySlider,
            frequencyUnitSpinner = binding.frequencyUnitSpinner
        )

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.shoppingRecyclerView)

        //set on click listener for "add" button to create a ShoppingItem class
        binding.buttonAddItem.setOnClickListener {
            ShoppingUtils.addItem(
                binding.editTextNewItem.text.toString().trim(),
                binding.quantityEditText.text.toString().trim(),
                binding.quantityUnitSpinner.selectedItem.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
                binding.currentCategoryText.text.toString().trim(),
                binding.frequencyEditText.text.toString().trim(),
                binding.frequencyUnitSpinner.selectedItem.toString().trim(),
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
            isInputBarExpanded = ShoppingUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        //set on click listener for "menu" button to toggle the expandable menu buttons
        binding.buttonMenuExpand.setOnClickListener {
            isBottomMenuExpanded = ShoppingUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }

        //set on click listener for "set category" card to show the category selection dialog
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

