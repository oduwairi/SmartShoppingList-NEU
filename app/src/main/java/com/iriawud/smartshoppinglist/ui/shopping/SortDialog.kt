package com.iriawud.smartshoppinglist.ui.shopping

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class SortDialog(
    private val items: List<Item>, // List of items to sort
    private val onApplySort: (List<Item>) -> Unit // Callback to return sorted items
) : DialogFragment() {

    private lateinit var sortItemsRecyclerView: RecyclerView
    private lateinit var sortBySpinner: Spinner
    private lateinit var sortedItemsAdapter: SortedItemsAdapter
    private lateinit var closeSortButton: ImageView
    private lateinit var finishSortCard: CardView

    val supermarketCategoryOrder = listOf(
        "Fruits and Vegetables",      // fruits_and_vegetables
        "Meat and Seafood",           // meat_and_seafood
        "Dairy Products",             // dairy_products
        "Beverages",                  // beverages
        "Bread and Pastry",           // bread_and_pastry
        "Snacks and Confectionery",   // snacks_and_confectionery
        "Canned and Jarred Goods",    // canned_and_jarred_goods
        "Condiments and Sauces",      // condiments_and_sauces
        "Spices and Herbs",           // spices_and_herbs
        "Baking Supplies",            // baking_supplies
        "Household Supplies",         // household_supplies
        "Cleaning Supplies",          // cleaning_supplies
        "Personal Care",              // personal_care
        "Health and Wellness",        // health_and_wellness
        "Baby Products",              // baby_products
        "Pet Supplies",               // pet_supplies
        "Electronics",                // electronics
        "Clothing and Accessories",   // clothing_and_accessories
        "Beauty and Cosmetics",       // beauty_and_cosmetics
        "Uncategorized"               // uncategorized
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sort_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortItemsRecyclerView = view.findViewById(R.id.sortItemsRecyclerView)
        sortBySpinner = view.findViewById(R.id.sortBySpinner)
        closeSortButton = view.findViewById(R.id.closeSortButton)
        finishSortCard = view.findViewById(R.id.finishSortCard)

        sortedItemsAdapter = SortedItemsAdapter(items)
        sortItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        sortItemsRecyclerView.adapter = sortedItemsAdapter

        // Populate sorting options
        val sortOptions = listOf("Aisle", "Stock Levels", "Priority")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortBySpinner.adapter = spinnerAdapter

        // Add listener to spinner for immediate preview updates
        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = sortOptions[position]
                updatePreview(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Close the dialog
        closeSortButton.setOnClickListener { dismiss() }

        // Handle sort button click
        finishSortCard.setOnClickListener {
            val sortedList = sortedItemsAdapter.items
            onApplySort(sortedList)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun updatePreview(selectedOption: String) {
        val sortedList = when (selectedOption) {
            "Aisle" -> sortByAisle()
            "Stock Levels" -> sortByStockLevels()
            "Priority" -> items.sortedByDescending { it.priority }
            else -> items
        }

        // Update RecyclerView with the sorted preview
        sortedItemsAdapter.updateData(sortedList)
    }

    private fun sortByAisle(): List<Item> {
        return items.sortedWith(compareBy { supermarketCategoryOrder.indexOf(it.category) })
    }

    private fun sortByStockLevels(): List<Item> {
        return items.sortedBy { it.amountLeftPercent }
    }
}
