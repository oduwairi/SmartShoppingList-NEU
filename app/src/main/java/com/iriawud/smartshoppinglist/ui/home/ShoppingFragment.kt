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
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding
import com.iriawud.smartshoppinglist.ui.ShoppingUtils


class ShoppingFragment : Fragment() {
    //setup adapter and view model
    private lateinit var adapter: ShoppingAdapter
    private lateinit var viewModel: ShoppingViewModel

    //view for empty recycler view
    private lateinit var emptyShoppingListView : View

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

        viewModel = ViewModelProvider(this)[ShoppingViewModel::class.java]

        adapter = ShoppingAdapter(mutableListOf<ShoppingItem>())
        binding.shoppingRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shoppingRecyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner, { updatedList ->
            adapter.updateItems(updatedList)
        })

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            updateEmptyStateView()
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
                binding.quantityUnitEditText.text.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
                viewModel,
                listOf(
                    binding.editTextNewItem,
                    binding.quantityEditText,
                    binding.quantityUnitEditText,
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
    }

    private fun updateEmptyStateView() {
        val emptyStateView = binding.root.findViewById<View>(R.id.emptyStateView)
        if (viewModel.isItemListEmpty()) {
            emptyStateView.visibility = View.VISIBLE
            binding.shoppingRecyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            binding.shoppingRecyclerView.visibility = View.VISIBLE
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

