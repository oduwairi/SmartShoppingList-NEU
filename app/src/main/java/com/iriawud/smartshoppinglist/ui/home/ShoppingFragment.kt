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


class ShoppingFragment : Fragment() {
    //setup adapter and view model
    private lateinit var adapter: ShoppingAdapter
    private lateinit var viewModel: ShoppingViewModel

    //setup binding and other variables
    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!
    private var isInputBarExpanded: Boolean = false
    private var isBottomMenuExpanded: Boolean = false
    private var selectedCategory: String = "Uncategorized"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ShoppingViewModel::class.java]

        adapter = ShoppingAdapter(mutableListOf<ShoppingItem>())
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner, { updatedList ->
            adapter.updateItems(updatedList)
        })

        viewModel.items.observe(viewLifecycleOwner) { items ->
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
                handleItemDeleted(item)
            },
            onItemDone = { item ->
                viewModel.markItemAsDone(item)
            }
        )

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.shoppingRecyclerView)

        binding.buttonAddItem.setOnClickListener {
            ShoppingUtils.addItem(
                binding.editTextNewItem.text.toString().trim(),
                binding.quantityEditText.text.toString().trim(),
                binding.quantityUnitSpinner.selectedItem.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
                selectedCategory,
                viewModel,
                listOf(
                    binding.editTextNewItem,
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

        binding.buttonItemDetails.setOnClickListener {
            ShoppingUtils.setupDropdownMenus(
                context = requireContext(),
                quantityUnitSpinner = binding.quantityUnitSpinner,
                prioritySpinner = binding.prioritySpinner,
                prioritySlider = binding.prioritySlider,
                frequencyUnitSpinner = binding.frequencyUnitSpinner
            )

            isInputBarExpanded = ShoppingUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        binding.buttonMenuExpand.setOnClickListener {
            isBottomMenuExpanded = ShoppingUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpand,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }

        binding.setCategoryCard.setOnClickListener {
            val dialog = CategorySelectionDialog { selectedCategory ->
                this.selectedCategory = selectedCategory
            }
            dialog.show(childFragmentManager, "CategorySelectionDialog")
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

