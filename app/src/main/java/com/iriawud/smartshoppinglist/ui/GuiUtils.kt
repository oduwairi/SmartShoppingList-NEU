package com.iriawud.smartshoppinglist.ui

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.iriawud.smartshoppinglist.network.PredefinedItem
import com.iriawud.smartshoppinglist.network.RecommendationItem
import com.iriawud.smartshoppinglist.ui.inventory.MathUtils
import com.iriawud.smartshoppinglist.ui.shopping.Item
import com.iriawud.smartshoppinglist.ui.shopping.RecommendationAdapter

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
            val newItem = Item(
                name = newItemName,
                quantity = if (newItemQuantity.isNotBlank() && newItemQuantityUnit.isNotBlank())
                    "$newItemQuantity $newItemQuantityUnit"
                else Item().quantity, // Default quantity
                category = newItemCategory,
                price = if (newItemCost.isNotBlank() && newItemCostUnit.isNotBlank())
                    "$newItemCost $newItemCostUnit"
                else Item().price,
                priority = newItemPriority ?: Item().priority, // Default priority
                imageUrl = newItemName.lowercase(),
                frequency = if (!newItemFrequency.isNullOrBlank() && !newItemFrequencyUnit.isNullOrBlank())
                    "$newItemFrequency per $newItemFrequencyUnit"
                else Item().frequency,
            )
            // Set explicit amount left percentage if provided
            if (newItemAmountLeftPercentage != null) {
                newItem.setExplicitStartingPercent(newItemAmountLeftPercentage)
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
        frequencyUnitSpinner: Spinner? = null
    ) {
        setupQuantityDropdown(context, quantityUnitSpinner)
        setupPriorityDropdown(context, prioritySpinner, prioritySlider)

        // Only set up the frequency dropdown if the spinner is provided
        frequencyUnitSpinner?.let {
            setupFrequencyDropdown(context, it)
        }
    }

    fun setupSearchBar(
        context: Context,
        searchBarLayout: View,
        editTextSearchQuery: EditText,
        searchBarIcon: ImageView,
        recyclerView: RecyclerView,
        toggleMenu: () -> Unit = {},
        onQueryChange: (String) -> Unit,
        onSearchBarClosed: () -> Unit
    ) {
        // Show the search bar and handle button clicks
        toggleMenu()

        // Show the search bar and focus the input
        searchBarLayout.visibility = View.VISIBLE
        editTextSearchQuery.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextSearchQuery, InputMethodManager.SHOW_IMPLICIT)

        // Set up text change listener for search input
        editTextSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onQueryChange(s.toString()) // Call the provided callback for query changes
            }
        })

        // Handle search icon click (acts as cancel button)
        searchBarIcon.setOnClickListener {
            // Hide the search bar
            searchBarLayout.visibility = View.GONE
            // Clear the search query
            editTextSearchQuery.text.clear()
            // Hide the keyboard
            imm.hideSoftInputFromWindow(editTextSearchQuery.windowToken, 0)
            // Call the provided callback to handle additional cleanup
            onSearchBarClosed()
        }
    }

    fun setupRecommendationRecyclerView(
        context: Context,
        recyclerView: RecyclerView,
        recommendations: List<RecommendationItem>,
        isRecommendationsExpanded: Boolean,
        onAddButtonClick: (RecommendationItem) -> Unit,
        expandButton: View,
        expandIcon: ImageView,
        onToggleExpand: (Boolean) -> Unit
    ) {
        val recommendationAdapter = RecommendationAdapter(recommendations, onAddButtonClick)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationAdapter
            isNestedScrollingEnabled = false
            visibility = if (isRecommendationsExpanded) {
                View.VISIBLE
            }
            else View.GONE
        }

        expandButton.setOnClickListener {
            val updatedVisibility = recyclerView.visibility != View.VISIBLE
            recyclerView.visibility = if (updatedVisibility) {
                View.VISIBLE
            }
                else View.GONE
            onToggleExpand(updatedVisibility) // Update the state in the calling context
        }

        recommendationAdapter.notifyDataSetChanged()
    }



    fun setupQuantityDropdown(
        context: Context,
        quantityUnitSpinner: Spinner,
        defaultUnit: String? = null
    ) {
        val units = listOf("kg", "lbs", "oz", "pcs", "lt", "gal", "pack", "dozen")
        val adapterUnits = ArrayAdapter(context, R.layout.simple_spinner_item, units)
        adapterUnits.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        quantityUnitSpinner.adapter = adapterUnits

        // Set default selection if provided
        defaultUnit?.let {
            val defaultIndex = units.indexOf(it)
            if (defaultIndex >= 0) {
                quantityUnitSpinner.setSelection(defaultIndex)
            }
        }
    }


    fun setupPriorityDropdown(
        context: Context,
        prioritySpinner: Spinner,
        prioritySlider: Slider,
        defaultPriorityValue: Int? = null // Default slider value (1-10)
    ) {
        val priorityValues = mapOf("Low" to 3, "Medium" to 5, "High" to 8)
        val priorities = listOf("Low", "Medium", "High")
        val adapterPriorities = ArrayAdapter(context, R.layout.simple_spinner_item, priorities)
        adapterPriorities.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapterPriorities

        // Set default selection and slider value based on the numeric priority
        defaultPriorityValue?.let { value ->
            // Find the closest priority label
            val closestPriority =
                priorityValues.entries.minByOrNull { Math.abs(it.value - value) }?.key
            val spinnerPosition = priorities.indexOf(closestPriority)

            // Set spinner selection and slider value
            if (spinnerPosition >= 0) {
                prioritySpinner.setSelection(spinnerPosition)
                prioritySlider.value = value.toFloat()
            }
        }

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
    }


    fun setupFrequencyDropdown(
        context: Context,
        frequencyUnitSpinner: Spinner
    ) {
        val frequencies = listOf("Day", "Week", "Month")
        val adapterFrequencies = ArrayAdapter(context, R.layout.simple_spinner_item, frequencies)
        adapterFrequencies.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        frequencyUnitSpinner.adapter = adapterFrequencies
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

    fun updateQuantity(
        quantityEditText: EditText,
        quantityUnitDropdown: Spinner,
        item: Item,
        viewModel: ItemViewModel,
        saveButton: CardView,
        discardButton: CardView,
        toggleExpansion: () -> Unit
    ) {
        // Set initial values
        val originalQuantity = item.quantity.split(" ")[0].trim()
        val originalUnit = item.quantity.split(" ")[1].trim()

        quantityEditText.setText(originalQuantity)
        setupQuantityDropdown(
            quantityEditText.context,
            quantityUnitDropdown,
            defaultUnit = originalUnit
        )

        // Local variables to store the updated values
        var newQuantity = originalQuantity
        var newUnit = originalUnit

        // Update local values when the quantity text changes
        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                newQuantity = s.toString().trim() // Update the local quantity value
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Update local values when the unit changes
        quantityUnitDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                newUnit = parent.getItemAtPosition(position).toString() // Update the local unit value
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Save changes when the "Save Changes" button is clicked
        saveButton.setOnClickListener {
            val updatedItem = item.copy(quantity = "$newQuantity $newUnit")
            viewModel.updateItem(updatedItem) // Call the ViewModel to persist the changes
            toggleExpansion() // Collapse the card
        }

        // Discard changes when the "Discard Changes" button is clicked
        discardButton.setOnClickListener {
            // Reset UI elements to their original state
            quantityEditText.setText(originalQuantity)
            val originalUnitPosition = (quantityUnitDropdown.adapter as ArrayAdapter<String>)
                .getPosition(originalUnit)
            quantityUnitDropdown.setSelection(originalUnitPosition)

            // Reset local variables to original values
            newQuantity = originalQuantity
            newUnit = originalUnit

            toggleExpansion() // Collapse the card
        }
    }


    fun updateCost(
        costEditText: EditText,
        costUnitEditText: EditText,
        item: Item
    ) {
        // Set initial values
        costEditText.setText(item.price.split(" ")[0].trim())
        costUnitEditText.setText(item.price.split(" ")[1].trim())

        // Update `item.price` when cost text changes
        costEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val costValue = s.toString().trim()
                val unit = costUnitEditText.text.toString().trim()
                item.price = "$costValue $unit"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Update `item.price` when unit text changes
        costUnitEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val costValue = costEditText.text.toString().trim()
                val unit = s.toString().trim()
                item.price = "$costValue $unit"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun updatePriority(
        priorityDropdown: Spinner,
        prioritySlider: Slider,
        item: Item
    ) {
        // Set initial values
        GuiUtils.setupPriorityDropdown(priorityDropdown.context, priorityDropdown, prioritySlider, defaultPriorityValue = item.priority)

        // Update `item.priority` when slider value changes
        prioritySlider.addOnChangeListener { slider, value, fromUser ->
            item.priority = value.toInt()
        }

        // Synchronize slider and dropdown
        priorityDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val priorities = listOf("Low", "Medium", "High")
                val priorityValues = mapOf("Low" to 3, "Medium" to 5, "High" to 8)

                val selectedPriority = priorities[position]
                val sliderValue = priorityValues[selectedPriority] ?: 5
                prioritySlider.value = sliderValue.toFloat()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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

    fun expandView(view: View) {
        // Temporarily set the view to INVISIBLE for accurate measurement
        view.visibility = View.INVISIBLE

        // Measure the target height
        view.measure(
            View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )
        val targetHeight = view.measuredHeight

        // Reset the height to 0 and set the visibility to VISIBLE
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        view.requestLayout()

        // Animate the height from 0 to the target height
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { animation ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Set the height to WRAP_CONTENT after the animation ends
                view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                view.requestLayout()
            }
        })

        animator.duration = 300 // Animation duration in milliseconds
        animator.start()
    }


    fun collapseView(view: View) {
        val initialHeight = view.measuredHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.addUpdateListener { animation ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })

        animator.duration = 300 // Animation duration in ms
        animator.start()
    }

    fun getHighlightedText(
        fullText: String,
        highlightText: String,
        color: Int,
        context: Context
    ): SpannableString {
        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf(highlightText)

        if (startIndex >= 0) {
            val endIndex = startIndex + highlightText.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, color)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

}