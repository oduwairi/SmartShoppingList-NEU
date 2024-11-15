package com.iriawud.smartshoppinglist.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iriawud.smartshoppinglist.R

class CategorySelectionDialog(
    private val onCategorySelected: (String) -> Unit
) : DialogFragment() {

    private var categoryPopupContainer: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.category_selection_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryPopupContainer = view.findViewById(R.id.categoryBoxConstraintLayout)
        val categoryListContainer = view.findViewById<LinearLayout>(R.id.categoryLinearLayout)

        // Define categories
        val categories = listOf(
            "Dairy Products", "Bread and Pastry", "Fruits and Vegetables", "Meat and Seafood",
            "Beverages", "Snacks and Confectionery", "Canned and Jarred Goods", "Condiments and Sauces",
            "Spices and Herbs", "Baking Supplies", "Personal Care", "Household Supplies",
            "Cleaning Supplies", "Health and Wellness", "Baby Products", "Pet Supplies",
            "Electronics", "Clothing and Accessories", "Beauty and Cosmetics"
        )

        // Populate the category list container
        categories.forEach { categoryName ->
            val itemView = layoutInflater.inflate(R.layout.category_selection_item, categoryListContainer, false)
            val categoryText = itemView.findViewById<TextView>(R.id.categoryName)
            categoryText.text = categoryName

            // Set the category icon using a helper function
            val categoryIcon = itemView.findViewById<ImageView>(R.id.categoryIcon)
            val drawableResId = getCategoryDrawableResId(categoryName)
            if (drawableResId != 0) {
                categoryIcon.setImageResource(drawableResId)
            } else {
                categoryIcon.setImageResource(R.drawable.ic_launcher_background) // Fallback icon
            }

            itemView.setOnClickListener {
                onCategorySelected(categoryName) // Call the callback with the selected category
                dismiss() // Close the dialog after selection
            }
            categoryListContainer.addView(itemView)
        }

        // Hide the popup when tapping outside the category list
        categoryPopupContainer?.setOnClickListener {
            dismiss()
        }
    }

    private fun getCategoryDrawableResId(categoryName: String): Int {
        // Convert the category name to a matching drawable name
        val drawableName = categoryName.lowercase().replace(" ", "_")
        return resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
    }

    override fun onStart() {
        super.onStart()
        // Set dialog to full-screen
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}