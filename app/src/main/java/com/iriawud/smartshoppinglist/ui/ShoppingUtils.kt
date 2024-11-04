package com.iriawud.smartshoppinglist.ui

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.cardview.widget.CardView
import com.google.android.material.slider.Slider
import com.iriawud.smartshoppinglist.ui.home.ShoppingItem
import com.iriawud.smartshoppinglist.ui.home.ShoppingViewModel

object ShoppingUtils {
    fun addItem(
        newItemName: String,
        newItemQuantity: String,
        newItemQuantityUnit: String,
        newItemCost: String,
        newItemCostUnit: String,
        newItemPriority: Int,
        viewModel: ItemViewModel, // Replace with your actual ViewModel type
        fieldsToClear: List<EditText>,
        prioritySlider: Slider,
        expandableCard: ViewGroup,
        context: Context,
        isInputBarExpanded: Boolean
    ): Boolean {
        if (newItemName.isNotBlank()) {
            val newItem = ShoppingItem(
                name = newItemName,
                quantity = "$newItemQuantity $newItemQuantityUnit",
                category = "Uncategorized",
                price = "$newItemCost $newItemCostUnit",
                priority = newItemPriority,
                imageUrl = newItemName.lowercase()
            )

            viewModel.addItem(newItem)

            // Clear the fields
            fieldsToClear.forEach { it.text.clear() }
            prioritySlider.value = 5f

            // Toggle expandable details card if needed
            if (isInputBarExpanded) {
                toggleExpandableDetailsCard(expandableCard, context, isInputBarExpanded)
                return true
            }
        }
        return false
    }

    fun toggleExpandableMenuButtons(
        ExpandableBottomMenu: CardView,
        ExpandableBottomMenuButtons: View,
        isBottomMenuExpanded: Boolean
    ): Boolean {
        if (!isBottomMenuExpanded) {
            ExpandableBottomMenu.animate()
                .scaleX(1.1f)  // Scale 10% larger in X direction
                .scaleY(1.1f)  // Scale 10% larger in Y direction
                .setDuration(100)  // Duration for the expand effect
                .start()

            ExpandableBottomMenuButtons.visibility = View.VISIBLE
        } else {
            ExpandableBottomMenu.animate()
                .scaleX(1.0f)  // Return to original X size
                .scaleY(1.0f)  // Return to original Y size
                .setDuration(100)  // Duration for the collapse effect
                .start()

            ExpandableBottomMenuButtons.visibility = View.GONE
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
            dpToPx(500f, context) // Expanded height
        } else {
            dpToPx(50f, context) // Collapsed height
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

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / 160f)).toInt()
    }
}

interface ItemViewModel {
    fun addItem(item: ShoppingItem)
}