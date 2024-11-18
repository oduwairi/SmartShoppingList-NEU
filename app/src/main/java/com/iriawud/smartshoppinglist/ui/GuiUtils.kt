package com.iriawud.smartshoppinglist.ui

import android.R
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.ui.text.toLowerCase
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.iriawud.smartshoppinglist.databinding.FragmentInventoryBinding
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding
import com.iriawud.smartshoppinglist.network.PredefinedItem
import com.iriawud.smartshoppinglist.ui.inventory.MathUtils
import com.iriawud.smartshoppinglist.ui.shopping.ShoppingItem

object GuiUtils {
    fun addItem(
        newItemName: String,
        newItemQuantity: String,
        newItemQuantityUnit: String,
        newItemCost: String,
        newItemCostUnit: String,
        newItemPriority: Int,
        newItemCategory: String,
        newItemFrequency: String?,
        newItemFrequencyUnit: String?,
        newItemAmountLeftPercentage: Int?,
        viewModel: ItemViewModel,
        fieldsToClear: List<EditText>,
        prioritySlider: Slider,
        expandableCard: ViewGroup,
        context: Context,
        isInputBarExpanded: Boolean
    ): Boolean {
        if (newItemName.isNotBlank()) {
            val newItem = ShoppingItem(
                name = newItemName,
                quantity = if (newItemQuantity.isNotBlank() && newItemQuantityUnit.isNotBlank())
                    "$newItemQuantity $newItemQuantityUnit"
                else ShoppingItem().quantity, // Default quantity
                category = newItemCategory,
                price = if (newItemCost.isNotBlank() && newItemCostUnit.isNotBlank())
                    "$newItemCost $newItemCostUnit"
                else ShoppingItem().price,
                priority = newItemPriority ?: ShoppingItem().priority, // Default priority
                imageUrl = newItemName.lowercase(),
                frequency = if (!newItemFrequency.isNullOrBlank() && !newItemFrequencyUnit.isNullOrBlank())
                    "$newItemFrequency per $newItemFrequencyUnit"
                else ShoppingItem().frequency,
            )
            // Set explicit amount left percentage if provided
            if (newItemAmountLeftPercentage != null) {
                newItem.setExplicitAmountLeftPercent(newItemAmountLeftPercentage)
            }

            viewModel.addItem(newItem)

            // Clear the fields
            fieldsToClear.forEach { it.text.clear() }
            prioritySlider.value = 5f

            // Toggle expandable details card if needed
            if (isInputBarExpanded) {
                toggleExpandableDetailsCard(expandableCard, context, true)
                return true
            }
        }
        return false
    }


    fun toggleExpandableMenuButtons(
        expandableBottomMenu: CardView,
        expandableBottomMenuButtons: View,
        isBottomMenuExpanded: Boolean
    ): Boolean {
        if (!isBottomMenuExpanded) {
            expandableBottomMenu.animate()
                .scaleX(1.1f)  // Scale 10% larger in X direction
                .scaleY(1.1f)  // Scale 10% larger in Y direction
                .setDuration(100)  // Duration for the expand effect
                .start()

            expandableBottomMenuButtons.visibility = View.VISIBLE
        } else {
            expandableBottomMenu.animate()
                .scaleX(1.0f)  // Return to original X size
                .scaleY(1.0f)  // Return to original Y size
                .setDuration(100)  // Duration for the collapse effect
                .start()

            expandableBottomMenuButtons.visibility = View.GONE
        }
        return !isBottomMenuExpanded
    }

    fun toggleExpandableDetailsCard(
        expandableCard: ViewGroup,
        context: Context,
        isCardExpanded: Boolean
    ): Boolean {
        val currentHeight = expandableCard.height
        val newHeight = if (!isCardExpanded) {
            MathUtils.dpToPx(510f, context) // Expanded height
        } else {
            MathUtils.dpToPx(50f, context) // Collapsed height
        }

        val valueAnimator = ValueAnimator.ofInt(currentHeight, newHeight)
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            expandableCard.layoutParams.height = animatedValue
            expandableCard.requestLayout()
        }
        valueAnimator.duration = 300
        valueAnimator.start()

