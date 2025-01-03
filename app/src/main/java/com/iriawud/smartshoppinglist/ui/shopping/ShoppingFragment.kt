package com.iriawud.smartshoppinglist.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding
import com.iriawud.smartshoppinglist.ui.CategoryRepository
import com.iriawud.smartshoppinglist.ui.CategorySelectionDialog
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.inventory.InventoryViewModel

class ShoppingFragment : Fragment() {
    // Setup adapter and view models
    private lateinit var adapter: ShoppingAdapter
    private lateinit var shoppingViewModel: ShoppingViewModel
    private lateinit var inventoryViewModel: InventoryViewModel

    // Setup binding and other variables
    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!
    private var isInputBarExpanded: Boolean = false
    private var isBottomMenuExpanded: Boolean = false
    private var isRecommendationsExpanded: Boolean = true

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

        adapter = ShoppingAdapter(
            items = listOf(),
            shoppingViewModel = shoppingViewModel,
            inventoryViewModel = inventoryViewModel,
            context = requireContext(),
            recommendations = listOf()
        )
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter
        shoppingViewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            val emptyStateSuggestionBox = binding.root.findViewById<View>(R.id.emptyStateSuggestionBoxContainer)
            val recommendationRecyclerView = emptyStateSuggestionBox.findViewById<RecyclerView>(R.id.recommendationRecyclerView)
            val minimizeCard = emptyStateSuggestionBox.findViewById<CardView>(R.id.minimizeRecommendationCard)

            GuiUtils.setupRecommendationRecyclerView(
                context = requireContext(),
                recyclerView = recommendationRecyclerView,
                recommendations = recommendations,
                isRecommendationsExpanded = isRecommendationsExpanded,
                onAddButtonClick = { recommendation ->
                    // Handle adding recommendation to shopping list
                    val categoryMap = CategoryRepository.getCategories().associateBy { it.category_id }
                    val newItem = Item(
                        id = recommendation.item_id,
                        name = recommendation.item_name,
                        quantity = "${recommendation.quantity} ${recommendation.quantity_unit ?: "pcs"}",
                        category = categoryMap[recommendation.category_id]?.category_name ?: "Uncategorized",
                        price = "${recommendation.price ?: 0.0} ${recommendation.currency ?: "USD"}",
                        priority = recommendation.priority,
                        imageUrl = recommendation.image_url,
                        frequency = "${recommendation.frequency_value ?: "Not set"} ${recommendation.frequency_unit ?: ""}",
                        createdAt = Item.getCurrentTimestamp()
                    )
                    shoppingViewModel.addItem(newItem)
                    shoppingViewModel.deleteRecommendationItem(recommendation)
                },
                expandButton = minimizeCard,
                onToggleExpand = { isExpanded ->
                    isRecommendationsExpanded = isExpanded
                }
            )
        }


        // Observe Shopping List Items
        shoppingViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            GuiUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.noItemsCard),
                recyclerView = binding.shoppingRecyclerView
            ) {
                items.isEmpty()
            }
        }

        shoppingViewModel.initializeData()

        shoppingViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            GuiUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.shoppingRecyclerView,
                isEmptyCheck = { items.isEmpty() }
            )
            updateBottomBarText(items)
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

        // Observe recommendation items
        shoppingViewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            adapter.updateRecommendations(recommendations)
        }

        inventoryViewModel.dueItems.observe(viewLifecycleOwner) { shoppingItem ->
            shoppingViewModel.addItem(shoppingItem)
        }

        // Setup dropdown menus with default values
        GuiUtils.setupDropdownMenus(
            context = requireContext(),
            quantityUnitSpinner = binding.quantityUnitSpinner,
            prioritySpinner = binding.prioritySpinner,
            prioritySlider = binding.prioritySlider,
            frequencyUnitSpinner = binding.frequencyUnitSpinner
        )

        // Set on click listener for "add" button to create an Item class
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

        // Set on click listener for "item details" button to toggle the expandable card
        binding.buttonItemDetails.setOnClickListener {
            isInputBarExpanded = GuiUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        // Set on click listener for "menu" button to toggle the expandable menu buttons
        binding.buttonMenuExpand.setOnClickListener {
            isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }

        binding.buttonDoneAll.setOnClickListener {
            isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )

            val checkoutDialog = CheckoutDialog(
                shoppingItems = shoppingViewModel.items.value ?: emptyList(), // Get items from ViewModel
                onFinishCheckout = {
                    // Add each item back to the database
                    shoppingViewModel.items.value?.forEach { item ->
                        shoppingViewModel.addItem(item)
                    }

                    // Handle checkout completion logic (delete all items)
                    shoppingViewModel.deleteAllItems()
                }
            )
            checkoutDialog.show(childFragmentManager, "CheckoutDialog")
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

        binding.buttonSearch.setOnClickListener {
            // Access the search bar layout
            val searchBarLayout = binding.root.findViewById<View>(R.id.searchBarLayout)
            GuiUtils.setupSearchBar(
                context = requireContext(),
                searchBarLayout = searchBarLayout,
                editTextSearchQuery = searchBarLayout.findViewById<EditText>(R.id.searchedText),
                searchBarIcon = searchBarLayout.findViewById<ImageView>(R.id.searchBarIcon),
                recyclerView = binding.shoppingRecyclerView,
                toggleMenu = {
                    isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                        binding.buttonMenuExpand,
                        binding.bottomExpandableMenuButtons,
                        isBottomMenuExpanded
                    )
                },
                onQueryChange = { query ->
                    filterItems(query) // Delegate query filtering to the fragment
                },
                onSearchBarClosed = {
                    // Reset the RecyclerView adapter or perform additional cleanup
                    val allItems = shoppingViewModel.items.value ?: listOf()
                    adapter.updateItems(allItems)
                }
            )
        }

        binding.buttonSort.setOnClickListener {
            isBottomMenuExpanded = GuiUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
            val items = shoppingViewModel.items.value ?: listOf()
            val sortDialog = SortDialog(
                items = items,
                onApplySort = { sortedItems ->
                    adapter.updateItems(sortedItems.toMutableList())
                }
            )
            sortDialog.show(childFragmentManager, "SortDialog")
        }

        // Observe ViewModel items and update adapter
        shoppingViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
        }

        binding.buttonDeleteAll.setOnClickListener {
            shoppingViewModel.deleteAllItems()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateBottomBarText(items: List<Item>) {
        val redColor = R.color.priority_high // Define your highlight color

        // Calculate item count
        val itemCount = items.size
        val itemCountText = "Item count: $itemCount"
        binding.itemCountText.text = GuiUtils.getHighlightedText(
            fullText = itemCountText,
            highlightText = itemCount.toString(),
            color = redColor,
            context = requireContext()
        )

        // Calculate total cost
        val totalCost = items.sumOf { item ->
            val priceString = item.price.replace(Regex("[^\\d.]"), "")
            priceString.toDoubleOrNull() ?: 0.0
        }
        val totalCostFormatted = "%.2f".format(totalCost)
        val totalCostText = "Total: $totalCostFormatted"
        binding.totalCostText.text = GuiUtils.getHighlightedText(
            fullText = totalCostText,
            highlightText = totalCostFormatted,
            color = redColor,
            context = requireContext()
        )
    }


    private fun filterItems(query: String) {
        // Get the full list of items from the ViewModel
        val allItems = shoppingViewModel.items.value ?: listOf()

        if (query.isEmpty()) {
            // If query is empty, show all items
            adapter.updateItems(allItems)
        } else {
            // Filter items based on the query
            val filteredItems = allItems.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
            adapter.updateItems(filteredItems)
        }
    }

}
