package com.iriawud.smartshoppinglist.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.databinding.FragmentInventoryBinding
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

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]

        adapter = InventoryAdapter(mutableListOf())
        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.inventoryRecyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateItems(updatedList)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            ShoppingUtils.updateEmptyStateView(
                emptyStateView = binding.root.findViewById(R.id.emptyStateView),
                recyclerView = binding.inventoryRecyclerView,
                isEmptyCheck = { items.isEmpty() })
        }

        binding.buttonAddItem.setOnClickListener {
            ShoppingUtils.addItem(
                binding.editTextNewItemInventory.text.toString().trim(),
                binding.quantityEditText.text.toString().trim(),
                binding.quantityUnitEditText.text.toString().trim(),
                binding.costEditText.text.toString().trim(),
                binding.costUnitEditText.text.toString().trim(),
                binding.prioritySlider.value.toInt(),
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

        binding.buttonItemDetails.setOnClickListener {
            isInputBarExpanded = ShoppingUtils.toggleExpandableDetailsCard(
                binding.expandableCardInputs,
                requireContext(),
                isInputBarExpanded
            )
        }

        binding.buttonMenuExpandInventory.setOnClickListener {
            isBottomMenuExpanded = ShoppingUtils.toggleExpandableMenuButtons(
                binding.buttonMenuExpandInventory,
                binding.bottomExpandableMenuButtons,
                isBottomMenuExpanded
            )
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}