        return !isCardExpanded
    }

    fun updateEmptyStateView(
        emptyStateView: View,
        recyclerView: RecyclerView,
        isEmptyCheck: () -> Boolean
    ) {
        if (isEmptyCheck()) {
            emptyStateView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun setupDropdownMenus(
        context: Context,
        quantityUnitSpinner: Spinner,
        prioritySpinner: Spinner,
        prioritySlider: Slider,
        frequencyUnitSpinner: Spinner? = null // Make this parameter nullable
    ) {
        // List of units
        val units = listOf("kg", "lbs", "oz", "pcs", "lt", "gal", "pack", "dozen")
        val adapterUnits = ArrayAdapter(context, R.layout.simple_spinner_item, units)
        adapterUnits.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        quantityUnitSpinner.adapter = adapterUnits

        // Define priority values
        val priorityValues = mapOf("Low" to 3, "Medium" to 5, "High" to 8)
        // List of priority labels
        val priorities = listOf("Low", "Medium", "High")
        // Adapter for the Priority Spinner
        val adapterPriorities =
            ArrayAdapter(context, R.layout.simple_spinner_item, priorities)
        adapterPriorities.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapterPriorities

        // Set listener on Priority Spinner to update the Slider
        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedPriority = priorities[position]
                val sliderValue = priorityValues[selectedPriority] ?: 5
                prioritySlider.value = sliderValue.toFloat()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Set listener on Slider to update the Priority Spinner
        prioritySlider.addOnChangeListener { slider, value, fromUser ->
            val closestPriority =
                priorityValues.entries.minByOrNull { Math.abs(it.value - value.toInt()) }?.key
            val spinnerPosition = priorities.indexOf(closestPriority)
            if (spinnerPosition >= 0 && spinnerPosition != prioritySpinner.selectedItemPosition) {
                prioritySpinner.setSelection(spinnerPosition)
            }
        }

        // Only set up the frequency spinner if it's provided
        frequencyUnitSpinner?.let {
            // List of frequency units
            val frequencies = listOf("Day", "Week", "Month")
            val adapterFrequencies =
                ArrayAdapter(context, R.layout.simple_spinner_item, frequencies)
            adapterFrequencies.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            it.adapter = adapterFrequencies
        }
    }

    fun setupAutoCompleteTextView(
        context: Context,
        autoCompleteTextView: AutoCompleteTextView,
        quantityEditText: EditText,
        quantityUnitSpinner: Spinner,
        frequencyEditText: EditText? = null,
        frequencyUnitSpinner: Spinner? = null,
        costEditText: EditText,
        costUnitEditText: EditText,
        prioritySlider: Slider,
        currentCategoryText: TextView,
        currentCategoryIcon: ImageView,
        predefinedItems: List<PredefinedItem>
    ) {
        val itemMap = predefinedItems.associateBy { it.item_name }

        // Fetch categories once
        val categoryMap = CategoryRepository.getCategories().associateBy { it.category_id }

        // Set up adapter for AutoCompleteTextView
        val adapter = PredefinedItemAdapter(context, predefinedItems)
        autoCompleteTextView.setAdapter(adapter)

        // Handle item selection
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            val itemDetails = itemMap[selectedItem?.item_name]

            // Populate UI fields based on the selected item
            itemDetails?.let {
                autoCompleteTextView.setText(it.item_name)
                quantityEditText.setText(it.average_quantity.toString())
                quantityUnitSpinner.setSelection(
                    (quantityUnitSpinner.adapter as ArrayAdapter<String>)
                        .getPosition(it.default_quantity_unit.lowercase())
                )
                costEditText.setText(it.average_price.toString())
                costUnitEditText.setText(it.default_currency)
                prioritySlider.value = it.average_priority.toFloat()
                currentCategoryText.text =
                    categoryMap[it.category_id]?.category_name ?: "Uncategorized"
                setDrawable(context, currentCategoryIcon, it.image_url)
                // Calculate frequency and set frequency-related fields
                val frequencyString = MathUtils.calculateFrequencyFromConsumptionRate(
                    consumptionRate = it.average_consumption_rate,
                    quantityToBuy = it.average_quantity,
                    consumptionUnit = it.default_consumption_unit
                )

                // Extract the numeric value and unit from the string
                val frequencyValue = frequencyString.substringBefore(" per ").toInt()
                val frequencyUnit = frequencyString.substringAfter(" per ").capitalize()

                // Set the frequency value and unit
                frequencyEditText?.setText(frequencyValue.toString())
                frequencyUnitSpinner?.setSelection(
                    (frequencyUnitSpinner.adapter as ArrayAdapter<String>)
                        .getPosition(frequencyUnit)
                )
            }
        }
    }

    /**
     * Get the drawable resource ID for a given category name.
     * The function converts the category name into a drawable resource name.
     *
     * @param context The application or activity context.
     * @param categoryName The name of the category (e.g., "Dairy Products").
     * @return The resource ID of the drawable or 0 if not found.
     */
    fun getDrawableResId(context: Context, categoryName: String): Int {
        val drawableName = categoryName.lowercase().replace(" ", "_")
        return context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    fun setDrawable(context: Context, imageView: ImageView, resourceName: String) {
        val fallbackResId = com.iriawud.smartshoppinglist.R.drawable.uncategorized
        val drawableResId = getDrawableResId(context, resourceName)
        if (drawableResId != 0) {
            imageView.setImageResource(drawableResId)
        } else {
            // Use the fallback image if no matching drawable is found
            imageView.setImageResource(fallbackResId)
        }
    }
}